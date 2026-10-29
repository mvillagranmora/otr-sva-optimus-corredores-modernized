package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.APV.CambiarAPlanActivoContratoSAAPV.CambiarAPlanActivoEntrada;
import cl.bcs.APV.CambiarAPlanActivoContratoSAAPV.CambiarAPlanActivoSalida;
import cl.bcs.APV.CambiarAPlanActivoContratoSAAPV.ContratoCambioPlanIn;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLADETALLE;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionCambiarAPlanActivoContratoSAAPV implements BcsAutorizaciones {
	private Logger log = LogManager.getLogger(AutorizacionCambiarAPlanActivoContratoSAAPV.class);
	
	private final String SQL_SELECT_DETALLE_EVENTO = "SELECT EvtDet.\"IdEvento\" AS \"IdUnion\",EvtDet.\"NombreCampo\",EvtDet.\"TipoDato\",EvtDet.\"Valor\",EvtDet.\"Clasificacion\" FROM \"BSCL.Plataforma.BD::AUT.EventoDetalle\" AS EvtDet WHERE EvtDet.\"IdEvento\" = :idsEvento";

	public static class Evento_Entrada {	
		public Long   IdEvento;
		public String Fecha;
		public Long   NumeroOperacionConcepto;
		
		public String toString() {
		   return Utils.printJson(this, false);
	    }		
    };
     
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		log.info("INICIO:AUTORIZACION");
		SQL sql = new SQL();
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		CambiarAPlanActivoContratoSAAPV confirmar = new CambiarAPlanActivoContratoSAAPV();
		CambiarAPlanActivoEntrada entradaConfirmar = new CambiarAPlanActivoEntrada();

		List<ObjTABLADETALLE> detallesEvento = null;
		ArrayList<Long> idsEvento = new ArrayList<Long>();
		
		//Obtiene id de evento para obtener datos necesarios
		for (Evento evento: entrada.p_Lista) {			
			idsEvento.add(evento.IdEvento);
		}				    	    
	    params.put("idsEvento", idsEvento);
				
	    try {
	    	detallesEvento = sql.query(conn, SQL_SELECT_DETALLE_EVENTO, params, ObjTABLADETALLE.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	    
	    List<ContratoCambioPlanIn> lstContratosCambioPlan = new ArrayList<ContratoCambioPlanIn>();
	    
	    for(ObjTABLADETALLE detalleEvento : detallesEvento) {
	    	ContratoCambioPlanIn cambioPlan = new ContratoCambioPlanIn();
	  	    String[] parts = detalleEvento.Valor.split(";");
	  	    
	    	cambioPlan.IdPersona = Long.parseLong(parts[0]);
	    	cambioPlan.IdCliente = Long.parseLong(parts[1]);
	    	cambioPlan.IdPortafolio = Long.parseLong(parts[2]);
	    	cambioPlan.IdContratoSAAPV = Long.parseLong(parts[3]);
	    	cambioPlan.FolioContratoSAAPV = parts[3];
    		
	    	lstContratosCambioPlan.add(cambioPlan);
		}
	    
	    entradaConfirmar.p_IdEmpresa = entrada.p_Lista.get(0).IdEmpresa;
	    entradaConfirmar.p_IdUsuario = entrada.p_Lista.get(0).IdUsuario;
	    entradaConfirmar.p_IdModulo = entrada.p_Lista.get(0).IdModulo;
		entradaConfirmar.p_ContratoCambioPlanIn = lstContratosCambioPlan;
		
		try {
			CambiarAPlanActivoSalida salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
			HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
			for(MensajeSalida salida : salidaConfirmacion.P_OUT_MENSAJES) {
				hashSalidaConfirmar.put(salida.id, salida.tipo);
			}
			
			for(Evento evento : entrada.p_Lista) {
				long id = 0L;
				if (!(evento.IdCliente==null) && evento.IdCliente.compareTo(0L) != 0) {
					id = evento.IdCliente;
				}
				if(hashSalidaConfirmar.containsKey(id) && hashSalidaConfirmar.get(id).equals(MensajeSalida.MENSAJE_OK)) {
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_AUTORIZADA").msg();
					s.P_OUT_ERRORESCONTADOR = 0L;
					s.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", descripcion));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					s.P_OUT_RESULTADO.add(res);
				}else {
					s.P_OUT_ERRORESCONTADOR = salidaConfirmacion.P_OUT_ERRORESCONTADOR;
					s.P_OUT_MENSAJES = salidaConfirmacion.P_OUT_MENSAJES;
				}		
			}
			
		} catch (Exception e1) {
			s.P_OUT_ERRORESCONTADOR = 1L;
		}
		log.info("TERMINO:AUTORIZACION");
		return s;
	}
	
	
	//El servicio de rechazo no es utilizado por el cambio de plan
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		log.info("INICIO:RECHAZO");
		entrada.p_Lista = entrada.p_Lista; 
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		
		try {

		} catch (Exception e1) {
			s.P_OUT_ERRORESCONTADOR = 1L;
		}
		log.info("TERMINO:RECHAZO");
		return s;
	}

	public static void main (String [] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AutorizacionCambiarAPlanActivoContratoSAAPV e = new AutorizacionCambiarAPlanActivoContratoSAAPV();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = 0L;
		
			Evento evento = new Evento();
			evento.IdCliente = 18161L;
			evento.IdEmpresa = entrada.p_IdEmpresa;
			evento.IdModulo = entrada.p_IdModulo;
			evento.IdUsuario = entrada.p_IdUsuario;
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
