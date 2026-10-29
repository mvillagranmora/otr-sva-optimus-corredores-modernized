package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.com.ConfirmarEliminarConfiguracionPerfilComercialAdicional.EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional;
import cl.bcs.plataforma.com.ConfirmarEliminarConfiguracionPerfilComercialAdicional.InPerfilComercial;
import cl.bcs.plataforma.com.ConfirmarEliminarConfiguracionPerfilComercialAdicional.SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional;
import cl.bcs.plataforma.com.RechazaEliminarConfiguracionPerfilComercialAdicional.EntradaRechazaEliminarConfiguracionPerfilComercialAdicional;
import cl.bcs.plataforma.com.RechazaEliminarConfiguracionPerfilComercialAdicional.InPerfilComercialPorRechazar;
import cl.bcs.plataforma.com.RechazaEliminarConfiguracionPerfilComercialAdicional.SalidaRechazaEliminarConfiguracionPerfilComercialAdicional;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;


public class AutorizacionEliminarConfiguracionPerfilComercialAdicional implements BcsAutorizaciones {
	
	private Logger log = LogManager.getLogger(AutorizacionEliminarConfiguracionPerfilComercialAdicional.class);
	
	private final String ID_ESTADO_AUTORIZACION = "AUT";
	private final String ID_ESTADO_RECHAZADO = "RZD";
	private final String USA_REGLAS_NEGOCIO = "N";
	
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		
		log.info("INICIO:AUTORIZACION");
		entrada.p_Lista = entrada.p_Lista;
		
		SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();
		ConfirmarEliminarConfiguracionPerfilComercialAdicional confirmar = new ConfirmarEliminarConfiguracionPerfilComercialAdicional();
		EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional entradaConfirmar = new EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional();
		
		entradaConfirmar.p_IdEmpresa = entrada.p_IdEmpresa;
	    entradaConfirmar.p_IdModulo = entrada.p_IdModulo;
	    entradaConfirmar.p_IdUsuario = entrada.p_IdUsuario;
	    entradaConfirmar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
	    entradaConfirmar.p_IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
		
		for (Evento evento : entrada.p_Lista) {
		    entradaConfirmar.p_IdsPerfilComercialAdicional.add(new InPerfilComercial(evento.IdCliente));
		}
		
		try {
			SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
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
					salidaAutoriza.P_OUT_ERRORESCONTADOR = 0L;
					salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", descripcion));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					salidaAutoriza.P_OUT_RESULTADO.add(res);
				}else {
					salidaAutoriza.P_OUT_ERRORESCONTADOR = salidaConfirmacion.P_OUT_ERRORESCONTADOR;
					salidaAutoriza.P_OUT_MENSAJES = salidaConfirmacion.P_OUT_MENSAJES;
				}		
			}
			
		} catch (Exception e1) {
			salidaAutoriza.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		log.info("TERMINO:AUTORIZACION");
		return salidaAutoriza;
		
	}
	
	public static class ModificacionRechazo {
		
		public Long IdPersona;

		public ModificacionRechazo(Long idPersona) {
			super();
			IdPersona = idPersona;
		}
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		
		log.info("INICIO:RECHAZO");
		entrada.p_Lista = entrada.p_Lista;

		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		RechazaEliminarConfiguracionPerfilComercialAdicional rechazar = new RechazaEliminarConfiguracionPerfilComercialAdicional();
		EntradaRechazaEliminarConfiguracionPerfilComercialAdicional entradaRechazar = new EntradaRechazaEliminarConfiguracionPerfilComercialAdicional();
		
		entradaRechazar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaRechazar.p_IdModulo =entrada.p_IdModulo;
		entradaRechazar.p_IdUsuario = entrada.p_IdUsuario;
		entradaRechazar.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		entradaRechazar.p_IdEstadoAutorizacion = ID_ESTADO_RECHAZADO;
		
		for (Evento evento : entrada.p_Lista) {
			entradaRechazar.p_IdsPerfilComercialAdicional.add(new InPerfilComercialPorRechazar(evento.IdCliente));
		}

		try {
			SalidaRechazaEliminarConfiguracionPerfilComercialAdicional salidaRechazado = rechazar.logica(conn, entradaRechazar);

		    HashMap<Long, String> hashSalidaRechazar = new HashMap<>();
		    for (MensajeSalida salida : salidaRechazado.P_OUT_MENSAJES) {
			hashSalidaRechazar.put(salida.id, salida.tipo);
		    }

		    for (Evento evento : entrada.p_Lista) {
			long id = 0L;
			if (!(evento.IdCliente == null) && evento.IdCliente.compareTo(0L) != 0) {
			    id = evento.IdCliente;
			}
			if (hashSalidaRechazar.containsKey(id) && hashSalidaRechazar.get(id).equals(MensajeSalida.MENSAJE_OK)) {
			    String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_RECHAZADA").msg();
			    salidaAutorizacion.P_OUT_ERRORESCONTADOR = 0L;
			    salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_RECHAZADA", descripcion));
			    ResultadoAutorizacion res = new ResultadoAutorizacion();
			    res.evento = evento.IdEvento;
			    res.resultado = EXITO_AUTORIZACION;
			    salidaAutorizacion.P_OUT_RESULTADO.add(res);
			} else {
			    salidaRechazado.P_OUT_ERRORESCONTADOR = salidaRechazado.P_OUT_ERRORESCONTADOR;
			    salidaRechazado.P_OUT_MENSAJES = salidaRechazado.P_OUT_MENSAJES;
			}
		    }

		} catch (Exception e1) {
		    salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
		}
		log.info("TERMINO:RECHAZO");
		return salidaAutorizacion;
		
	}

	
	public static void main (String [] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AutorizacionEliminarConfiguracionPerfilComercialAdicional e = new AutorizacionEliminarConfiguracionPerfilComercialAdicional();
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
