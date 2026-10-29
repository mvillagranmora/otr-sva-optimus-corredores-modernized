package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarArchivoTasasJob extends BcsJob implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CargarArchivoTasasJob.class);
	private String SQL_CARGAR_ARCHIVO_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_CargarArchivoTasas.sql");
	private String SQL_CREATE_TABLA_TEMPORAL_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_CreateTablaTemporalTasas.sql");
	private String SQL_INSERT_TABLA_TEMPORAL_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_InsertTablaTemporalTasas.sql");
	private String SQL_DROP_TABLA_TEMPORAL_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_DropTablaTemporalTasas.sql");
	private String SQL_MODIFICAR_ESTADO_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_ModificarEstadoTasas.sql");
	private String SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_CreateTablaTemporalModificarTasas.sql");
	private String SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_InsertTablaTemporalModificarTasas.sql");
	private String SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_DropTablaTemporalModificarTasas.sql");
	private String SQL_SELECT_TASAS_ANTERIORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasJob_SelectTasasAnteriores.sql");
	
	public static class ParametroCargaTasa extends BcsPojo{
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public String UsaReglasNegocio;
		public String SimularOperacion;
		public String Fuente;
		public String Fecha;
		public String Oficializar;
		public String NombreArchivo;
		public List<Tasa> p_Tasas;
		
		public ParametroCargaTasa() {
			this.p_Tasas = new ArrayList<Tasa>();
		}
		
	}
	
	public static class Tasa extends BcsPojo{
		public String IdMoneda;
		public Long Periodo;
		public BigDecimal Tasa;
		public BigDecimal TasaEstimada;
	}
	
	public static class SalidaCargarArchivoTasasJob extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoProceso> P_RESULTADO_PROCESO;
		
		public SalidaCargarArchivoTasasJob() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_RESULTADO_PROCESO = new ArrayList<ResultadoProceso>();
		}
	}
	
	public static class TasaAnterior extends BcsPojo{
		public Long IdPrecioTasa;
	}
	
	public static class ResultadoProceso extends BcsPojo{
		public Long IdPrecioTasa;
		public String Campos;
		public String Valores;
	}
	
	public static class AdvertenciasDetalle extends BcsPojo {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	public static class ErrorDetalle extends BcsPojo {
		public String Codigo;
		public String Descripcion;
	}
		
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::CargarArchivoTasasJob");
		SalidaCargarArchivoTasasJob out = new SalidaCargarArchivoTasasJob();
		ParametroCargaTasa parametroCargaTasa = debug ? obtenerParametrosCargaTasasDebug(con) : obtenerParametrosCargaTasas(con);
		
		out = limpiarTasasAnteriores(con, parametroCargaTasa);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			throw new Exception(out.P_OUT_MENSAJES.get(0).codigo);
		}
		
		out = cargarTasas(con, parametroCargaTasa);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			throw new Exception(out.P_OUT_MENSAJES.get(0).codigo);
		}
		
		if("S".equals(parametroCargaTasa.Oficializar)) {
			out = oficializar(con, parametroCargaTasa, out.P_RESULTADO_PROCESO);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				throw new Exception(out.P_OUT_MENSAJES.get(0).codigo);
			}
		}
		
		agregarLog(con, parametroCargaTasa);
		
		logger.info("FIN::CargarArchivoTasasJob");
		return true;
	}
	
	private ParametroCargaTasa obtenerParametrosCargaTasasDebug(Connection con) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.obtenerParametrosCargaTasasDebug");
		Gson gson = Utils.getGsonBuilder();	
		
		ParametroCargaTasa parametroCargaTasa = new ParametroCargaTasa();
		parametroCargaTasa.IdEmpresa = 1L;
		parametroCargaTasa.IdUsuario = 9L;
		parametroCargaTasa.IdModulo = "PLA";
		parametroCargaTasa.UsaReglasNegocio = "S";
		parametroCargaTasa.SimularOperacion = "N";
		parametroCargaTasa.Fuente = "ML_FINANCE";
		parametroCargaTasa.Fecha = "2022-11-18";
		parametroCargaTasa.Oficializar = "S";
		parametroCargaTasa.NombreArchivo = obtenerNombreArchivo(con, parametroCargaTasa.IdEmpresa);
		
		ParametroCargaTasa parametroTasas = gson.fromJson("{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Fuente\":\"ML_FINANCE\",\"p_Fecha\":\"2022-11-20\",\"p_Oficializar\":\"S\",\"p_Tasas\":[{\"IdMoneda\":\"USD\",\"Periodo\":7,\"Tasa\":35,\"TasaEstimada\":31}]}", ParametroCargaTasa.class);
		parametroCargaTasa.p_Tasas = parametroTasas.p_Tasas;
		logger.trace("FIN::CargarArchivoTasasJob.obtenerParametrosCargaTasasDebug");
		return parametroCargaTasa;
	}
	
	private ParametroCargaTasa obtenerParametrosCargaTasas(Connection con) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.obtenerParametrosCargaTasas");
		Gson gson = Utils.getGsonBuilder();	
		
		ParametroCargaTasa parametroCargaTasa = new ParametroCargaTasa();
		parametroCargaTasa.IdEmpresa = Long.valueOf(this.getParam("IdEmpresa"));
		parametroCargaTasa.IdUsuario = Long.valueOf(this.getParam("IdUsuario"));
		parametroCargaTasa.IdModulo = this.getParam("IdModulo");
		parametroCargaTasa.UsaReglasNegocio = this.getParam("UsaReglasNegocio");
		parametroCargaTasa.SimularOperacion = this.getParam("SimularOperacion");
		parametroCargaTasa.Fuente = this.getParam("Fuente");
		parametroCargaTasa.Fecha = this.getParam("Fecha");
		parametroCargaTasa.Oficializar = this.getParam("Oficializar");
		parametroCargaTasa.NombreArchivo = obtenerNombreArchivo(con, parametroCargaTasa.IdEmpresa);
		
		ParametroCargaTasa parametroTasas = gson.fromJson(this.getParam("Tasas"), ParametroCargaTasa.class);
		parametroCargaTasa.p_Tasas = parametroTasas.p_Tasas;
		logger.trace("FIN::CargarArchivoTasasJob.obtenerParametrosCargaTasas");
		return parametroCargaTasa;
	}
	
	private String obtenerNombreArchivo(Connection con, Long idEmpresa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.obtenerNombreArchivo");
		String nombreArchivo = "Carga_Web_Services_" + FechaDB.ObtenerFechaHoraSistemaLocal(con, idEmpresa);
		logger.trace("FIN::CargarArchivoTasasJob.obtenerNombreArchivo");
		return nombreArchivo;
	}
	
	private SalidaCargarArchivoTasasJob limpiarTasasAnteriores(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.limpiarTasasAnteriores");
		SalidaCargarArchivoTasasJob out = new SalidaCargarArchivoTasasJob();
		List<TasaAnterior> tasasAnteriores =  obtenerTasasAnteriores(con, parametroCargaTasa);
		if(tasasAnteriores.size() > 0) {
			out = eliminarTasasAnteriores(con, parametroCargaTasa, tasasAnteriores);
		}
		logger.trace("FIN::CargarArchivoTasasJob.obtenerTasasAnteriores");
		return out; 
	}
	
	private List<TasaAnterior> obtenerTasasAnteriores(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.obtenerTasasAnteriores");
		SQL sql = new SQL();
		
		List<String> idsMoneda = parametroCargaTasa.p_Tasas
				.stream()
				.map(tasa->tasa.IdMoneda)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("Fuente", parametroCargaTasa.Fuente);
		parametros.put("Fecha", parametroCargaTasa.Fecha);
		parametros.put("IdMoneda", idsMoneda);
		List<TasaAnterior> tasasAnteriores = sql.query(con, SQL_SELECT_TASAS_ANTERIORES, parametros, TasaAnterior.class);
		logger.trace("FIN::CargarArchivoTasasJob.obtenerTasasAnteriores");
		return tasasAnteriores;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasJob eliminarTasasAnteriores(Connection con, ParametroCargaTasa parametroCargaTasa, List<TasaAnterior> tasas) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.eliminarTasasAnteriores");
		SalidaCargarArchivoTasasJob out = new SalidaCargarArchivoTasasJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(9);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("Estado", "ELI");
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(6);
			parametrosSalida.put("p_out_RefId", Long.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaOficializar = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS, tasas, TasaAnterior.class);
			
			respuestaOficializar = sql.call(con, SQL_MODIFICAR_ESTADO_TASAS, parametrosEntrada, parametrosSalida);
			out.P_OUT_ERRORESCONTADOR = (Long) respuestaOficializar.get("p_out_ErroresContador");
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaOficializar.get("p_out_ErroresDetalle"); 
				out.P_OUT_MENSAJES = erroresDetalle
						.stream()
						.map(errorDetalle -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, errorDetalle.Codigo, errorDetalle.Descripcion))
						.collect(Collectors.toList());
				return out;
			}
			
		} catch (Exception e) {
			throw e;
		} finally {
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS);
		}
		
		logger.trace("FIN::CargarArchivoTasasJob.eliminarTasasAnteriores");
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasJob cargarTasas(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.cargarTasas");
		SalidaCargarArchivoTasasJob out = new SalidaCargarArchivoTasasJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(9);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("NombreArchivo", parametroCargaTasa.NombreArchivo);
			parametrosEntrada.put("SimularOperacion", parametroCargaTasa.SimularOperacion);
			parametrosEntrada.put("Fecha", parametroCargaTasa.Fecha);
			parametrosEntrada.put("Fuente", parametroCargaTasa.Fuente);
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(6);
			parametrosSalida.put("p_out_ResultadoProceso", ResultadoProceso.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaCargarArchivoTasas = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_TASAS);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_TASAS, parametroCargaTasa.p_Tasas, Tasa.class);
			
			respuestaCargarArchivoTasas = sql.call(con, SQL_CARGAR_ARCHIVO_TASAS, parametrosEntrada, parametrosSalida);
			out.P_OUT_ERRORESCONTADOR = (Long) respuestaCargarArchivoTasas.get("p_out_ErroresContador");
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaCargarArchivoTasas.get("p_out_ErroresDetalle"); 
				out.P_OUT_MENSAJES = erroresDetalle
						.stream()
						.map(errorDetalle -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, errorDetalle.Codigo, errorDetalle.Descripcion))
						.collect(Collectors.toList());
				return out;
			}
			
			out.P_RESULTADO_PROCESO = (List<ResultadoProceso>) respuestaCargarArchivoTasas.get("p_out_ResultadoProceso");
			
		} catch (Exception e) {
			throw e;
		} finally {
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_TASAS);
		}
		
		logger.trace("FIN::CargarArchivoTasasJob.cargarTasas");
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasJob oficializar(Connection con, ParametroCargaTasa parametroCargaTasa, List<ResultadoProceso> tasas) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.oficializar");
		SalidaCargarArchivoTasasJob out = new SalidaCargarArchivoTasasJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(9);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("Estado", "OFI");
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(6);
			parametrosSalida.put("p_out_RefId", Long.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaOficializar = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS, tasas, ResultadoProceso.class);
			
			respuestaOficializar = sql.call(con, SQL_MODIFICAR_ESTADO_TASAS, parametrosEntrada, parametrosSalida);
			out.P_OUT_ERRORESCONTADOR = (Long) respuestaOficializar.get("p_out_ErroresContador");
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaOficializar.get("p_out_ErroresDetalle"); 
				out.P_OUT_MENSAJES = erroresDetalle
						.stream()
						.map(errorDetalle -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, errorDetalle.Codigo, errorDetalle.Descripcion))
						.collect(Collectors.toList());
				return out;
			}
			
		} catch (Exception e) {
			throw e;
		} finally {
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS);
		}
		
		logger.trace("FIN::CargarArchivoTasasJob.oficializar");
		return out;
	}
	
	private void agregarLog(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasJob.agregarLog");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, parametroCargaTasa.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "INGRESO";
		log.IdModulo = parametroCargaTasa.IdModulo;
		log.IdUsuario = parametroCargaTasa.IdUsuario;
		log.IdEmpresa = parametroCargaTasa.IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = parametroCargaTasa.IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "TASA";
		log.EstadoRegistro = "S".equals(parametroCargaTasa.Oficializar) ? "OFI" : "CAR";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = new BigDecimal(parametroCargaTasa.p_Tasas.size());
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "S".equals(parametroCargaTasa.Oficializar) ? "OFI" : "CAR";
		log.NombreProceso = "Carga Tasas Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::CargarArchivoTasasJob.agregarLog");
	}
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String [] args) throws InterruptedException, Throwable {
		Connection c = PlataformaUtils.getDESAConnection();
		CargarArchivoTasasJob a = new CargarArchivoTasasJob();
		a.logica(c, true);
		c.commit();
		
	}

}
