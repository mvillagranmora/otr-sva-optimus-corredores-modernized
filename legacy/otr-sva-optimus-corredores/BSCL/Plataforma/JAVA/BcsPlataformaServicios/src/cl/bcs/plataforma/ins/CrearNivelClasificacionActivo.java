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

public class CrearNivelClasificacionActivo implements JavaCall {


	// -------------------------------------------------
	// Llamados SQL
	// -------------------------------------------------

	private final String SQL_CONSULTAR_NIVEL = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultaNivelesDeClasificacion_SelectNivelesDeClasificacion.sql");
	private final String SQL_CREAR_NIVEL = loadResourceToString("cl/bcs/plataforma/ins/sql/CrearNivelClasificacionActivo_InsertNivelClasificacion.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class CrearNivelClasificacionActivo_Entrada {
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

	public static class CrearNivelClasificacionActivo_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado> P_OUT_RESULTADOPROCESO;

		public CrearNivelClasificacionActivo_Salida() {
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
		public String Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CrearNivel {
		public String p_Descripcion;
		public long p_Padre;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public CrearNivelClasificacionActivo_Salida logica(Connection conn, CrearNivelClasificacionActivo_Entrada in)
			throws Exception {
		SQL sql = new SQL();

		String Mensaje = "NOOK";
		CrearNivelClasificacionActivo_Salida salida = new CrearNivelClasificacionActivo_Salida();

		
		Mensaje = "NO GUARDADO";
		HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
		parametroEntrada.put("p_IdArbolClasificacionActivo", null);
		parametroEntrada.put("p_Descripcion", in.p_Descripcion);

		List<ConsultarNivel> Consulnivel = sql.query(conn, SQL_CONSULTAR_NIVEL, parametroEntrada,ConsultarNivel.class);
		System.out.println(Consulnivel);
		if (Consulnivel.isEmpty()) {
			System.out.println("NO ES IGUAL");
			List<CrearNivel> listInsertNivel = new ArrayList<CrearNivel>();
			CrearNivel insert = new CrearNivel();
			insert.p_Descripcion = in.p_Descripcion;
			insert.p_Padre = in.p_Padre;
			listInsertNivel.add(insert);
			sql.batch(conn, SQL_CREAR_NIVEL, listInsertNivel, CrearNivel.class);
			Mensaje = "CREADO";
		} else {
			System.out.println("ES IGUAL");
			Mensaje = "EXISTE";
		}
		


		if (salida.P_OUT_ERRORESCONTADOR == 0) {

			if (Mensaje.equals("EXISTE")) {
				GeneraElementosError(conn, "ERROR", 1L, "UI_ERROR_MERCADO_DUPLICADO", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			} else {
				GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);
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
		CrearNivelClasificacionActivo_Entrada in = gson.fromJson(jin.toString(),
				CrearNivelClasificacionActivo_Entrada.class);
		CrearNivelClasificacionActivo_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getMBIPREConnection()) {
			SQL.exec(con, "set schema BSCL");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\"p_Descripcion\":\"ASDF\",\"p_Padre\":0}";

			// "{\"p_CodigoUsuario\": EVILLAGRAN}";

			CrearNivelClasificacionActivo_Entrada Objin = gson.fromJson(jsonCL,
					CrearNivelClasificacionActivo_Entrada.class);

			CrearNivelClasificacionActivo demo = new CrearNivelClasificacionActivo();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				CrearNivelClasificacionActivo_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
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