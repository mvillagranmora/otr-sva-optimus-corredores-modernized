package cl.bcs.plataforma.ins;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarNivelClasificacionActivo implements JavaCall {

	// -------------------------------------------------
	// Llamados SQL
	// -------------------------------------------------
	private final String SQL_CONSULTAR_NIVEL = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultaNivelesDeClasificacion_SelectNivelesDeClasificacion.sql");
	private final String SQL_ELIMINAR_NIVEL = loadResourceToString("cl/bcs/plataforma/ins/sql/EliminarNivelClasificacionActivo_DeleteNivelClasificacionActivo.sql");
	private final String SQL_ELIMINAR_RELACION = loadResourceToString("cl/bcs/plataforma/ins/sql/EliminarRelacionClasificacionActivo_DeleteRelacionClasificacionActivo.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class EliminarNivelClasificacionActivo_Entrada {
		public long p_IdArbolClasificacionActivo;
		public String p_Descripcion;
		public long p_Padre;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------

	public static class EliminarNivelClasificacionActivo_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado> P_OUT_RESULTADOPROCESO;

		public EliminarNivelClasificacionActivo_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO = new ArrayList<Resultado>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Resultado {
		String respuesta = "";
		String mensajerespuesta = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class ConsultarNivel {
		public long IdArbolClasificacionActivo;
		public String Tipo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EliminarNivel {
		public long p_IdArbolClasificacionActivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public EliminarNivelClasificacionActivo_Salida logic(Connection conn, EliminarNivelClasificacionActivo_Entrada in)
			throws Exception {
		SQL sql = new SQL();

		String Mensaje = "NOOK";
		EliminarNivelClasificacionActivo_Salida salida = new EliminarNivelClasificacionActivo_Salida();

		try {
			Mensaje = "OK";
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdArbolClasificacionActivo", in.p_IdArbolClasificacionActivo);
			parametroEntrada.put("p_Descripcion", null);

			List<ConsultarNivel> Consulnivel = sql.query(conn, SQL_CONSULTAR_NIVEL, parametroEntrada,ConsultarNivel.class);
			String Tipo = Consulnivel.get(0).Tipo;
			System.out.println(Consulnivel);

			if (Consulnivel.isEmpty()) {
				System.out.println("El dato no existe.");
				Mensaje = "NOEXISTE";
			}
			if (Tipo.equals("O")) {
				System.out.println("El dato es Optimus.");
				Mensaje = "ESOPTIMUS";
			} else {
				System.out.println("El dato si existe.");

				List<EliminarNivel> listDeleteNivel = new ArrayList<EliminarNivel>();
				EliminarNivel delete = new EliminarNivel();
				delete.p_IdArbolClasificacionActivo = in.p_IdArbolClasificacionActivo;
				listDeleteNivel.add(delete);
				sql.batch(conn, SQL_ELIMINAR_RELACION, listDeleteNivel, EliminarNivel.class); /// REVISAR CONSULTA
				sql.batch(conn, SQL_ELIMINAR_NIVEL, listDeleteNivel, EliminarNivel.class); /// REVISAR CONSULTA
				Mensaje = "ELIMINADO";
			}

		} catch (Exception e) {
			e.getMessage();
			Mensaje = "NOELIMINADO";
			salida.P_OUT_ERRORESCONTADOR++;
		}

		Resultado res = new Resultado();

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			if (Mensaje.equals("NOEXISTE")) {
				GeneraElementosError(conn, "ERROR", 1L, "UI_ERROR", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			} else if (Mensaje.equals("ESOPTIMUS")) {
				GeneraElementosError(conn, "ERROR", 1L, "UI_ERROR_ES_OPTIMUS", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			} else if (Mensaje.equals("ELIMINADO")) {
				GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);
			} else {
				GeneraElementosError(conn, "ERROR", 1L, "UI_ERROR", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			}

		} else {
			GeneraElementosError(conn, "ERROR", 1L, "UI_ERROR", salida.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR++;
		}

		return salida;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EliminarNivelClasificacionActivo_Entrada in = gson.fromJson(jin.toString(), EliminarNivelClasificacionActivo_Entrada.class);
		EliminarNivelClasificacionActivo_Salida out = logic(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getUATConnection()) {
			SQL.exec(con, "set schema BSCL");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String jsonCL = "{\"p_IdArbolClasificacionActivo\": 1039 }";

			EliminarNivelClasificacionActivo_Entrada Objin = gson.fromJson(jsonCL,
					EliminarNivelClasificacionActivo_Entrada.class);

			EliminarNivelClasificacionActivo demo = new EliminarNivelClasificacionActivo();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				EliminarNivelClasificacionActivo_Salida ObjOut = demo.logic(con, Objin);
				// con.rollback();
				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

	public void GeneraElementosError(Connection con, String tipo, Long idEmpresa, String codMsg,
			List<MensajeSalida> P_OUT_MENSAJES) throws Exception {

		String msg = Diccionario.getMensaje(con, idEmpresa, codMsg).msg();
		if (tipo == "ERROR") {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codMsg, msg));
		} else {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, codMsg, msg));
		}

	}
	
}