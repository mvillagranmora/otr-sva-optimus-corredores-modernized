package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizarCrearDireccionElectronicaPersonaMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizarCrearDireccionElectronicaPersonaMasivo.class);
	
	private final SQL sql = new SQL();
	
	private final String SQL_UPDATE_DIRECCION_ELECTRONICA = 
		PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/per/sql/AutorizarCrearDireccionElectronicaPersonaMasivo_Update_DireccionElectronica.sql"
		);
	
	public static class DireccionElectronica {
		public Long IdDireccionElectronica;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DireccionElectronica that = (DireccionElectronica) o;
			return IdDireccionElectronica != null && IdDireccionElectronica.equals(that.IdDireccionElectronica);
		}
		
		@Override
		public int hashCode() {
			return IdDireccionElectronica != null ? IdDireccionElectronica.hashCode() : 0;
		}
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDireccionElectronicaPersonaMasivo.autorizarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<DireccionElectronica> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionElectronica d = new DireccionElectronica();
					d.IdDireccionElectronica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "AUT";
					d.IdEstado = null;
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_DIRECCION_ELECTRONICA, direcciones, DireccionElectronica.class);
			
			for (DireccionElectronica direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDireccionElectronicaPersonaMasivo.autorizarEvento {}", fin - inicio);
		
		return salida;
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearDireccionElectronicaPersonaMasivo.rechazarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			List<DireccionElectronica> direcciones = entrada.p_Lista.stream()
				.map(evento -> {
					DireccionElectronica d = new DireccionElectronica();
					d.IdDireccionElectronica = Long.valueOf(evento.NumeroOperacionConcepto);
					d.IdEstadoAutorizacion = "RZD";
					
					String idTipoEvento = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					if ("NUEDIE".equals(idTipoEvento)) {
						d.IdEstado = "INA";
					} else {
						d.IdEstado = null;
					}
					return d;
				})
				.distinct()
				.collect(Collectors.toList());
			
			sql.batch(conn, SQL_UPDATE_DIRECCION_ELECTRONICA, direcciones, DireccionElectronica.class);
			
			List<EntidadOrigen> entidadesMODDIE = entrada.p_Lista.stream()
				.filter(evento -> {
					String tipo = evento.IdTipoEvento != null ? evento.IdTipoEvento : "";
					return "MODDIE".equals(tipo);
				})
				.map(evento -> {
					EntidadOrigen eo = new EntidadOrigen();
					eo.IdEntidadOrigen = evento.NumeroOperacionConcepto;
					eo.IdTipoEntidad = "DIRECCIONELECTRONICA";
					return eo;
				})
				.collect(Collectors.toList());
			
			if (!entidadesMODDIE.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo servicioSolicitud = new ModificarEstadoRegistroSolicitudCambioMasivo();
				
				EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
				entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
				entradaSolicitud.p_EntidadOrigen = entidadesMODDIE;
				entradaSolicitud.p_IdEstadoAutorizacion = "RZD";
				
				SalidaModificarEstado salidaSolicitud = servicioSolicitud.logica(conn, entradaSolicitud);
				
				if (salidaSolicitud.p_out_ErroresContador > 0) {
					salida.P_OUT_ERRORESCONTADOR += (long) salidaSolicitud.p_out_ErroresContador;
				}
			}
			
			for (DireccionElectronica direccion : direcciones) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = direccion;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AutorizarCrearDireccionElectronicaPersonaMasivo.rechazarEvento {}", fin - inicio);
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AutorizarCrearDireccionElectronicaPersonaMasivo servicio = new AutorizarCrearDireccionElectronicaPersonaMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 76770L;
			entrada.p_IdModulo = "AUT";
			
			cl.bcs.plataforma.aut.pojo.Evento evento = new cl.bcs.plataforma.aut.pojo.Evento();
			evento.NumeroOperacionConcepto = "876112";
			evento.IdTipoEvento = "NUEDIE";  // MODDIE, NUEDIE
			evento.IdEmpresa = 1L;
			evento.IdUsuario = 76770L;
			evento.IdModulo = "AUT";
			entrada.p_Lista.add(evento);
			
			System.out.println("\n=== Probando CONFIRMAR ===");
			SalidaAutorizacion salidaConfirmar = servicio.autorizarEvento(conn, entrada);
			System.out.println("Errores: " + salidaConfirmar.P_OUT_ERRORESCONTADOR);
			System.out.println("Resultados: " + salidaConfirmar.P_OUT_RESULTADO.size());

			conn.commit();
			
			//System.out.println("\n=== Probando RECHAZAR ===");
		//	SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
		//	System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
		//	System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
