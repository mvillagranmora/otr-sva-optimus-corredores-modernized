package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidarConyuge implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ValidarConyuge.class);
	public String SQL_SELECT_CONYUGE = loadResourceToString("cl/bcs/socionegocio/per/sql/ValidarConyuge_SelectConyuge.sql");
	
	public static class EntradaValidarConyuge extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<EntradaConyuge> p_Conyuges;
		
		public EntradaValidarConyuge() {
			this.p_Conyuges = new ArrayList<EntradaConyuge>();
		}
	}
	
	public static class EntradaConyuge extends BcsPojo{
		public Long IdInterno;
		public Long IdPersona;
		public Long IdPersonaConyuge;
	}
	
	public static class SalidaValidarConyuge extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidarConyuge() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
		
	public static class Conyuge extends BcsPojo{
		public Long IdPersona;
		public Long IdPersonaConyuge;
		public String DocumentoIdentidadPersona;
		public String DocumentoIdentidadConyuge;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaValidarConyuge jin = gson.fromJson(in.toString(), EntradaValidarConyuge.class);
		SalidaValidarConyuge out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaValidarConyuge logica(Connection con, EntradaValidarConyuge entrada) throws Exception {
		logger.info("INICIO::ValidarConyuge");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaValidarConyuge out = new SalidaValidarConyuge();
		
		List<Conyuge> conyuges = obtenerConyuges(con, entrada.p_Conyuges);
		
		out.P_OUT_MENSAJES = validarConyuges(con, entrada, conyuges);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValidarConyuge", finServicio - inicioServicio);
		return out;
	}
	
	private List<Conyuge> obtenerConyuges(Connection con, List<EntradaConyuge> entradaConyuges) throws Exception {
		logger.debug("INICIO::ValidarConyuge.obtenerConyuges");
		long inicio = System.currentTimeMillis();
		
		List<Long> idPersonasConyuges = entradaConyuges
				.stream()
				.map(entradaConyuge->entradaConyuge.IdPersonaConyuge)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonaConyuge", idPersonasConyuges);
		SQL sql = new SQL();
		List<Conyuge> conyuges = sql.query(con, SQL_SELECT_CONYUGE, params, Conyuge.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarConyuge.obtenerConyuges", fin - inicio);
		return conyuges;
	}
	
	private List<MensajeSalida> validarConyuges(Connection con, EntradaValidarConyuge entrada, List<Conyuge> conyuges) throws Exception {
		logger.debug("INICIO::ValidarConyuge.validarConyuges");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> validaciones = new ArrayList<MensajeSalida>();
		for(EntradaConyuge entradaConyuge : entrada.p_Conyuges) {
			List<Conyuge> conyugesExistenteEnOtraPersona = conyuges
					.stream()
					.filter(conyuge->esConyungeExistenteEnOtraPersona(entradaConyuge, conyuge))
					.collect(Collectors.toList());
			for(Conyuge conyuge : conyugesExistenteEnOtraPersona) {
				HashMap<String,String> parametros = new HashMap<String,String>(3);
				parametros.put("DocumentoIdentidadConyuge", conyuge.DocumentoIdentidadConyuge);
				parametros.put("DocumentoIdentidadPersona", conyuge.DocumentoIdentidadPersona);
				MensajeSalida mensajeSalida = new MensajeSalida(entradaConyuge.IdInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_PERSONA_CONYUGE_YA_EXISTE_EN_OTRA_PERSONA", parametros, con, entrada.p_IdEmpresa);
				validaciones.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarConyuge.validarConyuges", fin - inicio);
		return validaciones;
	}
	
	private Boolean esConyungeExistenteEnOtraPersona(EntradaConyuge entradaConyuge, Conyuge conyuge) {
		logger.trace("INICIO::ValidarConyuge.esConyungeExistenteEnOtraPersona");
		long inicio = System.currentTimeMillis();
		Boolean esConyungeExistenteEnOtraPersona = conyuge.IdPersonaConyuge.equals(entradaConyuge.IdPersonaConyuge) && !conyuge.IdPersona.equals(entradaConyuge.IdPersona);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarConyuge.esConyungeExistenteEnOtraPersona", fin - inicio);
		return esConyungeExistenteEnOtraPersona;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaValidarConyuge entradaValidarConyuge = new EntradaValidarConyuge();
			entradaValidarConyuge.p_IdEmpresa = 1L;
			entradaValidarConyuge.p_IdUsuario = 21L;
			entradaValidarConyuge.p_IdModulo = "SOC";
			EntradaConyuge entradaConyuge = new EntradaConyuge();
			entradaConyuge.IdInterno = 1L;
			entradaConyuge.IdPersona = 1773904L;
			entradaConyuge.IdPersonaConyuge = 1775121L;

			entradaValidarConyuge.p_Conyuges = Arrays.asList(entradaConyuge);
			ValidarConyuge validarConyuge = new ValidarConyuge();
			try {
				SalidaValidarConyuge salidaValidarConyuge = validarConyuge.logica(con, entradaValidarConyuge);
				System.out.println(salidaValidarConyuge.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}

}
