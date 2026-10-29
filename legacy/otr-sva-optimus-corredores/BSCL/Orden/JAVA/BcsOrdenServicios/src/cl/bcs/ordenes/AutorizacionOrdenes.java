package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.ConfirmarAutorizacionOrdenes.EntradaConfirmarAutorizacionOrdenes;
import cl.bcs.ordenes.ConfirmarAutorizacionOrdenes.OrdenesPorAutorizar;
import cl.bcs.ordenes.ConfirmarAutorizacionOrdenes.SalidaConfirmarAutorizacionOrdenes;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;

public class AutorizacionOrdenes implements BcsAutorizaciones {
	private Logger log = LogManager.getLogger(AutorizacionOrdenes.class);
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		log.info("INICIO:AUTORIZACION");
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO

		SalidaAutorizacion s = new SalidaAutorizacion();		
		ConfirmarAutorizacionOrdenes confirmar = new ConfirmarAutorizacionOrdenes();
		EntradaConfirmarAutorizacionOrdenes entradaConfirmar = new EntradaConfirmarAutorizacionOrdenes();		
		
		HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
		
		
		for(Evento evento : entrada.p_Lista) {	
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_Ordenes.add(new OrdenesPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaConfirmarAutorizacionOrdenes salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
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
		
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa")) {
			AutorizacionOrdenes e = new AutorizacionOrdenes();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = 0L;
			
			
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "160078";
			entrada.p_Lista.add(evento);
			System.out.println(e.autorizarEvento(conn, entrada));
			System.out.println(e.rechazarEvento(conn, entrada));
			conn.rollback();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}

}
