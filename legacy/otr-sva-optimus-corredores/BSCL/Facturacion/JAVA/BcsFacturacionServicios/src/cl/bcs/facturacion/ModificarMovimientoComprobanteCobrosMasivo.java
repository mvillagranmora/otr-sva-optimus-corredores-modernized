package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarMovimientoComprobanteCobrosMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarMovimientoComprobanteCobrosMasivo.class);
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteCobrosMasivo_SelectMovimientoComprobanteCobros.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_COBROS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteCobrosMasivo_UpdateMovimientoComprobanteCobros.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS_PARA_IMPUESTOS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteCobrosMasivo_SelectMovimientoComprobanteCobrosParaImpuestos.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS_IMPUESTOS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteCobrosMasivo_SelectMovimientoComprobanteCobrosImpuestos.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_COBROS_IMPUESTOS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteCobrosMasivo_UpdateMovimientoComprobanteCobrosImpuestos.sql");
	
	public static class EntradaModificarMovimientoComprobanteCobrosMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdCobro;
		public List<MovimientoComprobante> p_MovimientosComprobante;
		public List<Cobro> p_Cobros;
		
		public EntradaModificarMovimientoComprobanteCobrosMasivo() {
			this.p_MovimientosComprobante = new ArrayList<>();
			this.p_Cobros = new ArrayList<>();
		}
	}
	
	public static class Cobro extends BcsPojo {
		public Long IdInterno;
		public String IdCobro;
		public String IdMonedaCobro;
		public String IdMonedaCobroLiquidacion;
		public String IdMonedaMovimientoComprobanteCobro;
		public BigDecimal MontoCobro;
		public BigDecimal MontoCobroAnterior;
		public BigDecimal Porcentaje;
		public BigDecimal MontoLiquidacionCobro;
		public BigDecimal MontoMovimientoComprobanteCobro;
	}
	
	public static class MovimientoComprobante extends BcsPojo {
		public Long IdMovimientoComprobante;
	}
	
	public static class MovimientoComprobanteCobro extends BcsPojo {
		public Long IdMovimientoComprobanteCobro;
		public Long IdMovimientoComprobante;
		public String IdCobro;
		public String IdMonedaCobro;
		public String IdMonedaLiquidacionCobro;
		public String IdMonedaMovimientoComprobanteCobro;
		public BigDecimal MontoCobroAnterior;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoMovimientoComprobante;
	}
	
	public static class MovimientoComprobanteCobroModificar extends MovimientoComprobanteCobro {
		public BigDecimal MontoCobro;
		public BigDecimal MontoMovimientoComprobanteCobro;
		public BigDecimal MontoLiquidacionCobro;
		public BigDecimal Porcentaje;
	}
	
	public static class CobroParaImpuestos extends BcsPojo {
		public Long IdMovimientoComprobante;
		public BigDecimal MontoMovimientoComprobanteCobro;
	}
	
	public static class Impuesto extends BcsPojo {
		public Long IdMovimientoComprobanteCobro;
		public Long IdMovimientoComprobante;
		public String IdMonedaCobro;
		public BigDecimal MontoCobro;
		public BigDecimal Porcentaje;
	}
	
	public static class SalidaModificarMovimientoComprobanteCobrosMasivo extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarMovimientoComprobanteCobrosMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarMovimientoComprobanteCobrosMasivo in = gson.fromJson(jin.toString(), EntradaModificarMovimientoComprobanteCobrosMasivo.class);
		SalidaModificarMovimientoComprobanteCobrosMasivo out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarMovimientoComprobanteCobrosMasivo logica(Connection con, EntradaModificarMovimientoComprobanteCobrosMasivo entrada) throws Exception {
		logger.info("INICIO::ModificarMovimientoComprobanteCobrosMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarMovimientoComprobanteCobrosMasivo out = new SalidaModificarMovimientoComprobanteCobrosMasivo();
		
		entrada.p_Cobros = calcularParidadCobros(con, entrada.p_Cobros, entrada.p_IdEmpresa);
		List<MovimientoComprobanteCobro> movimientosComprobanteCobro = obtenerMovimientosComprobanteCobro(con, entrada.p_IdCobro, entrada.p_MovimientosComprobante);
		List<MovimientoComprobanteCobroModificar> movimientosComprobanteCobroModificar = obtenerMovimientosComprobanteCobroModificar(con, movimientosComprobanteCobro, entrada.p_Cobros);
		movimientosComprobanteCobroModificar = calcularParidadMovimientos(con, movimientosComprobanteCobroModificar, entrada.p_IdEmpresa);
		actualizarMovimientoComprobanteCobros(con, movimientosComprobanteCobroModificar);
		List<CobroParaImpuestos> cobrosParaImpuesto = obtenerCobrosParaImpuestos(con, entrada.p_MovimientosComprobante);
		List<Impuesto> impuestos = obtenerImpuestos(con, entrada.p_MovimientosComprobante);
		impuestos = calcularImpuestos(con, cobrosParaImpuesto, impuestos);
		actualizarImpuestos(con, impuestos);
		
		MensajeSalida mensaje = new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_MODIFICACION_COBROS_ACTUALIZADOS_EXITOSAMENTE", new HashMap<String, String>(), con, entrada.p_IdEmpresa);
		out.P_OUT_MENSAJES.add(mensaje);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarMovimientoComprobanteCobrosMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<Cobro> calcularParidadCobros(Connection con, List<Cobro> cobros, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.calcularParidadCobros");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.FechaSistema");
		
		CalcularParidadEntrada entrada = new CalcularParidadEntrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdTrabajo = 0L;
		
		List<Cobro> cobrosCOVA = cobros.stream()
				.filter(movimientoComprobanteCobroModificar->"COVA".equals(movimientoComprobanteCobroModificar.IdCobro))
				.collect(Collectors.toList());
		
		
		for(Cobro cobro : cobrosCOVA) {
			CalculoEntrada calculoEntradaMontoLiquidacion = new CalculoEntrada();
			calculoEntradaMontoLiquidacion.Fecha = fechaSistema;
			calculoEntradaMontoLiquidacion.IdInterno = cobro.IdInterno;
			calculoEntradaMontoLiquidacion.IdMonedaOrigen = cobro.IdMonedaCobro;
			calculoEntradaMontoLiquidacion.IdMonedaDestino = cobro.IdMonedaCobroLiquidacion;
			calculoEntradaMontoLiquidacion.IdTrabajo = 0L;
			calculoEntradaMontoLiquidacion.IdUso = "";
			calculoEntradaMontoLiquidacion.MontoOrigen = cobro.MontoCobro;
			calculoEntradaMontoLiquidacion.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			CalculoEntrada calculoEntradaMontoComprobante = new CalculoEntrada();
			calculoEntradaMontoComprobante.Fecha = fechaSistema;
			calculoEntradaMontoComprobante.IdInterno = cobro.IdInterno * -1;
			calculoEntradaMontoComprobante.IdMonedaOrigen = cobro.IdMonedaCobro;
			calculoEntradaMontoComprobante.IdMonedaDestino = cobro.IdMonedaMovimientoComprobanteCobro;
			calculoEntradaMontoComprobante.IdTrabajo = 0L;
			calculoEntradaMontoComprobante.IdUso = "";
			calculoEntradaMontoComprobante.MontoOrigen = cobro.MontoCobro;
			calculoEntradaMontoComprobante.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(calculoEntradaMontoLiquidacion);
			entrada.p_CalculosEntrada.add(calculoEntradaMontoComprobante);
		}
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, entrada);
		
		Map<Long, CalculoSalida> calculosParidadAgrupados = calcularParidadSalida.p_out_CalculosSalida.stream()
				.collect(Collectors.toMap(calculoParidad->calculoParidad.IdInterno, calculoParidad->calculoParidad));
		
		for(Cobro cobro : cobros) {
			cobro.MontoCobro = MonedaFormato.formatear(con, cobro.MontoCobro, MonedaFormato.MONTO, cobro.IdMonedaCobro).valor;
			cobro.Porcentaje = MonedaFormato.formatear(con, cobro.Porcentaje, MonedaFormato.TASA, cobro.IdMonedaCobro).valor;
			CalculoSalida calculoSalidaLiquidacion = calculosParidadAgrupados.get(cobro.IdInterno);
			CalculoSalida calculoSalidaComprobante = calculosParidadAgrupados.get(cobro.IdInterno * -1);
			
			if(calculoSalidaLiquidacion != null) {
				cobro.MontoLiquidacionCobro = MonedaFormato.formatear(con, calculoSalidaLiquidacion.MontoDestino, MonedaFormato.MONTO, cobro.IdMonedaCobroLiquidacion).valor;
			}
			
			if(calculoSalidaComprobante != null) {
				cobro.MontoMovimientoComprobanteCobro = MonedaFormato.formatear(con, calculoSalidaComprobante.MontoDestino, MonedaFormato.MONTO, cobro.IdMonedaMovimientoComprobanteCobro).valor;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.calcularParidadCobros", fin - inicio);
		return cobros;
	}
	
	private List<MovimientoComprobanteCobro> obtenerMovimientosComprobanteCobro(Connection con, String idCobro, List<MovimientoComprobante> movimientosComprobante) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerMovimientosComprobanteCobro");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsMovimientoComprobante = movimientosComprobante.stream()
				.map(movimientoComprobante->movimientoComprobante.IdMovimientoComprobante)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdCobro", idCobro);
		parametros.put("IdMovimientoComprobante", idsMovimientoComprobante);
		
		SQL sql = new SQL();
		List<MovimientoComprobanteCobro> movimientosComprobanteCobro = sql.query(con, SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS, parametros, MovimientoComprobanteCobro.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerMovimientosComprobanteCobro", fin - inicio);
		return movimientosComprobanteCobro;
	}
	
	private List<MovimientoComprobanteCobroModificar> obtenerMovimientosComprobanteCobroModificar(Connection con, List<MovimientoComprobanteCobro> movimientosComprobanteCobro, List<Cobro> cobros) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerMovimientosComprobanteCobroModificar");
		long inicio = System.currentTimeMillis();
		
		
		Map<String, Cobro> cobrosAgrupados = cobros.stream()
				.collect(Collectors.toMap(cobro-> obtenerLlaveCobro(cobro), cobro-> cobro));
		
		List<MovimientoComprobanteCobroModificar> movimientosComprobanteCobroModificar = movimientosComprobanteCobro.stream()
				.filter(movimientoComprobanteCobro->{
					Cobro cobro = cobrosAgrupados.get(obtenerLlaveCobro(movimientoComprobanteCobro));
					return cobro != null;
				})
				.map(movimientoComprobanteCobro->{
					MovimientoComprobanteCobroModificar movimientoComprobanteCobroModificar = new MovimientoComprobanteCobroModificar();
					Cobro cobro = cobrosAgrupados.get(obtenerLlaveCobro(movimientoComprobanteCobro));
					movimientoComprobanteCobroModificar.IdMovimientoComprobanteCobro = movimientoComprobanteCobro.IdMovimientoComprobanteCobro;
					movimientoComprobanteCobroModificar.IdMovimientoComprobante = movimientoComprobanteCobro.IdMovimientoComprobante;
					movimientoComprobanteCobroModificar.IdCobro = movimientoComprobanteCobro.IdCobro;
					movimientoComprobanteCobroModificar.IdMonedaCobro = movimientoComprobanteCobro.IdMonedaCobro;
					movimientoComprobanteCobroModificar.IdMonedaLiquidacionCobro = movimientoComprobanteCobro.IdMonedaLiquidacionCobro;
					movimientoComprobanteCobroModificar.IdMonedaMovimientoComprobanteCobro = movimientoComprobanteCobro.IdMonedaMovimientoComprobanteCobro;
					movimientoComprobanteCobroModificar.MontoCobroAnterior = movimientoComprobanteCobro.MontoCobroAnterior;
					movimientoComprobanteCobroModificar.Monto = movimientoComprobanteCobro.Monto;
					movimientoComprobanteCobroModificar.MontoLiquidacion = movimientoComprobanteCobro.MontoLiquidacion;
					movimientoComprobanteCobroModificar.MontoMovimientoComprobante = movimientoComprobanteCobro.MontoMovimientoComprobante;
					if("COVA".equals(movimientoComprobanteCobroModificar.IdCobro)) {
						movimientoComprobanteCobroModificar = obtenerNuevosMontosComisionVariable(con, movimientoComprobanteCobroModificar, cobro);
					}
					if("COFI".equals(movimientoComprobanteCobroModificar.IdCobro)) {
						movimientoComprobanteCobroModificar = obtenerNuevosMontosComisionFija(con, movimientoComprobanteCobroModificar, cobro);
					}
					return movimientoComprobanteCobroModificar;
				})
				.collect(Collectors.toList());
		
		movimientosComprobanteCobroModificar = recalcularMontoCobros(con, movimientosComprobanteCobroModificar, cobrosAgrupados);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerMovimientosComprobanteCobroModificar", fin - inicio);
		
		return movimientosComprobanteCobroModificar;
	}
	
	private String obtenerLlaveCobro(Cobro cobro) {
		logger.trace("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerLlaveCobro");
		long inicio = System.currentTimeMillis();
		
		String llave = cobro.IdCobro + "_" + cobro.IdMonedaCobro + "_" + cobro.IdMonedaCobroLiquidacion + "_" + cobro.IdMonedaMovimientoComprobanteCobro;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerLlaveCobro", fin - inicio);
		return llave;
	}
	
	private String obtenerLlaveCobro(MovimientoComprobanteCobro movimientoComprobanteCobro) {
		logger.trace("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerLlaveCobro");
		long inicio = System.currentTimeMillis();
		
		String llave = movimientoComprobanteCobro.IdCobro + "_" + movimientoComprobanteCobro.IdMonedaCobro + "_" + movimientoComprobanteCobro.IdMonedaLiquidacionCobro + "_" + movimientoComprobanteCobro.IdMonedaMovimientoComprobanteCobro;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerLlaveCobro", fin - inicio);
		return llave;
	}
	
	private MovimientoComprobanteCobroModificar obtenerNuevosMontosComisionVariable(Connection con, MovimientoComprobanteCobroModificar movimientoComprobanteCobroModificar, Cobro cobro) {
		logger.trace("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerNuevosMontosComisionVariable");
		long inicio = System.currentTimeMillis();
		
		movimientoComprobanteCobroModificar.Porcentaje = cobro.Porcentaje;
		BigDecimal porcentajeReal = movimientoComprobanteCobroModificar.Porcentaje.divide(new BigDecimal(100), MathContext.DECIMAL128);
		movimientoComprobanteCobroModificar.MontoCobro = movimientoComprobanteCobroModificar.Monto.multiply(porcentajeReal, MathContext.DECIMAL128);
		movimientoComprobanteCobroModificar.MontoLiquidacionCobro = movimientoComprobanteCobroModificar.MontoLiquidacion.multiply(porcentajeReal, MathContext.DECIMAL128);
		movimientoComprobanteCobroModificar.MontoMovimientoComprobanteCobro = movimientoComprobanteCobroModificar.MontoMovimientoComprobante.multiply(porcentajeReal, MathContext.DECIMAL128);
		
		try {
			movimientoComprobanteCobroModificar.MontoCobro = MonedaFormato.formatear(con, movimientoComprobanteCobroModificar.MontoCobro, MonedaFormato.MONTO, movimientoComprobanteCobroModificar.IdMonedaCobro).valor;
			movimientoComprobanteCobroModificar.MontoLiquidacionCobro = MonedaFormato.formatear(con, movimientoComprobanteCobroModificar.MontoLiquidacionCobro, MonedaFormato.MONTO, movimientoComprobanteCobroModificar.IdMonedaLiquidacionCobro).valor;
			movimientoComprobanteCobroModificar.MontoMovimientoComprobanteCobro = MonedaFormato.formatear(con, movimientoComprobanteCobroModificar.MontoMovimientoComprobanteCobro, MonedaFormato.MONTO, movimientoComprobanteCobroModificar.IdMonedaMovimientoComprobanteCobro).valor;
		} catch(Exception e) {
			logger.trace("ERROR::ModificarMovimientoComprobanteCobrosMasivo.obtenerNuevosMontosComisionVariable", e);
		}
		
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerNuevosMontosComisionVariable", fin - inicio);
		return movimientoComprobanteCobroModificar;
	}
	
	private MovimientoComprobanteCobroModificar obtenerNuevosMontosComisionFija(Connection con, MovimientoComprobanteCobroModificar movimientoComprobanteCobroModificar, Cobro cobro) {
		logger.trace("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerNuevosMontosComisionFija");
		long inicio = System.currentTimeMillis();
		
		movimientoComprobanteCobroModificar.Porcentaje = BigDecimal.ZERO;
		BigDecimal montoProporcional = movimientoComprobanteCobroModificar.MontoCobroAnterior.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO.multiply(cobro.MontoCobro, MathContext.DECIMAL128) :  movimientoComprobanteCobroModificar.MontoCobroAnterior.multiply(cobro.MontoCobro, MathContext.DECIMAL128);
		movimientoComprobanteCobroModificar.MontoCobro = (montoProporcional.compareTo(BigDecimal.ZERO)== 0 && cobro.MontoCobroAnterior.compareTo(BigDecimal.ZERO)== 0 ) ? BigDecimal.ZERO : montoProporcional.divide(cobro.MontoCobroAnterior, MathContext.DECIMAL128);
		try {
			movimientoComprobanteCobroModificar.MontoCobro = MonedaFormato.formatear(con, movimientoComprobanteCobroModificar.MontoCobro, MonedaFormato.MONTO, movimientoComprobanteCobroModificar.IdMonedaCobro).valor;
		} catch(Exception e) {
			logger.trace("ERROR::ModificarMovimientoComprobanteCobrosMasivo.obtenerNuevosMontosComisionFija", e);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerNuevosMontosComisionFija", fin - inicio);
		return movimientoComprobanteCobroModificar;
	}
	
	private List<MovimientoComprobanteCobroModificar> recalcularMontoCobros(Connection con, List<MovimientoComprobanteCobroModificar> movimientosComprobanteCobroModificar, Map<String, Cobro> cobrosAgrupados) throws Exception{
		logger.trace("INICIO::ModificarMovimientoComprobanteCobrosMasivo.recalcularMontoCobros");
		long inicio = System.currentTimeMillis();
		
		Map<String, List<MovimientoComprobanteCobroModificar>> movimientosComprobanteCobroModificarAgrupados = movimientosComprobanteCobroModificar.stream()
				.collect(Collectors.groupingBy(movimientoComprobanteCobroModificar->obtenerLlaveCobro(movimientoComprobanteCobroModificar)));
		
		for(Entry<String, List<MovimientoComprobanteCobroModificar>> movimientosComprobanteCobroPorId : movimientosComprobanteCobroModificarAgrupados.entrySet()) {
			BigDecimal montoCobroMovimientos = movimientosComprobanteCobroPorId.getValue().stream()
					.map(movimientoComprobanteCobro->movimientoComprobanteCobro.MontoCobro)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			Cobro cobro = cobrosAgrupados.get(obtenerLlaveCobro(movimientosComprobanteCobroPorId.getValue().get(0)));
			
			if(cobro.MontoCobro.compareTo(montoCobroMovimientos) != 0) {
				movimientosComprobanteCobroPorId.getValue().get(movimientosComprobanteCobroPorId.getValue().size() -1).MontoCobro = movimientosComprobanteCobroPorId.getValue().get(movimientosComprobanteCobroPorId.getValue().size() -1).MontoCobro.add(cobro.MontoCobro.subtract(montoCobroMovimientos, MathContext.DECIMAL128));
				
				if("COVA".equals(cobro.IdCobro)) {
					BigDecimal montoCobroLiquidacionMovimientos = movimientosComprobanteCobroPorId.getValue().stream()
							.map(movimientoComprobanteCobro->movimientoComprobanteCobro.MontoLiquidacionCobro)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					BigDecimal montoCobroComprobanteMovimientos = movimientosComprobanteCobroPorId.getValue().stream()
							.map(movimientoComprobanteCobro->movimientoComprobanteCobro.MontoMovimientoComprobante)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					movimientosComprobanteCobroPorId.getValue().get(movimientosComprobanteCobroPorId.getValue().size() -1).MontoLiquidacionCobro = movimientosComprobanteCobroPorId.getValue().get(movimientosComprobanteCobroPorId.getValue().size() -1).MontoLiquidacionCobro.add(cobro.MontoLiquidacionCobro.subtract(montoCobroLiquidacionMovimientos, MathContext.DECIMAL128));
					movimientosComprobanteCobroPorId.getValue().get(movimientosComprobanteCobroPorId.getValue().size() -1).MontoMovimientoComprobante = movimientosComprobanteCobroPorId.getValue().get(movimientosComprobanteCobroPorId.getValue().size() -1).MontoMovimientoComprobante.add(cobro.MontoMovimientoComprobanteCobro.subtract(montoCobroComprobanteMovimientos, MathContext.DECIMAL128));
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarMovimientoComprobanteCobrosMasivo.recalcularMontoCobros", fin - inicio);
		return movimientosComprobanteCobroModificar;
	}
	
	private List<MovimientoComprobanteCobroModificar> calcularParidadMovimientos(Connection con, List<MovimientoComprobanteCobroModificar> movimientosComprobanteCobroModificar, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.calcularParidadMovimientos");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.FechaSistema");
		
		CalcularParidadEntrada entrada = new CalcularParidadEntrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdTrabajo = 0L;
		
		List<MovimientoComprobanteCobroModificar> movimientosComprobanteCobroModificarCOFI = movimientosComprobanteCobroModificar.stream()
				.filter(movimientoComprobanteCobroModificar->"COFI".equals(movimientoComprobanteCobroModificar.IdCobro))
				.collect(Collectors.toList());
		
		
		for(MovimientoComprobanteCobroModificar movimientoComprobanteCobroModificar : movimientosComprobanteCobroModificarCOFI) {
			CalculoEntrada calculoEntradaMontoLiquidacion = new CalculoEntrada();
			calculoEntradaMontoLiquidacion.Fecha = fechaSistema;
			calculoEntradaMontoLiquidacion.IdInterno = movimientoComprobanteCobroModificar.IdMovimientoComprobanteCobro;
			calculoEntradaMontoLiquidacion.IdMonedaOrigen = movimientoComprobanteCobroModificar.IdMonedaCobro;
			calculoEntradaMontoLiquidacion.IdMonedaDestino = movimientoComprobanteCobroModificar.IdMonedaLiquidacionCobro;
			calculoEntradaMontoLiquidacion.IdTrabajo = 0L;
			calculoEntradaMontoLiquidacion.IdUso = "";
			calculoEntradaMontoLiquidacion.MontoOrigen = movimientoComprobanteCobroModificar.MontoCobro;
			calculoEntradaMontoLiquidacion.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			CalculoEntrada calculoEntradaMontoComprobante = new CalculoEntrada();
			calculoEntradaMontoComprobante.Fecha = fechaSistema;
			calculoEntradaMontoComprobante.IdInterno = movimientoComprobanteCobroModificar.IdMovimientoComprobanteCobro * -1;
			calculoEntradaMontoComprobante.IdMonedaOrigen = movimientoComprobanteCobroModificar.IdMonedaCobro;
			calculoEntradaMontoComprobante.IdMonedaDestino = movimientoComprobanteCobroModificar.IdMonedaMovimientoComprobanteCobro;
			calculoEntradaMontoComprobante.IdTrabajo = 0L;
			calculoEntradaMontoComprobante.IdUso = "";
			calculoEntradaMontoComprobante.MontoOrigen = movimientoComprobanteCobroModificar.MontoCobro;
			calculoEntradaMontoComprobante.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(calculoEntradaMontoLiquidacion);
			entrada.p_CalculosEntrada.add(calculoEntradaMontoComprobante);
		}
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, entrada);
		
		Map<Long, CalculoSalida> calculosParidadAgrupados = calcularParidadSalida.p_out_CalculosSalida.stream()
				.collect(Collectors.toMap(calculoParidad->calculoParidad.IdInterno, calculoParidad->calculoParidad));
		
		for(MovimientoComprobanteCobroModificar movimientoComprobanteCobroModificar : movimientosComprobanteCobroModificar) {
			CalculoSalida calculoSalidaLiquidacion = calculosParidadAgrupados.get(movimientoComprobanteCobroModificar.IdMovimientoComprobanteCobro);
			CalculoSalida calculoSalidaComprobante = calculosParidadAgrupados.get(movimientoComprobanteCobroModificar.IdMovimientoComprobanteCobro * -1);
			
			if(calculoSalidaLiquidacion != null) {
				movimientoComprobanteCobroModificar.MontoLiquidacionCobro = MonedaFormato.formatear(con, calculoSalidaLiquidacion.MontoDestino, MonedaFormato.MONTO, movimientoComprobanteCobroModificar.IdMonedaLiquidacionCobro).valor;
			}
			
			if(calculoSalidaComprobante != null) {
				movimientoComprobanteCobroModificar.MontoMovimientoComprobanteCobro = MonedaFormato.formatear(con, calculoSalidaComprobante.MontoDestino, MonedaFormato.MONTO, movimientoComprobanteCobroModificar.IdMonedaMovimientoComprobanteCobro).valor;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.calcularParidadMovimientos", fin - inicio);
		return movimientosComprobanteCobroModificar;
	}
	
	private void actualizarMovimientoComprobanteCobros(Connection con, List<MovimientoComprobanteCobroModificar> movimientosComprobanteCobroModificar) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.actualizarMovimientoComprobanteCobros");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_COBROS, movimientosComprobanteCobroModificar, MovimientoComprobanteCobroModificar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.actualizarMovimientoComprobanteCobros", fin - inicio); 
	}
	
	private List<CobroParaImpuestos> obtenerCobrosParaImpuestos(Connection con, List<MovimientoComprobante> movimientosComprobante) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerCobrosParaImpuestos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsMovimientoComprobante = movimientosComprobante.stream()
				.map(movimientoComprobante->movimientoComprobante.IdMovimientoComprobante)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdMovimientoComprobante", idsMovimientoComprobante);
		
		SQL sql = new SQL();
		List<CobroParaImpuestos> cobrosParaImpuestos = sql.query(con, SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS_PARA_IMPUESTOS, parametros, CobroParaImpuestos.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerCobrosParaImpuestos", fin - inicio);
		return cobrosParaImpuestos;
	}
	
	private List<Impuesto> obtenerImpuestos(Connection con, List<MovimientoComprobante> movimientosComprobante) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.obtenerImpuestos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsMovimientoComprobante = movimientosComprobante.stream()
				.map(movimientoComprobante->movimientoComprobante.IdMovimientoComprobante)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdMovimientoComprobante", idsMovimientoComprobante);
		
		SQL sql = new SQL();
		List<Impuesto> impuestos = sql.query(con, SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS_IMPUESTOS, parametros, Impuesto.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.obtenerImpuestos", fin - inicio);
		return impuestos;
	}
	
	private List<Impuesto> calcularImpuestos(Connection con, List<CobroParaImpuestos> cobrosParaImpuestos, List<Impuesto> impuestos) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.calcularImpuestos");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<CobroParaImpuestos>> cobrosParaImpuestosAgrupados = cobrosParaImpuestos.stream()
				.collect(Collectors.groupingBy(cobroParaImpuesto->cobroParaImpuesto.IdMovimientoComprobante));
		
		for(Impuesto impuesto : impuestos) {
			BigDecimal porcentaje = impuesto.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128);
			impuesto.MontoCobro = BigDecimal.ZERO;
			
			List<CobroParaImpuestos> cobrosParaImpuestosMovimiento = cobrosParaImpuestosAgrupados.get(impuesto.IdMovimientoComprobante);
			if(cobrosParaImpuestosMovimiento != null) {
				BigDecimal montoCobroMovimientos = cobrosParaImpuestosMovimiento.stream()
						.map(cobro->cobro.MontoMovimientoComprobanteCobro)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				impuesto.MontoCobro = montoCobroMovimientos.multiply(porcentaje, MathContext.DECIMAL128);
				impuesto.MontoCobro = MonedaFormato.formatear(con, impuesto.MontoCobro, MonedaFormato.MONTO, impuesto.IdMonedaCobro).valor;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.calcularImpuestos", fin - inicio);
		return impuestos;
	}
	
	private void actualizarImpuestos(Connection con, List<Impuesto> impuestos) throws Exception {
		logger.debug("INICIO::ModificarMovimientoComprobanteCobrosMasivo.actualizarImpuestos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_COBROS_IMPUESTOS, impuestos, Impuesto.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarMovimientoComprobanteCobrosMasivo.actualizarImpuestos", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();					
			    				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCobro\":\"COFI\",\"p_MovimientosComprobante\":[{\"IdMovimientoComprobante\":143285},{\"IdMovimientoComprobante\":143284}],\"p_Cobros\":[{\"IdInterno\":1,\"IdCobro\":\"COFI\",\"IdMonedaCobro\":\"CLP\",\"IdMonedaCobroLiquidacion\":\"CLP\",\"IdMonedaMovimientoComprobanteCobro\":\"CLP\",\"MontoCobro\":12000,\"MontoCobroAnterior\":10000,\"Porcentaje\":0}]}";

				EntradaModificarMovimientoComprobanteCobrosMasivo Objin = gson.fromJson(json, EntradaModificarMovimientoComprobanteCobrosMasivo.class);
				System.out.println(Objin.toString());
				ModificarMovimientoComprobanteCobrosMasivo modificarMovimientoComprobanteCobrosMasivo = new ModificarMovimientoComprobanteCobrosMasivo();
				SalidaModificarMovimientoComprobanteCobrosMasivo Objout = modificarMovimientoComprobanteCobrosMasivo.logica(con, Objin);				

				System.out.println("____Salida_____");
				System.out.println(Objout);
				con.rollback();
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		}
	}

}
