package cl.bcs.plataforma.lim;

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

public class ConfirmarModificarPlantillaLimite implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPlantillaLimite.class);

	private final String SQL_UPDATE_PLANTILLA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimite_Update_Plantilla.sql");
	private final String SQL_SELECT_CAMBIOS = loadResourceToString("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimite_Select_Cambios.sql");

	public static class EntradaConfirmarModificarPlantillaLimite {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPlantillaLimite;
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

	public static class SalidaConfirmarModificarPlantillaLimite {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarModificarPlantillaLimite() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CambioPlantillaLimite {
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public static class UpdatePlantilla {
		public String DescripcionPlantillaLimite;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public String IdPlantillaLimite;
		public Long IdEmpresa;
	}

	public SalidaConfirmarModificarPlantillaLimite logica(Connection conn, EntradaConfirmarModificarPlantillaLimite in) throws Exception {
		logger.info("INIT ConfirmarModificarPlantillaLimite");
		SalidaConfirmarModificarPlantillaLimite salida = new SalidaConfirmarModificarPlantillaLimite();
		SQL sql = new SQL();

		try {
			// Obtener cambios desde SolicitudCambio
			logger.debug("Obtener cambios de SolicitudCambio");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdTipoEntidad", "PLANTILLALIMITE");
			params.put("IdEntidadOrigen", in.p_IdPlantillaLimite);

			List<CambioPlantillaLimite> cambios = sql.query(conn, SQL_SELECT_CAMBIOS, params, CambioPlantillaLimite.class);

			if (cambios.isEmpty()) {
				salida.p_out_ErroresDetalle.add(new ErrorDetalle("SIN_CAMBIOS", "No se encontraron cambios pendientes"));
				salida.p_out_ErroresContador = (long) salida.p_out_ErroresDetalle.size();
				return salida;
			}

			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			// Preparar actualización
			UpdatePlantilla updatePlantilla = new UpdatePlantilla();
			updatePlantilla.IdPlantillaLimite = in.p_IdPlantillaLimite;
			updatePlantilla.IdEmpresa = in.p_IdEmpresa;
			updatePlantilla.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

			// Aplicar cambios
			for (CambioPlantillaLimite cambio : cambios) {
				switch (cambio.CampoModificado) {
					case "DescripcionPlantillaLimite":
						updatePlantilla.DescripcionPlantillaLimite = cambio.ValorNuevo;
						break;
					case "IdEstado":
						updatePlantilla.IdEstado = cambio.ValorNuevo;
						break;
				}
			}

			// Ejecutar actualización
			logger.debug("Actualizar plantilla límite");
			List<UpdatePlantilla> updateList = new ArrayList<>();
			updateList.add(updatePlantilla);
			sql.batch(conn, SQL_UPDATE_PLANTILLA, updateList, UpdatePlantilla.class);

			// Insertar log
			logger.debug("Insertar log de auditoría");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "PLANTILLA LIMITE";
			entradaAgregarLog.p_Log.add(log);

			for (CambioPlantillaLimite cambio : cambios) {
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
			logger.error("Error en ConfirmarModificarPlantillaLimite: " + ex.getMessage());
			salida.p_out_ErroresDetalle.add(new ErrorDetalle("ERROR_CONFIRMAR_MODIFICAR_PLANTILLA_LIMITE", ex.getMessage()));
			salida.p_out_ErroresContador = (long) salida.p_out_ErroresDetalle.size();
		}

		logger.info("END ConfirmarModificarPlantillaLimite");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarPlantillaLimite in = gson.fromJson(jin.toString(), EntradaConfirmarModificarPlantillaLimite.class);
		SalidaConfirmarModificarPlantillaLimite out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPlantillaLimite\":\"PLANTILLA001\",\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarPlantillaLimite entrada = gson.fromJson(json, EntradaConfirmarModificarPlantillaLimite.class);
			ConfirmarModificarPlantillaLimite demo = new ConfirmarModificarPlantillaLimite();
			SalidaConfirmarModificarPlantillaLimite salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
