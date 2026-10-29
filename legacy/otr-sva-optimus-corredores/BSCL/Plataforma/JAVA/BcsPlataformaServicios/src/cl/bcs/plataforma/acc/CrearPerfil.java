package cl.bcs.plataforma.acc;

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

public class CrearPerfil implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPerfil.class);

	private final String SQL_SELECT_COUNT_PERFILES = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearPerfil_Select_Count_Perfiles.sql");
	private final String SQL_INSERT_PERFIL = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearPerfil_Insert_Perfil.sql");

	public static class EntradaCrearPerfil {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Nombre;
		public String p_IdEstado;

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

	public static class SalidaCrearPerfil {
		public Long P_OUT_NEW_PERFIL_ID;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPerfil() {
			this.P_OUT_NEW_PERFIL_ID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InsertPerfil {
		public Long IdPerfil;
		public Long IdEmpresa;
		public String Nombre;
		public String IdEstado;
	}
	
	public static class Contador {
		public Long Contador;
	}

	public SalidaCrearPerfil logica(Connection conn, EntradaCrearPerfil in) throws Exception {
		logger.info("INIT CrearPerfil");
		SalidaCrearPerfil salida = new SalidaCrearPerfil();
		SQL sql = new SQL();

		try {
			// Validar perfil repetido
			logger.debug("Validar perfil repetido");
			HashMap<String, Object> params = new HashMap<>();
			params.put("Nombre", in.p_Nombre);

			List<Contador> countPerfiles = sql.query(conn, SQL_SELECT_COUNT_PERFILES, params, Contador.class);
			Long v_perfilesRepetidos = (countPerfiles != null && countPerfiles.size() > 0) ? countPerfiles.get(0).Contador : 0L;
			
			if (v_perfilesRepetidos > 0) {
				logger.debug("Perfil repetido encontrado");
				
				logger.warn("Rol ya existe");
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_PERFIL_REPETIDO", "El perfil " + in.p_Nombre + " ya existe"));
				salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
				return salida;
				
			}

			// Obtener secuencia para nuevo perfil
			logger.debug("Obtener secuencia");
			
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Perfil", 1L);
			Long v_IdPerfil = secuencia.get(0);
			salida.P_OUT_NEW_PERFIL_ID = v_IdPerfil;

			// Insertar perfil
			logger.debug("Insertar perfil");
			
			List<InsertPerfil> paramsInsertPerfil = new ArrayList<>();
			InsertPerfil insertData = new InsertPerfil();
			insertData.IdPerfil = v_IdPerfil;
			insertData.IdEmpresa = in.p_IdEmpresa;
			insertData.Nombre = in.p_Nombre;
			insertData.IdEstado = in.p_IdEstado;
			paramsInsertPerfil.add(insertData);

			sql.batch(conn, SQL_INSERT_PERFIL, paramsInsertPerfil, InsertPerfil.class);
			

		} catch (Exception ex) {
			logger.error("Error en CrearPerfil: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERFIL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPerfil");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPerfil in = gson.fromJson(jin.toString(), EntradaCrearPerfil.class);
		SalidaCrearPerfil out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_Nombre\":\"NOEXISTO!@#\",\"p_IdEstado\":\"ACT\"}";
			EntradaCrearPerfil entrada = gson.fromJson(json, EntradaCrearPerfil.class);
			CrearPerfil demo = new CrearPerfil();
			SalidaCrearPerfil salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
