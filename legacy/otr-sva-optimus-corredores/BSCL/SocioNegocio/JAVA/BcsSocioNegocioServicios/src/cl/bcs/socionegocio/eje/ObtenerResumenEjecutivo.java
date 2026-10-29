package cl.bcs.socionegocio.eje;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerResumenEjecutivo implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ObtenerResumenEjecutivo.class);

	private final String SQL_SELECT_USUARIO_COMPLETO = loadResourceToString("cl/bcs/socionegocio/eje/sql/ObtenerResumenEjecutivo_Select_UsuarioCompleto.sql");
	private final String SQL_COUNT_EJECUTIVO_ACTIVO   = loadResourceToString("cl/bcs/socionegocio/eje/sql/ObtenerResumenEjecutivo_Count_EjecutivoActivo.sql");
	private final String SQL_SELECT_RESUMEN           = loadResourceToString("cl/bcs/socionegocio/eje/sql/ObtenerResumenEjecutivo_Select_Resumen.sql");

	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	}

	public static class Salida {
		public List<DatosEjecutivo> P_OUT_DATOS_EJECUTIVO;
		public List<ResumenEjecutivo> P_OUT_RESUMEN_EJECUTIVO;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public Salida() {
			this.P_OUT_DATOS_EJECUTIVO = new ArrayList<>();
			this.P_OUT_RESUMEN_EJECUTIVO = new ArrayList<>();
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		@Override
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
	}

	public static class UsuarioCompleto {
		public Long IdEntidad;
		public String TipoEntidad;
	}

	public static class Contador {
		public Long Contador;
	}

	public static class ResumenRow {
		public String NombreEjecutivo;
		public String Direccion;
		public String Telefono;
		public String JefeDirecto;
		public String Supervisor;
		public Long AsuntosPendientes;
		public Long ClientesActivos;
		public Long OrdenesAccionesVigentes;
		public Long AutorizacionesPendientes;
		public Long PortafoliosActivos;
		public Long OrdenesFfmmVigentes;
	}

	public static class DatosEjecutivo {
		public String NombreEjecutivo;
		public String Direccion;
		public String Telefono;
		public String JefeDirecto;
		public String Supervisor;
	}

	public static class ResumenEjecutivo {
		public Long AsuntosPendientes;
		public Long ClientesActivos;
		public Long OrdenesAccionesVigentes;
		public Long AutorizacionesPendientes;
		public Long PortafoliosActivos;
		public Long OrdenesFfmmVigentes;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public Salida logica(Connection conn, Entrada in) throws Exception {
		logger.info("INIT ObtenerResumenEjecutivo");
		Salida salida = new Salida();
		SQL sql = new SQL();

		try {
			// 1. Validar que el usuario corresponde a un empleado
			logger.debug("Validar que el usuario es empleado");
			HashMap<String, Object> paramsUsuario = new HashMap<>();
			paramsUsuario.put("IdUsuario", in.p_IdUsuario);
			List<UsuarioCompleto> usuarios = sql.query(conn, SQL_SELECT_USUARIO_COMPLETO, paramsUsuario, UsuarioCompleto.class);

			Long idEmpleado = (usuarios != null && !usuarios.isEmpty() && usuarios.get(0).IdEntidad != null) ? usuarios.get(0).IdEntidad : 0L;
			String tipoEntidad = (usuarios != null && !usuarios.isEmpty()) ? usuarios.get(0).TipoEntidad : null;

			if (!"EMP".equals(tipoEntidad)) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_USUARIO_NO_EMPLEADO",
					Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_USUARIO_NO_EMPLEADO", new HashMap<>())));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
			}

			// 2. Validar que el empleado asociado al usuario sea un ejecutivo activo
			logger.debug("Validar que el empleado es ejecutivo activo");
			HashMap<String, Object> paramsEjecutivo = new HashMap<>();
			paramsEjecutivo.put("IdEmpleado", idEmpleado);
			List<Contador> countEjecutivo = sql.query(conn, SQL_COUNT_EJECUTIVO_ACTIVO, paramsEjecutivo, Contador.class);
			long esEjecutivoActivo = (countEjecutivo != null && !countEjecutivo.isEmpty() && countEjecutivo.get(0).Contador != null) ? countEjecutivo.get(0).Contador : 0L;

			if (esEjecutivoActivo == 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_USUARIO_NO_EJECUTIVO",
					Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_USUARIO_NO_EJECUTIVO", new HashMap<>())));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
			}

			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			// 3. Obtener datos del ejecutivo y su resumen (consulta unica)
			logger.debug("Obtener datos y resumen del ejecutivo");
			HashMap<String, Object> paramsResumen = new HashMap<>();
			paramsResumen.put("IdEmpleado", idEmpleado);
			paramsResumen.put("IdEmpresa", in.p_IdEmpresa);
			paramsResumen.put("IdUsuario", in.p_IdUsuario);
			List<ResumenRow> filas = sql.query(conn, SQL_SELECT_RESUMEN, paramsResumen, ResumenRow.class);

			if (filas != null && !filas.isEmpty()) {
				ResumenRow fila = filas.get(0);

				DatosEjecutivo datosEjecutivo = new DatosEjecutivo();
				datosEjecutivo.NombreEjecutivo = fila.NombreEjecutivo;
				datosEjecutivo.Direccion = fila.Direccion;
				datosEjecutivo.Telefono = fila.Telefono;
				datosEjecutivo.JefeDirecto = fila.JefeDirecto;
				datosEjecutivo.Supervisor = fila.Supervisor;
				salida.P_OUT_DATOS_EJECUTIVO.add(datosEjecutivo);

				ResumenEjecutivo resumenEjecutivo = new ResumenEjecutivo();
				resumenEjecutivo.AsuntosPendientes = fila.AsuntosPendientes;
				resumenEjecutivo.ClientesActivos = fila.ClientesActivos;
				resumenEjecutivo.OrdenesAccionesVigentes = fila.OrdenesAccionesVigentes;
				resumenEjecutivo.AutorizacionesPendientes = fila.AutorizacionesPendientes;
				resumenEjecutivo.PortafoliosActivos = fila.PortafoliosActivos;
				resumenEjecutivo.OrdenesFfmmVigentes = fila.OrdenesFfmmVigentes;
				salida.P_OUT_RESUMEN_EJECUTIVO.add(resumenEjecutivo);
			}

		} catch (Exception ex) {
			logger.error("Error en ObtenerResumenEjecutivo: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_OBTENER_RESUMEN_EJECUTIVO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ObtenerResumenEjecutivo");
		return salida;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":70,\"p_IdModulo\":\"SOC\"}";
			Entrada entrada = gson.fromJson(json, Entrada.class);

			ObtenerResumenEjecutivo demo = new ObtenerResumenEjecutivo();
			Salida salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
