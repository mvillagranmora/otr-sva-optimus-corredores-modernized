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
import datadog.trace.api.Trace;

public class AsignarClasificacionIndice implements JavaCall {

	private final String SQL_UPDATE_INDICES = loadResourceToString("cl/bcs/plataforma/ins/sql/AsignarClasificacionIndice_UpdateIndices.sql");

	// -----------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -----------------------------------------------------------------------------------------
	public static class AsignarClasificacionIndice_Entrada {
		public Long Padre;
		public String Indice;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ModificarNivel {
		public String p_Indice;
		public long p_Padre;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------

	public static class AsignarClasificacionIndice_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;

		public AsignarClasificacionIndice_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -----------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	// -----------------------------------------------------------------------------------------
	@Trace
	public AsignarClasificacionIndice_Salida logica(Connection conn, AsignarClasificacionIndice_Entrada in)
			throws Exception {

		AsignarClasificacionIndice_Salida salida = new AsignarClasificacionIndice_Salida();

		try {
			SQL sql = new SQL();
			List<ModificarNivel> listUpdateNivel = new ArrayList<ModificarNivel>();
			ModificarNivel update = new ModificarNivel();
			update.p_Indice = in.Indice;
			update.p_Padre = in.Padre;
			listUpdateNivel.add(update);
			sql.batch(conn, SQL_UPDATE_INDICES, listUpdateNivel, ModificarNivel.class);
			GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);
		} catch (Exception e) {
			System.out.println(e);
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
		AsignarClasificacionIndice_Entrada in = gson.fromJson(jin.toString(), AsignarClasificacionIndice_Entrada.class);
		AsignarClasificacionIndice_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -----------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -----------------------------------------------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\"Padre\": 1078 ,\"Indice\": 'UTM'}";

			AsignarClasificacionIndice_Entrada Objin = gson.fromJson(jsonCL, AsignarClasificacionIndice_Entrada.class);

			AsignarClasificacionIndice demo = new AsignarClasificacionIndice();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				AsignarClasificacionIndice_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				// con.commit();
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
