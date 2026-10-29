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

public class ConfirmarModificarPlantillaLimiteDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPlantillaLimiteDetalle.class);

	private final String SQL_UPDATE_DETALLE = loadResourceToString("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimiteDetalle_Update_Detalle.sql");
	private final String SQL_SELECT_CAMBIOS = loadResourceToString("cl/bcs/plataforma/lim/sql/ConfirmarModificarPlantillaLimiteDetalle_Select_Cambios.sql");

	public static class EntradaConfirmarModificarPlantillaLimiteDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdLimite;
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

	public static class SalidaConfirmarModificarPlantillaLimiteDetalle {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarModificarPlantillaLimiteDetalle() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CambioDetalle {
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public static class UpdateDetalle {
		public String Nombre;
		public String IdMesa;
		public String IdCanalIngreso;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdInstrumento;
		public String UnidadPlazo;
		public String Plazo;
		public String IdTipoReajuste;
		public String CodigoOperador;
		public String MontoTotal;
		public String MontoIntraday;
		public String MontoOvernight;
		public String IdMoneda;
		public String IdMonedaCalculo;
		public String ClasificacionRiesgoMinima;
		public String IdEstado;
		public String EsPersonalizada;
		public String EsTotalizador;
		public String IdAgente;
		public String IdClasificacionTipoInstrumento;
		public String Porcentaje;
		public String IdEstadoAutorizacion;
		public Long IdLimite;
	}

	public SalidaConfirmarModificarPlantillaLimiteDetalle logica(Connection conn, EntradaConfirmarModificarPlantillaLimiteDetalle in) throws Exception {
		logger.info("INIT ConfirmarModificarPlantillaLimiteDetalle");
		SalidaConfirmarModificarPlantillaLimiteDetalle salida = new SalidaConfirmarModificarPlantillaLimiteDetalle();
		SQL sql = new SQL();

		try {
			// Obtener cambios desde SolicitudCambio
			logger.debug("Obtener cambios de SolicitudCambio");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdTipoEntidad", "PLANTILLALIMITEDETALLE");
			params.put("IdEntidadOrigen", String.valueOf(in.p_IdLimite));

			List<CambioDetalle> cambios = sql.query(conn, SQL_SELECT_CAMBIOS, params, CambioDetalle.class);

			if (cambios.isEmpty()) {
				salida.p_out_ErroresDetalle.add(new ErrorDetalle("SIN_CAMBIOS", "No se encontraron cambios pendientes"));
				salida.p_out_ErroresContador = (long) salida.p_out_ErroresDetalle.size();
				return salida;
			}

			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			// Preparar actualización
			UpdateDetalle updateDetalle = new UpdateDetalle();
			updateDetalle.IdLimite = in.p_IdLimite;
			updateDetalle.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

			// Aplicar cambios
			for (CambioDetalle cambio : cambios) {
				switch (cambio.CampoModificado) {
					case "Nombre": updateDetalle.Nombre = cambio.ValorNuevo; break;
					case "IdMesa": updateDetalle.IdMesa = cambio.ValorNuevo; break;
					case "IdCanalIngreso": updateDetalle.IdCanalIngreso = cambio.ValorNuevo; break;
					case "IdTipoInstrumento": updateDetalle.IdTipoInstrumento = cambio.ValorNuevo; break;
					case "IdTipoOperacion": updateDetalle.IdTipoOperacion = cambio.ValorNuevo; break;
					case "IdTipoProducto": updateDetalle.IdTipoProducto = cambio.ValorNuevo; break;
					case "IdInstrumento": updateDetalle.IdInstrumento = cambio.ValorNuevo; break;
					case "UnidadPlazo": updateDetalle.UnidadPlazo = cambio.ValorNuevo; break;
					case "Plazo": updateDetalle.Plazo = cambio.ValorNuevo; break;
					case "IdTipoReajuste": updateDetalle.IdTipoReajuste = cambio.ValorNuevo; break;
					case "CodigoOperador": updateDetalle.CodigoOperador = cambio.ValorNuevo; break;
					case "MontoTotal": updateDetalle.MontoTotal = cambio.ValorNuevo; break;
					case "MontoIntraday": updateDetalle.MontoIntraday = cambio.ValorNuevo; break;
					case "MontoOvernight": updateDetalle.MontoOvernight = cambio.ValorNuevo; break;
					case "IdMoneda": updateDetalle.IdMoneda = cambio.ValorNuevo; break;
					case "IdMonedaCalculo": updateDetalle.IdMonedaCalculo = cambio.ValorNuevo; break;
					case "ClasificacionRiesgoMinima": updateDetalle.ClasificacionRiesgoMinima = cambio.ValorNuevo; break;
					case "IdEstado": updateDetalle.IdEstado = cambio.ValorNuevo; break;
					case "EsPersonalizada": updateDetalle.EsPersonalizada = cambio.ValorNuevo; break;
					case "EsTotalizador": updateDetalle.EsTotalizador = cambio.ValorNuevo; break;
					case "IdAgente": updateDetalle.IdAgente = cambio.ValorNuevo; break;
					case "IdClasificacionTipoInstrumento": updateDetalle.IdClasificacionTipoInstrumento = cambio.ValorNuevo; break;
					case "Porcentaje": updateDetalle.Porcentaje = cambio.ValorNuevo; break;
				}
			}

			// Ejecutar actualización
			logger.debug("Actualizar detalle límite");
			List<UpdateDetalle> updateList = new ArrayList<>();
			updateList.add(updateDetalle);
			sql.batch(conn, SQL_UPDATE_DETALLE, updateList, UpdateDetalle.class);

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
			log.IdTipoRegistro = "PLANTILLA LIMITE DETALLE";
			entradaAgregarLog.p_Log.add(log);

			for (CambioDetalle cambio : cambios) {
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
			logger.error("Error en ConfirmarModificarPlantillaLimiteDetalle: " + ex.getMessage());
			salida.p_out_ErroresDetalle.add(new ErrorDetalle("ERROR_CONFIRMAR_MODIFICAR_PLANTILLA_LIMITE_DETALLE", ex.getMessage()));
			salida.p_out_ErroresContador = (long) salida.p_out_ErroresDetalle.size();
		}

		logger.info("END ConfirmarModificarPlantillaLimiteDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarPlantillaLimiteDetalle in = gson.fromJson(jin.toString(), EntradaConfirmarModificarPlantillaLimiteDetalle.class);
		SalidaConfirmarModificarPlantillaLimiteDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"LIM\",\"p_UsaReglasNegocio\":\"N\",\"p_IdLimite\":1,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarPlantillaLimiteDetalle entrada = gson.fromJson(json, EntradaConfirmarModificarPlantillaLimiteDetalle.class);
			ConfirmarModificarPlantillaLimiteDetalle demo = new ConfirmarModificarPlantillaLimiteDetalle();
			SalidaConfirmarModificarPlantillaLimiteDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
