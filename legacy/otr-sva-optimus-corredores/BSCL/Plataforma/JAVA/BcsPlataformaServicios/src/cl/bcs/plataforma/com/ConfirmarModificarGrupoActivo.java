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

public class ConfirmarModificarGrupoActivo implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarGrupoActivo.class);

	private final String SQL_UPDATE_GRUPO = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarGrupoActivo_Update_Grupo.sql");
	private final String SQL_SELECT_CAMBIOS = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarGrupoActivo_Select_Cambios.sql");

	public static class EntradaConfirmarModificarGrupoActivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdGrupoActivos;
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

	public static class SalidaConfirmarModificarGrupoActivo {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaConfirmarModificarGrupoActivo() {
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

	public static class CambioGrupoActivo {
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public static class UpdateGrupo {
		public String DescripcionGrupoActivos;
		public String IdTipoGrupoActivos;
		public String IdEstado;
		public String PorDefecto;
		public String IdEstadoAutorizacion;
		public String IdGrupoActivos;
		public Long IdEmpresa;
	}

	public SalidaConfirmarModificarGrupoActivo logica(Connection conn, EntradaConfirmarModificarGrupoActivo in) throws Exception {
		logger.info("INIT ConfirmarModificarGrupoActivo");
		SalidaConfirmarModificarGrupoActivo salida = new SalidaConfirmarModificarGrupoActivo();
		SQL sql = new SQL();

		try {
			// Obtener cambios desde SolicitudCambio
			logger.debug("Obtener cambios de SolicitudCambio");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdTipoEntidad", "GRUPOACTIVO");
			params.put("IdEntidadOrigen", in.p_IdGrupoActivos);

			List<CambioGrupoActivo> cambios = sql.query(conn, SQL_SELECT_CAMBIOS, params, CambioGrupoActivo.class);

			if (cambios.isEmpty()) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("SIN_CAMBIOS", "No se encontraron cambios pendientes"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Preparar actualización
			UpdateGrupo updateGrupo = new UpdateGrupo();
			updateGrupo.IdGrupoActivos = in.p_IdGrupoActivos;
			updateGrupo.IdEmpresa = in.p_IdEmpresa;
			updateGrupo.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		// Aplicar cambios
		for (CambioGrupoActivo cambio : cambios) {
			switch (cambio.CampoModificado) {
				case "DescripcionGrupoActivos":
					updateGrupo.DescripcionGrupoActivos = cambio.ValorNuevo;
					break;
				case "IdTipoGrupoActivos":
					updateGrupo.IdTipoGrupoActivos = cambio.ValorNuevo;
					break;
				case "IdEstado":
					updateGrupo.IdEstado = cambio.ValorNuevo;
					break;
				case "PorDefecto":
					updateGrupo.PorDefecto = cambio.ValorNuevo;
					break;
			}
		}

		// Ejecutar actualización
		logger.debug("Actualizar grupo activos");
		List<UpdateGrupo> updateList = new ArrayList<>();
		updateList.add(updateGrupo);
		sql.batch(conn, SQL_UPDATE_GRUPO, updateList, UpdateGrupo.class);

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
		log.IdTipoRegistro = "GRUPO ACTIVOS";
		entradaAgregarLog.p_Log.add(log);

		for (CambioGrupoActivo cambio : cambios) {
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

			salida.P_OUT_REFID = 1L;

		} catch (Exception ex) {
			logger.error("Error en ConfirmarModificarGrupoActivo: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_MODIFICAR_GRUPO_ACTIVOS", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ConfirmarModificarGrupoActivo");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarGrupoActivo in = gson.fromJson(jin.toString(), EntradaConfirmarModificarGrupoActivo.class);
		SalidaConfirmarModificarGrupoActivo out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdGrupoActivos\":\"TEST\",\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarGrupoActivo entrada = gson.fromJson(json, EntradaConfirmarModificarGrupoActivo.class);
			ConfirmarModificarGrupoActivo demo = new ConfirmarModificarGrupoActivo();
			SalidaConfirmarModificarGrupoActivo salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
