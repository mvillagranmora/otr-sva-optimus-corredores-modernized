package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarGrupoActivoDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarGrupoActivoDetalle.class);

	private final String SQL_UPDATE_DETALLE = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarGrupoActivoDetalle_Update_Detalle.sql");
	private final String SQL_SELECT_CAMBIOS = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarGrupoActivoDetalle_Select_Cambios.sql");

	public static class EntradaConfirmarModificarGrupoActivoDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDetalleGrupoActivos;
		public String p_IdEstadoAutorizacion;

		public String toString() {
			return Utils.printJson(this, false);
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

	public static class SalidaConfirmarModificarGrupoActivoDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaConfirmarModificarGrupoActivoDetalle() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CambioGrupoActivoDetalle {
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public static class UpdateDetalle {
		public Long IdInstrumento;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public Long IdDetalleGrupoActivos;
	}

	public SalidaConfirmarModificarGrupoActivoDetalle logica(Connection conn, EntradaConfirmarModificarGrupoActivoDetalle in) throws Exception {
		logger.info("INIT ConfirmarModificarGrupoActivoDetalle");
		SalidaConfirmarModificarGrupoActivoDetalle salida = new SalidaConfirmarModificarGrupoActivoDetalle();
		SQL sql = new SQL();

		try {
			// Obtener cambios desde SolicitudCambio
			logger.debug("Obtener cambios de SolicitudCambio");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdTipoEntidad", "GRUPOACTIVODETALLE");
			params.put("IdEntidadOrigen", String.valueOf(in.p_IdDetalleGrupoActivos));

			List<CambioGrupoActivoDetalle> cambios = sql.query(conn, SQL_SELECT_CAMBIOS, params, CambioGrupoActivoDetalle.class);

			if (cambios.isEmpty()) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("SIN_CAMBIOS", "No se encontraron cambios pendientes"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Preparar actualización
			UpdateDetalle updateDetalle = new UpdateDetalle();
			updateDetalle.IdDetalleGrupoActivos = in.p_IdDetalleGrupoActivos;
			updateDetalle.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		// Aplicar cambios
			for (CambioGrupoActivoDetalle cambio : cambios) {
			switch (cambio.CampoModificado) {
				case "IdInstrumento":
					updateDetalle.IdInstrumento = Long.valueOf(cambio.ValorNuevo);
					break;
				case "IdEstado":
					updateDetalle.IdEstado = cambio.ValorNuevo;
					break;
			}
		}

		// Ejecutar actualización
		logger.debug("Actualizar detalle grupo activos");
		List<UpdateDetalle> updateList = new ArrayList<>();
		updateList.add(updateDetalle);
		sql.batch(conn, SQL_UPDATE_DETALLE, updateList, UpdateDetalle.class);

		// Insertar log
		logger.debug("Insertar log de auditoría");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "MODIFICACION";
		log.IdModulo = "COM";
		log.IdUsuario = in.p_IdUsuario;
		log.IdEmpresa = in.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = in.p_IdUsuario;
		log.IdTipoRegistro = "DETALLE GRUPO ACTIVOS";
		entradaAgregarLog.p_Log.add(log);

		for (CambioGrupoActivoDetalle cambio : cambios) {
			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 1L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = "MODIFICACION";
			detLog.CampoRegistro = cambio.CampoModificado;
			detLog.ValorAntiguo = cambio.ValorAntiguo;
			detLog.ValorNuevo = cambio.ValorNuevo;
			entradaAgregarLog.p_LogDetalle.add(detLog);
		}

		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarModificarGrupoActivoDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_MODIFICAR_GRUPO_ACTIVOS_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ConfirmarModificarGrupoActivoDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarGrupoActivoDetalle in = gson.fromJson(jin.toString(), EntradaConfirmarModificarGrupoActivoDetalle.class);
		SalidaConfirmarModificarGrupoActivoDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetalleGrupoActivos\":1,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarGrupoActivoDetalle entrada = gson.fromJson(json, EntradaConfirmarModificarGrupoActivoDetalle.class);
			ConfirmarModificarGrupoActivoDetalle demo = new ConfirmarModificarGrupoActivoDetalle();
			SalidaConfirmarModificarGrupoActivoDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
