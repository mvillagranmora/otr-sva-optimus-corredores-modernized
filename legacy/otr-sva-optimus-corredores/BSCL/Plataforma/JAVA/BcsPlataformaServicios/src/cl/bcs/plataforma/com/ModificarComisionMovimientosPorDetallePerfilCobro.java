package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar;
import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar.EntradaConsultarMovimientoComprobanteCobrosModificar;
import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar.MovimientoComprobante;
import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar.MovimientoComprobanteCobrosModificar;
import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar.SalidaConsultarMovimientoComprobanteCobrosModificar;
import cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo;
import cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.Cobro;
import cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.EntradaModificarMovimientoComprobanteCobrosMasivo;
import cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.SalidaModificarMovimientoComprobanteCobrosMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public class ModificarComisionMovimientosPorDetallePerfilCobro implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarComisionMovimientosPorDetallePerfilCobro.class);
	
	private final String SQL_SELECT_DETALLE_PERFIL_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_SelectDetallePerfilCobros.sql");
	private final String SQL_SELECT_TEMPORAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_SelectTemporal.sql");
	private final String SQL_CREATE_TABLA_TEMPORAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_CrearTablaTemporal.sql");
	private final String SQL_DELETE_TABLA_TEMPORAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_BorrarTablaTemporal.sql");
	private final String SQL_DROP_TEMPORAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_DropTablaTemporal.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_InsertarATablaTemporal.sql");
	private final String SQL_SELECT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_SelectMovimientosPorFacturarFiltrandoPorTablaTemporal.sql");
	private final String SQL_SELECT_EXISTENCIAS_MOVIMIENTOS_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_SelectMovimientosComprobantesCobros.sql");
	private final String SQL_INSERT_MOVIMIENTO_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarComisionMovimientosPorDetallePerfilCobro_InsertMovimientosComprobantesCobros.sql");
	
	
	public static class EntradaModificarComisionMovimientosPorDetallePerfilCobro extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<DetallePerfilCobro> p_IdDetallePerfilCobros;
		
		public EntradaModificarComisionMovimientosPorDetallePerfilCobro() {
			this.p_IdDetallePerfilCobros = new ArrayList<>();
		}
	}
	
	public static class DetallePerfilCobro extends BcsPojo {
		public Long IdDetallePerfilCobros;
	}
	
	public static class SalidaModificarComisionMovimientosPorDetallePerfilCobro extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarComisionMovimientosPorDetallePerfilCobro() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class DetallePerfilCobros extends BcsPojo{
		public Long IdDetallePerfilCobros;
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public Long IdPortafolio;
		public String IdCobro;
		public String IdMoneda;
		public BigDecimal ValorCobro;
		public String Borrado;
		
		public DetallePerfilCobros() {
			this.IdDetallePerfilCobros = 0L;
			this.IdBolsa= "";
			this.IdTipoProducto = "";
			this.IdTipoOperacion = "";
			this.IdTipoCanalIngreso = "";
			this.IdPortafolio = 0L;
			this.IdCobro= "";
			this.IdMoneda="";
			this.ValorCobro=BigDecimal.ZERO;
			this.Borrado = "N";
		}
	}
	
	public static class MovimientoComprobanteCobro extends BcsPojo {
		public Long IdMovimientoComprobanteCobro;
		public Long IdMovimientoComprobante;
		public Long IdDetallePerfilCobros;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public String IdBolsa;
		public String IdMonedaCobro;
		public String IdMonedaLiquidacionCobro;
		public String IdMonedaMovimientoComprobanteCobro;
		public BigDecimal Porcentaje;
		public BigDecimal MontoCobro;
		public BigDecimal MontoLiquidacionCobro;
		public BigDecimal MontoMovimientoComprobanteCobro;
		public BigDecimal Monto;
		public BigDecimal MontoMovimientoComprobante;
		public BigDecimal MontoLiquidacion;
	}
	
	public static class EntidadInsertCobro extends BcsPojo {
		public Long IdMovimientoComprobanteCobro;
		public String IdCobro;
		public Long IdMovimientoComprobante;
		public BigDecimal Porcentaje;
		public String FechaSistema;
		public String FechaIngresoRegistro;
		public String IdEstado;
		public BigDecimal MontoMovimientoComprobanteCobro;
		public BigDecimal MontoCobro;
		public String IdMonedaCobro;
		public String IdMonedaLiquidacionCobro;
		public String IdMonedaMovimientoComprobanteCobro;
		public BigDecimal MontoLiquidacionCobro;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoMovimientoComprobante;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarComisionMovimientosPorDetallePerfilCobro in = gson.fromJson(jin.toString(), EntradaModificarComisionMovimientosPorDetallePerfilCobro.class);
		SalidaModificarComisionMovimientosPorDetallePerfilCobro out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaModificarComisionMovimientosPorDetallePerfilCobro logica(Connection conn, EntradaModificarComisionMovimientosPorDetallePerfilCobro entrada) throws Exception{
		logger.debug("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarComisionMovimientosPorDetallePerfilCobro salida = new SalidaModificarComisionMovimientosPorDetallePerfilCobro();
		
		List<DetallePerfilCobros> detallesPerfilCobros = obtenerDatosDetallePerfilCobros(conn, entrada.p_IdDetallePerfilCobros);
		
		if(detallesPerfilCobros.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PERFIL_COBROS_NO_EXISTE", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream().filter( m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count(); 
			return salida; 
		}
		
		SalidaModificarMovimientoComprobanteCobrosMasivo salidaModificar = new SalidaModificarMovimientoComprobanteCobrosMasivo();
		
		salidaModificar = modificarMovimientosPorFacturar(conn, detallesPerfilCobros, entrada);
		
		if(salidaModificar.P_OUT_ERRORESCONTADOR == 0) {
			MensajeSalida mensaje = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_MODIFICACION_INGRESO_EXITO", new HashMap<String, String>(), conn, entrada.p_IdEmpresa);
			salida.P_OUT_MENSAJES.add(mensaje);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro", fin - inicio);
		return salida;
	}

	private SalidaModificarMovimientoComprobanteCobrosMasivo modificarMovimientosPorFacturar(Connection conn, List<DetallePerfilCobros> detallesPerfilCobros, EntradaModificarComisionMovimientosPorDetallePerfilCobro entrada) throws Exception {
		logger.debug("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerMovimientosPorfacturar");
		long inicio = System.currentTimeMillis();

		SalidaModificarMovimientoComprobanteCobrosMasivo salidaModificar = new SalidaModificarMovimientoComprobanteCobrosMasivo();
		generarTablaTemporal(conn, detallesPerfilCobros);

		String	fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		SQL sql = new SQL();
		List<DetallePerfilCobros> detalles = sql.query(conn, SQL_SELECT_TEMPORAL, parametros, DetallePerfilCobros.class);
		
		for( DetallePerfilCobros detalle : detalles){
			crearComprobanteCobrosFaltantes(conn, detalle, fechaSistema);
		};
		
		List<MovimientoComprobanteCobro> movimientosPorFacturar = sql.query(conn, SQL_SELECT_TABLA_TEMPORAL, parametros, MovimientoComprobanteCobro.class);
		dropTablaTemporal(conn);
		
		for( DetallePerfilCobros detalle : detalles){
			crearComprobanteCobrosFaltantes(conn, detalle, fechaSistema);
			
			List<MovimientoComprobanteCobro> movimientosComprobanteCobro = movimientosPorFacturar.parallelStream().filter(mov -> mov.IdDetallePerfilCobros.equals(detalle.IdDetallePerfilCobros)).collect(Collectors.toList());
			
			movimientosComprobanteCobro = filtrarMovimientosPorCanalIngresoYTipoOperacion(movimientosComprobanteCobro, detalle);
			
			List<MovimientoComprobante> comprobantes = movimientosComprobanteCobro.parallelStream().map(mov -> {
				MovimientoComprobante movimientoComprobante = new MovimientoComprobante();
				movimientoComprobante.IdMovimientoComprobante = mov.IdMovimientoComprobante;
				return movimientoComprobante;
			}).collect(Collectors.toList());
			
			
			List<MovimientoComprobanteCobrosModificar> movimientosComprobanteCobrosModificar = obtenerConsultarMovimientoComprobanteCobrosModificar(conn, entrada, detalle.IdCobro, comprobantes);
			
			Long cantidadComprobantes = comprobantes.parallelStream().count();
			BigDecimal montoCobroDetalle = detalle.ValorCobro.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : detalle.ValorCobro;
			List<Cobro> cobros = new ArrayList<Cobro>();
			if(detalle.IdCobro.equals("COVA")) {
				cobros = obtenerCobrosAModificarComisionVariable(conn, movimientosComprobanteCobrosModificar, detalle.ValorCobro);				
			}
			if(detalle.IdCobro.equals("COFI")) {
				cobros = obtenerCobrosAModificarComisionFija(conn, movimientosComprobanteCobrosModificar, cantidadComprobantes, montoCobroDetalle);
			}
			
			salidaModificar = modificarMovimientoComprobanteCobrosMasivo(conn, entrada, comprobantes, cobros, detalle.IdCobro);
			
			if(salidaModificar.P_OUT_ERRORESCONTADOR>0) {
				return salidaModificar;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerMovimientosPorfacturar", fin - inicio);
		return salidaModificar;
	}

	private void crearComprobantesCobros(Connection conn, DetallePerfilCobros detalle, List<MovimientoComprobanteCobro> movimientosCobros, String fechaSistema) throws Exception {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.crearComprobantesCobros");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<EntidadInsertCobro> entidadesInsert = new ArrayList<>();
		List<MovimientoComprobanteCobro> noExisten = movimientosCobros.stream().filter(mov -> mov.IdMovimientoComprobanteCobro.equals(0L)).collect(Collectors.toList());
		
		if(!noExisten.isEmpty()) {
			for(MovimientoComprobanteCobro comprobante : noExisten) {
				BigDecimal montoCobro = detalle.IdCobro.equals("COFI") ? determinarMontoCobroComisionFija(conn,detalle, comprobante ) : determinarMontoCobroComisionVariable(conn,detalle, comprobante );
				
				EntidadInsertCobro entidadInsert = new EntidadInsertCobro();
				entidadInsert.IdCobro = detalle.IdCobro;
				entidadInsert.IdMovimientoComprobante = comprobante.IdMovimientoComprobante;
				entidadInsert.IdEstado = "VIG";
				entidadInsert.FechaSistema = fechaSistema;
				entidadInsert.FechaIngresoRegistro = fechaSistema;
				entidadInsert.IdMonedaCobro = comprobante.IdMonedaCobro;
				entidadInsert.IdMonedaLiquidacionCobro = comprobante.IdMonedaLiquidacionCobro;
				entidadInsert.IdMonedaMovimientoComprobanteCobro = comprobante.IdMonedaMovimientoComprobanteCobro;
				entidadInsert.Porcentaje = comprobante.Porcentaje;
				entidadInsert.Monto = comprobante.Monto;
				entidadInsert.MontoCobro = montoCobro;
				entidadInsert.MontoLiquidacion = comprobante.MontoLiquidacion;
				entidadInsert.MontoLiquidacionCobro =montoCobro;
				entidadInsert.MontoMovimientoComprobante = comprobante.MontoMovimientoComprobante;
				entidadInsert.MontoMovimientoComprobanteCobro = montoCobro;
				entidadesInsert.add(entidadInsert);
			}
			
			sql.batch(conn, SQL_INSERT_MOVIMIENTO_COBROS, entidadesInsert, EntidadInsertCobro.class);
			
		}
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.crearComprobantesCobros", fin - inicio);
	}

	private BigDecimal determinarMontoCobroComisionVariable(Connection conn, DetallePerfilCobros detalle, MovimientoComprobanteCobro comprobante) {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.determinarMontoCobroComisionVariable");
		long inicio = System.currentTimeMillis();
		
		BigDecimal MontoCobro = BigDecimal.ZERO;

		BigDecimal porcentajeReal = detalle.ValorCobro.divide(new BigDecimal(100), MathContext.DECIMAL128);
		MontoCobro = comprobante.Monto.multiply(porcentajeReal, MathContext.DECIMAL128);

		try {
			MontoCobro = MonedaFormato.formatear(conn, MontoCobro, MonedaFormato.MONTO, comprobante.IdMonedaCobro).valor;

		} catch (Exception e) {
			logger.trace("ERROR::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", e);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.determinarMontoCobroComisionVariable", fin - inicio);
		return MontoCobro;
	}

	private BigDecimal determinarMontoCobroComisionFija(Connection conn, DetallePerfilCobros detalle, MovimientoComprobanteCobro comprobante) {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.determinarMontoCobroComisionFija");
		long inicio = System.currentTimeMillis();
		
		BigDecimal MontoCobro = BigDecimal.ZERO;
		MontoCobro = detalle.ValorCobro;
		try {
			MontoCobro = MonedaFormato.formatear(conn, MontoCobro, MonedaFormato.MONTO, comprobante.IdMonedaCobro).valor;
		} catch (Exception e) {
			logger.trace("ERROR::ModificarComisionMovimientosPorDetallePerfilCobro.determinarMontoCobroComisionFija", e);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.determinarMontoCobroComisionFija", fin - inicio);
		return MontoCobro;
	}

	private void crearComprobanteCobrosFaltantes(Connection conn, DetallePerfilCobros detalle, String fechaSistema) throws Exception {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.debeCrearComprobanteCobros");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("idCobro", detalle.IdCobro);
		parametros.put("idPortafolio", detalle.IdPortafolio);
		parametros.put("idBolsa", detalle.IdBolsa);
		parametros.put("idTipoProducto", detalle.IdTipoProducto);

		SQL sql = new SQL();
		
		List<MovimientoComprobanteCobro> movimientosCobros = sql.query(conn, SQL_SELECT_EXISTENCIAS_MOVIMIENTOS_COBROS, parametros, MovimientoComprobanteCobro.class);
		
		movimientosCobros = filtrarMovimientosPorCanalIngresoYTipoOperacion(movimientosCobros, detalle);
		
		crearComprobantesCobros(conn, detalle, movimientosCobros, fechaSistema);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.debeCrearComprobanteCobros", fin - inicio);
	}

	public List<MovimientoComprobanteCobro> filtrarMovimientosPorCanalIngresoYTipoOperacion(List<MovimientoComprobanteCobro> movimientos, DetallePerfilCobros detalle) {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.filtrarMovimientosPorCanalIngresoYTipoOperacion");
		long inicio = System.currentTimeMillis();

		if(!detalle.IdTipoCanalIngreso.equals("0")) {
			movimientos = movimientos.stream().filter(mov -> mov.IdTipoCanalIngreso.equals(detalle.IdTipoCanalIngreso)).collect(Collectors.toList());
		}
		
		if(!detalle.IdTipoOperacion.equals("0")) {
			movimientos = movimientos.stream().filter(mov -> mov.IdTipoOperacion.equals(detalle.IdTipoOperacion)).collect(Collectors.toList());
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.filtrarMovimientosPorCanalIngresoYTipoOperacion", fin - inicio);
		return movimientos;
	}

	public SalidaModificarMovimientoComprobanteCobrosMasivo modificarMovimientoComprobanteCobrosMasivo(Connection conn, EntradaModificarComisionMovimientosPorDetallePerfilCobro entrada, List<MovimientoComprobante> comprobantesConsultar, List<Cobro> cobros, String idCobro)  throws Exception {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.modificarMovimientoComprobanteCobrosMasivo");
		long inicio = System.currentTimeMillis();
		
		ModificarMovimientoComprobanteCobrosMasivo modificarMovimientoComprobanteCobrosMasivo = new ModificarMovimientoComprobanteCobrosMasivo();
		
		List<cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.MovimientoComprobante> comprobantes = comprobantesConsultar.stream().map(comp ->{
			cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.MovimientoComprobante comprobante = new cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.MovimientoComprobante();
			comprobante.IdMovimientoComprobante = comp.IdMovimientoComprobante;
			return comprobante;
		}).collect(Collectors.toList());
		
		EntradaModificarMovimientoComprobanteCobrosMasivo entradaModificarMovimientoComprobanteCobrosMasivo = new EntradaModificarMovimientoComprobanteCobrosMasivo();
		entradaModificarMovimientoComprobanteCobrosMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarMovimientoComprobanteCobrosMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarMovimientoComprobanteCobrosMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaModificarMovimientoComprobanteCobrosMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaModificarMovimientoComprobanteCobrosMasivo.p_IdCobro = idCobro;
		entradaModificarMovimientoComprobanteCobrosMasivo.p_MovimientosComprobante = comprobantes;
		entradaModificarMovimientoComprobanteCobrosMasivo.p_Cobros = cobros;
		
		SalidaModificarMovimientoComprobanteCobrosMasivo salidaModificarMovimientoComprobanteCobrosMasivo = modificarMovimientoComprobanteCobrosMasivo.logica(conn, entradaModificarMovimientoComprobanteCobrosMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerMovimientosPorfacturar", fin - inicio);
		return salidaModificarMovimientoComprobanteCobrosMasivo;
	}

	public List<Cobro> obtenerCobrosAModificarComisionVariable(Connection conn, List<MovimientoComprobanteCobrosModificar> movimientos, BigDecimal valorCobro) {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerCobrosAModificarComisionVariable");
		long inicio = System.currentTimeMillis();
		
		Long index = 1L;
		List<Cobro> cobros = new ArrayList<Cobro>();
		BigDecimal porcentajeReal = valorCobro.divide(new BigDecimal(100), MathContext.DECIMAL128);
		
		for(MovimientoComprobanteCobrosModificar mov : movimientos) {
			Cobro cobro = new Cobro();
			cobro.IdInterno = index;
			cobro.IdCobro = mov.IdTipoCobro;
			cobro.IdMonedaCobro = mov.IdMonedaCobro;
			cobro.IdMonedaCobroLiquidacion = mov.IdMonedaCobroLiquidacion;
			cobro.IdMonedaMovimientoComprobanteCobro = mov.IdMonedaCobroSistema;
			cobro.MontoCobro = mov.MontoOperacion.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : mov.MontoOperacion.multiply(porcentajeReal, MathContext.DECIMAL128);
			cobro.MontoCobroAnterior = mov.MontoCobroOriginal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : mov.MontoCobroOriginal;
			cobro.Porcentaje = valorCobro;
			index++;
			cobros.add(cobro);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerCobrosAModificarComisionVariable", fin - inicio);
		return cobros;
	}

	public List<Cobro> obtenerCobrosAModificarComisionFija(Connection conn, List<MovimientoComprobanteCobrosModificar> movimientos, Long cantidadComprobantes, BigDecimal montoCobroDetalle) {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerCobrosAModificarComisionFija");
		long inicio = System.currentTimeMillis();
		
		Long index = 1L;
		List<Cobro> cobros = new ArrayList<Cobro>();
		
		for(MovimientoComprobanteCobrosModificar mov : movimientos) {
			Cobro cobro = new Cobro();
			cobro.IdInterno = index;
			cobro.IdCobro = mov.IdTipoCobro;
			cobro.IdMonedaCobro = mov.IdMonedaCobro;
			cobro.IdMonedaCobroLiquidacion = mov.IdMonedaCobroLiquidacion;
			cobro.IdMonedaMovimientoComprobanteCobro = mov.IdMonedaCobroSistema;
			cobro.MontoCobro =  montoCobroDetalle.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : montoCobroDetalle.multiply(BigDecimal.valueOf(cantidadComprobantes), MathContext.DECIMAL128);
			cobro.MontoCobroAnterior = mov.MontoCobroOriginal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : mov.MontoCobroOriginal;
			cobro.Porcentaje =  BigDecimal.ZERO;
			index++;
			cobros.add(cobro);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerCobrosAModificarComisionFija", fin - inicio);
		return cobros;
	}

	public List<MovimientoComprobanteCobrosModificar> obtenerConsultarMovimientoComprobanteCobrosModificar(Connection conn, EntradaModificarComisionMovimientosPorDetallePerfilCobro entrada, String idCobro, List<MovimientoComprobante> comprobantes) throws Exception {
		logger.trace("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerConsultarMovimientoComprobanteCobrosModificar");
		long inicio = System.currentTimeMillis();
		
		ConsultarMovimientoComprobanteCobrosModificar consultarMovimientoComprobante = new ConsultarMovimientoComprobanteCobrosModificar();
		
		EntradaConsultarMovimientoComprobanteCobrosModificar entradaConsultar = new EntradaConsultarMovimientoComprobanteCobrosModificar();
		entradaConsultar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultar.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultar.p_IdModulo = entrada.p_IdModulo;
		entradaConsultar.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConsultar.p_IdCobro = idCobro;
		entradaConsultar.p_MovimientosComprobante = comprobantes;
		
		SalidaConsultarMovimientoComprobanteCobrosModificar salidaCOVA = consultarMovimientoComprobante.logica(conn, entradaConsultar);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerConsultarMovimientoComprobanteCobrosModificar", fin - inicio);
		return salidaCOVA.P_OUT_COBROS;
	}

	private List<DetallePerfilCobros> obtenerDatosDetallePerfilCobros(Connection conn, List<DetallePerfilCobro> IdDetallePerfilCobros) throws Exception {
		logger.debug("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		List<Long> idDetallePerfilCobros = IdDetallePerfilCobros.stream()
				.map(detalle ->detalle.IdDetallePerfilCobros)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("IdDetallePerfilCobros", idDetallePerfilCobros);
		
		SQL sql = new SQL();
		List<DetallePerfilCobros> detalles = sql.query(conn, SQL_SELECT_DETALLE_PERFIL_COBROS, parametros, DetallePerfilCobros.class).parallelStream().filter(detalle -> detalle.IdCobro.equals("COVA") || detalle.IdCobro.equals("COFI") ).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros", fin - inicio);
		return detalles;
	}

	public void generarTablaTemporal(Connection conn, List<DetallePerfilCobros> detallesPerfilCobros) throws Exception {
		logger.info("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.generarTablaTemporal");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		SQL.exec(conn, SQL_CREATE_TABLA_TEMPORAL);
		
		sql.batch(conn, SQL_DELETE_TABLA_TEMPORAL, detallesPerfilCobros, DetallePerfilCobros.class);
		
		sql.batch(conn, SQL_INSERT_TABLA_TEMPORAL, detallesPerfilCobros, DetallePerfilCobros.class);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		List<DetallePerfilCobros> detalle = sql.query(conn, SQL_SELECT_TEMPORAL, parametros, DetallePerfilCobros.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.generarTablaTemporal", finServicio - inicioServicio);
	}
	public void dropTablaTemporal(Connection conn) throws Exception {
		logger.info("INICIO::ModificarComisionMovimientosPorDetallePerfilCobro.dropTablaTemporal");
		long inicioServicio = System.currentTimeMillis();
		List<DetallePerfilCobros> param = new ArrayList<>();
		SQL sql = new SQL();
		sql.batch(conn, SQL_DROP_TEMPORAL, param, DetallePerfilCobros.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComisionMovimientosPorDetallePerfilCobro.dropTablaTemporal", finServicio - inicioServicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();					
			    			
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\r\n"
						+ "\"p_IdDetallePerfilCobros\":[ { \"IdDetallePerfilCobros\" : 	132261	} ,\r\n"
						+ "{ \"IdDetallePerfilCobros\" : 	131087	} ,\r\n"
						+ "{ \"IdDetallePerfilCobros\" : 	131632	} ,\r\n"
						+ "{ \"IdDetallePerfilCobros\" : 	131633	} ,\r\n"
						+ "{ \"IdDetallePerfilCobros\" : 	132299	} ,\r\n"
						+ "{ \"IdDetallePerfilCobros\" : 	132298	} ]}";
				String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":208,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetallePerfilCobros\":[{ \"IdDetallePerfilCobros\" : 	132297	} ]}";
				
				EntradaModificarComisionMovimientosPorDetallePerfilCobro Objin = gson.fromJson(json2, EntradaModificarComisionMovimientosPorDetallePerfilCobro.class);
				System.out.println(Objin.toString());
				ModificarComisionMovimientosPorDetallePerfilCobro modificarMovimientoComprobanteCobrosMasivo = new ModificarComisionMovimientosPorDetallePerfilCobro();
				SalidaModificarComisionMovimientosPorDetallePerfilCobro Objout = modificarMovimientoComprobanteCobrosMasivo.logica(con, Objin);				

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
