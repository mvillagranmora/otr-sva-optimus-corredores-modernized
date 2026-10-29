package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.MathContext;
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
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_Operacion_IN;
import cl.bcs.mesa.MRF.ObtenerCarterasRetirar.EntradaObtenerCarterasRetirar;
import cl.bcs.mesa.MRF.ObtenerCarterasRetirar.SalidaObtenerCarterasRetirar;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerNemoSVS;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Salida;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearOperacionesTerminoCargaMasiva implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CrearOperacionesTerminoCargaMasiva.class);
	private final String SQL_SELECT_OPERACION_CABECERA = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectOperacionCabecera.sql");
	private final String SQL_SELECT_OPERACION_DETALLE = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectOperacionDetalle.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectCarteras.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectInstrumentos.sql");
	private final String SQL_SELECT_CONDICION_LIQUIDACION = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectCondicionLiquidacion.sql");
	private final String SQL_UPDATE_OPERACION_CABECERA = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_UpdateCabecera.sql");
	private final String SQL_UPDATE_OPERACION_DETALLE = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_UpdateDetalle.sql");
	
	public static class EntradaCrearOperacionesTerminoCargaMasiva extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionTermino> p_Operaciones;
	}
	
	public static class OperacionTermino extends BcsPojo {
		public Long IdCargaMasivaDetalle;
	}
	
	public static class SalidaCrearOperacionesTerminoCargaMasiva extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearOperacionesTerminoCargaMasiva() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
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
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearOperacionesTerminoCargaMasiva jin = gson.fromJson(in.toString(), EntradaCrearOperacionesTerminoCargaMasiva.class);
		SalidaCrearOperacionesTerminoCargaMasiva out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearOperacionesTerminoCargaMasiva logica(Connection con, EntradaCrearOperacionesTerminoCargaMasiva entrada) throws Exception{
		logger.info("INICIO::CrearOperacionesTerminoCargaMasiva");
		
		SalidaCrearOperacionesTerminoCargaMasiva out = new SalidaCrearOperacionesTerminoCargaMasiva();
		List<OperacionTerminoCabecera> operaciones = obtenerOperaciones(con, entrada);
		
		List<MensajeSalida> salidaCrearOperacion = crearOperacion(con, entrada, operaciones);
		out.P_OUT_MENSAJES = salidaCrearOperacion;
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_MENSAJES = limpiarMensajes(con, salidaCrearOperacion, entrada.p_IdEmpresa);
			return out;
		}
		
		actualizarCabecera(con, operaciones);
		actualizarDetalle(con, operaciones);
		
		logger.info("FIN::CrearOperacionesTerminoCargaMasiva");
		return out;
	}
	
	private List<OperacionTerminoCabecera> obtenerOperaciones(Connection con, EntradaCrearOperacionesTerminoCargaMasiva entrada) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerOperaciones");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		List<Long> idOperaciones = entrada.p_Operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTerminoCabecera> operacionesCabecera = sql.query(con, SQL_SELECT_OPERACION_CABECERA, parametros, OperacionTerminoCabecera.class);
		List<OperacionTerminoDetalle> operacionesDetalle = obtenerOperacionesDetalle(con, entrada.p_Operaciones);
		
		Map<Long,List<OperacionTerminoDetalle>> operacionesDetallePorIdCabecera = operacionesDetalle.stream().collect(Collectors.groupingBy(operacion->operacion.IdCargaMasivaDetalleCabecera));
		
		Map<Long, CalculoSalida> paridadPorIdCabecera = obtenerParidadPorIdCabecera(con, operacionesCabecera, entrada.p_IdEmpresa, fechaSistema);
		Map<String, CondicionLiquidacion> condicionesLiquidacionPorCodigoYTipoOperacion = obtenerCondicionesDeLiquidacion(con);
		
		operacionesCabecera.stream().forEach(operacion->{
			CondicionLiquidacion condicionLiquidacion = condicionesLiquidacionPorCodigoYTipoOperacion.get(operacion.CondicionLiquidacion + "_" + operacion.TipoOperacionReal);
			operacion.Detalle = operacionesDetallePorIdCabecera.get(operacion.IdCargaMasivaDetalle);
			operacion.Monto = paridadPorIdCabecera.get(operacion.IdCargaMasivaDetalle).MontoDestino;
			operacion.PlazoEnDiasCondicionLiquidacion = Long.valueOf(condicionLiquidacion.PlazoEnDias);
			operacion.IdCondicionLiquidacion = condicionLiquidacion.IdCondicionLiquidacion;
		});
		
		HashMap<String, ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> hashFechasSalida = obtenerFechaLiquidacion(con, operacionesCabecera,  entrada.p_IdEmpresa, fechaSistema);
		
		operacionesCabecera.stream().forEach(operacion->{
			operacion.FechaLiquidacion = hashFechasSalida.get(fechaSistema + "_" + operacion.PlazoEnDiasCondicionLiquidacion).fechaCalculada;
		});
		
		operacionesCabecera = valorizar(con, operacionesCabecera, entrada, fechaSistema);
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerOperaciones");
		return operacionesCabecera;
	}
	
	private List<OperacionTerminoDetalle> obtenerOperacionesDetalle(Connection con, List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerOperacionesDetalle");
		
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
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerOperacionesDetalle");
		return operacionesDetalle;
	}
	
	private Map<Long, Cartera> obtenerCarterasAgrupadasPorFolio(Connection con, List<OperacionTerminoDetalle> detalles) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerCarterasAgrupadasPorFolio");
		
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
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerCarterasAgrupadasPorFolio");
		return carterasAgrupadasPorFolio;
	}
	
	private Map<String, Instrumento> obtenerInstrumentosAgrupadosPorNemo(Connection con, List<OperacionTerminoDetalle> detalles) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerInstrumentosAgrupadosPorNemo");
		
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
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerInstrumentosAgrupadosPorNemo");
		return instrumentosAgrupadosPorNemo;
	}
	
	private Map<Long, CalculoSalida> obtenerParidadPorIdCabecera(Connection con, List<OperacionTerminoCabecera> operacionesCabecera, Long idEmpresa, String fechaSistema) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerParidadPorIdCabecera");
		
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
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerParidadPorIdCabecera");
		return paridadPorIdCabecera;
	}
	
	private HashMap<String, ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> obtenerFechaLiquidacion(Connection con, List<OperacionTerminoCabecera> operacionesCabecera, Long idEmpresa, String fechaSistema) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerFechaLiquidacion");
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
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerFechaLiquidacion");
		return hashFechasSalida;
	}
	
	private Map<String, CondicionLiquidacion> obtenerCondicionesDeLiquidacion(Connection con) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerCondicionesDeLiquidacion");
		
		SQL sql = new SQL();
		List<CondicionLiquidacion> condicionesLiquidacion = sql.query(con, SQL_SELECT_CONDICION_LIQUIDACION, new HashMap<String, Object>(1), CondicionLiquidacion.class);
		Map<String, CondicionLiquidacion> condicionesLiquidacionPorCodigoYTipoOperacion = condicionesLiquidacion
				.stream()
				.collect(Collectors.toMap(condicion->condicion.CodigoCondicionLiquidacion + "_" + condicion.IdTipoOperacion, condicion->condicion));
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerCondicionesDeLiquidacion");
		return condicionesLiquidacionPorCodigoYTipoOperacion;
	}
	
	private List<OperacionTerminoCabecera> valorizar(Connection con, List<OperacionTerminoCabecera> operaciones, EntradaCrearOperacionesTerminoCargaMasiva entrada, String fechaSistema) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.valorizar");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		for(OperacionTerminoCabecera operacion : operaciones) {
			ValorizarInstrumento valorizarInstrumento = new ValorizarInstrumento();
			ValorizarInstrumentoEntrada valorizarInstrumentoEntrada = new ValorizarInstrumentoEntrada();
			valorizarInstrumentoEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			valorizarInstrumentoEntrada.p_IdUsuario = entrada.p_IdUsuario;
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
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.valorizar");
		return operaciones;
	}
	
	private List<MensajeSalida> crearOperacion(Connection con,  EntradaCrearOperacionesTerminoCargaMasiva entrada, List<OperacionTerminoCabecera> operaciones) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.crearOperacion");
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa);
		String fechaSistema = fechaHoraSistema.split(" ")[0];
		List<MensajeSalida> mensajesSalida = new ArrayList<MensajeSalida>();
		
		List<ObtenerCarterasRetirar.Resultado> carterasRetiroFifo = obtenerCarteraRetirar(con, entrada, operaciones);		
		Map<Long, ObtenerCarterasRetirar.Resultado> resultadosPorIdInterno = carterasRetiroFifo.stream().collect(Collectors.toMap(resultado->resultado.IdInterno, resultado->resultado));
		
		for(OperacionTerminoCabecera operacion : operaciones) {
			CrearOperacionMesa_IN entradaCrearOperacion = new CrearOperacionMesa_IN();
			entradaCrearOperacion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearOperacion.p_IdModulo = entrada.p_IdModulo;
			entradaCrearOperacion.p_IdUsuario = entrada.p_IdUsuario;
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
			operacionCrearOperacionMesa.FechaOperacion = fechaHoraSistema;
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
			
			salidaCrearOperacion.P_OUT_MENSAJES.forEach(mensaje->mensaje.id = operacion.IdCargaMasivaDetalle);
			mensajesSalida.addAll(salidaCrearOperacion.P_OUT_MENSAJES);
			
			if(salidaCrearOperacion.P_OUT_ERRORESCONTADOR == 0) {
				Map<Long, CrearOperacionMesa_Operacion_OUT> operacionPorIdInterno = salidaCrearOperacion.P_OUT_OPERACION.stream().collect(Collectors.toMap(operacionMesa->operacionMesa.IdInternoOperacion, operacionMesa->operacionMesa));
				
				operacion.IdOperacion = operacionPorIdInterno.get(operacion.IdCargaMasivaDetalle).IdOperacion;
				
				IngresarDetalleOperacionMesa ingresarDetalleOperacionMesa = new IngresarDetalleOperacionMesa();
				
				IngresarDetalleOperacionMesa_IN entradaIngresarDetalleOperacionMesa = new IngresarDetalleOperacionMesa_IN();
				entradaIngresarDetalleOperacionMesa.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaIngresarDetalleOperacionMesa.p_IdUsuario = entrada.p_IdUsuario;
				entradaIngresarDetalleOperacionMesa.p_IdModulo = entrada.p_IdModulo;
				entradaIngresarDetalleOperacionMesa.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
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
				
				List<MensajeSalida> mensajesErroresFIFO = new ArrayList<MensajeSalida>();
				for(OperacionTerminoDetalle detalle: operacion.Detalle) {
					if(esRetiroCustodia(operacion) && detalle.SecuenciaCartera == 0) {
						ObtenerCarterasRetirar.Resultado resultado = resultadosPorIdInterno.get(detalle.IdCargaMasivaDetalle);
						if(resultado == null || "ERR".equals(resultado.IdEstado)) {
							HashMap<String,String> parametrosMensaje = new HashMap<String,String>(3);
							parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
							mensajesErroresFIFO.add(new MensajeSalida(operacion.IdCargaMasivaDetalle, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_SIN_CARTERAS", parametrosMensaje, con, entrada.p_IdEmpresa));
							continue;
						}
						
						for(ObtenerCarterasRetirar.CarteraRetiro cartera : resultado.CarterasRetiro) {
							IngresarDetalleOperacionMesa_DetalleOperacion_IN detalleOperacion = new IngresarDetalleOperacionMesa_DetalleOperacion_IN();
							detalleOperacion.IdInternoDetalle = cartera.IdCartera * -1L;
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
							detalleOperacion.EsFungible = "RF".equals(detalle.Mercado) ? "N" : detalle.Fungible;
							detalleOperacion.IdBolsa = detalle.IdBolsa;
							detalleOperacion.IdBoveda = detalle.Boveda;
							detalleOperacion.IdCartera = cartera.IdCartera;
							detalleOperacion.FolioCartera = cartera.FolioCartera;
							detalleOperacion.CodigoIFRS = "T";
							detalleOperacion.IdEmisor = detalle.Emisor;
							detalleOperacion.IdMoneda = detalle.IdMonedaInstrumento;
							detalleOperacion.IdTipoReajuste = detalle.IdTipoReajuste;
							detalleOperacion.IdMonedaEmisionInstrumento = detalle.IdMonedaInstrumento;
							detalleOperacion.IdMonedaLiquidacionInstrumento = detalle.IdMonedaInstrumento;
							detalleOperacion.IdMonedaLiquidacionOperacion = operacion.TipoReajuste;
							detalleOperacion.CorteMinimo = detalle.CorteMinimo;
							detalleOperacion.Cantidad = cartera.SaldoRetiro;
							detalleOperacion.Precio = detalle.PrecioCompraValorizado;
							detalleOperacion.PrecioMercado = detalle.PrecioMercadoValorizado;
							detalleOperacion.Tasa = detalle.TasaCompraValorizado;
							detalleOperacion.TasaMercado = detalle.TasaMercadoValorizado;
							if("RF".equals(detalle.Mercado)) {
								detalleOperacion.Monto = cartera.SaldoRetiro.multiply(detalle.MontoValorizado, MathContext.DECIMAL128).divide(detalle.Cantidad, MathContext.DECIMAL128);
								detalleOperacion.MontoLiquidacion = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionValorizado, MathContext.DECIMAL128).divide(detalle.Cantidad, MathContext.DECIMAL128);
								detalleOperacion.MontoLiquidacionOperacion = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionOperacionValorizado, MathContext.DECIMAL128).divide(detalle.Cantidad, MathContext.DECIMAL128);
								detalleOperacion.MontoValorizadoReserva = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionOperacionValorizado, MathContext.DECIMAL128).divide(detalle.Cantidad, MathContext.DECIMAL128);
								detalleOperacion.INGMontoAPlazoDetalle = cartera.SaldoRetiro.multiply(detalle.MontoValorizado, MathContext.DECIMAL128).divide(detalle.Cantidad, MathContext.DECIMAL128);
								detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionValorizado, MathContext.DECIMAL128).divide(detalle.Cantidad, MathContext.DECIMAL128);
							}
							
							if("IF".equals(detalle.Mercado)) {
								detalleOperacion.Monto = cartera.SaldoRetiro.multiply(detalle.MontoValorizado, MathContext.DECIMAL128).divide(detalle.ValorRescate, MathContext.DECIMAL128);
								detalleOperacion.MontoLiquidacion = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionValorizado, MathContext.DECIMAL128).divide(detalle.ValorRescate, MathContext.DECIMAL128);
								detalleOperacion.MontoLiquidacionOperacion = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionOperacionValorizado, MathContext.DECIMAL128).divide(detalle.ValorRescate, MathContext.DECIMAL128);
								detalleOperacion.MontoValorizadoReserva = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionOperacionValorizado, MathContext.DECIMAL128).divide(detalle.ValorRescate, MathContext.DECIMAL128);
								detalleOperacion.INGMontoAPlazoDetalle = cartera.SaldoRetiro.multiply(detalle.MontoValorizado, MathContext.DECIMAL128).divide(detalle.ValorRescate, MathContext.DECIMAL128);
								detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = cartera.SaldoRetiro.multiply(detalle.MontoLiquidacionValorizado, MathContext.DECIMAL128).divide(detalle.ValorRescate, MathContext.DECIMAL128);
							}
							detalleOperacion.Monto = MonedaFormato.formatear(con, detalleOperacion.Monto, MonedaFormato.MONTO, operacion.TipoReajuste).valor;
							detalleOperacion.MontoLiquidacion = MonedaFormato.formatear(con, detalleOperacion.MontoLiquidacion, MonedaFormato.MONTO, operacion.TipoReajuste).valor;
							detalleOperacion.MontoLiquidacionOperacion = MonedaFormato.formatear(con, detalleOperacion.MontoLiquidacionOperacion, MonedaFormato.MONTO, operacion.TipoReajuste).valor;
							detalleOperacion.MontoValorizadoReserva = MonedaFormato.formatear(con, detalleOperacion.MontoValorizadoReserva, MonedaFormato.MONTO, operacion.TipoReajuste).valor;
							detalleOperacion.INGMontoAPlazoDetalle = MonedaFormato.formatear(con, detalleOperacion.INGMontoAPlazoDetalle, MonedaFormato.MONTO, operacion.TipoReajuste).valor;
							detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = MonedaFormato.formatear(con, detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle, MonedaFormato.MONTO, operacion.TipoReajuste).valor;
							detalleOperacion.ValorRescate = detalle.ValorRescate;
							detalleOperacion.FechaVencimiento = detalle.FechaVencimiento;
							detalleOperacion.TipoCarteraCorredor = "";
							entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.add(detalleOperacion);
						}
					} else {
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
						detalleOperacion.EsFungible = "RF".equals(detalle.Mercado) ? "N" : detalle.Fungible;
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
						entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.add(detalleOperacion);
					}
				}
				
				if(mensajesErroresFIFO.size() > 0) {
					mensajesSalida.addAll(mensajesErroresFIFO);
					continue;
				}
				
				operacionModificarOperacionMesa.MontoLiquidacion = entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.stream().map(detalle->detalle.MontoLiquidacionOperacion).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal montoOriginal = operacion.Detalle.stream().map(detalle->detalle.Monto).reduce(BigDecimal.ZERO, BigDecimal::add);
				if(montoOriginal.compareTo(operacionModificarOperacionMesa.MontoLiquidacion) != 0) {
					IngresarDetalleOperacionMesa_DetalleOperacion_IN detalle = entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.stream().findFirst().orElse(new IngresarDetalleOperacionMesa_DetalleOperacion_IN());
					detalle.MontoLiquidacionOperacion = detalle.MontoLiquidacionOperacion.add(montoOriginal.subtract(operacionModificarOperacionMesa.MontoLiquidacion), MathContext.DECIMAL128);
				}
				operacionModificarOperacionMesa.MontoLiquidacion = entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.stream().map(detalle->detalle.MontoLiquidacionOperacion).reduce(BigDecimal.ZERO, BigDecimal::add);
				
				IngresarDetalleOperacionMesa_OUT salidaIngresarDetalle = ingresarDetalleOperacionMesa.logica(con, entradaIngresarDetalleOperacionMesa);
				
				salidaIngresarDetalle.P_OUT_MENSAJES.forEach(mensaje->mensaje.id = operacion.IdCargaMasivaDetalle);
				mensajesSalida.addAll(salidaIngresarDetalle.P_OUT_MENSAJES);
				
				if(salidaIngresarDetalle.P_OUT_ERRORESCONTADOR == 0) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(3);
					parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
					parametrosMensaje.put("IdOperacion", String.valueOf(operacion.IdOperacion));
					mensajesSalida.add(new MensajeSalida(operacion.IdCargaMasivaDetalle, MensajeSalida.MENSAJE_INFORMACION,"OPERACION_TERMINO_OPERACION_CREADA", parametrosMensaje, con, entrada.p_IdEmpresa));
				}
			}
		}
		
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.crearOperacion");
		return mensajesSalida;
	}
	
	private List<ObtenerCarterasRetirar.Resultado> obtenerCarteraRetirar(Connection con, EntradaCrearOperacionesTerminoCargaMasiva entrada, List<OperacionTerminoCabecera> operaciones) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerCarteraRetirar");
		
		List<Instrumento_Salida> nemosSVS = obtenerNemoSVS(con, operaciones);
		Map<Long, String> nemoPorIdInterno = nemosSVS.stream().collect(Collectors.toMap(nemoSVS->nemoSVS.IdInterno, nemoSVS->nemoSVS.NemoSVS));
		
		EntradaObtenerCarterasRetirar entradaObtenerCarterasRetirar = new EntradaObtenerCarterasRetirar();
		entradaObtenerCarterasRetirar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaObtenerCarterasRetirar.p_IdUsuario = entrada.p_IdUsuario;
		entradaObtenerCarterasRetirar.p_IdModulo = entrada.p_IdModulo;
		for(OperacionTerminoCabecera operacion : operaciones) {
			if(esRetiroCustodia(operacion)) {
				List<ObtenerCarterasRetirar.Consulta> consultas = operacion.Detalle
						.stream()
						.filter(detalle->detalle.SecuenciaCartera == 0)
						.map(detalle->{
							ObtenerCarterasRetirar.Consulta consulta = new ObtenerCarterasRetirar.Consulta();
							consulta.IdInterno = detalle.IdCargaMasivaDetalle;
							consulta.IdClasificacionTipoInstrumento = detalle.Mercado;
							consulta.IdPortafolio = "CT".equals(operacion.TipoOperacion) ? operacion.IdPortafolio : operacion.IdPortafolioMesa;
							consulta.IdBoveda = detalle.Boveda;
							if("RF".equals(consulta.IdClasificacionTipoInstrumento)) {
								consulta.Nemotecnico = detalle.Nemotecnico;
								consulta.Cantidad = detalle.Cantidad;
							}
							if("IF".equals(consulta.IdClasificacionTipoInstrumento)) {
								consulta.Nemotecnico = nemoPorIdInterno.get(detalle.IdCargaMasivaDetalle);
								consulta.Cantidad = detalle.ValorRescate;
							}
							return consulta;
						}).collect(Collectors.toList());
				entradaObtenerCarterasRetirar.p_Consultas.addAll(consultas);
			}
		}
		
		SalidaObtenerCarterasRetirar salidaObtenerCarterasRetirar = new SalidaObtenerCarterasRetirar();
		ObtenerCarterasRetirar obtenerCarterasRetirar = new ObtenerCarterasRetirar();
		if(entradaObtenerCarterasRetirar.p_Consultas.size() > 0) {
			salidaObtenerCarterasRetirar =  obtenerCarterasRetirar.logica(con, entradaObtenerCarterasRetirar);
		}
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerCarteraRetirar");
		return salidaObtenerCarterasRetirar.P_OUT_RESULTADO;
	}
	
	private Boolean esRetiroCustodia(OperacionTerminoCabecera operacion) {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.esRetiroFIFO");
		Boolean esRetiroFIFO = esCompraTerminoCustodia(operacion) || "VT".equals(operacion.TipoOperacion);
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.esRetiroFIFO");
		return esRetiroFIFO;
	}
	
	private Boolean esCompraTerminoCustodia(OperacionTerminoCabecera operacion) {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.esCompraTerminoCustodia");
		Boolean esCompraTerminoCustodia = "CT".equals(operacion.TipoOperacion) && "S".equals(operacion.AfectaCustodia);
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.esCompraTerminoCustodia");
		return esCompraTerminoCustodia;
	}
	
	private List<Instrumento_Salida> obtenerNemoSVS(Connection con, List<OperacionTerminoCabecera> operaciones) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerNemoSVS");
		ObtenerNemoSVS_Entrada entradaObtenerNemoSVS = new ObtenerNemoSVS_Entrada();
		
		for(OperacionTerminoCabecera operacion : operaciones) {
			entradaObtenerNemoSVS.p_Instrumentos = operacion.Detalle
					.stream()
					.filter(detalle->"IF".equals(detalle.Mercado))
					.map(detalle->{
						Instrumento_Entrada instrumento = new Instrumento_Entrada();
						instrumento.IdInterno = detalle.IdCargaMasivaDetalle;
						instrumento.Nemotecnico = detalle.Nemotecnico;
						instrumento.IdEmisor = detalle.Emisor;
						instrumento.FechaVencimiento = detalle.FechaVencimiento;
						instrumento.IdTipoReajuste = detalle.IdTipoReajuste;
						instrumento.PagaderoEn = detalle.IdMonedaEmision;
						return instrumento;
					}).collect(Collectors.toList());
		}
		
		ObtenerNemoSVS obtenerNemoSVS = new ObtenerNemoSVS();
		ObtenerNemoSVS_Salida salidaObtenerNemoSVS = new ObtenerNemoSVS_Salida();
		
		if(entradaObtenerNemoSVS.p_Instrumentos.size() > 0) {
			salidaObtenerNemoSVS = obtenerNemoSVS.logica(con, entradaObtenerNemoSVS);
		}
		
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerNemoSVS");
		return salidaObtenerNemoSVS.p_out_Instrumento;
	}
	
	private List<MensajeSalida> limpiarMensajes(Connection con, List<MensajeSalida> mensajesSalida, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.limpiarMensajes");
		List<MensajeSalida> mensajesCorrectos = mensajesSalida
				.stream()
				.filter(mensajeSalida->!MensajeSalida.MENSAJE_ERROR.equals(mensajeSalida.tipo))
				.collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = mensajesSalida
				.stream()
				.filter(mensajeSalida->MensajeSalida.MENSAJE_ERROR.equals(mensajeSalida.tipo))
				.collect(Collectors.toList());
		
		List<Long> idsMensajes = mensajesCorrectos
				.stream()
				.map(mensajeSalida->mensajeSalida.id)
				.distinct()
				.collect(Collectors.toList());
		List<MensajeSalida> mensajesAdvertencia = new ArrayList<MensajeSalida>();
		for(Long idMensaje : idsMensajes) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(idMensaje));
			mensajesAdvertencia.add(new MensajeSalida(idMensaje, MensajeSalida.MENSAJE_ADVERTENCIA,"OPERACION_TERMINO_NO_PROCESADA_POR_ERROR_EN_OTRA_OPERACION", parametrosMensaje, con, idEmpresa));
		}
		
		List<MensajeSalida> mensajesFinales = new ArrayList<MensajeSalida>();
		mensajesFinales.addAll(mensajesErrores);
		mensajesFinales.addAll(mensajesAdvertencia);
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.limpiarMensajes");
		return mensajesFinales;
	}
	
	private void actualizarCabecera(Connection con, List<OperacionTerminoCabecera> operaciones) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.actualizarCabecera");
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_OPERACION_CABECERA, operaciones, OperacionTerminoCabecera.class);
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.actualizarCabecera");
	}
	
	private void actualizarDetalle(Connection con, List<OperacionTerminoCabecera> operaciones) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.actualizarDetalle");
		
		SQL sql = new SQL();
		List<OperacionTerminoDetalle> operacionesActualizar = new ArrayList<OperacionTerminoDetalle>();
		for(OperacionTerminoCabecera operacion : operaciones) {
			operacionesActualizar.addAll(operacion.Detalle);
		}
		sql.batch(con, SQL_UPDATE_OPERACION_DETALLE, operacionesActualizar, OperacionTerminoDetalle.class);
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.actualizarDetalle");
	}
	
}
