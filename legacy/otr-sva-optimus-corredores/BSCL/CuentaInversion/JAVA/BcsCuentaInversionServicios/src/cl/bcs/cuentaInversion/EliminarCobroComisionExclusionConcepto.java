package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCobroComisionExclusionConcepto.Concepto;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarCobroComisionExclusionConcepto implements JavaCall {
	private static final Logger logger = LogManager.getLogger(EliminarCobroComisionExclusionConcepto.class );
	private final String SQL_DELETE_CONCEPTOS_EXCLUIDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_DeleteCobroComisionExclusionConceptos.sql");
	private final String SQL_SELECT_CONCEPTOS_EXCLUIDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_SelectCobroComisionExclusionConceptos.sql");
	
	public static class EntradaEliminarCobroComisionExclusionConcepto extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Concepto> p_Conceptos;
		
		public EntradaEliminarCobroComisionExclusionConcepto() {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CTA";
			this.p_UsaReglasNegocio = "N";
			this.p_Conceptos = new ArrayList<Concepto>();
		}
	}
	
	public static class SalidaEliminarCobroComisionExclusionConcepto extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaEliminarCobroComisionExclusionConcepto () {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		}
	}
	
	public SalidaEliminarCobroComisionExclusionConcepto logica (Connection con, EntradaEliminarCobroComisionExclusionConcepto entrada) throws Exception {
		logger.info("INICIO::EliminarCobroComisionExclusionConcepto");
		long inicio = System.currentTimeMillis();
		
		SalidaEliminarCobroComisionExclusionConcepto salida = new SalidaEliminarCobroComisionExclusionConcepto();
		
		if(entrada.p_Conceptos.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_CONCEPTOS",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		SQL sql =new SQL();
		
		HashMap<String, Object> paramExistencia = new HashMap<String, Object>(1);
		List<Long> idsConceptos = new ArrayList<Long>();
		entrada.p_Conceptos.stream().forEach(c -> {
			idsConceptos.add(c.IdExclusionConcepto);
		});
		paramExistencia.put("idsExclusionConcepto", idsConceptos);
		Map<Long, Concepto> conceptosExistentes = sql.query(con, SQL_SELECT_CONCEPTOS_EXCLUIDOS, paramExistencia, Concepto.class).stream().collect(Collectors.toMap(c -> c.IdExclusionConcepto, c -> c));
		List<Concepto> conceptosEliminar = new ArrayList<Concepto>();
		
		for(Concepto concepto : entrada.p_Conceptos) {
			concepto.IdConcepto = conceptosExistentes.get(concepto.IdExclusionConcepto) != null ? conceptosExistentes.get(concepto.IdExclusionConcepto).IdConcepto : concepto.IdConcepto;
			concepto.Existe = conceptosExistentes.get(concepto.IdExclusionConcepto) != null  ? "S" : "N";
			if (("S").equals(concepto.Existe)) {
				conceptosEliminar.add(concepto);
			} else {
				HashMap<String, String> paramMensaje = new HashMap<String, String>(1);
				paramMensaje.put("IdExclusionConcepto", concepto.IdExclusionConcepto.toString());
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_EXCLUSION_CONCEPTO_NO_EXISTE", paramMensaje, con, entrada.p_IdEmpresa));
			}
		}
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0L) {
			return salida;
		}
		
		if(!conceptosEliminar.isEmpty()) {
			sql.batch(con, SQL_DELETE_CONCEPTOS_EXCLUIDOS, conceptosEliminar, Concepto.class);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_EXCLUSION_CONCEPTO_BORRADA",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			
			agregarLog(con, conceptosEliminar, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		}
		
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarCobroComisionExclusionConcepto", fin - inicio);
		return salida;
	}
	
	private void agregarLog(Connection con, List<Concepto> conceptos, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::EliminarCobroComisionExclusionConcepto.agregarLog");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
		
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		for (Concepto concepto : conceptos) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = concepto.IdExclusionConcepto;
			log.IdTipoLog = "ELIMINACION";
			log.IdModulo = "CTA";
			log.IdUsuario = idUsuario;
			log.IdEmpresa = idEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = idUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "EXCLUSION_CONCEPTO_COBRO_COMISION";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "Eliminacion de Concepto "+concepto.IdConcepto+" en Exclusion de Concepto Cobro Comision Patrimonio";						
			entradaAgregarLog.p_Log.add(log);
		}
		
		agregarLogService.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarCobroComisionExclusionConcepto.agregarLog", fin - inicio);
	}


	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 33,\r\n"
					+ "  \"p_IdModulo\": \"CTA\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "  \"p_Conceptos\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdExclusionConcepto\": 105\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdExclusionConcepto\": 105\r\n"
					+ "    }\r\n"
					+ "  ]\r\n"
					+ "}";
			EntradaEliminarCobroComisionExclusionConcepto Objin = gson.fromJson(json, EntradaEliminarCobroComisionExclusionConcepto.class);
			EliminarCobroComisionExclusionConcepto demo = new EliminarCobroComisionExclusionConcepto();
			try {
				SalidaEliminarCobroComisionExclusionConcepto salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
//				con.commit();
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}



	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaEliminarCobroComisionExclusionConcepto jin=gson.fromJson(in.toString() , EntradaEliminarCobroComisionExclusionConcepto.class);
		SalidaEliminarCobroComisionExclusionConcepto out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
}