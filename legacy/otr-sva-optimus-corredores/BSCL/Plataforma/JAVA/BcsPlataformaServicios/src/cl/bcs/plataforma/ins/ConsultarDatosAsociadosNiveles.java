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

public class ConsultarDatosAsociadosNiveles implements JavaCall {


	// -------------------------------------------------
	// Llamados SQL
	// -------------------------------------------------

	private final String SQL_CONSULTAR_ASOCIADOS = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarDatosAsociadosNiveles.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class ConsultarDatosAsociadosNiveles_Entrada {
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

	public static class ConsultarDatosAsociadosNiveles_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado> P_OUT_RESULTADOPROCESO;

		public ConsultarDatosAsociadosNiveles_Salida() {
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

		String res = "";
		long p_IdArbolClasificacionActivo;
		String p_Descripcion = "";
		long p_Padre;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class ResultadoDeEntrada {
		long IdArbolClasificacionActivo = 0;
		String Descripcion = "";
		long Padre = 0;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class ConsultaNiveles {
		public long IdArbolClasificacionActivo;
		public String Descripcion;
		public long Padre;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public ConsultarDatosAsociadosNiveles_Salida logic(Connection conn, ConsultarDatosAsociadosNiveles_Entrada in)
			throws Exception {
		SQL sql = new SQL();

		String Mensaje = "NOOK";
		ConsultarDatosAsociadosNiveles_Salida salida = new ConsultarDatosAsociadosNiveles_Salida();

		try {
			Mensaje = "OK";
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdArbolClasificacionActivo", in.p_IdArbolClasificacionActivo);

			List<ConsultaNiveles> Consulnivel = sql.query(conn, SQL_CONSULTAR_ASOCIADOS, parametroEntrada,ConsultaNiveles.class);
			System.out.println(Consulnivel);

			if (Consulnivel.isEmpty()) {
				System.out.println("No tiene datos asociados");
				Mensaje = "NOASOCIADOS";
			} else {
				System.out.println("Si tiene datos asociados");
				Mensaje = "SIASOCIADOS";
			}

		} catch (Exception e) {
			e.getMessage();
			salida.P_OUT_ERRORESCONTADOR++;
		}
		Resultado res = new Resultado();
		res.p_IdArbolClasificacionActivo = in.p_IdArbolClasificacionActivo;
		res.p_Descripcion = in.p_Descripcion;
		res.p_Padre = in.p_Padre;

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			
			if (Mensaje.equals("NOASOCIADOS")) {
				ModificarNivelClasificacionActivo.ModificarNivelClasificacionActivo_Entrada entrada = new ModificarNivelClasificacionActivo.ModificarNivelClasificacionActivo_Entrada();

				entrada.p_IdArbolClasificacionActivo = in.p_IdArbolClasificacionActivo;
				entrada.p_Descripcion = in.p_Descripcion;
				entrada.p_Padre = in.p_Padre;

				ModificarNivelClasificacionActivo mod = new ModificarNivelClasificacionActivo();
				ModificarNivelClasificacionActivo.ModificarNivelClasificacionActivo_Salida salidaa = mod.logic(conn,entrada);

				if (salidaa.P_OUT_RESULTADOPROCESO.get(0).res == "MODIFICADO") {
					GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);

					System.out.println("MODIFICADO OK OK !!");
				}

			} else {
				GeneraElementosError(conn, "ALERTA", 1L, "UI_OPERACION_TIENE_ADVERTENCIAS", salida.P_OUT_MENSAJES);

			}
		} else {
			GeneraElementosError(conn, "ERROR", 1L, "UI_OPERACION_TIENE_ERRORES", salida.P_OUT_MENSAJES);
			
		}
		

		return salida;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarDatosAsociadosNiveles_Entrada in = gson.fromJson(jin.toString(),ConsultarDatosAsociadosNiveles_Entrada.class);
		ConsultarDatosAsociadosNiveles_Salida out = logic(conn, in);

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

			String jsonCL = "{\"p_IdArbolClasificacionActivo\": 1002 ,\"p_Descripcion\": 'RENTA FIJA OSO',\"p_Padre\":0}";

			// "{\"p_CodigoUsuario\": EVILLAGRAN}";

			ConsultarDatosAsociadosNiveles_Entrada Objin = gson.fromJson(jsonCL,
					ConsultarDatosAsociadosNiveles_Entrada.class);

			ConsultarDatosAsociadosNiveles demo = new ConsultarDatosAsociadosNiveles();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				ConsultarDatosAsociadosNiveles_Salida ObjOut = demo.logic(con, Objin);
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
		} else if (tipo == "ALERTA") {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, codMsg, msg));
		} else {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, codMsg, msg));
		}
		
	}
	
}