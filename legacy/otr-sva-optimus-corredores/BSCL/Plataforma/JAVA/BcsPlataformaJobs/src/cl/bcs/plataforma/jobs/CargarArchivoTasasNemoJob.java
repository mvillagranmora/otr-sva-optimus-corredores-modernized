package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
import cl.bcs.utils.StatsD;
import cl.bcs.utils.Utils;

public class CargarArchivoTasasNemoJob extends BcsJob implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CargarArchivoTasasNemoJob.class);
	private String SQL_CARGAR_ARCHIVO_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_CargarArchivoTasas.sql");
	private String SQL_CREATE_TABLA_TEMPORAL_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_CreateTablaTemporalTasas.sql");
	private String SQL_INSERT_TABLA_TEMPORAL_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_InsertTablaTemporalTasas.sql");
	private String SQL_DROP_TABLA_TEMPORAL_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_DropTablaTemporalTasas.sql");
	private String SQL_MODIFICAR_ESTADO_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_ModificarEstadoTasas.sql");
	private String SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_CreateTablaTemporalModificarTasas.sql");
	private String SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_InsertTablaTemporalModificarTasas.sql");
	private String SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_DropTablaTemporalModificarTasas.sql");
	private String SQL_SELECT_TASAS_ANTERIORES_NEMO = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarArchivoTasasNemoJob_SelectTasasAnteriores.sql");
	
	public static final String METRIC_PREFIX = "plataforma.tas.ws.cargar_archivo_tasas_nemo_job";
	public static final String METRIC_TIEMPO_EJECUCION = METRIC_PREFIX + "." + "tiempo_ejecucion";
	public static final String METRIC_VOLUMEN_DATOS = METRIC_PREFIX + "." + "volumen_datos";
	
	public static class ParametroCargaTasa extends BcsPojo{
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public String UsaReglasNegocio;
		public String SimularOperacion;
		public String Fuente;
		public String Fecha;
		public String Mercado;
		public String TipoRegistro;
		public String Oficializar;
		public String NombreArchivo;
		public List<Tasa> p_Tasas;
		
		public ParametroCargaTasa() {
			this.p_Tasas = new ArrayList<Tasa>();
		}
		
	}
	
	public static class Tasa extends BcsPojo{
		public String Nemo;
		public BigDecimal Tasa;
	}
	
	public static class SalidaCargarArchivoTasasNemoJob extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoProceso> P_RESULTADO_PROCESO;
		
		public SalidaCargarArchivoTasasNemoJob() {
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
		logger.info("INICIO::CargarArchivoTasasNemoJob.logica");
		long inicio = System.currentTimeMillis();
		
		SalidaCargarArchivoTasasNemoJob out = new SalidaCargarArchivoTasasNemoJob();
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
			List<ResultadoProceso> resultadoProceso = out.P_RESULTADO_PROCESO; 
			out = oficializar(con, parametroCargaTasa, resultadoProceso);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				throw new Exception(out.P_OUT_MENSAJES.get(0).codigo);
			}
			
			out = publicar(con, parametroCargaTasa, resultadoProceso);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				throw new Exception(out.P_OUT_MENSAJES.get(0).codigo);
			}
		}
		
		agregarLog(con, parametroCargaTasa);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemoJob.logica", fin - inicio);
		
		StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_EJECUCION, (fin - inicio), "fuente:" + parametroCargaTasa.Fuente, "mercado:" + parametroCargaTasa.Mercado, 
				"tipoRegistro:" + parametroCargaTasa.TipoRegistro, "fecha:" + parametroCargaTasa.Fecha, "oficializar:" + parametroCargaTasa.Oficializar);
		
		return true;
	}
	
	private ParametroCargaTasa obtenerParametrosCargaTasasDebug(Connection con) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.obtenerParametrosCargaTasasDebug");
		Gson gson = Utils.getGsonBuilder();	
		
		ParametroCargaTasa parametroCargaTasa = new ParametroCargaTasa();
		parametroCargaTasa.IdEmpresa = 1L;
		parametroCargaTasa.IdUsuario = 9L;
		parametroCargaTasa.IdModulo = "PLA";
		parametroCargaTasa.UsaReglasNegocio = "S";
		parametroCargaTasa.SimularOperacion = "N";
		parametroCargaTasa.Fuente = "TASASMERCADO";
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, parametroCargaTasa.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		parametroCargaTasa.Fecha = fechaSistema;
		parametroCargaTasa.Mercado = "RF";
		parametroCargaTasa.TipoRegistro = "TASA";
		parametroCargaTasa.Oficializar = "S";
		parametroCargaTasa.NombreArchivo = obtenerNombreArchivo(con, parametroCargaTasa.IdEmpresa);
		
		ParametroCargaTasa parametroTasas = gson.fromJson("{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Fuente\":\"FCLP - Curva ML_FINANCE CLP\",\"p_Mercado\":\"RF\",\"p_TipoRegistro\":\"TASA\",\"p_Oficializar\":\"N\",\"p_Tasas\":[{\"Nemo\":\"VENTA CORTA INTERMEDIACIÓN DE RENTA FIJA\",\"Tasa\":45}]}", ParametroCargaTasa.class);
		parametroCargaTasa.p_Tasas = parametroTasas.p_Tasas;
		logger.trace("FIN::CargarArchivoTasasNemoJob.obtenerParametrosCargaTasasDebug");
		return parametroCargaTasa;
	}
	
	private ParametroCargaTasa obtenerParametrosCargaTasas(Connection con) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.obtenerParametrosCargaTasas");
		Gson gson = Utils.getGsonBuilder();	
		
		ParametroCargaTasa parametroCargaTasa = new ParametroCargaTasa();
		parametroCargaTasa.IdEmpresa = Long.valueOf(this.getParam("IdEmpresa"));
		parametroCargaTasa.IdUsuario = Long.valueOf(this.getParam("IdUsuario"));
		parametroCargaTasa.IdModulo = this.getParam("IdModulo");
		parametroCargaTasa.UsaReglasNegocio = this.getParam("UsaReglasNegocio");
		parametroCargaTasa.SimularOperacion = this.getParam("SimularOperacion");
		parametroCargaTasa.Fuente = this.getParam("Fuente");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, parametroCargaTasa.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		parametroCargaTasa.Fecha = fechaSistema;
		parametroCargaTasa.Mercado = this.getParam("Mercado");
		parametroCargaTasa.TipoRegistro = this.getParam("TipoRegistro");
		parametroCargaTasa.Oficializar = this.getParam("Oficializar");
		parametroCargaTasa.NombreArchivo = obtenerNombreArchivo(con, parametroCargaTasa.IdEmpresa);
		
		ParametroCargaTasa parametroTasas = gson.fromJson(this.getParam("Tasas"), ParametroCargaTasa.class);
		parametroCargaTasa.p_Tasas = parametroTasas.p_Tasas;
		logger.trace("FIN::CargarArchivoTasasNemoJob.obtenerParametrosCargaTasas");
		return parametroCargaTasa;
	}
	
	private String obtenerNombreArchivo(Connection con, Long idEmpresa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.obtenerNombreArchivo");
		String nombreArchivo = "Carga_Web_Services_" + FechaDB.ObtenerFechaHoraSistemaLocal(con, idEmpresa);
		logger.trace("FIN::CargarArchivoTasasNemoJob.obtenerNombreArchivo");
		return nombreArchivo;
	}
	
	private SalidaCargarArchivoTasasNemoJob limpiarTasasAnteriores(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.limpiarTasasAnteriores");
		SalidaCargarArchivoTasasNemoJob out = new SalidaCargarArchivoTasasNemoJob();
		List<TasaAnterior> tasasAnteriores =  obtenerTasasAnteriores(con, parametroCargaTasa);
		if(tasasAnteriores.size() > 0) {
			out = eliminarTasasAnteriores(con, parametroCargaTasa, tasasAnteriores);
		}
		logger.trace("FIN::CargarArchivoTasasNemoJob.obtenerTasasAnteriores");
		return out; 
	}
	
	private List<TasaAnterior> obtenerTasasAnteriores(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.obtenerTasasAnteriores");
		SQL sql = new SQL();
		
		List<String> nemos = parametroCargaTasa.p_Tasas
				.stream()
				.map(tasa->tasa.Nemo)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(5);
		parametros.put("Fuente", parametroCargaTasa.Fuente);
		parametros.put("Mercado", parametroCargaTasa.Mercado);
		parametros.put("TipoRegistro", parametroCargaTasa.TipoRegistro);
		parametros.put("Nemo", nemos);
		parametros.put("Fecha", parametroCargaTasa.Fecha);
		
		List<TasaAnterior> tasasAnteriores = sql.query(con, SQL_SELECT_TASAS_ANTERIORES_NEMO, parametros, TasaAnterior.class);
		logger.trace("FIN::CargarArchivoTasasNemoJob.obtenerTasasAnteriores");
		return tasasAnteriores;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasNemoJob eliminarTasasAnteriores(Connection con, ParametroCargaTasa parametroCargaTasa, List<TasaAnterior> tasas) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.eliminarTasasAnteriores");
		SalidaCargarArchivoTasasNemoJob out = new SalidaCargarArchivoTasasNemoJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(6);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("Estado", "ELI");
			parametrosEntrada.put("Origen", "S");
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(5);
			parametrosSalida.put("p_out_RefId", Long.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaOficializar = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO, tasas, TasaAnterior.class);
			
			respuestaOficializar = sql.call(con, SQL_MODIFICAR_ESTADO_TASAS_NEMO, parametrosEntrada, parametrosSalida);
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
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO);
		}
		
		logger.trace("FIN::CargarArchivoTasasNemoJob.eliminarTasasAnteriores");
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasNemoJob cargarTasas(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.cargarTasas");
		SalidaCargarArchivoTasasNemoJob out = new SalidaCargarArchivoTasasNemoJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(10);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("NombreArchivo", parametroCargaTasa.NombreArchivo);
			parametrosEntrada.put("SimularOperacion", parametroCargaTasa.SimularOperacion);
			parametrosEntrada.put("Fuente", parametroCargaTasa.Fuente);
			parametrosEntrada.put("Fecha", parametroCargaTasa.Fecha);
			parametrosEntrada.put("Mercado", parametroCargaTasa.Mercado);
			parametrosEntrada.put("TipoRegistro", parametroCargaTasa.TipoRegistro);
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(5);
			parametrosSalida.put("p_out_ResultadoProceso", ResultadoProceso.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaCargarArchivoTasas = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_TASAS_NEMO);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_TASAS_NEMO, parametroCargaTasa.p_Tasas, Tasa.class);
			
			respuestaCargarArchivoTasas = sql.call(con, SQL_CARGAR_ARCHIVO_TASAS_NEMO, parametrosEntrada, parametrosSalida);
			out.P_OUT_ERRORESCONTADOR = (Long) respuestaCargarArchivoTasas.get("p_out_ErroresContador");
			
			List<ResultadoProceso> resultadosProceso = (List<ResultadoProceso>) respuestaCargarArchivoTasas.get("p_out_ResultadoProceso");
			long cantidadRegistros = resultadosProceso.stream().map(x -> x.IdPrecioTasa).count();
			
			StatsD.reportCount(METRIC_VOLUMEN_DATOS, cantidadRegistros, "fuente:" + parametroCargaTasa.Fuente, "mercado:" + parametroCargaTasa.Mercado, 
					"tipoRegistro:" + parametroCargaTasa.TipoRegistro, "fecha:" + parametroCargaTasa.Fecha, "oficializar:" + parametroCargaTasa.Oficializar);
			
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
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_TASAS_NEMO);
		}
		
		logger.trace("FIN::CargarArchivoTasasNemoJob.cargarTasas");
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasNemoJob oficializar(Connection con, ParametroCargaTasa parametroCargaTasa, List<ResultadoProceso> tasas) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.oficializar");
		SalidaCargarArchivoTasasNemoJob out = new SalidaCargarArchivoTasasNemoJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(5);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("Estado", "OFI");
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(5);
			parametrosSalida.put("p_out_RefId", Long.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaOficializar = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO, tasas, ResultadoProceso.class);
			
			respuestaOficializar = sql.call(con, SQL_MODIFICAR_ESTADO_TASAS_NEMO, parametrosEntrada, parametrosSalida);
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
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO);
		}
		
		logger.trace("FIN::CargarArchivoTasasNemoJob.oficializar");
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCargarArchivoTasasNemoJob publicar(Connection con, ParametroCargaTasa parametroCargaTasa, List<ResultadoProceso> tasas) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.publicar");
		SalidaCargarArchivoTasasNemoJob out = new SalidaCargarArchivoTasasNemoJob();
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(5);
			parametrosEntrada.put("IdEmpresa", parametroCargaTasa.IdEmpresa);
			parametrosEntrada.put("IdUsuario", parametroCargaTasa.IdUsuario);
			parametrosEntrada.put("IdModulo", parametroCargaTasa.IdModulo);
			parametrosEntrada.put("UsaReglasNegocio", parametroCargaTasa.UsaReglasNegocio);
			parametrosEntrada.put("Estado", "PUB");
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(5);
			parametrosSalida.put("p_out_RefId", Long.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaOficializar = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO, tasas, ResultadoProceso.class);
			
			respuestaOficializar = sql.call(con, SQL_MODIFICAR_ESTADO_TASAS_NEMO, parametrosEntrada, parametrosSalida);
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
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_MODIFICAR_TASAS_NEMO);
		}
		
		logger.trace("FIN::CargarArchivoTasasNemoJob.oficializar");
		return out;
	}
	
	private void agregarLog(Connection con, ParametroCargaTasa parametroCargaTasa) throws Throwable {
		logger.trace("INICIO::CargarArchivoTasasNemoJob.agregarLog");
		
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
		log.IdTipoRegistro = parametroCargaTasa.TipoRegistro;
		log.EstadoRegistro = "S".equals(parametroCargaTasa.Oficializar) ? "PUB" : "CAR";
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
		log.EstadoProceso = "S".equals(parametroCargaTasa.Oficializar) ? "PUB" : "CAR";
		log.NombreProceso = "Carga Tasas Nemo Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::CargarArchivoTasasNemoJob.agregarLog");
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
		Connection con = PlataformaUtils.getPropertiesConnection("qa","connections.properties");
		CargarArchivoTasasNemoJob a = new CargarArchivoTasasNemoJob();
		a.logica(con, true);
		con.rollback();
		//con.commit();
		
	}

}
