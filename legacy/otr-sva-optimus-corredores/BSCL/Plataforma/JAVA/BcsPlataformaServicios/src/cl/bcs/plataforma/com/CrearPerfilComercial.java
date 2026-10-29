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

public class CrearPerfilComercial implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPerfilComercial.class);

	private final String SQL_SELECT_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilComercial_Select_Count_Existe.sql");
	private final String SQL_INSERT_PERFIL = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilComercial_Insert_Perfil.sql");
	private final String SQL_UPDATE_POR_DEFECTO = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilComercial_Update_PorDefecto.sql");

	public static class EntradaCrearPerfilComercial {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilComercialBase;
		public String p_DescripcionPerfilComercialBase;
		public String p_IdCategoriaInversionista;
		public String p_IdEstado;
		public String p_PorDefecto;
		public String p_IdPerfilProductos;
		public String p_IdPerfilCobros;
		public String p_IdPerfilCobrosPeru;
		public String p_IdPerfilLimites;
		public String p_IdPerfilCondicionesOperativas;
		public String p_IdModeloFacturacion;
		public String p_TipoEnrolamiento;

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

	public static class SalidaCrearPerfilComercial {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPerfilComercial() {
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

	public static class Contador {
		public Long Contador;
	}

	public static class InsertPerfil {
		public String IdPerfilComercialBase;
		public String DescripcionPerfilComercialBase;
		public String IdCategoriaInversionista;
		public String IdPerfilProductos;
		public String IdPerfilCobros;
		public String IdPerfilCobrosPeru;
		public String IdPerfilLimites;
		public String IdPerfilCondicionesOperativas;
		public String IdModeloFacturacion;
		public String IdEstado;
		public String PorDefecto;
		public Long IdEmpresa;
		public String TipoEnrolamiento;
	}

	public static class UpdatePorDefecto {
		public String IdCategoriaInversionista;
		public String TipoEnrolamiento;
		public String IdPerfilComercialBase;
		public Long IdEmpresa;
	}

	public SalidaCrearPerfilComercial logica(Connection conn, EntradaCrearPerfilComercial in) throws Exception {
		logger.info("INIT CrearPerfilComercial");
		SalidaCrearPerfilComercial salida = new SalidaCrearPerfilComercial();
		SQL sql = new SQL();

		try {
			// Validar si perfil comercial ya existe
			logger.debug("Validar si perfil comercial base ya existe");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdPerfilComercialBase", in.p_IdPerfilComercialBase);

			List<Contador> countResult = sql.query(conn, SQL_SELECT_COUNT_EXISTE, params, Contador.class);
			Long v_countExiste = (countResult != null && countResult.size() > 0) ? countResult.get(0).Contador : 0L;

			if (v_countExiste > 0) {
				logger.warn("Perfil comercial base ya existe");
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("CODIGO_MODELO_COMERCIAL_EXISTE", 
					"El perfil comercial '" + in.p_IdPerfilComercialBase + "' ya existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Solo simulación
			if ("S".equals(in.p_simularOperacion)) {
				logger.debug("Solo simulación, saliendo");
				return salida;
			}

			// Insertar perfil comercial
			logger.debug("INSERT perfil comercial base");
			InsertPerfil insertData = new InsertPerfil();
			insertData.IdPerfilComercialBase = in.p_IdPerfilComercialBase;
			insertData.DescripcionPerfilComercialBase = in.p_DescripcionPerfilComercialBase;
			insertData.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
			insertData.IdPerfilProductos = in.p_IdPerfilProductos;
			insertData.IdPerfilCobros = in.p_IdPerfilCobros;
			insertData.IdPerfilCobrosPeru = in.p_IdPerfilCobrosPeru;
			insertData.IdPerfilLimites = in.p_IdPerfilLimites;
			insertData.IdPerfilCondicionesOperativas = in.p_IdPerfilCondicionesOperativas;
			insertData.IdModeloFacturacion = in.p_IdModeloFacturacion;
			insertData.IdEstado = in.p_IdEstado;
			insertData.PorDefecto = in.p_PorDefecto;
			insertData.IdEmpresa = in.p_IdEmpresa;
			insertData.TipoEnrolamiento = in.p_TipoEnrolamiento != null ? in.p_TipoEnrolamiento : "FULL";

			sql.batch(conn, SQL_INSERT_PERFIL, java.util.Arrays.asList(insertData), InsertPerfil.class);

			salida.P_OUT_REFID = 1L;

			// Si es por defecto, actualizar otros perfiles de la misma categoría
			if ("S".equals(in.p_PorDefecto)) {
				logger.debug("Actualizar otros perfiles a PorDefecto='N'");
				UpdatePorDefecto updateData = new UpdatePorDefecto();
				updateData.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
				updateData.TipoEnrolamiento = insertData.TipoEnrolamiento;
				updateData.IdPerfilComercialBase = in.p_IdPerfilComercialBase;
				updateData.IdEmpresa = in.p_IdEmpresa;

				sql.batch(conn, SQL_UPDATE_POR_DEFECTO, java.util.Arrays.asList(updateData), UpdatePorDefecto.class);
			}

			// Agregar log
			logger.debug("Agregar log de ingreso");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "MODELO COMERCIAL";
			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.SubRegistro = "INGRESO";
			detLog.CampoRegistro = "IdPerfilComercialBase";
			detLog.ValorAntiguo = "";
			detLog.ValorNuevo = in.p_IdPerfilComercialBase;
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en CrearPerfilComercial: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERFIL_COMERCIAL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPerfilComercial");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPerfilComercial in = gson.fromJson(jin.toString(), EntradaCrearPerfilComercial.class);
		SalidaCrearPerfilComercial out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"COM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPerfilComercialBase\":\"TEST001\",\"p_DescripcionPerfilComercialBase\":\"Test Perfil\",\"p_IdCategoriaInversionista\":\"CAT001\",\"p_IdEstado\":\"ACT\",\"p_PorDefecto\":\"N\",\"p_IdPerfilProductos\":\"PROD001\",\"p_IdPerfilCobros\":\"COB001\",\"p_IdPerfilCobrosPeru\":\"\",\"p_IdPerfilLimites\":\"LIM001\",\"p_IdPerfilCondicionesOperativas\":\"COND001\",\"p_IdModeloFacturacion\":\"FACT001\",\"p_TipoEnrolamiento\":\"FULL\"}";
			EntradaCrearPerfilComercial entrada = gson.fromJson(json, EntradaCrearPerfilComercial.class);
			CrearPerfilComercial demo = new CrearPerfilComercial();
			SalidaCrearPerfilComercial salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
