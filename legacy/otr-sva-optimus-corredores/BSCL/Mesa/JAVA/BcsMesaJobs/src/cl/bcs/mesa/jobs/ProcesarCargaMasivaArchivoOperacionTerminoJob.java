package cl.bcs.mesa.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.mesa.MRF.CrearOperacionMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_Operacion_IN;
import cl.bcs.mesa.MRF.ProcesarCargaMasivaArchivoOperacionTermino.EntradaProcesarCargaMasivaArchivoOperacionTermino;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaMasivaArchivoOperacionTerminoJob extends BcsJob implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ProcesarCargaMasivaArchivoOperacionTerminoJob.class);
	private final String SQL_SELECT_OPERACION_CABECERA = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_SelectOperacionCabecera.sql");
	private final String SQL_SELECT_OPERACION_DETALLE = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_SelectOperacionDetalle.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_SelectCarteras.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_SelectInstrumentos.sql");
	private final String SQL_SELECT_CONDICION_LIQUIDACION = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_SelectCondicionLiquidacion.sql");
	private final String SQL_UPDATE_OPERACION_CABECERA = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_UpdateCabecera.sql");
	private final String SQL_UPDATE_OPERACION_DETALLE = loadResourceToString("cl/bcs/mesa/jobs/sql/ProcesarCargaMasivaArchivoOperacionTerminoJob_UpdateDetalle.sql");
	
	public static class ParametroProcesarCargaOperacionTermino extends BcsPojo {
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public String UsaReglasNegocio;
		public String SimularOperacion;
		public List<OperacionTermino> Operaciones;
	}
	
	public static class OperacionTermino extends BcsPojo {
		public Long IdCargaMasivaDetalle;
	}
	
	public static class OperacionTerminoCabecera extends BcsPojo {
		public Long IdCargaMasivaDetalle;
		public String FolioAgrupadorCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public String TipoOperacion;
		public String TipoOperacionReal;
		public String TipoReajuste;
		public String MonedaLiquidacion;
		public BigDecimal ValorInicial;
		public String CondicionLiquidacion;
		public String IdCondicionLiquidacion;
		public Long IdMesa;
		public String AfectaCustodia;
		public Long IdClienteMesa;
		public Long IdPortafolioMesa;
		public BigDecimal Monto;
		public Long IdPersonaEjecutivo;
		public Long PlazoEnDiasCondicionLiquidacion;
		public String FechaLiquidacion;
		public Long IdOperacion;
		public List<OperacionTerminoDetalle> Detalle;
		
		public OperacionTerminoCabecera() {
			this.Detalle = new ArrayList<OperacionTerminoDetalle>();
		}
	}
	
	public static class OperacionTerminoDetalle extends BcsPojo {
		public Long IdCargaMasivaDetalle;
		public Long IdCargaMasivaDetalleCabecera;
		public String TipoOperacion;
		public String CondicionLiquidacion;
		public String TipoReajuste;
		public BigDecimal ValorInicial;
		public String Mercado;
		public Long SecuenciaCartera;
		public String Nemotecnico;
		public String Emisor;
		public String Boveda;
		public String ReajusteCartera;
		public String MonedaLiquidacion;
		public String FechaVencimiento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String Fungible;
		public BigDecimal Monto;
		public Long IdPortafolio;
		public BigDecimal Tasa;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String FechaEmisionInstrumento;
		public String IdMonedaInstrumento;
		public String IdTipoReajuste;
		public BigDecimal TasaCompra;
		public String IdMonedaEmision;
		public String IdClasificacionTipoInstrumento;
		public String IdBolsa;
		public Long IdCartera;
		public BigDecimal CorteMinimo;
		public BigDecimal MontoValorizado;
		public BigDecimal MontoLiquidacionValorizado;
		public BigDecimal MontoLiquidacionOperacionValorizado;
		public BigDecimal PrecioCompraValorizado;
		public BigDecimal TasaCompraValorizado;
		public BigDecimal PrecioMercadoValorizado;
		public BigDecimal TasaMercadoValorizado;
		
		public OperacionTerminoDetalle() {
			this.IdCartera = 0L;
			this.IdBolsa = "";
			this.MontoValorizado = BigDecimal.ZERO;
			this.MontoLiquidacionValorizado = BigDecimal.ZERO;
			this.MontoLiquidacionOperacionValorizado = BigDecimal.ZERO;
			this.PrecioCompraValorizado = BigDecimal.ZERO;
			this.TasaCompraValorizado = BigDecimal.ZERO;
			this.PrecioMercadoValorizado = BigDecimal.ZERO;
			this.TasaMercadoValorizado = BigDecimal.ZERO;
		}
	}
	
	public static class CondicionLiquidacion extends BcsPojo {
		public String IdCondicionLiquidacion;
		public String CodigoCondicionLiquidacion;
		public String IdTipoOperacion;
		public String SoloDiasHabiles;
		public Integer PlazoEnDias;
	}
	
	public static class Cartera extends BcsPojo {
		public Long IdCartera;
		public Long FolioCartera;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public java.sql.Date FechaVencimientoInstrumento;
		public java.sql.Date FechaEmisionInstrumento;
		public String IdMonedaInstrumento;
		public String IdTipoReajuste;
		public BigDecimal TasaCompra;
		public String Nemotecnico;
		public String IdMonedaEmision;
		public String IdEmisor;
		public String IdBolsa;
		public String IdBoveda;
		public BigDecimal CorteMinimo;
	}
	
	public static class Instrumento extends BcsPojo {
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public java.sql.Date FechaVencimiento;
		public java.sql.Date FechaEmision;
		public String IdMoneda;
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		public String IdEmisor;
		public BigDecimal CorteMinimo;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob");
		
		ParametroProcesarCargaOperacionTermino parametroProcesarCargaOperacionTermino = debug ? obtenerParametrosDebug() : obtenerParametros();
		List<OperacionTerminoCabecera> operaciones = obtenerOperaciones(con, parametroProcesarCargaOperacionTermino.Operaciones, parametroProcesarCargaOperacionTermino);
		List<MensajeSalida> salidaCrearOperacion = crearOperacion(con, parametroProcesarCargaOperacionTermino, operaciones);
		Long errores = MensajeSalida.getNumeroErrores(salidaCrearOperacion);
		if(errores > 0) {
			String mensajeSalida = salidaCrearOperacion
					.stream()
					.filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo))
					.map(mensaje->mensaje.descripcion)
					.collect(Collectors.joining(", "));
			throw new Exception(mensajeSalida);
		}
		actualizarCabecera(con, operaciones);
		actualizarDetalle(con, operaciones);
		
		logger.info("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob");
		return true;
	}
	
	private ParametroProcesarCargaOperacionTermino obtenerParametrosDebug() {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerParametrosDebug");
		Gson gson = Utils.getGsonBuilder();	
		
		ParametroProcesarCargaOperacionTermino parametroProcesarCargaOperacionTermino = new ParametroProcesarCargaOperacionTermino();
		parametroProcesarCargaOperacionTermino.IdEmpresa = 1L;
		parametroProcesarCargaOperacionTermino.IdUsuario = 98L;
		parametroProcesarCargaOperacionTermino.IdModulo = "MRF";
		parametroProcesarCargaOperacionTermino.UsaReglasNegocio = "S";
		parametroProcesarCargaOperacionTermino.SimularOperacion = "N";
		
		EntradaProcesarCargaMasivaArchivoOperacionTermino entradaProcesarCargaMasivaArchivoOperacionTermino = gson.fromJson("{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Operaciones\":[{\"IdCargaMasivaDetalle\":89}]}", EntradaProcesarCargaMasivaArchivoOperacionTermino.class);
		parametroProcesarCargaOperacionTermino.Operaciones = entradaProcesarCargaMasivaArchivoOperacionTermino.p_Operaciones
				.stream()
				.map(operacion->{
					OperacionTermino operacionTermino = new OperacionTermino();
					operacionTermino.IdCargaMasivaDetalle = operacion.IdCargaMasivaDetalle;
					return operacionTermino;
				})
				.collect(Collectors.toList());
		
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerParametrosDebug");
		return parametroProcesarCargaOperacionTermino;
	}
	
	private ParametroProcesarCargaOperacionTermino obtenerParametros() {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerParametros");
		Gson gson = Utils.getGsonBuilder();	
		
		ParametroProcesarCargaOperacionTermino parametroProcesarCargaOperacionTermino = new ParametroProcesarCargaOperacionTermino();
		parametroProcesarCargaOperacionTermino.IdEmpresa = Long.valueOf(this.getParam("IdEmpresa"));
		parametroProcesarCargaOperacionTermino.IdUsuario = Long.valueOf(this.getParam("IdUsuario"));
		parametroProcesarCargaOperacionTermino.IdModulo = this.getParam("IdModulo");
		parametroProcesarCargaOperacionTermino.UsaReglasNegocio = this.getParam("UsaReglasNegocio");
		parametroProcesarCargaOperacionTermino.SimularOperacion = this.getParam("SimularOperacion");
		
		EntradaProcesarCargaMasivaArchivoOperacionTermino entradaProcesarCargaMasivaArchivoOperacionTermino = gson.fromJson(this.getParam("Operaciones"), EntradaProcesarCargaMasivaArchivoOperacionTermino.class);
		parametroProcesarCargaOperacionTermino.Operaciones = entradaProcesarCargaMasivaArchivoOperacionTermino.p_Operaciones
				.stream()
				.map(operacion->{
					OperacionTermino operacionTermino = new OperacionTermino();
					operacionTermino.IdCargaMasivaDetalle = operacion.IdCargaMasivaDetalle;
					return operacionTermino;
				})
				.collect(Collectors.toList());
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerParametros");
		return parametroProcesarCargaOperacionTermino;
	}
	
	private List<OperacionTerminoCabecera> obtenerOperaciones(Connection con, List<OperacionTermino> operaciones, ParametroProcesarCargaOperacionTermino parametrosCarga) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerOperaciones");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, parametrosCarga.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTerminoCabecera> operacionesCabecera = sql.query(con, SQL_SELECT_OPERACION_CABECERA, parametros, OperacionTerminoCabecera.class);
		List<OperacionTerminoDetalle> operacionesDetalle = obtenerOperacionesDetalle(con, operaciones);
		
		Map<Long,List<OperacionTerminoDetalle>> operacionesDetallePorIdCabecera = operacionesDetalle.stream().collect(Collectors.groupingBy(operacion->operacion.IdCargaMasivaDetalleCabecera));
		
		Map<Long, CalculoSalida> paridadPorIdCabecera = obtenerParidadPorIdCabecera(con, operacionesCabecera, parametrosCarga.IdEmpresa, fechaSistema);
		Map<String, CondicionLiquidacion> condicionesLiquidacionPorCodigoYTipoOperacion = obtenerCondicionesDeLiquidacion(con);
		
		operacionesCabecera.stream().forEach(operacion->{
			CondicionLiquidacion condicionLiquidacion = condicionesLiquidacionPorCodigoYTipoOperacion.get(operacion.CondicionLiquidacion + "_" + operacion.TipoOperacionReal);
			operacion.Detalle = operacionesDetallePorIdCabecera.get(operacion.IdCargaMasivaDetalle);
			operacion.Monto = paridadPorIdCabecera.get(operacion.IdCargaMasivaDetalle).MontoDestino;
			operacion.AfectaCustodia = operacion.Detalle.stream().filter(detalle->detalle.SecuenciaCartera > 0).count() > 0 ? "S" : "N";
			operacion.PlazoEnDiasCondicionLiquidacion = Long.valueOf(condicionLiquidacion.PlazoEnDias);
			operacion.IdCondicionLiquidacion = condicionLiquidacion.IdCondicionLiquidacion;
		});
		
		HashMap<String, ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> hashFechasSalida = obtenerFechaLiquidacion(con, operacionesCabecera, parametrosCarga.IdEmpresa, fechaSistema);
		
		operacionesCabecera.stream().forEach(operacion->{
			operacion.FechaLiquidacion = hashFechasSalida.get(fechaSistema + "_" + operacion.PlazoEnDiasCondicionLiquidacion).fechaCalculada;
		});
		
		operacionesCabecera = valorizar(con, operacionesCabecera, parametrosCarga, fechaSistema);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerOperaciones");
		return operacionesCabecera;
	}
	
	private List<OperacionTerminoDetalle> obtenerOperacionesDetalle(Connection con, List<OperacionTermino> operaciones) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerOperacionesDetalle");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTerminoDetalle> operacionesDetalle = sql.query(con, SQL_SELECT_OPERACION_DETALLE, parametros, OperacionTerminoDetalle.class);
		Map<Long, Cartera> carterasAgrupadasPorFolio = obtenerCarterasAgrupadasPorFolio(con, operacionesDetalle);
		Map<String, Instrumento> obtenerInstrumentosAgrupadosPorNemo = obtenerInstrumentosAgrupadosPorNemo(con, operacionesDetalle);
		
		operacionesDetalle.stream().forEach(detalle->{
			if(detalle.SecuenciaCartera > 0) {
				Cartera cartera = carterasAgrupadasPorFolio.get(detalle.SecuenciaCartera);
				detalle.IdTipoInstrumento = cartera.IdTipoInstrumento;
				detalle.IdInstrumento = cartera.IdInstrumento;
				detalle.FechaVencimiento = Fecha.toStringDate(cartera.FechaVencimientoInstrumento);
				detalle.FechaEmisionInstrumento = Fecha.toStringDate(cartera.FechaEmisionInstrumento);
				detalle.IdMonedaInstrumento = cartera.IdMonedaInstrumento;
				detalle.IdTipoReajuste = cartera.IdTipoReajuste;
				detalle.TasaCompra = cartera.TasaCompra;
				detalle.Nemotecnico = cartera.Nemotecnico;
				detalle.IdMonedaEmision = cartera.IdMonedaEmision;
				detalle.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
				detalle.Emisor = cartera.IdEmisor;
				detalle.IdCartera = cartera.IdCartera;
				detalle.IdBolsa = cartera.IdBolsa;
				detalle.Boveda = cartera.IdBoveda;
				detalle.CorteMinimo = cartera.CorteMinimo;
			} else {
				Instrumento instrumento = obtenerInstrumentosAgrupadosPorNemo.get(detalle.Nemotecnico);
				detalle.IdTipoInstrumento = instrumento.IdTipoInstrumento;
				detalle.IdInstrumento = instrumento.IdInstrumento;
				detalle.FechaEmisionInstrumento = Fecha.toStringDate(instrumento.FechaEmision);
				detalle.IdMonedaInstrumento = instrumento.IdMoneda;
				detalle.IdTipoReajuste = instrumento.IdTipoReajuste;
				detalle.IdMonedaEmision = instrumento.IdMonedaEmision;
				detalle.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
				detalle.CorteMinimo = instrumento.CorteMinimo;
				if("RF".equals(detalle.Mercado)) {
					detalle.FechaVencimiento = Fecha.toStringDate(instrumento.FechaVencimiento);
					detalle.Emisor = instrumento.IdEmisor;
				}
			}
		});
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerOperacionesDetalle");
		return operacionesDetalle;
	}
	
	private Map<Long, Cartera> obtenerCarterasAgrupadasPorFolio(Connection con, List<OperacionTerminoDetalle> detalles) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerCarterasAgrupadasPorFolio");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		List<Long> secuenciasCartera = detalles
				.stream()
				.filter(detalle->detalle.SecuenciaCartera > 0)
				.map(detalle->detalle.SecuenciaCartera)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("SecuenciaCartera", secuenciasCartera);
		
		SQL sql = new SQL();
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, parametros, Cartera.class);
		Map<Long, Cartera> carterasAgrupadasPorFolio = carteras.stream().collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerCarterasAgrupadasPorFolio");
		return carterasAgrupadasPorFolio;
	}
	
	private Map<String, Instrumento> obtenerInstrumentosAgrupadosPorNemo(Connection con, List<OperacionTerminoDetalle> detalles) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerInstrumentosAgrupadosPorNemo");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		List<String> nemotecnicos = detalles
				.stream()
				.filter(detalle->detalle.SecuenciaCartera == 0)
				.map(detalle->detalle.Nemotecnico)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("Nemotecnico", nemotecnicos);
		
		SQL sql = new SQL();
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, parametros, Instrumento.class);
		Map<String, Instrumento> instrumentosAgrupadosPorNemo = instrumentos.stream().collect(Collectors.toMap(nemotecnico->nemotecnico.Nemotecnico, nemotecnico->nemotecnico));
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerInstrumentosAgrupadosPorNemo");
		return instrumentosAgrupadosPorNemo;
	}
	
	private Map<Long, CalculoSalida> obtenerParidadPorIdCabecera(Connection con, List<OperacionTerminoCabecera> operacionesCabecera, Long idEmpresa, String fechaSistema) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerParidadPorIdCabecera");
		
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		CalcularParidad calcularParidad = new CalcularParidad();
		
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 0L;
		
		calcularParidadEntrada.p_CalculosEntrada = operacionesCabecera
				.stream()
				.map(operacion->{
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaSistema;
					calculoEntrada.IdInterno = operacion.IdCargaMasivaDetalle;
					calculoEntrada.IdMonedaOrigen = operacion.TipoReajuste;
					calculoEntrada.IdMonedaDestino = idMonedaSistema;
					calculoEntrada.IdTrabajo = 0L;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = operacion.ValorInicial;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					return calculoEntrada;
				}).collect(Collectors.toList());
		
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
		Map<Long, CalculoSalida> paridadPorIdCabecera = calcularParidadSalida.p_out_CalculosSalida
				.stream()
				.collect(Collectors.toMap(calculo->calculo.IdInterno, calculo->calculo));
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerParidadPorIdCabecera");
		return paridadPorIdCabecera;
	}
	
	private HashMap<String, ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> obtenerFechaLiquidacion(Connection con, List<OperacionTerminoCabecera> operacionesCabecera, Long idEmpresa, String fechaSistema) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerFechaLiquidacion");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_diaHabil = "S";
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
		inObtenerDiaSiguiente.p_fechas = operacionesCabecera
				.stream()
				.map(operacion->{
					ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
					fecha.fechaInicial = fechaSistema;
					fecha.numeroDias = operacion.PlazoEnDiasCondicionLiquidacion;
					fecha.pais = paisSistema;
					return fecha;
				})
				.collect(Collectors.toList());
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		HashMap<String, ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> hashFechasSalida = new HashMap<String, ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut>();
		
		for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
			hashFechasSalida.put(fechaSalida.fechaInicial + "_" + fechaSalida.numeroDiasInicial, fechaSalida);
		}
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerFechaLiquidacion");
		return hashFechasSalida;
	}
	
	private Map<String, CondicionLiquidacion> obtenerCondicionesDeLiquidacion(Connection con) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerCondicionesDeLiquidacion");
		
		SQL sql = new SQL();
		List<CondicionLiquidacion> condicionesLiquidacion = sql.query(con, SQL_SELECT_CONDICION_LIQUIDACION, new HashMap<String, Object>(1), CondicionLiquidacion.class);
		Map<String, CondicionLiquidacion> condicionesLiquidacionPorCodigoYTipoOperacion = condicionesLiquidacion
				.stream()
				.collect(Collectors.toMap(condicion->condicion.CodigoCondicionLiquidacion + "_" + condicion.IdTipoOperacion, condicion->condicion));
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.obtenerCondicionesDeLiquidacion");
		return condicionesLiquidacionPorCodigoYTipoOperacion;
	}
	
	private List<OperacionTerminoCabecera> valorizar(Connection con, List<OperacionTerminoCabecera> operaciones, ParametroProcesarCargaOperacionTermino parametrosCarga, String fechaSistema) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.valorizar");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, parametrosCarga.IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		for(OperacionTerminoCabecera operacion : operaciones) {
			ValorizarInstrumento valorizarInstrumento = new ValorizarInstrumento();
			ValorizarInstrumentoEntrada valorizarInstrumentoEntrada = new ValorizarInstrumentoEntrada();
			valorizarInstrumentoEntrada.p_IdEmpresa = parametrosCarga.IdEmpresa;
			valorizarInstrumentoEntrada.p_IdUsuario = parametrosCarga.IdUsuario;
			valorizarInstrumentoEntrada.p_IdModulo = "MES";
			valorizarInstrumentoEntrada.p_IdPais = paisSistema;
			valorizarInstrumentoEntrada.p_OrigenProcesamiento = "MESA_UI";
			valorizarInstrumentoEntrada.p_IdMonedaAdicional = operacion.TipoReajuste;
			valorizarInstrumentoEntrada.p_Instrumentos = operacion.Detalle.stream().map(detalle->{
				ObjEntradaInstrumentos instrumento = new ObjEntradaInstrumentos();
				instrumento.IdValorizacion = detalle.IdCargaMasivaDetalle;
				instrumento.FechaOperacion = fechaSistema;
				instrumento.FechaValorizacion = fechaSistema;
				instrumento.IdClasificacionTipoInstrumento = detalle.IdClasificacionTipoInstrumento;
				instrumento.IdTipoInstrumento = detalle.IdTipoInstrumento;
				instrumento.IdInstrumento = detalle.IdInstrumento;
				instrumento.FechaVencimiento = detalle.FechaVencimiento;
				instrumento.FechaEmision = detalle.FechaEmisionInstrumento;
				instrumento.IdMonedaInstrumento = detalle.IdMonedaInstrumento;
				instrumento.IdTipoReajusteInstrumento = detalle.IdTipoReajuste;
				instrumento.Cantidad = detalle.Cantidad;
				instrumento.ValorRescate = "IF".equals(detalle.IdClasificacionTipoInstrumento) ? detalle.ValorRescate : null;
				instrumento.PrecioCompra = null;
				instrumento.TasaCompra = null;
				instrumento.MontoCompra = detalle.Monto;
				instrumento.Emisor = detalle.Emisor;
				instrumento.TablaOrigen = null;
				instrumento.Nemotecnico = detalle.Nemotecnico;
				instrumento.IdCondicionDeLiquidacion = operacion.IdCondicionLiquidacion;
				instrumento.IdMonedaEmisionInstrumento = detalle.IdTipoReajuste;
				return instrumento;
			}).collect(Collectors.toList());
			
			ValorizarInstrumentoSalida valorizarInstrumentoSalida = valorizarInstrumento.logica(con, valorizarInstrumentoEntrada);
			Map<Long, List<ObjResultadoValorizacion>> valorizacionesPorIdValorizacion = valorizarInstrumentoSalida.P_OUT_INSTRUMENTOSVALORIZADOS.stream().collect(Collectors.groupingBy(valorizacion->valorizacion.IdValorizacion));
			
			operacion.Detalle.stream().forEach(detalle->{
				List<ObjResultadoValorizacion> valorizaciones = valorizacionesPorIdValorizacion.get(detalle.IdCargaMasivaDetalle);
				if(valorizaciones != null) {
					ObjResultadoValorizacion valorizacionTasaCompra = valorizaciones.stream().filter(valorizacion->"COMP".equals(valorizacion.DimensionTasa) && "AC".equals(valorizacion.DimensionTiempo)).findFirst().orElse(null);
					ObjResultadoValorizacion valorizacionTasaMercado = valorizaciones.stream().filter(valorizacion->"MERC".equals(valorizacion.DimensionTasa) && "AC".equals(valorizacion.DimensionTiempo)).findFirst().orElse(null);
					if(valorizacionTasaCompra != null) {
						detalle.MontoValorizado = valorizacionTasaCompra.MontoMonedaSistema;
						detalle.MontoLiquidacionValorizado = valorizacionTasaCompra.MontoMonedaInstrumento;
						detalle.MontoLiquidacionOperacionValorizado = valorizacionTasaCompra.MontoMonedaAdicional;
						detalle.PrecioCompraValorizado = valorizacionTasaCompra.Precio == null ? BigDecimal.ZERO : valorizacionTasaCompra.Precio;
						detalle.TasaCompraValorizado = valorizacionTasaCompra.Tasa == null ? BigDecimal.ZERO : valorizacionTasaCompra.Tasa;
						
					}
					if(valorizacionTasaMercado != null) {
						detalle.PrecioMercadoValorizado = valorizacionTasaMercado.Precio == null ? BigDecimal.ZERO : valorizacionTasaMercado.Precio;
						detalle.TasaMercadoValorizado = valorizacionTasaMercado.Tasa  == null ? BigDecimal.ZERO : valorizacionTasaMercado.Tasa;
					}
				}
			});
		}
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.valorizar");
		return operaciones;
	}
	
	private List<MensajeSalida> crearOperacion(Connection con, ParametroProcesarCargaOperacionTermino parametroProcesarCargaOperacionTermino, List<OperacionTerminoCabecera> operaciones) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.crearOperacion");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, parametroProcesarCargaOperacionTermino.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<MensajeSalida> mensajesSalida = new ArrayList<MensajeSalida>();
		
		
		for(OperacionTerminoCabecera operacion : operaciones) {
			CrearOperacionMesa_IN entradaCrearOperacion = new CrearOperacionMesa_IN();
			entradaCrearOperacion.p_IdEmpresa = parametroProcesarCargaOperacionTermino.IdEmpresa;
			entradaCrearOperacion.p_IdModulo = parametroProcesarCargaOperacionTermino.IdModulo;
			entradaCrearOperacion.p_IdUsuario = parametroProcesarCargaOperacionTermino.IdUsuario;
			CrearOperacionMesa_Operacion_IN operacionCrearOperacionMesa = new CrearOperacionMesa_Operacion_IN();
			operacionCrearOperacionMesa.TasaAnualizada = BigDecimal.ZERO;
			operacionCrearOperacionMesa.CostoFondo = "0";
			operacionCrearOperacionMesa.UtilidadPerdida = BigDecimal.ZERO;
			operacionCrearOperacionMesa.UtilidadFinal = BigDecimal.ZERO;
			operacionCrearOperacionMesa.Accion = "CREAR";
			operacionCrearOperacionMesa.IdInternoOperacion = operacion.IdCargaMasivaDetalle;
			operacionCrearOperacionMesa.IdOperacion = 0L;
			operacionCrearOperacionMesa.FolioOperacion = 0L;
			operacionCrearOperacionMesa.IdCliente = operacion.IdCliente;
			operacionCrearOperacionMesa.IdPortafolio = operacion.IdPortafolio;
			operacionCrearOperacionMesa.IdClienteCorredor = operacion.IdClienteMesa;
			operacionCrearOperacionMesa.IdPortafolioCorredor = operacion.IdPortafolioMesa;
			operacionCrearOperacionMesa.Intermesa = "N";
			operacionCrearOperacionMesa.IdMesaOrigen = operacion.IdMesa;
			operacionCrearOperacionMesa.IdMesaDestino = operacion.IdMesa;
			operacionCrearOperacionMesa.AfectaCustodia = operacion.AfectaCustodia;
			operacionCrearOperacionMesa.IdTipoProducto = "MERF";
			operacionCrearOperacionMesa.IdTipoOperacion = "CT".equals(operacion.TipoOperacion) ? "COMTER" : "VENTER";
			operacionCrearOperacionMesa.IdTipoReajuste = operacion.TipoReajuste;
			operacionCrearOperacionMesa.Plazo = 0L;
			operacionCrearOperacionMesa.Monto = operacion.Monto;
			operacionCrearOperacionMesa.MontoMoneda = operacion.ValorInicial;
			operacionCrearOperacionMesa.IdMoneda = operacion.TipoReajuste;
			operacionCrearOperacionMesa.IdMonedaLiquidacion = operacion.TipoReajuste;
			operacionCrearOperacionMesa.MontoLiquidacion = BigDecimal.ZERO;
			operacionCrearOperacionMesa.Tasa = BigDecimal.ZERO;
			operacionCrearOperacionMesa.ValorFinal = operacion.Monto;
			operacionCrearOperacionMesa.ValorFinalMoneda = operacion.ValorInicial;
			operacionCrearOperacionMesa.TasaAgente = BigDecimal.ZERO;
			operacionCrearOperacionMesa.FechaOperacion = fechaSistema;
			operacionCrearOperacionMesa.FechaVencimiento = operacion.FechaLiquidacion;
			operacionCrearOperacionMesa.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
			operacionCrearOperacionMesa.FechaLiquidacion = operacion.FechaLiquidacion;
			operacionCrearOperacionMesa.IdAgente = operacion.IdPersonaEjecutivo;
			operacionCrearOperacionMesa.IdOperador = entradaCrearOperacion.p_IdUsuario;
			operacionCrearOperacionMesa.IdEstado = "";
			operacionCrearOperacionMesa.Glosa = operacion.TipoOperacion + " " + operacion.TipoReajuste + " FOLIO " + operacion.FolioAgrupadorCartera;
			operacionCrearOperacionMesa.IdTipoCanalIngreso = "AGE";
			operacionCrearOperacionMesa.TipoDocumento = "";
			operacionCrearOperacionMesa.IdContacto = 0L;
			operacionCrearOperacionMesa.GuardandoCabecera = "S";
			entradaCrearOperacion.p_Operacion = Arrays.asList(operacionCrearOperacionMesa);
			
			CrearOperacionMesa crearOperacionMesa = new CrearOperacionMesa();
			CrearOperacionMesa_OUT salidaCrearOperacion = crearOperacionMesa.logica(con, entradaCrearOperacion);
			mensajesSalida.addAll(salidaCrearOperacion.P_OUT_MENSAJES);
			
			if(salidaCrearOperacion.P_OUT_ERRORESCONTADOR == 0) {
				Map<Long, CrearOperacionMesa_Operacion_OUT> operacionPorIdInterno = salidaCrearOperacion.P_OUT_OPERACION.stream().collect(Collectors.toMap(operacionMesa->operacionMesa.IdInternoOperacion, operacionMesa->operacionMesa));
				
				operacion.IdOperacion = operacionPorIdInterno.get(operacion.IdCargaMasivaDetalle).IdOperacion;
				
				IngresarDetalleOperacionMesa ingresarDetalleOperacionMesa = new IngresarDetalleOperacionMesa();
				
				IngresarDetalleOperacionMesa_IN entradaIngresarDetalleOperacionMesa = new IngresarDetalleOperacionMesa_IN();
				entradaIngresarDetalleOperacionMesa.p_IdEmpresa = parametroProcesarCargaOperacionTermino.IdEmpresa;
				entradaIngresarDetalleOperacionMesa.p_IdUsuario = parametroProcesarCargaOperacionTermino.IdUsuario;
				entradaIngresarDetalleOperacionMesa.p_IdModulo = parametroProcesarCargaOperacionTermino.IdModulo;
				entradaIngresarDetalleOperacionMesa.p_UsaReglasNegocio = parametroProcesarCargaOperacionTermino.UsaReglasNegocio;
				IngresarDetalleOperacionMesa_Operacion_IN operacionModificarOperacionMesa = new IngresarDetalleOperacionMesa_Operacion_IN();
				operacionModificarOperacionMesa.Accion = "MODIFICACION";
				operacionModificarOperacionMesa.IdInternoOperacion = operacion.IdCargaMasivaDetalle;
				operacionModificarOperacionMesa.IdOperacion = operacion.IdOperacion;
				operacionModificarOperacionMesa.FolioOperacion = 0L;
				operacionModificarOperacionMesa.FechaOperacion = fechaSistema;
				operacionModificarOperacionMesa.FechaLiquidacion = fechaSistema;
				operacionModificarOperacionMesa.IdCliente = operacion.IdCliente;
				operacionModificarOperacionMesa.IdPortafolio = operacion.IdPortafolio;
				operacionModificarOperacionMesa.IdClienteCorredor = operacion.IdClienteMesa;
				operacionModificarOperacionMesa.IdPortafolioCorredor = operacion.IdPortafolioMesa;
				operacionModificarOperacionMesa.Intermesa = "N";
				operacionModificarOperacionMesa.IdMesaOrigen = operacion.IdMesa;
				operacionModificarOperacionMesa.IdMesaDestino = operacion.IdMesa;
				operacionModificarOperacionMesa.AfectaCustodia = operacion.AfectaCustodia;
				operacionModificarOperacionMesa.IdTipoProducto = "MERF";
				operacionModificarOperacionMesa.IdTipoOperacion = "CT".equals(operacion.TipoOperacion) ? "COMTER" : "VENTER";
				operacionModificarOperacionMesa.IdTipoReajuste = operacion.TipoReajuste;
				operacionModificarOperacionMesa.Plazo = 0L;
				operacionModificarOperacionMesa.Monto = operacion.Monto;
				operacionModificarOperacionMesa.MontoMoneda = operacion.ValorInicial;
				operacionModificarOperacionMesa.IdMoneda = operacion.TipoReajuste;
				operacionModificarOperacionMesa.IdMonedaLiquidacion = operacion.TipoReajuste;
				operacionModificarOperacionMesa.MontoLiquidacion = BigDecimal.ZERO;
				operacionModificarOperacionMesa.Tasa = BigDecimal.ZERO;
				operacionModificarOperacionMesa.ValorFinal = operacion.Monto;
				operacionModificarOperacionMesa.ValorFinalMoneda = operacion.ValorInicial;
				operacionModificarOperacionMesa.TasaAgente = BigDecimal.ZERO;
				operacionModificarOperacionMesa.FechaVencimiento = operacion.FechaLiquidacion;
				operacionModificarOperacionMesa.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
				operacionModificarOperacionMesa.FechaLiquidacion = operacion.FechaLiquidacion;
				operacionModificarOperacionMesa.IdAgente = operacion.IdPersonaEjecutivo;
				operacionModificarOperacionMesa.IdOperador = entradaCrearOperacion.p_IdUsuario;
				operacionModificarOperacionMesa.IdEstado = "PEN";
				operacionModificarOperacionMesa.Glosa = operacion.TipoOperacion + " " + operacion.TipoReajuste + " FOLIO " + operacion.FolioAgrupadorCartera;
				operacionModificarOperacionMesa.IdTipoCanalIngreso = "AGE";
				operacionModificarOperacionMesa.TipoDocumento = "";
				operacionModificarOperacionMesa.IdContacto = 0L;
				operacionModificarOperacionMesa.TasaAnualizada = BigDecimal.ZERO;
				operacionModificarOperacionMesa.CostoFondo = "0";
				operacionModificarOperacionMesa.UtilidadPerdida = BigDecimal.ZERO;
				operacionModificarOperacionMesa.UtilidadFinal = BigDecimal.ZERO;
				entradaIngresarDetalleOperacionMesa.p_Operacion = Arrays.asList(operacionModificarOperacionMesa);
				
				entradaIngresarDetalleOperacionMesa.p_DetalleOperacion = operacion.Detalle
						.stream()
						.map(detalle->{
							IngresarDetalleOperacionMesa_DetalleOperacion_IN detalleOperacion = new IngresarDetalleOperacionMesa_DetalleOperacion_IN();
							detalleOperacion.IdInternoDetalle = detalle.IdCargaMasivaDetalle;
							detalleOperacion.IdInternoOperacion = detalle.IdCargaMasivaDetalleCabecera;
							detalleOperacion.IdReserva = 0L;
							detalleOperacion.IdCliente = operacion.IdCliente;
							detalleOperacion.IdPortafolio = operacion.IdPortafolio;
							detalleOperacion.IdClienteCorredor = operacion.IdClienteMesa;
							detalleOperacion.IdPortafolioCorredor = operacion.IdPortafolioMesa;
							detalleOperacion.IdMesa = operacion.IdMesa;
							detalleOperacion.IdTipoProducto = "MERF";
							detalleOperacion.IdTipoOperacion = "CT".equals(operacion.TipoOperacion) ? "COMTER" : "VENTER";
							detalleOperacion.IdClasificacionTipoInstrumento = detalle.IdClasificacionTipoInstrumento;
							detalleOperacion.IdInstrumento = detalle.IdInstrumento;
							detalleOperacion.Nemotecnico = detalle.Nemotecnico;
							detalleOperacion.EsFungible = detalle.Fungible;
							detalleOperacion.IdBolsa = detalle.IdBolsa;
							detalleOperacion.IdBoveda = detalle.Boveda;
							detalleOperacion.IdCartera = detalle.IdCartera;
							detalleOperacion.FolioCartera = detalle.SecuenciaCartera;
							detalleOperacion.CodigoIFRS = "T";
							detalleOperacion.IdEmisor = detalle.Emisor;
							detalleOperacion.IdMoneda = detalle.IdMonedaInstrumento;
							detalleOperacion.IdTipoReajuste = detalle.IdTipoReajuste;
							detalleOperacion.IdMonedaEmisionInstrumento = detalle.IdMonedaInstrumento;
							detalleOperacion.IdMonedaLiquidacionInstrumento = detalle.IdMonedaInstrumento;
							detalleOperacion.IdMonedaLiquidacionOperacion = operacion.TipoReajuste;
							detalleOperacion.CorteMinimo = detalle.CorteMinimo;
							detalleOperacion.Cantidad = detalle.Cantidad;
							detalleOperacion.Precio = detalle.PrecioCompraValorizado;
							detalleOperacion.PrecioMercado = detalle.PrecioMercadoValorizado;
							detalleOperacion.Tasa = detalle.TasaCompraValorizado;
							detalleOperacion.TasaMercado = detalle.TasaMercadoValorizado;
							detalleOperacion.Monto = detalle.MontoValorizado;
							detalleOperacion.MontoLiquidacion = detalle.MontoLiquidacionValorizado;
							detalleOperacion.MontoLiquidacionOperacion = detalle.MontoLiquidacionOperacionValorizado;
							detalleOperacion.MontoValorizadoReserva = detalle.MontoLiquidacionOperacionValorizado;
							detalleOperacion.ValorRescate = detalle.ValorRescate;
							detalleOperacion.FechaVencimiento = detalle.FechaVencimiento;
							detalleOperacion.TipoCarteraCorredor = "";
							detalleOperacion.INGMontoAPlazoDetalle = detalle.MontoValorizado;
							detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = detalle.MontoLiquidacionValorizado;
							return detalleOperacion;
						}).collect(Collectors.toList());
				operacionModificarOperacionMesa.MontoLiquidacion = entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.stream().map(detalle->detalle.MontoLiquidacionOperacion).reduce(BigDecimal.ZERO, BigDecimal::add);
				IngresarDetalleOperacionMesa_OUT salidaIngresarDetalle = ingresarDetalleOperacionMesa.logica(con, entradaIngresarDetalleOperacionMesa);
				mensajesSalida.addAll(salidaIngresarDetalle.P_OUT_MENSAJES);
			}
		}
		
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.crearOperacion");
		return mensajesSalida;
	}
	
	private void actualizarCabecera(Connection con, List<OperacionTerminoCabecera> operaciones) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.actualizarCabecera");
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_OPERACION_CABECERA, operaciones, OperacionTerminoCabecera.class);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.actualizarCabecera");
	}
	
	private void actualizarDetalle(Connection con, List<OperacionTerminoCabecera> operaciones) throws Throwable {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTerminoJob.actualizarDetalle");
		
		SQL sql = new SQL();
		List<OperacionTerminoDetalle> operacionesActualizar = new ArrayList<OperacionTerminoDetalle>();
		for(OperacionTerminoCabecera operacion : operaciones) {
			operacionesActualizar.addAll(operacion.Detalle);
		}
		sql.batch(con, SQL_UPDATE_OPERACION_DETALLE, operacionesActualizar, OperacionTerminoDetalle.class);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTerminoJob.actualizarDetalle");
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String [] args) throws InterruptedException, Throwable {
		Connection c = PlataformaUtils.getDESAConnection();
		ProcesarCargaMasivaArchivoOperacionTerminoJob a = new ProcesarCargaMasivaArchivoOperacionTerminoJob();
		a.logica(c, true);
		c.rollback();
		
	}
	
	
	

}
