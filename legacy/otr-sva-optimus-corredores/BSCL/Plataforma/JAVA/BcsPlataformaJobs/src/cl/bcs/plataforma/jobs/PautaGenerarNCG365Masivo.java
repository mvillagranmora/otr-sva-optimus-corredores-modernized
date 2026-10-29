package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PautaGenerarNCG365Masivo implements BcsProcesoPauta, JavaCall{
	private final Log log = new Log("NORMATIVA_GENERAR_NCG365_MASIVO_POR_PAUTA");
	static final Logger logger = LogManager.getLogger(PautaGenerarNCG365Masivo.class);

	private final String SQL_CALL_CREAR_SOLICITUD_CIRCULAR_ANTIGUO = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_CallCrearSolicitudCircularAntiguo.sql");
	private final String SQL_CREATE_TEMPORAL_LISTA_RUT = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_CrearTablaTemporalListaRut.sql");
	private final String SQL_CREATE_TEMPORAL_LISTA_RUT_AUT = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_CrearTablaTemporalListaRutAut.sql");
	private final String SQL_CREATE_TEMPORAL_LISTA_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_CrearTablaTemporalListaNemo.sql");
	private final String SQL_CREATE_TEMPORAL_LISTA_RUT_EMPL = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_CrearTablaTemporalListaRutEmpl.sql");
	private final String SQL_CREATE_TEMPORAL_PARAMETROS_ENTRADA_CALL = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_CrearTablaTemporalParametrosEntradaCall.sql");
	private final String SQL_INSERT_TEMPORAL_PARAMETROS_ENTRADA_CALL = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_InsertTablaTemporalParametrosEntradaCall.sql");
	private final String SQL_DROP_TEMPORAL_LISTA_RUT = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_DropTablaTemporalListaRut.sql");
	private final String SQL_DROP_TEMPORAL_LISTA_RUT_AUT = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_DropTablaTemporalListaRutAut.sql");
	private final String SQL_DROP_TEMPORAL_LISTA_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_DropTablaTemporalListaNemo.sql");
	private final String SQL_DROP_TEMPORAL_LISTA_RUT_EMPL = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_DropTablaTemporalListaRutEmpl.sql");
	private final String SQL_DROP_TEMPORAL_PARAMETROS_ENTRADA_CALL = loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaGenerarNCG365Masivo_DropTablaTemporalParametrosEntradaCall.sql");
	
	public static class ObjetoParametro extends BcsPojo {
		public String NombreParametro;
		public String Valor;
		
		public ObjetoParametro() {
			this.NombreParametro	= "";
			this.Valor				= "";
		}

		public ObjetoParametro(String nombreParametro, String valor) {
			this.NombreParametro	= nombreParametro;
			this.Valor				= valor;
		}
	}

	public static class ObjetoAdvertencia extends BcsPojo {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public ObjetoAdvertencia() {
			this.Codigo					= "";
			this.RequiereAutorizacion	= "N";
			this.Descripcion			= "";
		}
	}

	public static class ObjetoError extends BcsPojo {
		public String Codigo;
		public String Descripcion;
		
		public ObjetoError() {
			this.Codigo			= "";
			this.Descripcion	= "";
		}
	}
	
	public static class ObjetoRut extends BcsPojo{
		public String Rut;
		
		public ObjetoRut() {
			this.Rut	= "";
		}
	}
	
	public static class ObjetoRutAut extends BcsPojo{
		public String RutAut;
		
		public ObjetoRutAut() {
			this.RutAut		= "";
		}
	}
	
	public static class ObjetoNemo extends BcsPojo{
		public String Nemo;

		public ObjetoNemo() {
			this.Nemo	= "";
		}
	}
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable , Exception{
		log.write("INICIO:: PautaGenerarNCG365Masivo");
		logger.info("INICIO:: PautaGenerarNCG365Masivo");
		long inicio = System.currentTimeMillis();
		
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
		try {
			String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, (Long)parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");
			
			HashMap<String, Object> entradaSP = obtenerEntradaCallSP(parameters, fechaSistema);//new HashMap<>();
			
			crearInsertTablasTemporales(con,parameters,fechaSistema);
			
			SQL sql = new SQL();
			
			HashMap<String, Class> salidaSP = new HashMap<>(5);
			salidaSP.put("p_out_resultado", Long.class);
			salidaSP.put("p_out_AdvertenciasContador", Long.class);
			salidaSP.put("p_out_AdvertenciasDetalle", ObjetoAdvertencia.class);
			salidaSP.put("p_out_ErroresContador", Long.class);
			salidaSP.put("p_out_ErroresDetalle", ObjetoError.class);
			
			HashMap<String, Object> resultadoSP = sql.call(con, SQL_CALL_CREAR_SOLICITUD_CIRCULAR_ANTIGUO, entradaSP, salidaSP);
			
			salida.P_OUT_MENSAJE = "";
			salida.P_OUT_ERRORESCONTADOR = (Long)resultadoSP.get("p_out_ErroresContador");
			
			if( (Long)resultadoSP.get("p_out_ErroresContador") > 0L ) {
				for(ObjetoError error : (List<ObjetoError>) resultadoSP.get("p_out_ErroresDetalle") ) {
					log.write("Errores: " + error.Codigo);					
				}
			}
		} catch (Exception e) {
    		log.write(e);
    		log.write("ERROR:: PautaGenerarNCG365Masivo", e);
    		throw e;
    	} finally {
    		log.write("Se eliminan tablas temporales");
    		SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_RUT);
			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_RUT_AUT);
			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_NEMO);
			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_RUT_EMPL);
			SQL.exec(con,SQL_DROP_TEMPORAL_PARAMETROS_ENTRADA_CALL);
    	}
		
		long fin = System.currentTimeMillis();
		log.write("FIN:: PautaGenerarNCG365Masivo");
		logger.info("FIN:: PautaGenerarNCG365Masivo {}", fin -inicio);
		return salida;
	}
	
	private void crearInsertTablasTemporales(Connection con, Map<String, Object> parameters, String fechaSistema) throws Exception {
		log.write("INICIO:: PautaGenerarNCG365Masivo.crearInsertTablasTemporales");
		logger.info("INICIO:: PautaGenerarNCG365Masivo.crearInsertTablasTemporales");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		SQL.exec(con,SQL_CREATE_TEMPORAL_LISTA_RUT);
		SQL.exec(con,SQL_CREATE_TEMPORAL_LISTA_RUT_AUT);
		SQL.exec(con,SQL_CREATE_TEMPORAL_LISTA_NEMO);
		SQL.exec(con,SQL_CREATE_TEMPORAL_LISTA_RUT_EMPL);
		SQL.exec(con,SQL_CREATE_TEMPORAL_PARAMETROS_ENTRADA_CALL);
		
		List<ObjetoParametro> parametros = new ArrayList<ObjetoParametro>(11);
		parametros.add(new ObjetoParametro("idUsuario", parameters.get("IdUsuario").toString()));
		parametros.add(new ObjetoParametro("schema", "BSCL"));
		parametros.add(new ObjetoParametro("grabar", "false"));
		parametros.add(new ObjetoParametro("reemplazar", "false"));
		parametros.add(new ObjetoParametro("separadorListas", ";"));
		parametros.add(new ObjetoParametro("simboloDecimal", ","));
		parametros.add(new ObjetoParametro("simboloFecha", "dd/MM/yyyy"));
		parametros.add(new ObjetoParametro("aceptarFechasNull", "true"));
		parametros.add(new ObjetoParametro("EsPauta","S"));
		parametros.add(new ObjetoParametro("IdEmpresa", parameters.get("IdEmpresa").toString()));
		parametros.add(new ObjetoParametro("FTER",  fechaSistema));
		
		sql.batch(con,SQL_INSERT_TEMPORAL_PARAMETROS_ENTRADA_CALL,parametros, ObjetoParametro.class);
		
		long fin = System.currentTimeMillis();
		log.write("FIN:: PautaGenerarNCG365Masivo.crearInsertTablasTemporales");
		logger.info("INICIO:: PautaGenerarNCG365Masivo.crearInsertTablasTemporales {}" , fin - inicio);
	}

	private HashMap<String, Object> obtenerEntradaCallSP(Map<String, Object> parameters, String fechaSistema) {
		log.write("INICIO:: PautaGenerarNCG365Masivo.obtenerEntradaCallSP");
		logger.info("INICIO:: PautaGenerarNCG365Masivo.obtenerEntradaCallSP");
		
		HashMap<String, Object> entradaSP = new HashMap<>(10);
		
		entradaSP.put("p_IdEmpresa", (Long)parameters.get("IdEmpresa"));
		entradaSP.put("p_IdUsuario", (Long)parameters.get("IdUsuario"));
		entradaSP.put("p_IdModulo", "NOR");
		entradaSP.put("p_FechaIngresoSolicitud", fechaSistema);
		entradaSP.put("p_FechaInicio", fechaSistema);
		entradaSP.put("p_FechaTermino", fechaSistema);
		entradaSP.put("p_IdTipoDocumento", "NCG365MAS");
		entradaSP.put("p_IdEstadoDocumento", "PEN");
		entradaSP.put("p_CreaParam", "S");
		entradaSP.put("p_EsPauta", "S");
		
		log.write("FIN:: PautaGenerarNCG365Masivo.obtenerEntradaCallSP");
		logger.info("INICIO:: PautaGenerarNCG365Masivo.obtenerEntradaCallSP");
		return entradaSP;
	}

	public static void main(String[] args){
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
			PautaGenerarNCG365Masivo sp = new PautaGenerarNCG365Masivo();
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdEmpresa", 1L);
			params.put("IdUsuario", 0L);
			sp.transaction(con,params);
			con.commit();
		} catch (Throwable e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
