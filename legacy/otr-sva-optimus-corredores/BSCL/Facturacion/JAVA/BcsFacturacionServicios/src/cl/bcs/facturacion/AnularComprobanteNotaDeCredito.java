package cl.bcs.facturacion;


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.facturacion.CrearComprobante.MovimientoNoLiquidadoTesoreria;
import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.ordenes.CambiarEstadoAsignaciones;
import cl.bcs.ordenes.CambiarEstadoAsignaciones.CambiarEstadoAsignaciones_Entrada;
import cl.bcs.ordenes.CambiarEstadoAsignaciones.CambiarEstadoAsignaciones_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Proceso;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularComprobanteNotaDeCredito implements JavaCall {
	private final String SQL_SELECT_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectComprobante.sql");
	private final String SQL_SELECT_COMPROBANTESIM = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectComprobanteSim.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectMovimientoComprobante.sql");
	private final String SQL_SELECT_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectTesoreria.sql");
	private final String SQL_SELECT_MOVIMIENTOS_NO_LIQUIDADOS_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosNoLiquidadosTesoreria.sql");
	private final String SQL_UPDATE_INGRESO_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateIngresoMovimientos.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCuentaInversion.sql");
	private final String SQL_SELECT_CUSTODIA_TRANSITOS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectTransitos.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectCarterasCompra.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_VENTAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectCarterasVenta.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_NEGATIVAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectCarterasNegativas.sql");
	private final String SQL_UPDATE_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateComprobante.sql");
	private final String SQL_UPDATE_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateComprobanteDetalle.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateMovimientoComprobanteDetalle.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateMovimientoComprobante.sql");
    
	private final String SQL_SELECT_PERIODO_CIERRE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectPeriodoCierre.sql");
	private final String SQL_SELECT_SPOT_ANULADO_FFMM	= loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectSpotFFMM.sql");

	private final String SQL_UPDATE_MOVIMIENTO_TRANSITO=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UPD_MovimientoCarteraEnTransito.sql");
	private final String SQL_UPDATE_CARTERA=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UPD_Cartera.sql");
	
	
	private final String SQL_SELECT_ASIGNACION=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectAsignacion.sql");
	private final String SQL_UPDATE_COMPROBANTE_FACTURA=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateComprobanteFactura.sql");
	private final String SQL_SELECT_COMPROBANTE_TESORERIA=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectComprobanteTesoreria.sql");
	private final String SQL_UPDATE_CARTERAS_NEGATIVAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateCarterasNegativas.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectEventoCapital.sql");
	
	
	
	ConstantesFacturacion _c = new ConstantesFacturacion();
	// Log
	private static final Logger logger = LogManager.getLogger(AnularComprobanteNotaDeCredito.class);
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		AnularComprobanteNotaDeCredito_Entrada in = gson.fromJson(jin.toString(), AnularComprobanteNotaDeCredito_Entrada.class);
		AnularComprobanteNotaDeCredito_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	public static class Comprobante_Proceso extends Comprobante {
		public long ok;
		public long errores;
		public long autorizaciones;
		public long advertencias;
		
		public String periodoLiquidacion;

		public List<CargoAbono_Proceso> cargosAbonos;
		public List<EntidadTesoreria_Proceso> entidadesTesoreria;
		public List<MovimientoCarteraEnTransito_Proceso> movsCarteraTransito;
		public List<Cartera_Proceso> carteras;
		public List<Subyacente_Proceso> subyacentes;
		
		public Comprobante_Proceso() {
			super();
			cargosAbonos = new ArrayList<>();
			entidadesTesoreria = new ArrayList<>();
			movsCarteraTransito = new ArrayList<>();
			carteras = new ArrayList<>();
			subyacentes = new ArrayList<>();
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	private void ProcesaRequisito(Connection conn,Long idEmpresa,boolean esError,String codMsg,String codRequisito,Comprobante_Proceso cmbte,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		if (esError) {
	
			String msg = Diccionario.getMensaje(conn, idEmpresa,codMsg)
					.add("IdComprobante", cmbte.IdComprobante)
					.msg();
			
			if (codRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				cmbte.errores++;
			}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				cmbte.autorizaciones++;						
			}
			else if (codRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				cmbte.advertencias++;
			}
			else {
				cmbte.ok++;
			}
		}
		else {
			cmbte.ok++;
		}
	}
	
	public void limpiaComprobantes(List<Comprobante_Proceso> cmbtes,List<Comprobante_Proceso> cmbtesError,List<ResultadoProceso_Salida> ress) {
		
		for (Comprobante_Proceso cmbte : cmbtes) {
			if (cmbte.errores > 0) {
				cmbtesError.add(cmbte);
				
				ResultadoProceso_Salida res = new ResultadoProceso_Salida();
				res.Campo = "IdComprobante";
				res.Id = cmbte.IdComprobante;
				res.Resultado = MensajeSalida.MENSAJE_ERROR;
				res.Servicio = "AnularComprobante.java";
				ress.add(res);
				
			}
		}
		
		for (Comprobante_Proceso cmbte : cmbtesError) {
			if (cmbtes.contains(cmbte)) {
				cmbtes.remove(cmbte);
			}
		}		
	}
	
	public static class CargoAbono_Proceso {
		public Long IdComprobante;
		public String IdTipoComprobante;
		public Long IdCargoAbonoMovimiento;
		public String NombreTipoMovimiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EntidadTesoreria_Proceso {
		
		public Long IdComprobanteOrigen;
		public Long Id;
		public String Entidad;
		public String NombreTipoMovimiento;
		public String IdEstado;
		public String IdEstadoComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoCarteraEnTransito_Proceso {
		
		public Long IdMovimientoCarteraEnTransito;
		public String IdEstado;
		public Long FolioComprobante;
		public String EsNominal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Cartera_Proceso {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioComprobante;
		public String IdTipoComprobante;
		public String IdModulo;
		public Long FolioCartera;
		public String IdEstado;
		public String IdTipoOperacion;
		public Long IdCartera;
		public Long IdGarantia;
		public Long PlazoEnDiasOperacion;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		public String IdConcepto;
		public BigDecimal TasaCompra;
		
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal INGCantidad;
		public BigDecimal INGPrecioCompra;
		public BigDecimal EGRCantidad;
		public Long EGRFolioComprobante;
		public String TipoCartera;
		public Long IdInstrumento;
		public BigDecimal ValorRescate;
		public BigDecimal INGValorRescate;
		public BigDecimal EGRValorRescate;
		public String EsFungible;
		public Date INGFechaLiquidacion;
		
		public String IdOrigenGarantia;
		public Date FechaVencimientoInstrumento;

		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;
		
		public String IdCondicionLiquidacion;
		public BigDecimal MontoCompraMonedaInstrumento;
		public String IdBoveda;
		public String IdTipoProducto;
		public Long IdAgente;
		public String IdMonedaInstrumento;
		public Long IdSucursal;
		public String Glosa;
		public String IdTipoCanalIngreso;
		public String NemoSVS;
		public BigDecimal CorteMinimo;
		public String FolioExternoReferencia;
		public String IdClasificacionTipoInstrumento;
		public String EGRIdTipoOperacion;
		public String EGRIdConcepto; 
		public String EGRIdTipoComprobante;
		
		public BigDecimal INGMontoATasaCompraActual = BigDecimal.ZERO;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Subyacente_Proceso {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioComprobante;
		public String IdTipoComprobante;
		public String IdModulo;
		public Long FolioCartera;
		public String IdEstado;
		public String IdTipoOperacion;
		public String IdTipoOperacionAnulacion;
		public Long IdCartera;
		public Long IdSubyacente;
		public Long FolioSubyacente;
		public String IdConcepto;
		public Long EGRFolioComprobante;
		
		public Long FolioCarteraRelacionada;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public String IdEstadoSubyacente;
		public Long IdInstrumento;
		public String IdEmisor;
		public String Nemotecnico;
		public String TipoCartera;
		public Long IdCarteraRetirada;
		public Long FolioCarteraRetirada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public Subyacente_Proceso() {
			this.IdCarteraRetirada = 0L;
			this.FolioCarteraRetirada = 0L;
		}
	}
	public static class PeriodoCierre {
		public String estadoPeriodo;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	public static class OperacionSpot {
		public Long IdOperacionSpot;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	public static class ComprobanteTesoreria {
		public Long IdComprobanteTesoreria;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}		 
	public static class AsignacionComprobante_Proceso {
		public Long NumeroMvtoCmbte;
		public Long IdAsignacion;
		public Long IdComprobante;
		public String IdEstadoAsignacion;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EventoCapital_Proceso {
		public Long IdComprobanteFac;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	// LOGICA
	// ------------------------------------------------------------------------------
	@Trace
	public AnularComprobanteNotaDeCredito_Salida logica(Connection conn, AnularComprobanteNotaDeCredito_Entrada in) throws Exception {
		logger.info("Inicio Anular Comprobante: " + in.toString());
		AnularComprobanteNotaDeCredito_Salida out = new AnularComprobanteNotaDeCredito_Salida();
		SQL _sql = new SQL();
		
		if (in.P_COMPROBANTEAANULAR.size() == 0) {
			String codigo = "NO_VIENEN_COMPROBANTES";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Comprobante_Proceso> comprobantes = new ArrayList<>();
		List<Comprobante_Proceso> comprobantesConError = new ArrayList<>();
		List<Comprobante_Proceso> comprobantesFactura = new ArrayList<>();

		List<Cartera_Proceso> carterasCompra = new ArrayList<>();
		List<Cartera_Proceso> carterasVenta = new ArrayList<>();
		List<Cartera_Proceso> carterasNegativas = new ArrayList<>();

		
		HashMap<Long,Object> hashComprobantes = new HashMap<>();
		HashMap<Long,Object> hashComprobantesProceso = new HashMap<>();
		HashMap<Long,Object> hashComprobantesProcesoFactura = new HashMap<>();
		Map<Long, Date> dataMapIdFecha = new HashMap<>();
		Map<Long, Long> dataComprobantesaReferencia = new HashMap<>();
		Map<Long, String> dataComprobantesAsignacion = new HashMap<>();
		
		{
			HashMap<String,Object> params = new HashMap<>();

			
			Set<Long> idsComprobante = new HashSet<>();

			for (ComprobanteNC_Entrada cmbte : in.P_COMPROBANTEAANULAR) {
				if (!idsComprobante.contains(cmbte.IdComprobante)) {
					idsComprobante.add(cmbte.IdComprobante);	
				}
				
			}
			params.put("idsComprobante", new ArrayList<>(idsComprobante));
			List<Comprobante> _comprobantes = _sql.query(conn, SQL_SELECT_COMPROBANTE, params , Comprobante.class);			
			//eliminar comprobantes duplicados (por si se seleccionan todos los comprobantes de un reporte)
			Set<Long> idsComprobantesNoRepetidos = new HashSet<>();
			_comprobantes = _comprobantes.stream().filter(comp -> idsComprobantesNoRepetidos.add(comp.IdComprobante)).collect(Collectors.toList());
			
			hashComprobantes = Utils.List2Hash(_comprobantes, "IdComprobante");
			_comprobantes.forEach(_cmp -> idsComprobante.add(_cmp.IdComprobante));
			params.put("idsComprobante", new ArrayList<>(idsComprobante));
			

			
			_comprobantes.forEach(_cmp -> dataMapIdFecha.put(_cmp.IdComprobante,new Date(_cmp.FechaComprobante.getTime())));
			_comprobantes.forEach(_cmp -> dataComprobantesaReferencia.put(_cmp.IdComprobante,_cmp.IdComprobanteReferencia));
	
			//Logica para no insertar idscomprobante repetidos
			List<Long> idsComprobanteNoRepetido = new ArrayList<>();
			List<Long> idsComprobantesObtenidos = new ArrayList<>();
			idsComprobantesObtenidos.addAll(idsComprobante);
			for (Comprobante _comprobante : _comprobantes) {
				
				List<Comprobante> _comprobantesSim = new ArrayList<>();
				if(_comprobante.IdTipoInstrumento.equalsIgnoreCase("SIM") || _comprobante.IdTipoInstrumento.equalsIgnoreCase("RRV") || "S".equals(_comprobante.Contrapartida)){
					_comprobantesSim = _sql.query(conn, SQL_SELECT_COMPROBANTESIM, params , Comprobante.class);					
					
					for (Comprobante _cmprobanteSim : _comprobantesSim) {
						if (_cmprobanteSim.IdComprobante != null) {
							idsComprobantesObtenidos.add(_cmprobanteSim.IdComprobante);
							hashComprobantes.put(_cmprobanteSim.IdComprobante, _cmprobanteSim);
						}
					}
				}
			}
				
			List<Long> idsComprobantesOriginales = new ArrayList<>();
			for (ComprobanteNC_Entrada cmbte : in.P_COMPROBANTEAANULAR) {
				idsComprobantesOriginales.add(cmbte.IdComprobante);
			}
			
			List<Long> idsComprobantesTotales = new ArrayList<>();
			idsComprobantesTotales.addAll(idsComprobantesOriginales);
			idsComprobantesTotales.addAll(idsComprobantesObtenidos);
			
			idsComprobanteNoRepetido = idsComprobantesTotales.stream().distinct().collect(Collectors.toList());

			for (Long idComprobanteNoRepetido : idsComprobanteNoRepetido) {
				long cantidad = IntStream.range(0, idsComprobantesOriginales.size()).filter(i -> idComprobanteNoRepetido.equals(idsComprobantesOriginales.get(i))).count();
				if (cantidad == 0) {
					ComprobanteNC_Entrada entradaNueva = new ComprobanteNC_Entrada();
					entradaNueva.IdComprobante = idComprobanteNoRepetido;
					in.P_COMPROBANTEAANULAR.add(entradaNueva);
				}
			}
		}
		
				
		for (ComprobanteNC_Entrada cmbte : in.P_COMPROBANTEAANULAR ) {
			Comprobante_Proceso c = new Comprobante_Proceso();
			Utils.copyPojo(cmbte, c);
			c.IdComprobante = cmbte.IdComprobante;
			comprobantes.add(c);
			hashComprobantesProceso.put(c.IdComprobante, c);
		}

		
		
		//Validaciones basicas
		for (Comprobante_Proceso cmbte : comprobantes ) {
			Comprobante infoComprobante = (Comprobante) hashComprobantes.get(cmbte.IdComprobante);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoComprobante == null, "COMPROBANTE_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
		}
		
		
		limpiaComprobantes(comprobantes, comprobantesConError, out.p_out_ResultadoProceso);
		if (comprobantes.size() == 0) {
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}

		/** Buscare el periodo que corresponde la fecha actual del sistema **/
		String periodo = Fecha.toFormat(Fecha.toDate(fechaSistema),"MM-YYYY");
		HashMap<String,Object> periodoCierre = new HashMap<>();
		periodoCierre.put("fechaPeriodo",periodo);
		List<PeriodoCierre> periodoCierreEstado = _sql.query(conn, SQL_SELECT_PERIODO_CIERRE, periodoCierre,PeriodoCierre.class);
		
		//para buscar los movs asociados a los comprobantes
		List<Long> idsComprobante = new ArrayList<>();
		List<Long> idsComprobanteReferencia = new ArrayList<>();
		for (Comprobante_Proceso cmbte : comprobantes ) {
			idsComprobante.add(cmbte.IdComprobante);
		}
		/*ASIGNACION*/
		List<AsignacionComprobante_Proceso> mvtoComprobantesNC = null;
		HashMap<String,Object> paramAsignacion = new HashMap<>();
		paramAsignacion.put("idsComprobante",idsComprobante);
		mvtoComprobantesNC = _sql.query(conn, SQL_SELECT_ASIGNACION, paramAsignacion, AsignacionComprobante_Proceso.class);
		mvtoComprobantesNC.forEach(_cmp ->{
											if(_cmp.IdEstadoAsignacion!=null) {
												dataComprobantesAsignacion.put(_cmp.IdComprobante,_cmp.IdEstadoAsignacion);
											}
			
		} );
		
		for (Long valorIdComprobanteReferencia : dataComprobantesaReferencia.values()) {		
			idsComprobanteReferencia.add(valorIdComprobanteReferencia);
			Comprobante_Proceso d = new Comprobante_Proceso();
			//Utils.copyPojo(cmbte, c);
			d.IdComprobante = valorIdComprobanteReferencia;
			comprobantesFactura.add(d);
			Comprobante_Proceso c = new Comprobante_Proceso();
			c.IdComprobante=valorIdComprobanteReferencia;
			hashComprobantesProcesoFactura.put(c.IdComprobante, c);
		}
		
		
		List<EventoCapital_Proceso> listEventoCapital = null;
		List<Long> idsConEventoCapital = new ArrayList<>(); ;
		HashMap<String,Object> paramEventoCapital = new HashMap<>();
		paramEventoCapital.put("idsComprobante",idsComprobanteReferencia);
		listEventoCapital = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, paramEventoCapital, EventoCapital_Proceso.class);
		listEventoCapital.forEach(_cmp ->{
											if(_cmp.IdComprobanteFac!=null) {
												idsConEventoCapital.add(_cmp.IdComprobanteFac);
											}
			
		} );
		
		/*ASIGNACION*/
		//Validaciones basicas
		for (Comprobante_Proceso cmbte : comprobantes ) {
			Comprobante infoComprobante = (Comprobante) hashComprobantes.get(cmbte.IdComprobante);
			cmbte.periodoLiquidacion = Fecha.toFormat(infoComprobante.FechaLiquidacion, "MM-YYYY");
			//ProcesaRequisito(conn, in.p_IdEmpresa, _c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante) && !_c.tiposAnticipoSimultanea.contains(infoComprobante.IdTipoOperacion), "CMBTE_TIPO_COMPROBANTE_ANULAR_NOTA_CREDITO", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") && !infoComprobante.IdEstadoComprobante.equalsIgnoreCase("FAC") && !infoComprobante.IdEstadoComprobante.equalsIgnoreCase("ENV"), "CMBTE_NO_ESTA_FACTURADO", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") && infoComprobante.IdEstadoComprobante.equalsIgnoreCase("ENV"), "CMBTE_ENVIADO_AL_DTE", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("PE") && infoComprobante.IdTipoComprobante.equalsIgnoreCase("POL") && infoComprobante.IdEstadoComprobante.equalsIgnoreCase("ANU"), "CMBTE_ANULADO", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);			
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("PE") && !infoComprobante.IdTipoComprobante.equalsIgnoreCase("POL") && !infoComprobante.IdEstadoComprobante.equalsIgnoreCase("FAC"), "CMBTE_ANULADO", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			
			ProcesaRequisito(conn,in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("PE") && infoComprobante.FechaLiquidacion.compareTo(Fecha.toDate(fechaSistema))<=0,"FECHA_LIQ_MAYOR_IGUAL",MensajeSalida.MENSAJE_ERROR,cmbte,out.P_OUT_MENSAJES);
			ProcesaRequisito(conn,in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("PE") && cmbte.periodoLiquidacion.equalsIgnoreCase(periodo) && !periodoCierreEstado.isEmpty() && periodoCierreEstado.get(0).estadoPeriodo.equalsIgnoreCase("CER"),"CMBTE_PERIODO_CERRADO",MensajeSalida.MENSAJE_ERROR,cmbte,out.P_OUT_MENSAJES);
			
			if("FMM".equalsIgnoreCase(infoComprobante.IdTipoCanalIngreso) && ("VENSPT".equalsIgnoreCase(infoComprobante.IdTipoOperacion) || "COMSPT".equalsIgnoreCase(infoComprobante.IdTipoOperacion))) {
				HashMap<String,Object> paramCmbte = new HashMap<>();
				paramCmbte.put("idComprobante",cmbte.IdComprobante.toString());
				List<OperacionSpot> IdOperacionSpot = _sql.query(conn, SQL_SELECT_SPOT_ANULADO_FFMM,paramCmbte, OperacionSpot.class);
				if(IdOperacionSpot.size() == 0)
					ProcesaRequisito(conn, in.p_IdEmpresa, infoComprobante.IdTipoProducto.equalsIgnoreCase("MEME") && in.p_EsIngresoAutomatico.equalsIgnoreCase("N"), "MVTO_CMBTE_SPOT_NO_ANULA", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			}
			//dataMapIdFecha 
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") && (_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante)|| infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEE")||infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEO")) && infoComprobante.IdEstadoSII.equalsIgnoreCase("ENV"), "CMBTE_ENVIADO_AL_SII", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") && (_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante)|| infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEE")||infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEO")) && (infoComprobante.IdTipoProducto.equalsIgnoreCase("INSI") ), "CMBTE_EVCA_INSI", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") && 
					(_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante)|| 
							infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEE")||
							infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEO")) 
					&& (idsConEventoCapital.contains(dataComprobantesaReferencia.get(infoComprobante.IdComprobante))), "CMBTE_EVCA_INSI", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);

			ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") && (_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante)|| infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEE")||infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEO")) && dataMapIdFecha.get(cmbte.IdComprobante).compareTo(Fecha.toDate(fechaSistema))!=0,"CMBTE_FECHA_INV", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);			
			if(dataComprobantesAsignacion.containsKey(cmbte.IdComprobante)) {
				ProcesaRequisito(conn, in.p_IdEmpresa, localizacionSistema.equalsIgnoreCase("CL") &&(_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante)|| infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEE")||infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEO")) && dataComprobantesAsignacion.get(cmbte.IdComprobante).equalsIgnoreCase("EAF"),"ASIG_ENVIADA_FACTURAR", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			}
			if((_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante)|| 
					infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEE")||
					infoComprobante.IdTipoComprobante.equalsIgnoreCase("NEO"))
					&&
					(!infoComprobante.IdTipoProducto.equalsIgnoreCase("EVCA")||
							!infoComprobante.IdTipoProducto.equalsIgnoreCase("INSI") )
				) {
				
				HashMap<String,Object> paramCmbteTesoreria = new HashMap<>();
				paramCmbteTesoreria.put("idComprobante",cmbte.IdComprobante.toString());
				List<ComprobanteTesoreria> IdsComprobanteTesoreria = _sql.query(conn, SQL_SELECT_COMPROBANTE_TESORERIA,paramCmbteTesoreria, ComprobanteTesoreria.class);				
				if(IdsComprobanteTesoreria.size()>0) {
						ComprobanteTesoreria comprobanteTes = IdsComprobanteTesoreria.get(0);
					    String msgBase = Diccionario.getMensaje(conn, in.p_IdEmpresa, "MVTO_CMBTE_TESORERIA_NO_ANULA")
					                                .add("IdComprobante", cmbte.IdComprobante)
					                                .msg();
					    String msgFinal = msgBase + " N° " + comprobanteTes.IdComprobanteTesoreria;
					    MensajeSalida mensaje = new MensajeSalida(cmbte.IdComprobante,
					                                              MensajeSalida.MENSAJE_ERROR,
					                                              "MVTO_CMBTE_TESORERIA_NO_ANULA",
					                                              msgFinal);
					    out.P_OUT_MENSAJES.add(mensaje);
					    cmbte.errores++;
				}
				
			}
			
		}
		
		
		

		
		
		//buscamos cargos y abonos
		{
			HashMap<String,Object> params = new HashMap<>();
			params.put("idsComprobante",idsComprobante);
			List<CargoAbono_Proceso> cargosAbonos = _sql.query(conn, SQL_SELECT_CUENTA_INVERSION, params, CargoAbono_Proceso.class);
			
			cargosAbonos = cargosAbonos.stream().filter(ca -> idsComprobante.contains(ca.IdComprobante)).collect(Collectors.toList());
			
			if (cargosAbonos.size() > 0) {
				
				AnularCargoAbono_Entrada entradaAnularCargoAbono = new AnularCargoAbono_Entrada();
				List<CargoAbono_Entrada> lista = new ArrayList<>();
				for (CargoAbono_Proceso cabon : cargosAbonos) {
					
					Comprobante_Proceso cmbte = (Comprobante_Proceso) hashComprobantesProceso.get(cabon.IdComprobante);
					if (cmbte != null) {
						cmbte.cargosAbonos.add(cabon);
					}
					
					CargoAbono_Entrada arg0 = new CargoAbono_Entrada();
					arg0.IdCargoAbonoMovimiento = cabon.IdCargoAbonoMovimiento;
					arg0.GlosaAnulacion = in.p_Glosa;
					lista.add(arg0);
				}
				
				entradaAnularCargoAbono.p_CargoAbonoAAnular = lista ;
				//entradaAnularCargoAbono.p_EjecutaAutorizacion = in.p_EjecutaAutorizacion;
				//entradaAnularCargoAbono.p_OmiteRegistroSinAutorizar = in.p_OmiteRegistroSinAutorizar;
				entradaAnularCargoAbono.p_EjecutaAutorizacion = "N";
				entradaAnularCargoAbono.p_OmiteRegistroSinAutorizar = "N";
				entradaAnularCargoAbono.p_IdModulo = in.p_IdModulo;
				entradaAnularCargoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaAnularCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
				entradaAnularCargoAbono.p_simularOperacion = in.p_simularOperacion;
				entradaAnularCargoAbono.p_UsaReglasNegocio = "S";
				entradaAnularCargoAbono.p_FechaMovimiento = fechaSistema;
				entradaAnularCargoAbono.p_EsAnulacionManual = "N";
				
				AnularCargoAbono anularCargoAbonoService = new AnularCargoAbono();
				AnularCargoAbono_Salida salidaAnularCargoAbono = anularCargoAbonoService.logica(conn, entradaAnularCargoAbono);
				logger.debug("Salida AnularCargoAbono: " + salidaAnularCargoAbono.toString());
				out.P_OUT_MENSAJES.addAll(salidaAnularCargoAbono.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(out);
					return out;
				}
				
			}
			
		}
		
		//OBTIENE ENTIDADES PARA ANULAR EN TESORERIA
		{
			//EN EL CASO QUE NO SE ENCUENTRE LIQUIDADA, BUSCAR MOVIMIENTO DE LA FACTURA POR idsComprobanteReferencia Y UPDATEAR EL MOVIMIENTO DE TESORERÍA DE ANULADO A INGRESADO
			HashMap<String,Object> paramsMovNoLiquidado = new HashMap<>();
			paramsMovNoLiquidado.put("idsComprobanteOrigen",idsComprobanteReferencia); 
			List<MovimientoNoLiquidadoTesoreria> movimientosTesoreriaNoLiquidados = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_NO_LIQUIDADOS_TESORERIA, paramsMovNoLiquidado, MovimientoNoLiquidadoTesoreria.class);					
			   
			if (!movimientosTesoreriaNoLiquidados.isEmpty()) {
			    _sql.batch(conn,SQL_UPDATE_INGRESO_MOVIMIENTOS,movimientosTesoreriaNoLiquidados,MovimientoNoLiquidadoTesoreria.class);
			}
				
			HashMap<String,Object> params = new HashMap<>();
			params.put("idsComprobante",idsComprobante);
			List<EntidadTesoreria_Proceso> entidadesTesoreria = _sql.query(conn, SQL_SELECT_TESORERIA, params, EntidadTesoreria_Proceso.class);
			
			if (entidadesTesoreria.size() > 0) {
				
				AnularRegistros_Entrada entradaAnularTesoreria = new AnularRegistros_Entrada();
				entradaAnularTesoreria.p_EjecutaAutorizacion = "N";
				entradaAnularTesoreria.p_FechaMovimiento = fechaSistema;
				entradaAnularTesoreria.p_IdEmpresa = in.p_IdEmpresa;
				entradaAnularTesoreria.p_IdModulo = in.p_IdModulo;
				entradaAnularTesoreria.p_IdUsuario = in.p_IdUsuario;
				List<Registro_Entrada> lista = new ArrayList<>();
				for (EntidadTesoreria_Proceso ent : entidadesTesoreria) {
					
					Comprobante_Proceso cmbte = (Comprobante_Proceso) hashComprobantesProceso.get(ent.IdComprobanteOrigen);
					if (cmbte != null) {
						cmbte.entidadesTesoreria.add(ent);
					}
					
					//Comprueba que el comprobante no este liquidado para continuar con la anulacion					
					if(!("LIQ").equalsIgnoreCase(ent.IdEstadoComprobante)){
						Registro_Entrada arg0 = new Registro_Entrada();
						arg0.Entidad = ent.Entidad;
						arg0.Id = ent.Id;
						lista.add(arg0 );
					}
					else {
						out.P_OUT_MENSAJES.add(new MensajeSalida(ent.IdComprobanteOrigen,MensajeSalida.MENSAJE_ERROR,"COM_TEST_LIQ","El comprobante " + ent.Id + " se encuentra liquidado."));
					}
					
				}
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					List<MensajeSalida> ListMensajeSalida= new ArrayList<MensajeSalida>();
					
					for(MensajeSalida msn: out.P_OUT_MENSAJES) {
						if(msn.tipo.equals("ERROR")) {
							ListMensajeSalida.add(msn);							
						}
					}
					out.P_OUT_MENSAJES.clear();
					out.P_OUT_MENSAJES.addAll(ListMensajeSalida);					
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return out;
				}
				
				entradaAnularTesoreria.p_RegistrosAProcesar = lista ;
				entradaAnularTesoreria.p_simularOperacion = in.p_simularOperacion;
				entradaAnularTesoreria.p_UsaReglasNegocio = "S";
				
				AnularRegistros anularTesoreriaService = new AnularRegistros();
				AnularRegistros_Salida salidaAnularTesoreria = anularTesoreriaService.logica(conn, entradaAnularTesoreria);
				logger.debug("Salida AnularRegistrosTesoreria: " + salidaAnularTesoreria.toString());
				
				out.P_OUT_MENSAJES.addAll(salidaAnularTesoreria.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(out);
					return out;
				}
			}
			
		}
		
		//OBTIENE TRANSITOS DE CUSTODIA
		{

			HashMap<String,Object> params = new HashMap<>();
			params.put("idsComprobante",idsComprobanteReferencia);
			List<MovimientoCarteraEnTransito_Proceso> transitos = _sql.query(conn, SQL_SELECT_CUSTODIA_TRANSITOS, params, MovimientoCarteraEnTransito_Proceso.class);
			
			List<ObjMovimientosAActualizar> movimientosCarteraEnTransito = new ArrayList<>();
			List<ObjMovimientosAActualizar> movimientosCarteraEnTransitoNominales = new ArrayList<>();
			String NominalCertificaAutomatico = "N";
			try {
				NominalCertificaAutomatico = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "NominalCertificaAutomaticoTransaccion");
			}catch(Exception e) {
					
			};
			
			for (MovimientoCarteraEnTransito_Proceso transito : transitos) {
				
				Comprobante_Proceso cmbte = (Comprobante_Proceso) hashComprobantesProceso.get(transito.FolioComprobante);
				if (cmbte != null) {
					cmbte.movsCarteraTransito.add(transito);
				}
				
				ObjMovimientosAActualizar arg0 = new ObjMovimientosAActualizar();
				arg0.EsAnulacion = "N";
				arg0.EsCertificacion = "S";
				arg0.IdMovimientoCarteraEnTransito = transito.IdMovimientoCarteraEnTransito;
				arg0.IdUsuario = in.p_IdUsuario;
				if(transito.EsNominal.equalsIgnoreCase("S") && NominalCertificaAutomatico.equalsIgnoreCase("N")) {
					movimientosCarteraEnTransitoNominales.add(arg0);
				}else {
					movimientosCarteraEnTransito.add(arg0);
				}
				
			}
			if (movimientosCarteraEnTransito.size() > 0 && in.p_simularOperacion.equalsIgnoreCase("N")) {
				boolean flag= GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, movimientosCarteraEnTransito );
				if(flag) {
					for(ObjMovimientosAActualizar objList:movimientosCarteraEnTransito ) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(objList.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_OK,"COMPROBANTE_ACTUALIZADO","El movimiento en transito "+objList.IdMovimientoCarteraEnTransito+" fue actualizado con éxito"));
					}
					
				}
			}
			
			if (movimientosCarteraEnTransitoNominales.size() > 0 && in.p_simularOperacion.equalsIgnoreCase("N")) {	
				boolean flag= ActualizarTransitoNominales(conn, movimientosCarteraEnTransitoNominales );
				if(flag) {
					for(ObjMovimientosAActualizar objList:movimientosCarteraEnTransitoNominales ) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(objList.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_AUTORIZACION,"COMPROBANTE_PENDIENTE","El movimiento en transito "+objList.IdMovimientoCarteraEnTransito+" se encuentra pendiente de autorización"));
					}
					
				}
			}
		}
		 
		//OBTIENE REGISTROS PARA REVIVIR EN CUSTODIA
		{
			HashMap<String,Object> params = new HashMap<>();
			params.put("idsComprobante",idsComprobanteReferencia);
			carterasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, params, Cartera_Proceso.class);
			carterasVenta = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_VENTAS, params, Cartera_Proceso.class);
			carterasNegativas = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_NEGATIVAS, params, Cartera_Proceso.class);

			if (carterasVenta.size() > 0) {
				carterasVenta.removeIf(c -> !(idsComprobante.contains(c.EGRFolioComprobante) || "VIG".equals(c.IdEstado))
											&& ("COMSIM".equals(c.IdTipoOperacion) || "VENSIM".equals(c.IdTipoOperacion)));

			}
			
			HashMap<Long,List<AnulacionCompraNormalCustodia>> hashComprobantesAnulacionCompras = new HashMap<>();
			HashMap<Long,List<AnulacionVentaNormalCustodia>> hashComprobantesAnulacionVentas = new HashMap<>();
			HashMap<Long,List<AnulacionVentaSimultanea>> hashComprobantesAnulacionSimultanea = new HashMap<>();

			HashMap<Long,RetiroCustodia.ObjDatosEntrada> carterasAnular = new HashMap<Long,RetiroCustodia.ObjDatosEntrada>();
			HashMap<Long,IngresoCustodia.ObjDatosEntrada> carterasIngresos = new HashMap<Long,IngresoCustodia.ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> carterasRetiro = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			List<IngresoCustodia.ObjDatosEntrada> carterasReingreso = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			HashMap<Long,Long> folioCarteraComprobante = new HashMap<Long,Long>();
			
			//Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
			
			//VALIDAMOS LAS CARTERAS ANTES DE ANULAR NOTA DE CREDITO
			Map<Long, List<Cartera_Proceso>> carterasCompraAgrupadasPorIdComprobante = carterasCompra.stream().collect(Collectors.groupingBy(cartera->cartera.FolioComprobante));
			for (Comprobante_Proceso cmbte : comprobantes) {
				List<Cartera_Proceso> carterasCompraComprobante = carterasCompraAgrupadasPorIdComprobante.get(cmbte.IdComprobante);
				if(carterasCompraComprobante != null) {
					for (Cartera_Proceso cartera : carterasCompraComprobante) {
						ProcesaRequisito(conn, in.p_IdEmpresa, "VIG".equalsIgnoreCase(cartera.IdEstado), "ANULAR_COMPROBANTE_CARTERA_VIGENTE", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
						//ProcesaRequisito(conn, in.p_IdEmpresa, "VIG".equalsIgnoreCase(cartera.IdEstado) && cartera.Cantidad.compareTo(cartera.INGCantidad) != 0, "ANULAR_COMPROBANTE_CARTERA_SALDO_INFUCIENTE", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
					}
				}
			}
			
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
				
				out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
				out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				logger.info("Fin");
				return out;
			}
			
			
			//ACTUALIZACION DE CUSTODIA
			//carterasCompra.addAll(garantiaCompra);

			
			if (carterasCompra.size()>0) {
				boolean flag= ActualizarCarteraCompra(conn, carterasCompra);
				if(flag) {
					for(Cartera_Proceso objList:carterasCompra ) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(objList.FolioCartera,MensajeSalida.MENSAJE_OK,"CARTERA_ACTUALIZADA","Cartera "+objList.TipoCartera+" creada, Folio: "+objList.FolioCartera));
					}
					
				}
			}
			

	
			for (Cartera_Proceso cartera : carterasVenta) {
				Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProcesoFactura.get(cartera.EGRFolioComprobante);
				if (info != null) {
					info.carteras.add(cartera);
				}
				if (cartera.IdEstado.equalsIgnoreCase("RPA") || cartera.IdEstado.equalsIgnoreCase("LIQ") || cartera.IdEstado.equalsIgnoreCase("ANT") ) {
					
						RetiroCustodia.ObjDatosEntrada anulacionCartera = null;
						BigDecimal restoSaldoCarteraVigenteParcial=BigDecimal.ZERO;
						BigDecimal restoSaldoCarteraVigenteParcialIF=BigDecimal.ZERO;
						BigDecimal restoSaldoCarteraVigenteTotal=BigDecimal.ZERO;
						BigDecimal restoSaldoCarteraVigenteTotalIF=BigDecimal.ZERO;
						//Boolean carteranegativa=false;
						if ("RPA".equalsIgnoreCase(cartera.IdEstado)||"ANT".equalsIgnoreCase(cartera.IdEstado) ||"LIQ".equalsIgnoreCase(cartera.IdEstado)) {
							//BUSCAMOS LA VIG PARA ANULARLA
							Cartera_Proceso carteraVigente = null;
							Cartera_Proceso carteraVigenteParcial = null;
							Cartera_Proceso carteraVigenteTotal = null;
							for (Cartera_Proceso carteraVig : carterasVenta) {
								
								if(carteraVig.IdEstado.equalsIgnoreCase("VIG") && 
										carteraVig.FolioCartera.compareTo(cartera.FolioCartera) == 0 ) {
									int cmp = cantidadORescate(carteraVig, cartera);
						            boolean esIFVig = "IF".equalsIgnoreCase(carteraVig.IdClasificacionTipoInstrumento);

						            if (cmp == 0) { // exacto: a=b
						            	carteraVigente = carteraVig;
						                break;
						            } else if (cmp < 0) { // parcial: VIG < egreso
						                carteraVigenteParcial = carteraVig;
						                if (esIFVig) {
						                	restoSaldoCarteraVigenteParcialIF = cartera.EGRValorRescate.subtract(carteraVig.INGValorRescate);
						                } else {
						                	restoSaldoCarteraVigenteParcial = cartera.EGRCantidad.subtract(carteraVig.INGCantidad);
						                }
						                break;
						            } else { // exceso: VIG > egreso
						            	carteraVigenteTotal = carteraVig;
						                if (esIFVig) {
						                	restoSaldoCarteraVigenteTotalIF = carteraVig.INGValorRescate.subtract(cartera.EGRValorRescate);
						                } else {
						                	restoSaldoCarteraVigenteTotal = carteraVig.INGCantidad.subtract(cartera.EGRCantidad);
						                }
						                break;
						            }
									
								}
							}
							
							boolean carteranegativa = carteraVigente == null
					                && carteraVigenteParcial == null
					                && carteraVigenteTotal == null;
							 //Cartera vigente con lo justo de la venta a=b
							if(carteraVigente != null && carterasAnular.get(carteraVigente.FolioCartera) == null){
								anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
								anulacionCartera = AnulacionDeVig(carteraVigente, Fecha.toDate(fechaSistema));
								carterasAnular.put(anulacionCartera.FolioCustodia, anulacionCartera);
								if (cartera.IdTipoOperacion.equalsIgnoreCase("VENSIM") || cartera.IdTipoOperacion.equalsIgnoreCase("COMSIM")
										|| cartera.IdTipoOperacion.equalsIgnoreCase("COMRRV") || cartera.IdTipoOperacion.equalsIgnoreCase("VENRRV") ) {
									List<AnulacionVentaSimultanea> lista = hashComprobantesAnulacionSimultanea.get(info.IdComprobante);
									if (lista == null) {
										lista = new ArrayList<>();
										hashComprobantesAnulacionSimultanea.put(info.IdComprobante,lista);
									}
									
									anulacionCartera.IdEmisor = cartera.IdEmisor;
									anulacionCartera.FechaVencimientoInstrumento = cartera.FechaVencimientoInstrumento;
									AnulacionVentaSimultanea anulacion = new AnulacionVentaSimultanea(cartera.FolioCartera,anulacionCartera);
									lista.add(anulacion);
								}
								else {
									List<AnulacionCompraNormalCustodia> lista = hashComprobantesAnulacionCompras.get(info.IdComprobante);
									if (lista == null) {
										lista = new ArrayList<>();
										hashComprobantesAnulacionCompras.put(info.IdComprobante,lista);
									}
									
									AnulacionCompraNormalCustodia anulacion = new AnulacionCompraNormalCustodia(anulacionCartera);
									lista.add(anulacion);
								}
							}
							//cartera vigente con menos de la venta a<b : Anulamos la VIG y generamos NEGATIVA por diferencia
							if(carteraVigenteParcial != null && carterasAnular.get(carteraVigenteParcial.FolioCartera) == null){
								anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
								anulacionCartera = AnulacionDeVig(carteraVigenteParcial, Fecha.toDate(fechaSistema));
								
								carterasAnular.put(anulacionCartera.FolioCustodia, anulacionCartera);
								
								
								// INGRESAR CARTERA NEGATIVA PARCIAL
								IngresoCustodia.ObjDatosEntrada	reingresoCartera = carterasIngresos.get(cartera.FolioCartera);
								if(reingresoCartera == null) {
									reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
									reingresoCartera = ReingresoNegativo(cartera, Fecha.toDate(fechaSistema),in.p_IdUsuario,dataComprobantesaReferencia);
									if (cartera.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
										reingresoCartera.FechaVencimientoOperacion				= cartera.FechaVencimientoInstrumento;
									}
									if (anulacionCartera != null) {
										if (carteraVigenteParcial.EsFungible.equalsIgnoreCase("S")) {
											reingresoCartera.Cantidad = BigDecimal.ONE;
											reingresoCartera.ValorRescate = restoSaldoCarteraVigenteParcialIF;
										}
										else {
											reingresoCartera.Cantidad = restoSaldoCarteraVigenteParcial;//reingresoCartera.Cantidad.add(anulacionCartera.Cantidad); 
											reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(anulacionCartera.ValorRescate);	
										}
										 
									}
									folioCarteraComprobante.put(reingresoCartera.FolioCustodia, info.IdComprobante);
								}else {
									reingresoCartera.Cantidad = reingresoCartera.Cantidad.add(carteraVigenteParcial.EGRCantidad, MathContext.DECIMAL128);
									reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(carteraVigenteParcial.EGRValorRescate, MathContext.DECIMAL128);
								}
								carterasIngresos.put(reingresoCartera.FolioCustodia, reingresoCartera);
							}
							//cartera vigente con mas de la venta a>b
							if(carteraVigenteTotal != null && carterasAnular.get(carteraVigenteTotal.FolioCartera) == null){
								anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
								anulacionCartera = AnulacionDeVig(carteraVigenteTotal, Fecha.toDate(fechaSistema));								
								carterasAnular.put(anulacionCartera.FolioCustodia, anulacionCartera);
								
								IngresoCustodia.ObjDatosEntrada	reingresoCartera = carterasIngresos.get(cartera.FolioCartera);
								if(reingresoCartera == null) {
									reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
									reingresoCartera=  ReingresoCartera(cartera, carteraVigenteTotal.IdCartera, Fecha.toDate(fechaSistema));
									if (anulacionCartera != null) {
										if (carteraVigenteTotal.EsFungible.equalsIgnoreCase("S")) {
											reingresoCartera.Cantidad = BigDecimal.ONE;
											reingresoCartera.ValorRescate = restoSaldoCarteraVigenteTotalIF;
										}
										else {
											reingresoCartera.Cantidad = restoSaldoCarteraVigenteTotal;
											reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(anulacionCartera.ValorRescate);	
										}
										 
									}
									folioCarteraComprobante.put(reingresoCartera.FolioCustodia, info.IdComprobante);
								}else {
									reingresoCartera.Cantidad = reingresoCartera.Cantidad.add(carteraVigenteTotal.EGRCantidad, MathContext.DECIMAL128);
									reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(carteraVigenteTotal.EGRValorRescate, MathContext.DECIMAL128);
								}
								carterasIngresos.put(reingresoCartera.FolioCustodia, reingresoCartera);
							}
							//cartera negativa total
							//preguntar que folio comprobante, idtipocomprobante va 
							if(carteranegativa) {
								IngresoCustodia.ObjDatosEntrada	reingresoCartera = carterasIngresos.get(cartera.FolioCartera);
								if(reingresoCartera == null) {
									reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
									reingresoCartera= ReingresoNegativoTotal(cartera, Fecha.toDate(fechaSistema),in.p_IdUsuario,dataComprobantesaReferencia);
									
									if (anulacionCartera != null) {
										if (cartera.EsFungible.equalsIgnoreCase("S")) {
											reingresoCartera.Cantidad = BigDecimal.ONE;
											reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(anulacionCartera.ValorRescate);
										}
										else {
											reingresoCartera.Cantidad = reingresoCartera.Cantidad.add(anulacionCartera.Cantidad); 
											reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(anulacionCartera.ValorRescate);	
										}
										 
									}
									folioCarteraComprobante.put(reingresoCartera.FolioCustodia, info.IdComprobante);
								}else {
									reingresoCartera.Cantidad = reingresoCartera.Cantidad.add(cartera.EGRCantidad, MathContext.DECIMAL128);
									reingresoCartera.ValorRescate = reingresoCartera.ValorRescate.add(cartera.EGRValorRescate, MathContext.DECIMAL128);
								}
								carterasIngresos.put(reingresoCartera.FolioCustodia, reingresoCartera);
							}
						}						
				}
				
			}
			
			
			//reingreso carteras negativas
		if(carterasNegativas.size()>0) {			
			boolean flag= ActualizarCarteraNegativa(conn, carterasNegativas);
			if(flag) {
				for(Cartera_Proceso objList:carterasNegativas ) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(objList.FolioCartera,MensajeSalida.MENSAJE_OK,"CARTERA_ACTUALIZADA","Cartera "+objList.TipoCartera+" creada, Folio: "+objList.IdCartera));
				}
			}				
		}
		

			
			for(IngresoCustodia.ObjDatosEntrada reingresoCartera : carterasIngresos.values()) {
				Long idComprobante = folioCarteraComprobante.get(reingresoCartera.FolioCustodia);
				List<AnulacionVentaNormalCustodia> lista = hashComprobantesAnulacionVentas.get(idComprobante);
				if(lista == null) {
					lista = new ArrayList<>();
				}
				RetiroCustodia.ObjDatosEntrada anulacionCartera = carterasAnular.get(reingresoCartera.FolioCustodia);
				AnulacionVentaNormalCustodia anulacion = new AnulacionVentaNormalCustodia(anulacionCartera,reingresoCartera);
				lista.add(anulacion);
				hashComprobantesAnulacionVentas.put(idComprobante,lista);
			}


			
			//ANULAR COMPRAS Y VENTAS PARCIALES
			RetiroCustodia retiroCustodiaService = new RetiroCustodia();
			RetiroCustodiaEntrada retiroCustodiaEntrada = new RetiroCustodiaEntrada();
			
			
			for (List<AnulacionVentaSimultanea> lista : hashComprobantesAnulacionSimultanea.values()) {
				for (AnulacionVentaSimultanea retiro : lista) {
					if (retiro.pasoAnulacionCabecera != null) {
						retiroCustodiaEntrada.p_DatosEntrada.add(retiro.pasoAnulacionCabecera);
					}
				}
			}
			for (List<AnulacionVentaSimultanea> lista : hashComprobantesAnulacionSimultanea.values()) {
				for (AnulacionVentaSimultanea retiro : lista) {
					if (retiro.pasoRetiroSubyacente != null) {
						retiroCustodiaEntrada.p_DatosEntrada.addAll(retiro.pasoRetiroSubyacente);
					}
				}
			}
			for (List<AnulacionCompraNormalCustodia> lista : hashComprobantesAnulacionCompras.values()) {
				for (AnulacionCompraNormalCustodia retiro : lista) {
					if (retiro.pasoAnulacionCartera != null) {
						retiroCustodiaEntrada.p_DatosEntrada.add(retiro.pasoAnulacionCartera);
					}
				}
			}
			for (List<AnulacionVentaNormalCustodia> lista : hashComprobantesAnulacionVentas.values()) {
				for (AnulacionVentaNormalCustodia retiro : lista) {
					if (retiro.pasoAnulacionCartera != null) {
						retiroCustodiaEntrada.p_DatosEntrada.add(retiro.pasoAnulacionCartera);
					}
				}
			}
			
			
			retiroCustodiaEntrada.p_IdEmpresa = in.p_IdEmpresa;
			retiroCustodiaEntrada.p_IdModulo = in.p_IdModulo;
			retiroCustodiaEntrada.p_IdUsuario = in.p_IdUsuario;
			retiroCustodiaEntrada.p_SimularOperacion = in.p_simularOperacion;
			retiroCustodiaEntrada.p_TicketAtencion = 0L;
			retiroCustodiaEntrada.p_ValorizacionAutomatica = "N";
			
			if (retiroCustodiaEntrada.p_DatosEntrada.size() > 0) {
				RetiroCustodiaSalida retiroCustodiaSalida = retiroCustodiaService.logica(conn, retiroCustodiaEntrada );
				logger.debug("Salida retiro Custodia:" + retiroCustodiaSalida);
				
				out.P_OUT_MENSAJES.addAll(retiroCustodiaSalida.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(out);
					return out;
				}
			}

			//se repite llamado para los retiros, para los que estan sin id (esto no esta en el otro java)
			if(carterasRetiro.size() >0) {

				retiroCustodiaEntrada.p_DatosEntrada = carterasRetiro;
				RetiroCustodiaSalida retiroCustodiaSalida = retiroCustodiaService.logica(conn, retiroCustodiaEntrada );
				logger.debug("Salida retiro Custodia:" + retiroCustodiaSalida);
				
				out.P_OUT_MENSAJES.addAll(retiroCustodiaSalida.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(out);
					return out;
				}
			}
			

			
			//66
			IngresoCustodia ingresoCustodiaService = new IngresoCustodia();
			IngresoCustodiaEntrada ingresoCustodiaEntrada = new IngresoCustodiaEntrada();
			
			
			for (List<AnulacionVentaNormalCustodia> lista : hashComprobantesAnulacionVentas.values()) {
				for (AnulacionVentaNormalCustodia retiro : lista) {
					if (retiro.pasoIngresoCartera != null) {
						ingresoCustodiaEntrada.p_DatosEntrada.add(retiro.pasoIngresoCartera);
					}
				}
			}

			ingresoCustodiaEntrada.p_IdEmpresa = in.p_IdEmpresa;
			ingresoCustodiaEntrada.p_IdModulo = in.p_IdModulo;
			ingresoCustodiaEntrada.p_IdUsuario = in.p_IdUsuario;
			ingresoCustodiaEntrada.p_SimularOperacion = in.p_simularOperacion;
			ingresoCustodiaEntrada.p_TicketAtencion = 0L;
			ingresoCustodiaEntrada.p_ValorizacionAutomatica = "N";
			if (ingresoCustodiaEntrada.p_DatosEntrada.size() > 0) {
				IngresoCustodiaSalida ingresoCustodiaSalida = ingresoCustodiaService.logica(conn, ingresoCustodiaEntrada );
				logger.debug("Salida ingreso Custodia:" + ingresoCustodiaSalida);
				
				out.P_OUT_MENSAJES.addAll(ingresoCustodiaSalida.P_OUT_MENSAJES);
				if(ingresoCustodiaSalida.P_OUT_DATOSSALIDA.size()>0) {
					for(IngresoCustodiaSalida.ObjDatosSalida carterasnegativas :ingresoCustodiaSalida.P_OUT_DATOSSALIDA) {
						if(carterasnegativas.TablaOrigen.equalsIgnoreCase("NEGATIVA") && MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) <= 0) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(carterasnegativas.FolioCustodia,MensajeSalida.MENSAJE_OK,"CARTERA_NEGATIVA","Cartera "+carterasnegativas.TablaOrigen+" creada, Folio: "+carterasnegativas.IdCustodia));
						}
					}											
				}
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(out);
					return out;
				}
			}

			//se repite llamado para los que estan sin id (esto no esta en el otro java)
			if(carterasReingreso.size() >0) {

				ingresoCustodiaEntrada.p_DatosEntrada = carterasReingreso;
				IngresoCustodiaSalida ingresoCustodiaSalida = ingresoCustodiaService.logica(conn, ingresoCustodiaEntrada );
				logger.debug("Salida ingreso Custodia:" + ingresoCustodiaSalida);
				
				out.P_OUT_MENSAJES.addAll(ingresoCustodiaSalida.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					logger.debug("Fin");
					logger.debug(out);
					return out;
				}
			}
			
		}
		
		CacheValorizacion.valorizar(conn);
		logger.debug("Fin Valorizacion");
		//ANULA EN CUSTODIA
			//ANULAR TRANSITOS
			//ANULAR VENTAS
			//ANULAR COMPRAS
			//TODO ANULAR VENTAS SIMULTANEA
			//TODO ANULAR COMPRAS SIMULTANEA
			//TODO ANULAR VENTAS CORTAS
			//SI SON NOMINALES QUE SE HACE?
		
		//TODO SI ANULA NOTA DE CREDITO VUELVE LA ASIGNACION A EAF (LA NOTA DE CREDITO LA ANULO) 

		if (in.p_simularOperacion.equalsIgnoreCase("N")) {
			_sql.batch(conn, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE, comprobantes, Comprobante_Proceso.class);
			_sql.batch(conn, SQL_UPDATE_COMPROBANTE_DETALLE, comprobantes, Comprobante_Proceso.class);
			_sql.batch(conn, SQL_UPDATE_COMPROBANTE, comprobantes, Comprobante_Proceso.class);
			_sql.batch(conn, SQL_UPDATE_COMPROBANTE_FACTURA, comprobantesFactura, Comprobante_Proceso.class);

			//ANULAR MOVIMIENTO COMPROBANTE -> PARAMETRO
			{
				idsComprobante.clear();
				for (Comprobante c : comprobantes) {
					idsComprobante.add(c.IdComprobante);
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("idsComprobante",idsComprobante);
				List<MovimientoComprobante> movsAnular = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE, params , MovimientoComprobante.class);
				
				if (movsAnular.size() > 0) {					
					List<MensajeSalida> salida=ActualizarMovimientoComprobante(conn,movsAnular);
					out.P_OUT_MENSAJES.addAll(salida);
					//_sql.batch(conn, SQL_UPDATE_MOVIMIENTO_COMPROBANTE, movsAnular, MovimientoComprobante.class);					
				}
				
				List<Asignacion_Proceso> _mvtoComprobantesNC = null;
				HashMap<String,Object> paramsAsignacion = new HashMap<>();
				paramsAsignacion.put("idsComprobante",idsComprobante);
				_mvtoComprobantesNC = _sql.query(conn, SQL_SELECT_ASIGNACION, paramsAsignacion, Asignacion_Proceso.class);

				
				
				CambiarEstadoAsignaciones_Entrada entradaCambiarEstadoAsignaciones = new CambiarEstadoAsignaciones_Entrada();
				for (Asignacion_Proceso asig : _mvtoComprobantesNC) {
					if (asig.IdAsignacion!=null && asig.IdAsignacion>0) {
						CambiarEstadoAsignaciones.Asignacion_Entrada asigCambioEstado = new CambiarEstadoAsignaciones.Asignacion_Entrada();
						asigCambioEstado.IdAsignacion = asig.IdAsignacion;
						asigCambioEstado.IdEstadoAsignacion = "EAF";
						entradaCambiarEstadoAsignaciones.p_IdEmpresa = in.p_IdEmpresa;
						entradaCambiarEstadoAsignaciones.p_Asignaciones.add(asigCambioEstado);
					}
				}
				
				if (entradaCambiarEstadoAsignaciones.p_Asignaciones.size() > 0) {
					CambiarEstadoAsignaciones serviceCambiarEstadoAsignaciones = new CambiarEstadoAsignaciones();
					CambiarEstadoAsignaciones_Salida salidaCambiarEstadoAsignaciones = serviceCambiarEstadoAsignaciones.logica(conn, entradaCambiarEstadoAsignaciones);
					out.P_OUT_MENSAJES.addAll(salidaCambiarEstadoAsignaciones.P_OUT_MENSAJES.stream().filter(t->t.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList()));
					
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
						out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
						out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						logger.debug("Fin");
						logger.debug(out);
						return out;
					}
				}
			}
			
			//AGREGAR LOG
			{
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				for (Comprobante_Proceso cmbte : comprobantes) {
					Comprobante infoComprobante = (Comprobante) hashComprobantes.get(cmbte.IdComprobante);
					
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = cmbte.IdComprobante;
					log.IdTipoLog = "ANULACION";
					log.IdModulo = "FAC";
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdCliente = infoComprobante.IdCliente;
					log.EstadoCliente = "";
					log.NombreCliente = infoComprobante.NombreLargoCliente;
					log.IdTipoDocumentoIdentidad = infoComprobante.TipoDocumentoIdentidad;
					log.IdDocumentoIdentidad = infoComprobante.DocumentoIdentidad;
					log.IdPortafolio = infoComprobante.IdPortafolio;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "COMPROBANTEFACTURACION";
					log.EstadoRegistro = "ANU";
					log.IdTipoOperacion = infoComprobante.IdTipoOperacion;
					log.Folio = infoComprobante.IdComprobante;
					log.Cantidad = infoComprobante.Cantidad;
					log.PrecioTasa = infoComprobante.Precio;
					log.Monto = infoComprobante.MontoLiquidacionFinal;
					log.IdMonedaOrigen = infoComprobante.IdMoneda;
					log.IdMonedaLiquidacion = infoComprobante.IdMonedaLiquidacion;
					log.TipoCambio = infoComprobante.ValorTipoCambio.doubleValue()+"";
					log.Nemo = infoComprobante.NemotecnicoInstrumento;
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "";						
					entradaAgregarLog.p_Log.add(log);
					
					for (CargoAbono_Proceso cargoAbono : cmbte.cargosAbonos) {
						AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
						d.CampoRegistro = cargoAbono.IdCargoAbonoMovimiento+"";
						d.IdCabeceraLog = cmbte.IdComprobante;
						d.SubRegistro = cargoAbono.NombreTipoMovimiento;
						d.ValorAntiguo = cargoAbono.IdEstado;
						d.ValorNuevo = "ANU";
						entradaAgregarLog.p_LogDetalle.add(d);
					}
					
					for (EntidadTesoreria_Proceso entidad : cmbte.entidadesTesoreria) {
						AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
						d.CampoRegistro = entidad.Id+"";
						d.IdCabeceraLog = cmbte.IdComprobante;
						d.SubRegistro = entidad.NombreTipoMovimiento;
						d.ValorAntiguo = entidad.IdEstado;
						d.ValorNuevo = "ANU";
						entradaAgregarLog.p_LogDetalle.add(d);
					}
					
					for (Cartera_Proceso cartera : cmbte.carteras) {
						AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
						d.CampoRegistro = cartera.FolioCartera+"";
						d.IdCabeceraLog = cmbte.IdComprobante;
						d.SubRegistro = "CARTERA";
						d.ValorAntiguo = cartera.IdEstado;
						d.ValorNuevo = "ANU";
						entradaAgregarLog.p_LogDetalle.add(d);
					}
					
				}
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
				logger.debug("Fin Log");
			}
			
		
		}
		
		//PREPARAR SALIDA
		for (Comprobante_Proceso cte : comprobantes) {
			String codigo = "UI_COMPROBANTE_ANULADO";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobante", cte.IdComprobante).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(cte.IdComprobante,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
			
			ResultadoProceso_Salida res = new ResultadoProceso_Salida();
			res.Campo = "IdComprobante";
			res.Id = cte.IdComprobante;
			res.Servicio = "AnularComprobante.java";
			res.Resultado = "ING";
			out.p_out_ResultadoProceso.add(res);
			
		}
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);		
		logger.info("Fin AnularComprobanteNotaDeCredito");
		return out;
	}
	
	
	/* Reingreso NEGATIVA TOTAL */
	private static IngresoCustodia.ObjDatosEntrada ReingresoNegativoTotal(Cartera_Proceso mov, Date fechaOperacion,Long usuario,Map<Long, Long> dataComprobantesaReferencia) {
	    IngresoCustodia.ObjDatosEntrada o = ReingresoNegativo(mov, fechaOperacion,usuario,dataComprobantesaReferencia);
	    o.Cantidad = mov.Cantidad;
	    o.Glosa = mov.Glosa + mov.Glosa + " - Anulación de NC Folio Comprobante :"
	            + dataComprobantesaReferencia.entrySet().stream()
	                .filter(e -> mov.EGRFolioComprobante.equals(e.getValue()))
	                .map(Map.Entry::getKey)
	                .findFirst().orElse(0L)
	            + " de la Factura Folio Comprobante: " + mov.EGRFolioComprobante;
	    return o;
	}
	
	/* Reingreso CARTERA (exceso) a>b*/
	private static IngresoCustodia.ObjDatosEntrada ReingresoCartera(Cartera_Proceso mov, Long idCarteraVig, Date fechaOperacion) {
	    IngresoCustodia.ObjDatosEntrada o = new IngresoCustodia.ObjDatosEntrada();
	    o.IdGenerarMovimientoCustodia = idCarteraVig;
	    o.TablaAfectada = "CARTERA";
	    o.AccionOperacion = "INGRESO";
	    o.IdCliente = mov.IdCliente;
	    o.IdPortafolio = mov.IdPortafolio;
	    o.FolioCustodia = mov.FolioCartera;
	    o.IdCustodia = mov.IdCartera;
	    o.Cantidad = mov.EGRCantidad; // se recalcula afuera
	    o.IdInstrumento = mov.IdInstrumento;
	    o.ValorRescate = mov.EGRValorRescate;
	    o.FechaOperacion = fechaOperacion;
	    o.EsRegistroNuevo = "N";
	    o.GenerarNuevoFolioCustodia = "N";
	    o.PlazoEnDiasOperacion = mov.PlazoEnDiasOperacion;
	    o.IdEmisor = mov.IdEmisor;
	    o.IdTipoReajusteInstrumento = mov.IdTipoReajuste;
	    o.IdMonedaEmision = mov.IdMonedaEmision;
	    o.PrecioCompra = mov.INGPrecioCompra;
	    o.FechaLiquidacion = mov.INGFechaLiquidacion;
	    o.TasaCompra = mov.TasaCompra;
	    o.PrecioAPlazo = mov.PrecioAPlazo;
	    o.MontoAPlazo = mov.MontoAPlazo;
	    if ("IF".equalsIgnoreCase(mov.IdClasificacionTipoInstrumento)) {
	        o.FechaVencimientoOperacion = mov.FechaVencimientoInstrumento;
	    }
	    return o;
	}
	
	/* Reingreso como cartera NEGATIVA (parcial) */
	private static IngresoCustodia.ObjDatosEntrada ReingresoNegativo(Cartera_Proceso mov, Date fechaOperacion,Long usuario,Map<Long, Long> dataComprobantesaReferencia) {
	    IngresoCustodia.ObjDatosEntrada o = new IngresoCustodia.ObjDatosEntrada();
	    o.IdGenerarMovimientoCustodia = mov.IdCartera;
	    o.TablaAfectada = "NEGATIVA";
	    o.AccionOperacion = "INGRESO";
	    o.IdCliente = mov.IdCliente;
	    o.IdPortafolio = mov.IdPortafolio;
	    o.FolioCustodia = mov.FolioCartera;
	    o.IdCustodia = mov.IdCartera;
	    o.Cantidad = BigDecimal.ZERO; // se ajusta afuera
	    o.IdInstrumento = mov.IdInstrumento;
	    o.ValorRescate = mov.ValorRescate;
	    o.FechaOperacion = fechaOperacion;
	    o.EsRegistroNuevo = "S";
	    o.GenerarNuevoFolioCustodia = "N";
	    o.PlazoEnDiasOperacion = mov.PlazoEnDiasOperacion;
	    o.IdEmisor = mov.IdEmisor;
	    o.IdTipoReajusteInstrumento = mov.IdTipoReajuste;
	    o.IdMonedaEmision = mov.IdMonedaEmision;
	    o.PrecioCompra = mov.INGPrecioCompra;
	    o.FechaLiquidacion = mov.INGFechaLiquidacion;
	    o.TasaCompra = mov.TasaCompra;
	    o.PrecioAPlazo = mov.PrecioAPlazo;
	    o.MontoAPlazo = mov.MontoAPlazo;
	    o.IdUsuario = usuario;
	    o.FolioComprobante = mov.EGRFolioComprobante;

	    // Campos contables/negocio
	    o.IdTipoOperacion = mov.EGRIdTipoOperacion;
	    o.IdConcepto = mov.EGRIdConcepto;
	    o.IdTipoComprobante = mov.IdTipoComprobante;
	    o.Nemotecnico = mov.Nemotecnico;
	    o.EsFungible = mov.EsFungible;
	    o.FechaVencimientoInstrumento = mov.FechaVencimientoInstrumento;
	    o.IdMovimientoComprobante = 0L;
	    o.IdCondicionLiquidacion = mov.IdCondicionLiquidacion;
	    o.MontoCompraMonedaInstrumento = mov.MontoCompraMonedaInstrumento.multiply(BigDecimal.valueOf(-1));
	    o.IdBoveda = mov.IdBoveda;
	    o.IdTipoProducto = mov.IdTipoProducto;
	    o.IdAgente = mov.IdAgente;
	    o.IdMonedaInstrumento = mov.IdMonedaInstrumento;
	    o.IdSucursal = mov.IdSucursal;
	    o.Glosa = mov.Glosa + " - Anulación de NC Folio Comprobante :"
	            + dataComprobantesaReferencia.entrySet().stream()
	                .filter(e -> mov.EGRFolioComprobante.equals(e.getValue()))
	                .map(Map.Entry::getKey)
	                .findFirst().orElse(0L)
	            + " de la Factura Folio Comprobante: " + mov.EGRFolioComprobante;
	    o.IdTipoCanalIngreso = mov.IdTipoCanalIngreso;
	    o.NemoSVS = mov.NemoSVS;
	    o.CorteMinimo = mov.CorteMinimo;
	    o.FolioExternoReferencia = mov.FolioExternoReferencia;
	    if ("IF".equalsIgnoreCase(mov.IdClasificacionTipoInstrumento)) {
	        o.FechaVencimientoOperacion = mov.FechaVencimientoInstrumento;
	    }
	    return o;
	}
	/*  anulacion desde una cartera VIG */
	private static RetiroCustodia.ObjDatosEntrada AnulacionDeVig(Cartera_Proceso vig, Date fechaOperacion) {
	    RetiroCustodia.ObjDatosEntrada o = new RetiroCustodia.ObjDatosEntrada();
	    o.IdGenerarMovimientoCustodia = vig.IdCartera * -1;
	    o.TablaAfectada = "CARTERA";
	    o.AccionOperacion = "ANULACION";
	    o.IdCliente = vig.IdCliente;
	    o.IdPortafolio = vig.IdPortafolio;
	    o.FolioCustodia = vig.FolioCartera;
	    o.FechaLiquidacion = vig.INGFechaLiquidacion;
	    o.IdCustodia = vig.IdCartera;
	    o.Cantidad = vig.INGCantidad;
	    o.IdInstrumento = vig.IdInstrumento;
	    o.ValorRescate = vig.INGValorRescate;
	    o.IdModulo = "FAC";
	    o.FechaOperacion = fechaOperacion;
	    o.PrecioCompra = vig.INGPrecioCompra;
	    o.PlazoEnDiasOperacion = vig.PlazoEnDiasOperacion;
	    o.IdEmisor = vig.IdEmisor;
	    o.IdTipoReajusteInstrumento = vig.IdTipoReajuste;
	    o.IdMonedaEmision = vig.IdMonedaEmision;
	    o.PrecioAPlazo = vig.PrecioAPlazo;
	    o.MontoAPlazo = vig.MontoAPlazo;
	    return o;
	}
	/* Compara por cantidad o valor rescate segun si es IF */
	private static int cantidadORescate(Cartera_Proceso vig, Cartera_Proceso mov) {
	    boolean esIF = "IF".equalsIgnoreCase(vig.IdClasificacionTipoInstrumento);
	    if (esIF) {
	        return vig.INGValorRescate.compareTo(mov.EGRValorRescate);
	    } else {
	        return vig.INGCantidad.compareTo(mov.EGRCantidad);
	    }
	}
	public  Boolean ActualizarTransitoNominales(Connection conn,List<ObjMovimientosAActualizar> IdMovimientosCarteraEnTransito) throws SQLException{
		SQL sql = new SQL();
		try {
			List<ObjMovimientosEnTransito> ActualizacionTransitos = new ArrayList<ObjMovimientosEnTransito>();
			for (ObjMovimientosAActualizar tra : IdMovimientosCarteraEnTransito) {
				ObjMovimientosEnTransito paramsMovimientoTransito = new ObjMovimientosEnTransito();
				paramsMovimientoTransito.IdMovimientoCarteraEnTransito 	= tra.IdMovimientoCarteraEnTransito;
				paramsMovimientoTransito.IdEstado						= "PEN";
				paramsMovimientoTransito.IdUsuarioCertificacion			= 0L;
				paramsMovimientoTransito.IdUsuarioAnulacion				= 0L;
				ActualizacionTransitos.add(paramsMovimientoTransito);
			};
			
			sql.batch(conn, SQL_UPDATE_MOVIMIENTO_TRANSITO, ActualizacionTransitos, ObjMovimientosEnTransito.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}		
		return true;		
	}
	public  Boolean ActualizarCarteraNegativa(Connection conn,List<Cartera_Proceso> carterasNegativas) throws SQLException{
		SQL sql = new SQL();
		try {
			List<Cartera_Proceso> ActualizacionCarterasNegativas = new ArrayList<Cartera_Proceso>();
			for (Cartera_Proceso tra : carterasNegativas) {
				Cartera_Proceso paramsCarterasNegativas = new Cartera_Proceso();
				paramsCarterasNegativas.IdCartera=tra.IdCartera;
				paramsCarterasNegativas.IdEstado= "VIG";
				ActualizacionCarterasNegativas.add(paramsCarterasNegativas);
			};
			sql.batch(conn, SQL_UPDATE_CARTERAS_NEGATIVAS, ActualizacionCarterasNegativas, Cartera_Proceso.class);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}		
		return true;		
	}
	public  Boolean ActualizarCarteraCompra(Connection conn,List<Cartera_Proceso> carterasCompra) throws SQLException{
		SQL sql = new SQL();
		try {
			List<Cartera_Proceso> ActualizacionCarterasCompra = new ArrayList<Cartera_Proceso>();
			for (Cartera_Proceso tra : carterasCompra) {
				Cartera_Proceso paramsCarterasCompra = new Cartera_Proceso();
				paramsCarterasCompra.IdCartera=tra.IdCartera;
				paramsCarterasCompra.IdEstado= "VIG";
				ActualizacionCarterasCompra.add(paramsCarterasCompra);
			};
			
			sql.batch(conn, SQL_UPDATE_CARTERA, ActualizacionCarterasCompra, Cartera_Proceso.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}		
		return true;		
	}
	
	public  List<MensajeSalida> ActualizarMovimientoComprobante(Connection conn,List<MovimientoComprobante> movsAnular) throws SQLException{
		SQL sql = new SQL();
		try {
			List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
			sql.batch(conn, SQL_UPDATE_MOVIMIENTO_COMPROBANTE, movsAnular, MovimientoComprobante.class);
			for (MovimientoComprobante tra : movsAnular) {
				//tra.IdMovimientoComprobante
				MensajeSalida mensajeSalida = new MensajeSalida(tra.IdMovimientoComprobante, MensajeSalida.MENSAJE_INFORMACION, "MOVIMIENTO_COMPROBANTE_ANULADO","El movimiento comprobante "+tra.IdMovimientoComprobante+" fue anulado con éxito.");
				mensajes.add(mensajeSalida);	
			};
			return mensajes;		
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}		
		
	}
	
	public static class AnulacionVentaSimultanea {
		public Long FolioCartera;
		public RetiroCustodia.ObjDatosEntrada pasoAnulacionCabecera;	//anulacion cartera liquidada
		public List<RetiroCustodia.ObjDatosEntrada> pasoRetiroSubyacente;		//reingreso cartera
		
		public AnulacionVentaSimultanea(Long FolioCartera, RetiroCustodia.ObjDatosEntrada pasoAnulacionCabecera) {
			this.pasoAnulacionCabecera = pasoAnulacionCabecera;
			this.FolioCartera =  FolioCartera;
			this.pasoRetiroSubyacente = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnulacionVentaNormalCustodia {
		
		public RetiroCustodia.ObjDatosEntrada pasoAnulacionCartera;	//anulacion cartera liquidada
		public IngresoCustodia.ObjDatosEntrada pasoIngresoCartera;		//reingreso cartera
		
		public AnulacionVentaNormalCustodia(RetiroCustodia.ObjDatosEntrada pasoAnulacionCartera1,IngresoCustodia.ObjDatosEntrada pasoIngresoCartera1) {
			this.pasoAnulacionCartera = pasoAnulacionCartera1;
			this.pasoIngresoCartera =  pasoIngresoCartera1;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AnulacionCompraNormalCustodia {
		
		
		public RetiroCustodia.ObjDatosEntrada pasoAnulacionCartera;	//anulacion de cartera vigente
		
		public AnulacionCompraNormalCustodia(RetiroCustodia.ObjDatosEntrada dato) {
			this.pasoAnulacionCartera = dato;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	// CLASES IN
	// ------------------------------------------------------------------------------
	public static class ComprobanteNC_Entrada {
		public Long IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class AnularComprobanteNotaDeCredito_Entrada {
	
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_simularOperacion = "N";
		public String 	p_UsaReglasNegocio = "S";
		public String	p_EsIngresoAutomatico = "N";
		public String	p_EjecutaAutorizacion = "S";
		public String	p_OmiteRegistroSinAutorizar = "S";
		public String 	p_Glosa = "";
		
		public List<ComprobanteNC_Entrada> P_COMPROBANTEAANULAR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public AnularComprobanteNotaDeCredito_Entrada() {
			P_COMPROBANTEAANULAR = new ArrayList<ComprobanteNC_Entrada>();
		}
	}

	// CLASES OUT
	// ------------------------------------------------------------------------------	
	public static class AnularComprobanteNotaDeCredito_Salida {
		public List<Comprobante_Salida>					P_OUT_COMPROBANTEANULADO;
		public List<PlataformaUtils.MensajeSalida> 		P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> 			p_out_ResultadoProceso;
		
		public AnularComprobanteNotaDeCredito_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_ResultadoProceso = new ArrayList<>();
			P_OUT_COMPROBANTEANULADO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Comprobante_Salida {
		public Long IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ResultadoProceso_Salida {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false); 
		}
	}
	
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
		{
			Gson gson = Utils.getGsonBuilder();		
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":289,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_EjecutaAutorizacion\":\"N\",\"p_OmiteRegistroSinAutorizar\":\"S\",\"p_Glosa\":\"Anulado por Sistema\",\"P_COMPROBANTEAANULAR\":[{\"IdComprobante\":1199739,\"IdPortafolio\":8088,\"IdCliente\":7345,\"IdTipoComprobante\":\"NCE\"}]}";
			
			String json = "{\r\n"
			+ "    \"p_IdEmpresa\": 1,\r\n"
			+ "    \"p_IdUsuario\": 97,\r\n"
			+ "    \"p_IdModulo\": \"FAC\",\r\n"
			+ "    \"p_simularOperacion\": \"N\",\r\n"
			+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
			+ "    \"p_EsIngresoAutomatico\": \"N\",\r\n"
			+ "    \"p_EjecutaAutorizacion\": \"N\",\r\n"
			+ "    \"p_OmiteRegistroSinAutorizar\": \"S\",\r\n"
			+ "    \"p_Glosa\": \"Anulado por Sistema\",\r\n"
			+ "    \"P_COMPROBANTEAANULAR\": [\r\n"
			+ "        {\r\n"
			+ "            \"IdComprobante\": 2578957,\r\n"
			+ "            \"IdPortafolio\": 15049,\r\n"
			+ "            \"IdCliente\": 13461,\r\n"
			+ "            \"IdTipoComprobante\": \"NCE\"\r\n"
			+ "        }\r\n"
			+ "    ]\r\n"
			+ "}";
			AnularComprobanteNotaDeCredito_Entrada Objin = gson.fromJson(json, AnularComprobanteNotaDeCredito_Entrada.class);
			AnularComprobanteNotaDeCredito demo=new AnularComprobanteNotaDeCredito();
			AnularComprobanteNotaDeCredito_Salida ObjOut=demo.logica(con, Objin);
 			System.out.println(ObjOut);
 			//con.commit();
 			con.rollback();
	    }

		System.out.println("== END ==");
	}
	
}