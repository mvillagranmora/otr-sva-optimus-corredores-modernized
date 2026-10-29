package cl.bcs.tesoreria;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ConfirmarAutorizarAnularMovimientosTesoreria.EntradaConfirmarAutorizarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.ConfirmarAutorizarAnularMovimientosTesoreria.MovimientosPorAutorizar;
import cl.bcs.tesoreria.ConfirmarAutorizarAnularMovimientosTesoreria.SalidaConfirmarAutorizarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.ConfirmarRechazarAnularMovimientosTesoreria.EntradaConfirmarRechazarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.ConfirmarRechazarAnularMovimientosTesoreria.MovimientosPorRechazar;
import cl.bcs.tesoreria.ConfirmarRechazarAnularMovimientosTesoreria.SalidaConfirmarRechazarAnularMovimientosTesoreria;

public class AutorizacionAnularMovimientosTesoreria implements BcsAutorizaciones {
	private Logger log = LogManager.getLogger(AutorizacionAnularMovimientosTesoreria.class);

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		log.info("INICIO:AUTORIZACION");
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		ConfirmarAutorizarAnularMovimientosTesoreria confirmar = new ConfirmarAutorizarAnularMovimientosTesoreria();
		EntradaConfirmarAutorizarAnularMovimientosTesoreria entradaConfirmar = new EntradaConfirmarAutorizarAnularMovimientosTesoreria();
		
		for(Evento evento : entrada.p_Lista) {	
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_Movimientos.add(new MovimientosPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaConfirmarAutorizarAnularMovimientosTesoreria salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
			HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
			for(MensajeSalida salida : salidaConfirmacion.P_OUT_MENSAJES) {
				hashSalidaConfirmar.put(salida.id, salida.tipo);
			}
			
			for(Evento evento : entrada.p_Lista) {
				long id = 0L;
				if (!(evento.NumeroOperacionConcepto==null) && !evento.NumeroOperacionConcepto.equals("")) {
					id = Long.parseLong(evento.NumeroOperacionConcepto);
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
	
	public static class MovimientoRechazo {
		public Long IdIngresoEgreso;

		public MovimientoRechazo(Long idIngresoEgreso) {
			super();
			IdIngresoEgreso = idIngresoEgreso;
		}
	}
	
	

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		log.info("INICIO:RECHAZO");
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		ConfirmarRechazarAnularMovimientosTesoreria confirmar = new ConfirmarRechazarAnularMovimientosTesoreria();
		EntradaConfirmarRechazarAnularMovimientosTesoreria entradaConfirmar = new EntradaConfirmarRechazarAnularMovimientosTesoreria();
		
		for(Evento evento : entrada.p_Lista) {
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
			entradaConfirmar.p_Movimientos.add(new MovimientosPorRechazar(Long.parseLong(evento.NumeroOperacionConcepto)));
		}
		
		try {
			SalidaConfirmarRechazarAnularMovimientosTesoreria salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
			HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
			for(MensajeSalida salida : salidaConfirmacion.P_OUT_MENSAJES) {
				hashSalidaConfirmar.put(salida.id, salida.tipo);
			}
			
			for(Evento evento : entrada.p_Lista) {
				long id = 0L;
				if (!(evento.NumeroOperacionConcepto==null) && !evento.NumeroOperacionConcepto.equals("")) {
					id = Long.parseLong(evento.NumeroOperacionConcepto);
				}
				if(hashSalidaConfirmar.containsKey(id) && hashSalidaConfirmar.get(id).equals(MensajeSalida.MENSAJE_OK)) {
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "AUTORIZACION_RECHAZADA").msg();
					s.P_OUT_ERRORESCONTADOR = 0L;
					s.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "AUTORIZACION_RECHAZADA", descripcion));
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
		log.info("TERMINO:RECHAZO");
		return s;
	}

	
	public static void main (String [] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AutorizacionAnularMovimientosTesoreria e = new AutorizacionAnularMovimientosTesoreria();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = 0L;
		
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "18161";
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
