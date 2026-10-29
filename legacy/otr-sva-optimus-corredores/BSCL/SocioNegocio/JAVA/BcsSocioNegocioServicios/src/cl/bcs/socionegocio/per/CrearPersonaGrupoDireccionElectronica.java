package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Wrapper para llamar al stored procedure CrearPersonaGrupoDireccionElectronica
 * Este wrapper NO migra la lógica, solo ejecuta el SP existente
 * 
 * Autor: Migración Java
 * Fecha: Mayo 2026
 */
public class CrearPersonaGrupoDireccionElectronica implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CrearPersonaGrupoDireccionElectronica.class);

	// Clases de entrada/salida
	public static class EntradaCrearPersonaGrupoDireccionElectronica {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Grupo;
		public String p_Comentario;
		public Long p_IdPersona;
		public String p_IdEstado;
	}

	public static class SalidaCrearPersonaGrupoDireccionElectronica {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPersonaGrupoDireccionElectronica() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
	}

	public SalidaCrearPersonaGrupoDireccionElectronica logica(Connection conn,
			EntradaCrearPersonaGrupoDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::CrearPersonaGrupoDireccionElectronica (Wrapper)");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		SalidaCrearPersonaGrupoDireccionElectronica salida = new SalidaCrearPersonaGrupoDireccionElectronica();

		try {
			// Preparar parámetros de entrada
			HashMap<String, Object> parmsIN = new HashMap<>();
			parmsIN.put("p_IdEmpresa", entrada.p_IdEmpresa);
			parmsIN.put("p_IdUsuario", entrada.p_IdUsuario);
			parmsIN.put("p_IdModulo", entrada.p_IdModulo);
			parmsIN.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
			parmsIN.put("p_Grupo", entrada.p_Grupo);
			parmsIN.put("p_Comentario", entrada.p_Comentario);
			parmsIN.put("p_IdPersona", entrada.p_IdPersona);
			parmsIN.put("p_IdEstado", entrada.p_IdEstado);

			// Preparar tipos de salida
			HashMap<String, Class> parmsOUT = new HashMap<>();
			parmsOUT.put("p_out_RefId", Long.class);
			parmsOUT.put("p_out_AdvertenciasContador", Long.class);
			parmsOUT.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
			parmsOUT.put("p_out_ErroresContador", Long.class);
			parmsOUT.put("p_out_ErroresDetalle", ErrorDetalle.class);

			// Ejecutar SP
			String queryCall = "CALL \"BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronica\" ("
					+ ":p_IdEmpresa,"
					+ ":p_IdUsuario,"
					+ ":p_IdModulo,"
					+ ":p_UsaReglasNegocio,"
					+ ":p_Grupo,"
					+ ":p_Comentario,"
					+ ":p_IdPersona,"
					+ ":p_IdEstado,"
					+ "@p_out_RefId,"
					+ "@p_out_AdvertenciasContador,"
					+ "$p_out_AdvertenciasDetalle,"
					+ "@p_out_ErroresContador,"
					+ "$p_out_ErroresDetalle)";

			HashMap<String, Object> resultados = sql.call(conn, queryCall, parmsIN, parmsOUT);

			// Mapear resultados
			salida.P_OUT_REFID = (Long) resultados.get("p_out_RefId");
			
			Long advContador = (Long) resultados.get("p_out_AdvertenciasContador");
			salida.P_OUT_ADVERTENCIASCONTADOR = (advContador != null) ? advContador.intValue() : 0;
			
			salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) resultados.get("p_out_AdvertenciasDetalle");
			
			Long errContador = (Long) resultados.get("p_out_ErroresContador");
			salida.P_OUT_ERRORESCONTADOR = (errContador != null) ? errContador.intValue() : 0;
			
			salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) resultados.get("p_out_ErroresDetalle");

		} catch (Exception ex) {
			logger.error("Error en wrapper CrearPersonaGrupoDireccionElectronica: " + ex.getMessage(), ex);
			throw ex;
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaGrupoDireccionElectronica (Wrapper) - Tiempo: {} ms", fin - inicio);

		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPersonaGrupoDireccionElectronica entrada = gson.fromJson(jin.toString(),
				EntradaCrearPersonaGrupoDireccionElectronica.class);
		SalidaCrearPersonaGrupoDireccionElectronica salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST CrearPersonaGrupoDireccionElectronica (Wrapper) ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\","
						+ "\"p_Grupo\":\"Grupo Test\",\"p_Comentario\":\"Comentario de prueba\","
						+ "\"p_IdPersona\":123,\"p_IdEstado\":\"ACT\"}";
				EntradaCrearPersonaGrupoDireccionElectronica entrada = gson.fromJson(json,
						EntradaCrearPersonaGrupoDireccionElectronica.class);

				CrearPersonaGrupoDireccionElectronica wrapper = new CrearPersonaGrupoDireccionElectronica();
				SalidaCrearPersonaGrupoDireccionElectronica salida = wrapper.logica(conn, entrada);

				System.out.println("RefID: " + salida.P_OUT_REFID);
				System.out.println("Errores: " + salida.P_OUT_ERRORESCONTADOR);
				System.out.println("Advertencias: " + salida.P_OUT_ADVERTENCIASCONTADOR);

				conn.rollback();

			} catch (Exception e) {
				e.printStackTrace();
				conn.rollback();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
