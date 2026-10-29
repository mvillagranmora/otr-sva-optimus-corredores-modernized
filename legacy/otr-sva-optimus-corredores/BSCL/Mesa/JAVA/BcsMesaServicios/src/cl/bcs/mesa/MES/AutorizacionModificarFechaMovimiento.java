package cl.bcs.mesa.MES;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.mesa.MES.ConfirmarModificarFechaMovimiento.ConfirmarModificarFechaMovimiento_Entrada;
import cl.bcs.mesa.MES.ConfirmarModificarFechaMovimiento.ConfirmarModificarFechaMovimiento_Salida;
import cl.bcs.mesa.MES.ConfirmarModificarFechaMovimiento.Movimiento_Entrada;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificarFechaMovimiento implements BcsAutorizaciones {
	private Logger log = LogManager.getLogger(AutorizacionModificarFechaMovimiento.class);
	
	private final String SQL_SELECT_EVENTO = "SELECT Evt.\"IdEvento\", TO_VARCHAR (Evt.\"Fecha\",'YYYY-MM-DD') AS \"Fecha\", CAST(Evt.\"NumeroOperacionConcepto\" AS Bigint) as \"NumeroOperacionConcepto\" FROM \"BSCL.Plataforma.BD::AUT.Evento\" AS Evt WHERE Evt.\"IdEvento\" = :idsEvento";
	
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
		HashMap<String,Object> params = new HashMap<String,Object>();
		String _fechaOperacionNueva = "0001-01-01";
		
		SalidaAutorizacion s = new SalidaAutorizacion();		
		ConfirmarModificarFechaMovimiento confirmar = new ConfirmarModificarFechaMovimiento();
		ConfirmarModificarFechaMovimiento_Entrada entradaConfirmar = new ConfirmarModificarFechaMovimiento_Entrada();		
		
		HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
		List<Evento_Entrada> movimientoEvento = null;
		ArrayList<Long> idsEvento = new ArrayList<Long>();
		
		//Obtiene id de evento para obtener datos necesarios
		List<Movimiento_Entrada> detalleEvento = new ArrayList<Movimiento_Entrada>();
		
		for (Evento evento: entrada.p_Lista) {			
			//idsEvento.add(Long.parseLong(evento.NumeroOperacionConcepto));
			idsEvento.add(evento.IdEvento);
		}				    	    
	    params.put("idsEvento", idsEvento);
				
	    try {
			movimientoEvento = sql.query(conn, SQL_SELECT_EVENTO, params, Evento_Entrada.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	    
	    //Obtiene nro. spot desde el evento para enviar a proceso que actualiza fecha
	    for(Evento_Entrada mvtoEvento : movimientoEvento) {
	    	Movimiento_Entrada evt = new Movimiento_Entrada();
	    	
	    	evt.IdOperacion = mvtoEvento.NumeroOperacionConcepto;
	    	evt.FechaOperacionTimeStamp = "";
	    	evt.IdUsuarioModifica = 0L;
	    	evt.FechaModifica = "";
	    	evt.FechaOperacionNueva = mvtoEvento.Fecha;
	    	
	    	_fechaOperacionNueva = mvtoEvento.Fecha;
	    	detalleEvento.add(evt);
		}
		
		entradaConfirmar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmar.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmar.p_IdModulo = entrada.p_IdModulo;		
		entradaConfirmar.p_FechaOperacionNueva = _fechaOperacionNueva;
		entradaConfirmar.P_MOVIMIENTO.addAll(detalleEvento);
		
		try {
			ConfirmarModificarFechaMovimiento_Salida salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
			for(MensajeSalida salida : salidaConfirmacion.P_OUT_MENSAJES) {
				hashSalidaConfirmar.put(salida.id, salida.tipo);
			}
			
			for(Evento evento : entrada.p_Lista) {
				if(hashSalidaConfirmar.containsKey(evento.NumeroOperacionConcepto) && !hashSalidaConfirmar.get(evento.NumeroOperacionConcepto).equals("ERROR")) {
					s.P_OUT_ERRORESCONTADOR = 0L;
					s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"OPERACION_AUTORIZADA","Operacion autorizada con exito"));
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
	
	private String loadResourceToString(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static class OrdenRechazo{
		public Long IdOrden;

		public OrdenRechazo(Long idOrden) {
			super();
			IdOrden = idOrden;
		}
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		log.info("INICIO:RECHAZO");
		SalidaAutorizacion salida = new SalidaAutorizacion();	
		
		SQL sql = new SQL();
		List<OrdenRechazo> ordenes = entrada.p_Lista.stream().map(ord -> new OrdenRechazo(Long.parseLong(ord.NumeroOperacionConcepto))).collect(Collectors.toList());
		try {
			sql.batch(conn, "INSERT INTO  \"BSCL.Orden.BD::ORD.AutorizacionOrdenes_Entrada\" VALUES(:IdOrden)", ordenes, OrdenRechazo.class);
		
			HashMap<String,Object> parmsIN=new HashMap<>();
			
			parmsIN.put("P_IDEMPRESA", entrada.p_IdEmpresa);
			parmsIN.put("P_IDUSUARIO", entrada.p_IdUsuario);
			parmsIN.put("P_IDMODULO", entrada.p_IdModulo);
					
			HashMap<String,Class> parmsOUT=new HashMap<>();
		
			String queryCall="CALL \"BSCL.Orden.BD.Servicios::RechazaAutorizacionOrdenes\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,\"BSCL.Orden.BD::ORD.AutorizacionOrdenes_Entrada\")";
			HashMap<String,Object> rpt=sql.call(conn, queryCall, parmsIN, parmsOUT);
			
		} catch (SQLException e1) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_NO_SE_PUEDE_ANULAR","Operacion no se puede Anular"));
			e1.printStackTrace();
		}
		log.info("TERMINO:RECHAZO");
		return salida;	
	}
	
	
	
	
	
	public static void main (String [] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getBOLSAConnection()) {
			AutorizacionModificarFechaMovimiento e = new AutorizacionModificarFechaMovimiento();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "MES";
			entrada.p_IdUsuario = 1020L;
			
			
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "106445";
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();
			//conn.commit();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}

}
