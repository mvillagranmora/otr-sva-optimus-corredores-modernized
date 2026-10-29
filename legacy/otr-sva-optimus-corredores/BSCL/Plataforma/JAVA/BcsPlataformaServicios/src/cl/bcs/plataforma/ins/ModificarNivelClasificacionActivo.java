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

public class ModificarNivelClasificacionActivo implements JavaCall {

	// -------------------------------------------------
	// Llamados SQL
	// -------------------------------------------------

	private final String SQL_CONSULTAR_NIVEL = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultaNivelesDeClasificacion_SelectNivelesDeClasificacion.sql");
	private final String SQL_MODIFICAR_NIVEL = loadResourceToString("cl/bcs/plataforma/ins/sql/ModificarNivelClasificacionActivo_UpdateNivelClasificacion.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ModificarNivelClasificacionActivo_Entrada {
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

	public static class ModificarNivelClasificacionActivo_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado> P_OUT_RESULTADOPROCESO;

		public ModificarNivelClasificacionActivo_Salida() {
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

	public static class ConsultarNivel {
		public long IdArbolClasificacionActivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ModificarNivel {
		public long p_IdArbolClasificacionActivo;
		public String p_Descripcion;
		public long p_Padre;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public ModificarNivelClasificacionActivo_Salida logic(Connection conn, ModificarNivelClasificacionActivo_Entrada in)
			throws Exception {
		SQL sql = new SQL();

		String Mensaje = "NOOK";
		ModificarNivelClasificacionActivo_Salida salida = new ModificarNivelClasificacionActivo_Salida();

		try {
			Mensaje = "OK";
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdArbolClasificacionActivo", in.p_IdArbolClasificacionActivo);
			parametroEntrada.put("p_Descripcion", null);

			List<ConsultarNivel> Consulnivel = sql.query(conn, SQL_CONSULTAR_NIVEL, parametroEntrada,ConsultarNivel.class);
			System.out.println(Consulnivel);

			if (Consulnivel.isEmpty()) {
				System.out.println("El dato no existe.");
				Mensaje = "NOEXISTE";
			} else {

				System.out.println("ES IGUAL");
				List<ModificarNivel> listUpdateNivel = new ArrayList<ModificarNivel>();
				ModificarNivel update = new ModificarNivel();
				update.p_IdArbolClasificacionActivo = in.p_IdArbolClasificacionActivo;
				update.p_Descripcion = in.p_Descripcion;
				update.p_Padre = in.p_Padre;
				listUpdateNivel.add(update);
				sql.batch(conn, SQL_MODIFICAR_NIVEL, listUpdateNivel, ModificarNivel.class);
				Mensaje = "MODIFICADO";
			}

		} catch (Exception e) {
			e.getMessage();
			salida.P_OUT_ERRORESCONTADOR++;
		}

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			if (Mensaje.equals("NOEXISTE")) {
				GeneraElementosError(conn, "ERROR", 1L, "UI_OPERACION_TIENE_ERRORES", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			}
			if (Mensaje.equals("MODIFICADO")) {
				GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);
			} else {
				GeneraElementosError(conn, "ERROR", 1L, "UI_OPERACION_TIENE_ERRORES", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			}
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
		ModificarNivelClasificacionActivo_Entrada in = gson.fromJson(jin.toString(),ModificarNivelClasificacionActivo_Entrada.class);
		ModificarNivelClasificacionActivo_Salida out = logic(conn, in);

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

			String jsonCL = "{\"p_IdArbolClasificacionActivo\": 1055 ,\"p_Descripcion\": 'JUANJO 4 CLA',\"p_Padre\": 1054}";

			// "{\"p_CodigoUsuario\": EVILLAGRAN}";

			ModificarNivelClasificacionActivo_Entrada Objin = gson.fromJson(jsonCL,ModificarNivelClasificacionActivo_Entrada.class);

			ModificarNivelClasificacionActivo demo = new ModificarNivelClasificacionActivo();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				ModificarNivelClasificacionActivo_Salida ObjOut = demo.logic(con, Objin);
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