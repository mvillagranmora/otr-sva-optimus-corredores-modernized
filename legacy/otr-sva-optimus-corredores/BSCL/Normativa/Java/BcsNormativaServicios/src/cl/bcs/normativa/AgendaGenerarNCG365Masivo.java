package cl.bcs.normativa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;


import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores.FechasOut;
import cl.bcs.plataforma.pla.utils.Feriados;

import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AgendaGenerarNCG365Masivo implements JavaCall{
	private final Log log = new Log("NORMATIVA_GENERAR_NCG365_MASIVO_POR_AGENDA");
	static final Logger logger = LogManager.getLogger(AgendaGenerarNCG365Masivo.class);

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
	private final String SQL_OBTENER_FECHA_REAL = loadResourceToString("cl/bcs/normativa/sql/AgendaGenerarNCG365Masivo_ObtenerFechaActual.sql");
	
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
	
	
	public static class FechaActual{
		 
		public String Fecha;
		public String toString() {
 
			return Utils.printJson(this, false);
		}
 
 
	}
	
	
	public static class SalidaJobNCG365MasivoAgenda extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		

		public SalidaJobNCG365MasivoAgenda() {
			P_OUT_MENSAJES = new ArrayList<>();
			
			P_OUT_ERRORESCONTADOR = 0L;
		}
	};

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
	
	public SalidaJobNCG365MasivoAgenda transaction(Connection con, Map<String, Object> parameters) throws Throwable , Exception{
		log.write("INICIO:: AgendaGenerarNCG365Masivo");
		logger.info("INICIO:: AgendaGenerarNCG365Masivo");
		long inicio = System.currentTimeMillis();
		
		SalidaJobNCG365MasivoAgenda salida = new SalidaJobNCG365MasivoAgenda();
		SQL sql = new SQL();
		
		Boolean esDiaHabil = false;
		
		try {
			HashMap<String, Object> param = new HashMap<String, Object>();
			
			
			// OBTENEMOS FECHA DE SISTEMA Y FECHA REAL
			
			String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, (Long)parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			Long idPaisEmpresa = empresa.IdPais;
			
			List<FechaActual> fechaIg = sql.query(con,SQL_OBTENER_FECHA_REAL,param, FechaActual.class);
			
			String fechaReal = fechaIg.get(0).Fecha;
			
			// VALIDA SI EL DIA REAL ES HABIL
			
			esDiaHabil = Feriados.esDiaHabil(con, fechaReal , idPaisEmpresa);
			
			if(esDiaHabil) {
				
				// OBTENEMOS DIA HABIL ANTERIOR
				
				ObtenerDiasHabilesAnteriores fecha = new ObtenerDiasHabilesAnteriores ();
				
				InObtenerDiasHabilesAnteriores entradaDia = new InObtenerDiasHabilesAnteriores();
				entradaDia.idPais = new Long(44);
				entradaDia.diaHabil = "S";
				entradaDia.soloCaeDiaHabil="S";
				InObtenerDiasHabilesAnteriores.FechasIn f1 = new InObtenerDiasHabilesAnteriores.FechasIn();
				f1.fechaInicial = fechaSistema;
				f1.numeroDias = new Long(1);
				
				List<InObtenerDiasHabilesAnteriores.FechasIn> fechasIn = new ArrayList<InObtenerDiasHabilesAnteriores.FechasIn>();
				fechasIn.add(f1);
				entradaDia.fechas = fechasIn;
				
				OutObtenerDiasHabilesAnteriores salidaFecha = fecha.logica(con, entradaDia);
				
				FechasOut fechaNueva = salidaFecha.fechas.get(0);
				logger.info("esta es la fecha de sistema: " + fechaSistema);
				logger.info("esta es la fecha habil anterior: " + fechaNueva.fechaCalculada);
							
				
				// PROCEDIMIENTO
				
				if(fechaNueva.fechaCalculada != null) {
					HashMap<String, Object> entradaSP = obtenerEntradaCallSP(parameters, fechaNueva.fechaCalculada);//new HashMap<>();
					
					crearInsertTablasTemporales(con,parameters,fechaNueva.fechaCalculada);

					HashMap<String, Class> salidaSP = new HashMap<>(5);
					salidaSP.put("p_out_resultado", Long.class);
					salidaSP.put("p_out_AdvertenciasContador", Long.class);
					salidaSP.put("p_out_AdvertenciasDetalle", ObjetoAdvertencia.class);
					salidaSP.put("p_out_ErroresContador", Long.class);
					salidaSP.put("p_out_ErroresDetalle", ObjetoError.class);
					
					HashMap<String, Object> resultadoSP = sql.call(con, SQL_CALL_CREAR_SOLICITUD_CIRCULAR_ANTIGUO, entradaSP, salidaSP);
					
					
					salida.P_OUT_ERRORESCONTADOR = (Long)resultadoSP.get("p_out_ErroresContador");
					
					if( (Long)resultadoSP.get("p_out_ErroresContador") > 0L ) {
						for(ObjetoError error : (List<ObjetoError>) resultadoSP.get("p_out_ErroresDetalle") ) {
							log.write("Errores: " + error.Codigo);					
						}
					}
				}
				
				else {
					salida.P_OUT_ERRORESCONTADOR ++;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "FECHA_INVALIDA", new HashMap<>(), con, (Long)parameters.get("IdEmpresa")));
					
				}
			}
			
			else {
				
				salida.P_OUT_ERRORESCONTADOR ++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "FECHA_INVALIDA", new HashMap<>(), con, (Long)parameters.get("IdEmpresa")));
				
				return salida;
			}
			
			
		} catch (Exception e) {
    		log.write(e);
    		salida.P_OUT_ERRORESCONTADOR ++;
    		log.write("ERROR:: AgendaGenerarNCG365Masivo", e);
    		throw e;
    	} finally {
    		if(esDiaHabil && salida.P_OUT_ERRORESCONTADOR == 0) {
    			log.write("Se eliminan tablas temporales");
    			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_RUT);
    			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_RUT_AUT);
    			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_NEMO);
    			SQL.exec(con,SQL_DROP_TEMPORAL_LISTA_RUT_EMPL);
    			SQL.exec(con,SQL_DROP_TEMPORAL_PARAMETROS_ENTRADA_CALL);
    		}
    	}
		
		long fin = System.currentTimeMillis();
		log.write("FIN:: AgendaGenerarNCG365Masivo");
		logger.info("FIN:: AgendaGenerarNCG365Masivo {}", fin -inicio);
		return salida;
	}
	
	private void crearInsertTablasTemporales(Connection con, Map<String, Object> parameters, String fechaSistema) throws Exception {
		log.write("INICIO:: AgendaGenerarNCG365Masivo.crearInsertTablasTemporales");
		logger.info("INICIO:: AgendaGenerarNCG365Masivo.crearInsertTablasTemporales");
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
		log.write("FIN:: AgendaGenerarNCG365Masivo.crearInsertTablasTemporales");
		logger.info("INICIO:: AgendaGenerarNCG365Masivo.crearInsertTablasTemporales {}" , fin - inicio);
	}

	private HashMap<String, Object> obtenerEntradaCallSP(Map<String, Object> parameters, String fechaSistema) {
		log.write("INICIO:: AgendaGenerarNCG365Masivo.obtenerEntradaCallSP");
		logger.info("INICIO:: AgendaGenerarNCG365Masivo.obtenerEntradaCallSP");
		
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
		
		log.write("FIN:: AgendaGenerarNCG365Masivo.obtenerEntradaCallSP");
		logger.info("INICIO:: AgendaGenerarNCG365Masivo.obtenerEntradaCallSP");
		return entradaSP;
	}

	public static void main(String[] args){
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			AgendaGenerarNCG365Masivo sp = new AgendaGenerarNCG365Masivo();
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdEmpresa", 1L);
			params.put("IdUsuario", 0L);
			sp.transaction(con,params);
			con.rollback();
			//con.commit();
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
