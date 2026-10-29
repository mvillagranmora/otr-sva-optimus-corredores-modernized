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

public class AsociarInstrumentoEnClasificacion implements JavaCall {

	// -------------------------------------------------
	// Llamados SQL
	// -------------------------------------------------
	private final String SQL_CONSULTAASOCIARINSTRUMENTOSCLASIFICACION = loadResourceToString("cl/bcs/plataforma/ins/sql/AsociarInstrumentoEnClasificacion_SelectClasificacionIstrumento.sql");

	private final String SQL_ASOCIARINSTRUMENTOSCLASIFICACION = loadResourceToString("cl/bcs/plataforma/ins/sql/AsociarInstrumentoEnClasificacion_InsertClasificacionIstrumento.sql");

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class AsociarInstrumentoEnClasificacion_Entrada {
		public int IdRelacionClasificacion;
		public int IdArbolClasificacionActivo;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public int IdInstrumento;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------

	public static class AsociarInstrumentoEnClasificacion_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado> P_OUT_RESULTADOPROCESO;

		public AsociarInstrumentoEnClasificacion_Salida() {
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

	public static class InstrumentoEnClasificacion { // Doc

		public long IdArbolClasificacionActivo;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public long IdInstrumento;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public AsociarInstrumentoEnClasificacion_Salida logica(Connection conn, AsociarInstrumentoEnClasificacion_Entrada in) throws Exception {
		AsociarInstrumentoEnClasificacion_Salida salida = new AsociarInstrumentoEnClasificacion_Salida();
		SQL sql = new SQL();

		HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
		parametroEntrada.put("IdInstrumento", in.IdInstrumento);
		InstrumentoEnClasificacion p = new InstrumentoEnClasificacion();

		try {
			List<InstrumentoEnClasificacion> activos = sql.query(conn,SQL_CONSULTAASOCIARINSTRUMENTOSCLASIFICACION, parametroEntrada,InstrumentoEnClasificacion.class);
			if (!(activos.size() > 0)) {
				p.IdArbolClasificacionActivo = in.IdArbolClasificacionActivo;
				p.IdClasificacionTipoInstrumento = in.IdClasificacionTipoInstrumento;
				p.IdTipoInstrumento = in.IdTipoInstrumento;
				p.IdInstrumento = in.IdInstrumento;

				List<InstrumentoEnClasificacion> pp = new ArrayList<>();
				pp.add(p);
				sql.batch(conn, SQL_ASOCIARINSTRUMENTOSCLASIFICACION, pp, InstrumentoEnClasificacion.class);

				GeneraElementosError(conn, "OK", 1L, "UI_OPERACION_INGRESO_EXITO", salida.P_OUT_MENSAJES);
			} else {
				GeneraElementosError(conn, "ERROR", 1L, "UI_EXISTE_INSTRUMENTO", salida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR++;
			}

		} catch (Exception e) {
			String codigo = "ERROR DE EJECUCION ";
			String descripcion = Diccionario.getMensaje(conn, Long.valueOf(in.IdInstrumento), codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		return salida;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AsociarInstrumentoEnClasificacion_Entrada in = gson.fromJson(jin.toString(),
				AsociarInstrumentoEnClasificacion_Entrada.class);
		AsociarInstrumentoEnClasificacion_Salida out = logica(conn, in);

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
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\"IdRelacionClasificacion\": 0 ,\"IdArbolClasificacionActivo\": 1038,\"IdClasificacionTipoInstrumento\": 'FM',\"IdTipoInstrumento\": 'FFM',\"IdInstrumento\":471679 }";

			// "{\"p_CodigoUsuario\": EVILLAGRAN}";

			AsociarInstrumentoEnClasificacion_Entrada Objin = gson.fromJson(jsonCL,
					AsociarInstrumentoEnClasificacion_Entrada.class);

			AsociarInstrumentoEnClasificacion demo = new AsociarInstrumentoEnClasificacion();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				AsociarInstrumentoEnClasificacion_Salida ObjOut = demo.logica(con, Objin);
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
