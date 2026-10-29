
package cl.bcs.plataforma.ins;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class EliminarInstrumentoEnClasificacion implements JavaCall {

	// -------------------------------------------------
	// Llamados SQL
	// -------------------------------------------------

	private final String SQL_ELIMINARINSTRUMENTOENCLASIFICACION = loadResourceToString("cl/bcs/plataforma/ins/sql/EliminarInstrumentoEnClasificacion_DeleteClasificacionIstrumento.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class EliminarInstrumentoEnClasificacion_Entrada {
		public long p_IdInstrumento;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------

	public static class EliminarNivel {
		public long IdInstrumento;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EliminarInstrumentoEnClasificacion_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado> P_OUT_RESULTADOPROCESO;

		public EliminarInstrumentoEnClasificacion_Salida() {
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
		public String res;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public EliminarInstrumentoEnClasificacion_Salida logica(Connection conn, EliminarInstrumentoEnClasificacion_Entrada in) throws Exception {

		EliminarInstrumentoEnClasificacion_Salida salida = new EliminarInstrumentoEnClasificacion_Salida();

		SQL sql = new SQL();

		List<EliminarNivel> listDelete = new ArrayList<EliminarNivel>();
		EliminarNivel delete = new EliminarNivel();
		delete.IdInstrumento = in.p_IdInstrumento;
		listDelete.add(delete);

		try {
			sql.batch(conn, SQL_ELIMINARINSTRUMENTOENCLASIFICACION, listDelete, EliminarNivel.class);
		} catch (Exception e) {
			GeneraElementosError(conn, "ERROR", 1L, "UI_OPERACION_TIENE_ERRORES", salida.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR++;
		}

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);
		} else {
			GeneraElementosError(conn, "ERROR", 1L, "UI_OPERACION_TIENE_ERRORES", salida.P_OUT_MENSAJES);
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
		EliminarInstrumentoEnClasificacion_Entrada in = gson.fromJson(jin.toString(),
				EliminarInstrumentoEnClasificacion_Entrada.class);
		EliminarInstrumentoEnClasificacion_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			SQL.exec(con, "set schema BSCL");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\"p_IdInstrumento\":300732}";

			EliminarInstrumentoEnClasificacion_Entrada Objin = gson.fromJson(jsonCL,
					EliminarInstrumentoEnClasificacion_Entrada.class);

			EliminarInstrumentoEnClasificacion demo = new EliminarInstrumentoEnClasificacion();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				EliminarInstrumentoEnClasificacion_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
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
