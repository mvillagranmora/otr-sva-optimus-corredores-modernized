package cl.bcs.normativa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.ModificarReporteAPV.InReporteAPV;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarReporteAPV implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarReporteAPV.class);
	
	public String SQL_SELECT_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/ModificarReporteAPV_SelectReporteAPV.sql");
	public String SQL_DELETE_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/EliminarReporteAPV_DeleteReporteAPV.sql");
    
	public static class EntradaEliminarReporteAPV extends BcsPojo {
		public Long					        p_IdEmpresa;
		public Long					        p_IdUsuario;
		public String				        p_IdModulo;
		public List<EntradaIdReporteAPV>	p_ReporteAPV;
	}

	public static class EntradaIdReporteAPV extends BcsPojo {
		public Long	IdReporteAPV;
		public String DocumentoIdentidad;
		public String NombreRegimenTributario;
		public String NombrePlanTipoInversion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarReporteAPV jin = gson.fromJson(in.toString(), EntradaEliminarReporteAPV.class);
		SalidaAdministrarBorrador salida = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject datasalida = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return datasalida;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarReporteAPV entrada) throws Exception {
		logger.info("INICIO::EliminarReporteAPV");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salida = new SalidaAdministrarBorrador();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);

		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
		};
		
		Map<Long, InReporteAPV> mapaValoresAntiguos = obtenerValoresAntiguos(con, entrada);
		
		eliminarReporte(con, entrada.p_ReporteAPV);
		agregarLogs(con, entrada, mapaValoresAntiguos);
		
		for(EntradaIdReporteAPV reporteAPV : entrada.p_ReporteAPV) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("DocumentoIdentidad", reporteAPV.DocumentoIdentidad);
			params.put("NombreRegimenTributario", reporteAPV.NombreRegimenTributario);
			params.put("NombrePlanTipoInversion", reporteAPV.NombrePlanTipoInversion);

			salida.P_OUT_MENSAJES.add(new MensajeSalida(reporteAPV.IdReporteAPV, MensajeSalida.MENSAJE_OK, "UI_REPORTE_APV_ELIMINADO", params, con, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarReporteAPV {}", (fin - inicio));
		return salida;
	}	
	
	public Map<Long, InReporteAPV> obtenerValoresAntiguos(Connection con, EntradaEliminarReporteAPV entrada) throws Exception {
	    SQL sql = new SQL();
	    List<Long> idsReporteAPV = entrada.p_ReporteAPV.stream().map(r -> r.IdReporteAPV).collect(Collectors.toList());
	    
	    HashMap<String, Object> params = new HashMap<String, Object>();
	    params.put("IdReporteAPV", idsReporteAPV);
	    
	    logger.debug("INICIO::SQL_SELECT_REPORTE_APV");
		List<InReporteAPV> lista = sql.query(con, SQL_SELECT_REPORTE_APV, params, InReporteAPV.class);
		logger.debug("FIN::SQL_SELECT_REPORTE_APV");

		Map<Long, InReporteAPV> mapaBD = lista.stream().collect(Collectors.toMap(r -> r.IdReporteAPV, r -> r));
	    return mapaBD;
	}
	
	private void agregarLogs(Connection con, EntradaEliminarReporteAPV entrada, Map<Long, InReporteAPV> mapaValoresAntiguos) throws Exception {
		logger.debug("INICIO::EliminarReporteAPV.AgregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for (EntradaIdReporteAPV reporteAPV : entrada.p_ReporteAPV) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
            InReporteAPV reporteInfo = mapaValoresAntiguos.get(reporteAPV.IdReporteAPV);

			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = reporteAPV.IdReporteAPV;
			log.IdTipoLog = "ELIMINACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = reporteInfo.IdCliente;
			log.EstadoCliente = "";
			log.NombreCliente = reporteInfo.NombreCliente;
			log.IdTipoDocumentoIdentidad = reporteInfo.TipoDocumentoIdentidad.toString();
			log.IdDocumentoIdentidad = reporteInfo.IdDocumentoIdentidad.toString();
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "ELIMINACION_REPORTE_APV";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.IdRegistro = reporteAPV.IdReporteAPV.toString();
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "Eliminación reporte APV " + reporteAPV.IdReporteAPV;						
			entradaAgregarLog.p_Log.add(log);
		}
		
		if (!entradaAgregarLog.p_Log.isEmpty()) {
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarReporteAPV.AgregarLog {}", (fin - inicio));
	}

	private void eliminarReporte(Connection con, List<EntradaIdReporteAPV> p_ReporteAPV) throws Exception {
		logger.debug("INICIO:: EliminarReporteAPV.eliminarReporte");
		SQL sql = new SQL(); 
		sql.batch(con, SQL_DELETE_REPORTE_APV, p_ReporteAPV, EntradaIdReporteAPV.class);
		logger.debug("FIN:: EliminarReporteAPV.eliminarReporte");
	}

	public static void main(String[] args) throws Exception {
	try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
		try {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 76522,\r\n"
					+ "    \"p_IdModulo\": \"NOR\",\r\n"
					+ "    \"p_ReporteAPV\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdReporteAPV\": 841330,\r\n"
					+ "            \"DocumentoIdentidad\": \"10261676-6\",\r\n"
					+ "            \"NombreRegimenTributario\": \"APV depósitos convenidos\",\r\n"
					+ "            \"NombrePlanTipoInversion\": \"A. ABIERTO\"\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";

			EntradaEliminarReporteAPV objIn = gson.fromJson(json, EntradaEliminarReporteAPV.class);
			EliminarReporteAPV servicio = new EliminarReporteAPV();
			SalidaAdministrarBorrador objOut = servicio.logica(con, objIn);
			
			con.rollback();
			
			System.out.println(gson.toJson(objOut));
			
		} catch (Exception er) {
			er.printStackTrace();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	System.out.println("== END ==");
	}
}
