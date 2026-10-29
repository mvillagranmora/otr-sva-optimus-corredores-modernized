package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio: ModificarEstadoRegistroSolicitudCambioMasivo
 * Propósito: Modificar estado de autorización de registros de SolicitudCambio de forma masiva
 * Migrado de: BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo
 * Autor: Equipo Migración
 * Fecha: Abril 2026
 */
public class ModificarEstadoRegistroSolicitudCambioMasivo implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ModificarEstadoRegistroSolicitudCambioMasivo.class);
	
	// Archivos SQL
	private final String SQL_UPDATE_POR_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambioMasivo_Update_PorSolicitud.sql");
	private final String SQL_UPDATE_POR_ENTIDAD = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambioMasivo_Update_PorEntidad.sql");
	
	// Clases de entrada
	public static class EntradaModificarEstado {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<SolicitudCambio> p_SolicitudCambio;
		public List<EntidadOrigen> p_EntidadOrigen;
		public String p_IdEstadoAutorizacion;
		
		public EntradaModificarEstado() {
			this.p_SolicitudCambio = new ArrayList<>();
			this.p_EntidadOrigen = new ArrayList<>();
		}
	}
	
	public static class SolicitudCambio {
		public Long IdSolicitudCambio;
	}
	
	public static class EntidadOrigen {
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
	}
	
	// Clases de salida
	public static class SalidaModificarEstado {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;
		
		public SalidaModificarEstado() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}
	}
	
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}
	
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
		
		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}
	
	// Clases para UPDATE
	public static class UpdatePorSolicitud {
		public Long IdSolicitudCambio;
		public String IdEstadoAutorizacion;
	}
	
	public static class UpdatePorEntidad {
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
	}
	
	/**
	 * Lógica principal del servicio
	 */
	public SalidaModificarEstado logica(Connection conn, EntradaModificarEstado entrada) throws Exception {
		logger.info("INICIO ModificarEstadoRegistroSolicitudCambioMasivo");
		SalidaModificarEstado salida = new SalidaModificarEstado();
		SQL sql = new SQL();
		
		try {
			// Verificar si hay solicitudes directas (por IdSolicitudCambio)
			if (entrada.p_SolicitudCambio != null && !entrada.p_SolicitudCambio.isEmpty()) {
				// Actualizar por IdSolicitudCambio
				logger.debug("UPDATE por IdSolicitudCambio - Total: " + entrada.p_SolicitudCambio.size());
				
				ArrayList<UpdatePorSolicitud> solicitudes = new ArrayList<>();
				for (SolicitudCambio sc : entrada.p_SolicitudCambio) {
					UpdatePorSolicitud update = new UpdatePorSolicitud();
					update.IdSolicitudCambio = sc.IdSolicitudCambio;
					update.IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
					solicitudes.add(update);
				}
				sql.batch(conn, SQL_UPDATE_POR_SOLICITUD, solicitudes, UpdatePorSolicitud.class);
				
			} else {
				// Actualizar por IdEntidadOrigen e IdTipoEntidad
				logger.debug("UPDATE por EntidadOrigen - Total: " + entrada.p_EntidadOrigen.size());
				
				ArrayList<UpdatePorEntidad> entidades = new ArrayList<>();
				for (EntidadOrigen eo : entrada.p_EntidadOrigen) {
					UpdatePorEntidad update = new UpdatePorEntidad();
					update.IdEntidadOrigen = eo.IdEntidadOrigen;
					update.IdTipoEntidad = eo.IdTipoEntidad;
					update.IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
					entidades.add(update);
				}
				sql.batch(conn, SQL_UPDATE_POR_ENTIDAD, entidades, UpdatePorEntidad.class);
			}
			
		} catch (Exception ex) {
			logger.error("Error en ModificarEstadoRegistroSolicitudCambioMasivo: " + ex.getMessage(), ex);
			// Error no controlado - línea 66 del SP original
			salida.p_out_ErroresDetalle.add(new ErrorDetalle("", ex.getMessage()));
			salida.p_out_ErroresContador = 1L;
		}
		
		logger.info("FIN ModificarEstadoRegistroSolicitudCambioMasivo");
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarEstado in = gson.fromJson(jin.toString(), EntradaModificarEstado.class);
		SalidaModificarEstado out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**
	 * Método main para pruebas
	 */
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			
			ModificarEstadoRegistroSolicitudCambioMasivo servicio = new ModificarEstadoRegistroSolicitudCambioMasivo();
			
			// TEST: Modificar estado por EntidadOrigen
			EntradaModificarEstado entrada = new EntradaModificarEstado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdEstadoAutorizacion = "RZD";
			
			// Agregar entidades origen - REEMPLAZAR con datos reales
			EntidadOrigen eo = new EntidadOrigen();
			eo.IdEntidadOrigen = "1000";
			eo.IdTipoEntidad = "PORTAFOLIO";
			entrada.p_EntidadOrigen.add(eo);
			
			SalidaModificarEstado salida = servicio.logica(conn, entrada);
			System.out.println("Errores: " + salida.p_out_ErroresContador);
			System.out.println("Advertencias: " + salida.p_out_AdvertenciasContador);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
