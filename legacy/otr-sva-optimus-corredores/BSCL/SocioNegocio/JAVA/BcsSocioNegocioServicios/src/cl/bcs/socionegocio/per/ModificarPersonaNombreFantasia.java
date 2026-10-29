package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migracion fiel de PER.ModificarPersonaNombreFantasia.sql.
 *
 * A diferencia de sus hermanos (ModificarPersonaDireccionFisica, ModificarPersonaTarjetaBancaria, etc.),
 * el SP original NUNCA usa el flujo de RegistroSolicitudCambio/autorizacion: declara v_IdConcepto='MODNFA'
 * y v_RequiereAutorizacion pero jamas los utiliza (ni llama a AUT.RequiereEventoAutorizacion, ni valida
 * existencia de la entidad). Simplemente registra el log de auditoria y actualiza la tabla de inmediato,
 * siempre. Confirmado con el usuario: se replica ese comportamiento tal cual (fidelidad estricta), no se
 * completa el flujo de autorizacion que aparenta ser codigo incompleto/abandonado.
 */
public class ModificarPersonaNombreFantasia implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ModificarPersonaNombreFantasia.class);

	private final String SQL_SELECT_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaNombreFantasia_SelectNombreFantasia.sql");
	private final String SQL_UPDATE_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaNombreFantasia_UpdateNombreFantasia.sql");

	public static class EntradaServicioModificarPersonaNombreFantasia{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public Long p_IdNombreFantasia;
		public String p_NombreFantasia;
		public String p_IdEstado;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class NombreFantasia{
		public Long IdNombreFantasia;
		public Long IdPersona;
		public String DescripcionNombreFantasia;
		public String IdEstado;
	}

	public static class NombreFantasiaUpdate{
		public Long p_IdNombreFantasia;
		public Long p_IdPersona;
		public String p_NombreFantasia;
		public String p_IdEstado;
	}

	public static class SalidaModificarPersonaNombreFantasia extends SalidaEstandar{
		public SalidaModificarPersonaNombreFantasia(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new java.util.ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new java.util.ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioModificarPersonaNombreFantasia in = gson.fromJson(jin.toString(), EntradaServicioModificarPersonaNombreFantasia.class);
		SalidaModificarPersonaNombreFantasia out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaModificarPersonaNombreFantasia logica(Connection conn, EntradaServicioModificarPersonaNombreFantasia in) throws Exception{
		logger.info("INICIO::ModificarPersonaNombreFantasia");
		SalidaModificarPersonaNombreFantasia salida = new SalidaModificarPersonaNombreFantasia();
		SQL sql = new SQL();

		String fechaHoraSistema = Utils.getFechaHoraSistema(conn, in.p_IdEmpresa);
		String fechaSistemaCorta = fechaHoraSistema != null && fechaHoraSistema.length() >= 10 ? fechaHoraSistema.substring(0, 10) : fechaHoraSistema;

		HashMap<String, Object> paramsSelect = new HashMap<>();
		paramsSelect.put("p_IdNombreFantasia", in.p_IdNombreFantasia);
		paramsSelect.put("p_IdPersona", in.p_IdPersona);
		List<NombreFantasia> resultado = sql.query(conn, SQL_SELECT_NOMBRE_FANTASIA, paramsSelect, NombreFantasia.class);

		try {
			InAgregarLog entrada = new InAgregarLog();

			InAgregarLog.Log log = new InAgregarLog.Log();
			log.IdCabeceraLog = in.p_IdPersona;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = in.p_IdModulo;
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistemaCorta;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdPersona = in.p_IdPersona;
			log.IdTipoRegistro = "SOC";
			entrada.p_Log.add(log);

			if(!resultado.isEmpty()){
				NombreFantasia datos = resultado.get(0);
				// El SP original usa p_IdUsuario (no un rotulo de categoria) como "SubRegistro" en ambos detalles.
				String subRegistro = String.valueOf(in.p_IdUsuario);
				entrada.agregarDetalleLogBasico(in.p_IdPersona, 0L, "NombreFantasia", datos.DescripcionNombreFantasia, in.p_NombreFantasia, subRegistro);
				entrada.agregarDetalleLogBasico(in.p_IdPersona, 0L, "Estado", datos.IdEstado, in.p_IdEstado, subRegistro);
			}

			new AgregarLog().logica(conn, entrada);
		} catch (Exception exLog) {
			logger.error("Error registrando auditoria en ModificarPersonaNombreFantasia: " + exLog.getMessage(), exLog);
		}

		NombreFantasiaUpdate update = new NombreFantasiaUpdate();
		update.p_IdNombreFantasia = in.p_IdNombreFantasia;
		update.p_IdPersona = in.p_IdPersona;
		update.p_NombreFantasia = in.p_NombreFantasia;
		update.p_IdEstado = in.p_IdEstado;
		sql.batch(conn, SQL_UPDATE_NOMBRE_FANTASIA, java.util.Arrays.asList(update), NombreFantasiaUpdate.class);

		logger.info("FIN::ModificarPersonaNombreFantasia");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":1790725,\"p_IdNombreFantasia\":1,\"p_NombreFantasia\":\"TEST\",\"p_IdEstado\":\"ACT\"}";
			EntradaServicioModificarPersonaNombreFantasia in = gson.fromJson(json, EntradaServicioModificarPersonaNombreFantasia.class);

			ModificarPersonaNombreFantasia demo = new ModificarPersonaNombreFantasia();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaModificarPersonaNombreFantasia out = demo.logica(con, in);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(out.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
