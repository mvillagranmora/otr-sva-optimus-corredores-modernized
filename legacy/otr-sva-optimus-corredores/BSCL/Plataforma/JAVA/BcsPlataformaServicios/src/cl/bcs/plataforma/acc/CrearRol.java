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

public class CrearRol implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearRol.class);

	private final String SQL_SELECT_COUNT_ROLES = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearRol_Select_Count_Roles.sql");
	private final String SQL_INSERT_ROL = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearRol_Insert_Rol.sql");

	public static class EntradaCrearRol {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Nombre;
		public Long p_IdEmpresaRol;
		public String p_IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InsertRol
	{
		public Long IdRol;
		public Long IdEmpresa;
		public String Nombre;
		public String IdEstado;
		
		public String toString()
		{
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

	public static class SalidaCrearRol {
		public Long P_OUT_NEW_ROL_ID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearRol() {
			this.P_OUT_NEW_ROL_ID = 0L;
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

	public SalidaCrearRol logica(Connection conn, EntradaCrearRol in) throws Exception {
		logger.info("INIT CrearRol");
		SalidaCrearRol salida = new SalidaCrearRol();
		SQL sql = new SQL();

		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("Nombre", in.p_Nombre);

			logger.debug("Validar si rol repetido");
			List<Contador> countRoles = sql.query(conn, SQL_SELECT_COUNT_ROLES, params, Contador.class);
			Long v_rolesRepetidos = (countRoles != null && countRoles.size() > 0) ? countRoles.get(0).Contador : 0L;

			if (v_rolesRepetidos > 0) {
				logger.warn("Rol ya existe");
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_ROL_REPETIDO", "El rol " + in.p_Nombre + " ya existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			logger.debug("Generar ID Rol");
			List<Long> v_newRolId = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Rol", 1L);
			salida.P_OUT_NEW_ROL_ID = v_newRolId.get(0);

			logger.debug("INSERT Rol");
			
			List<InsertRol> paramsInsertRol = new ArrayList<>();
			InsertRol inRol = new InsertRol();
			inRol.IdRol = salida.P_OUT_NEW_ROL_ID;
			inRol.IdEmpresa = in.p_IdEmpresaRol;
			inRol.Nombre = in.p_Nombre;
			inRol.IdEstado = in.p_IdEstado;
			paramsInsertRol.add(inRol);
			
			sql.batch(conn, SQL_INSERT_ROL, paramsInsertRol, InsertRol.class);
			
		} catch (Exception ex) {
			logger.error("Error en CrearRol: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_ROL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearRol");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearRol in = gson.fromJson(jin.toString(), EntradaCrearRol.class);
		SalidaCrearRol out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_Nombre\":\"NuevoRol\",\"p_IdEmpresaRol\":1,\"p_IdEstado\":\"ACT\"}";
			EntradaCrearRol entrada = gson.fromJson(json, EntradaCrearRol.class);
			CrearRol demo = new CrearRol();
			SalidaCrearRol salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
