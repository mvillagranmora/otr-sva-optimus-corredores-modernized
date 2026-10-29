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

public class ConfirmarCrearGrupoActivoDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearGrupoActivoDetalle.class);

	private final String SQL_UPDATE_DETALLE = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearGrupoActivoDetalle_Update_Detalle.sql");
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearGrupoActivoDetalle_Select_Detalle.sql");

	public static class EntradaConfirmarCrearGrupoActivoDetalle {
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

	public static class SalidaConfirmarCrearGrupoActivoDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaConfirmarCrearGrupoActivoDetalle() {
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

	public static class UpdateDetalle {
		public String IdEstadoAutorizacion;
		public Long IdDetalleGrupoActivos;
	}

	public static class SelectDetalle {
		public Long IdDetalleGrupoActivos;
		public String IdGrupoActivos;
		public Long IdInstrumento;
		public String IdEstado;
	}

	public SalidaConfirmarCrearGrupoActivoDetalle logica(Connection conn, EntradaConfirmarCrearGrupoActivoDetalle in) throws Exception {
		logger.info("INIT ConfirmarCrearGrupoActivoDetalle");
		SalidaConfirmarCrearGrupoActivoDetalle salida = new SalidaConfirmarCrearGrupoActivoDetalle();
		SQL sql = new SQL();

		try {
			// Obtener datos del detalle
			logger.debug("Obtener detalle grupo activos");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdDetalleGrupoActivos", in.p_IdDetalleGrupoActivos);

			List<SelectDetalle> detalles = sql.query(conn, SQL_SELECT_DETALLE, params, SelectDetalle.class);
			if (detalles.isEmpty()) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("DETALLE_NO_EXISTE", "El detalle no existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			SelectDetalle detalle = detalles.get(0);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			// Actualizar estado de autorización
			logger.debug("Actualizar estado de autorización");
			UpdateDetalle updateDetalle = new UpdateDetalle();
			updateDetalle.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
			updateDetalle.IdDetalleGrupoActivos = in.p_IdDetalleGrupoActivos;

		List<UpdateDetalle> updateList = new ArrayList<>();
		updateList.add(updateDetalle);
		sql.batch(conn, SQL_UPDATE_DETALLE, updateList, UpdateDetalle.class);

		// Insertar log
		logger.debug("Insertar log de auditoría");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "CREACION";
		log.IdModulo = "COM";
		log.IdUsuario = in.p_IdUsuario;
		log.IdEmpresa = in.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = in.p_IdUsuario;
		log.IdTipoRegistro = "DETALLE GRUPO ACTIVOS";
		entradaAgregarLog.p_Log.add(log);

		AgregarLog.InAgregarLog.DetalleLog detLog1 = new AgregarLog.InAgregarLog.DetalleLog();
		detLog1.IdCabeceraLog = 1L;
		detLog1.IdTrabajo = 0L;
		detLog1.SubRegistro = "CREACION";
		detLog1.CampoRegistro = "IdDetalleGrupoActivos";
		detLog1.ValorAntiguo = "";
		detLog1.ValorNuevo = String.valueOf(detalle.IdDetalleGrupoActivos);
		entradaAgregarLog.p_LogDetalle.add(detLog1);

		AgregarLog.InAgregarLog.DetalleLog detLog2 = new AgregarLog.InAgregarLog.DetalleLog();
		detLog2.IdCabeceraLog = 1L;
		detLog2.IdTrabajo = 0L;
		detLog2.SubRegistro = "CREACION";
		detLog2.CampoRegistro = "IdGrupoActivos";
		detLog2.ValorAntiguo = "";
		detLog2.ValorNuevo = detalle.IdGrupoActivos;
		entradaAgregarLog.p_LogDetalle.add(detLog2);

		AgregarLog.InAgregarLog.DetalleLog detLog3 = new AgregarLog.InAgregarLog.DetalleLog();
		detLog3.IdCabeceraLog = 1L;
		detLog3.IdTrabajo = 0L;
		detLog3.SubRegistro = "CREACION";
		detLog3.CampoRegistro = "IdInstrumento";
		detLog3.ValorAntiguo = "";
		detLog3.ValorNuevo = String.valueOf(detalle.IdInstrumento);
		entradaAgregarLog.p_LogDetalle.add(detLog3);

		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);
		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearGrupoActivoDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_CREAR_GRUPO_ACTIVOS_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ConfirmarCrearGrupoActivoDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearGrupoActivoDetalle in = gson.fromJson(jin.toString(), EntradaConfirmarCrearGrupoActivoDetalle.class);
		SalidaConfirmarCrearGrupoActivoDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetalleGrupoActivos\":1,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarCrearGrupoActivoDetalle entrada = gson.fromJson(json, EntradaConfirmarCrearGrupoActivoDetalle.class);
			ConfirmarCrearGrupoActivoDetalle demo = new ConfirmarCrearGrupoActivoDetalle();
			SalidaConfirmarCrearGrupoActivoDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
