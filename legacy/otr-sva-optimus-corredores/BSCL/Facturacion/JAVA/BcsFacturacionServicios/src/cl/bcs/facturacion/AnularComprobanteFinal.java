package cl.bcs.facturacion;


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
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
import cl.bcs.custodia.AnularCarteraNegativa;
import cl.bcs.custodia.AnularCarteraNegativa.AnularCarteraNegativa_Entrada;
import cl.bcs.custodia.AnularCarteraNegativa.AnularCarteraNegativa_Salida;
import cl.bcs.custodia.AnularCarteraNegativa.CarteraNegativa_Entrada;
import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.UpdateEventoCapitalCliente;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.facturacion.AnularComprobanteNotaDeCredito.AnularComprobanteNotaDeCredito_Entrada;
import cl.bcs.facturacion.AnularComprobanteNotaDeCredito.AnularComprobanteNotaDeCredito_Salida;
import cl.bcs.facturacion.AnularComprobanteNotaDeCredito.ComprobanteNC_Entrada;
import cl.bcs.facturacion.AnularComprobanteNotaDeCredito.Comprobante_Proceso;
import cl.bcs.facturacion.AnularComprobanteNotaDeCredito.EventoCapital_Proceso;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Salida;
import cl.bcs.facturacion.AnularMovimientoComprobante.Movimiento_Entrada;
import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularComprobanteFinal implements JavaCall {

	private final String SQL_SELECT_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectComprobante.sql");
	private final String SQL_SELECT_COMPROBANTESIM = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectComprobanteSim.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectMovimientoComprobante.sql");
	private final String SQL_SELECT_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectTesoreria.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCuentaInversion.sql");
	private final String SQL_SELECT_CUSTODIA_TRANSITOS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectTransitos.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasCompra.sql");
	private final String SQL_SELECT_CUSTODIA_GARANTIAS_COMPRAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectGarantiasCompra.sql");
	private final String SQL_SELECT_CUSTODIA_GARANTIAS_ANTICIPO = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectGarantiasAnticipo.sql");	
	private final String SQL_SELECT_CUSTODIA_CARTERAS_VENTAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasVenta.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_NEGATIVAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasNegativas.sql");
	private final String SQL_SELECT_CUSTODIA_SUBYACENTES = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectSubyacentes.sql");
	private final String SQL_UPDATE_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateComprobante.sql");
	private final String SQL_UPDATE_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateComprobanteDetalle.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateMovimientoComprobanteDetalle.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateMovimientoComprobante.sql");

	private final String SQL_UPDATE_ANTICIPO_CARTERA = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateAnticipoCartera.sql");
	private final String SQL_UPDATE_ANTICIPO_SUBYACENTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateAnticipoSubyacente.sql");
	private final String SQL_UPDATE_ANTICIPO_GARANTIA = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateAnticipoGarantia.sql");
	
	private final String SQL_SELECT_PERIODO_CIERRE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectPeriodoCierre.sql");
	private final String SQL_SELECT_SPOT_ANULADO_FFMM	= loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectSpotFFMM.sql");
	
	private final String SQL_UPDATE_ESTADO_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateEstadoEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL=loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectEventoCapital.sql");
	
	ConstantesFacturacion _c = new ConstantesFacturacion();
	// Log
	private static final Logger logger = LogManager.getLogger(AnularComprobanteFinal.class);
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		AnularComprobante_Entrada in = gson.fromJson(jin.toString(), AnularComprobante_Entrada.class);
		AnularComprobante_Salida out = logica(con, in);
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
		
	
	// LOGICA
	// ------------------------------------------------------------------------------
	@Trace
	public AnularComprobante_Salida logica(Connection conn, AnularComprobante_Entrada in) throws Exception {
		logger.info("Inicio Anular Comprobante: " + in.toString());
		AnularComprobante_Salida out = new AnularComprobante_Salida();
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
		String pasaAGarantiaReporteSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "CUSTODIA", "IngresaAGarantiaSimRepDeCompra");
		
		List<Comprobante_Proceso> comprobantes = new ArrayList<>();
		List<Comprobante_Proceso> comprobantesConError = new ArrayList<>();
		
		

		List<Cartera_Proceso> carterasCompra = new ArrayList<>();
		List<Cartera_Proceso> garantiaCompra = new ArrayList<>();
		List<Cartera_Proceso> carterasVenta = new ArrayList<>();
		List<Cartera_Proceso> carterasNegativas = new ArrayList<>();
		List<Subyacente_Proceso> subyacentes = new ArrayList<>();
		List<Cartera_Proceso> garantiaAnticipo = new ArrayList<>();
		
		HashMap<Long,Object> hashComprobantes = new HashMap<>();
		HashMap<Long,Object> hashComprobantesProceso = new HashMap<>();
		Map<Long, Long> dataComprobantesaReferencia = new HashMap<>();
		{
			HashMap<String,Object> params = new HashMap<>();
			Set<Long> idsComprobante = new HashSet<>();
			for (Comprobante_Entrada cmbte : in.P_COMPROBANTEAANULAR) {
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
			
			

			_comprobantes.forEach(_cmp -> {
			    if ("NEE".equals(_cmp.IdTipoComprobante)) {
			        dataComprobantesaReferencia.put(
			            _cmp.IdComprobante,
			            _cmp.IdComprobanteReferencia
			        );
			    }
			});

			
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
			for (Comprobante_Entrada cmbte : in.P_COMPROBANTEAANULAR) {
				idsComprobantesOriginales.add(cmbte.IdComprobante);
			}
			
			List<Long> idsComprobantesTotales = new ArrayList<>();
			idsComprobantesTotales.addAll(idsComprobantesOriginales);
			idsComprobantesTotales.addAll(idsComprobantesObtenidos);
			
			idsComprobanteNoRepetido = idsComprobantesTotales.stream().distinct().collect(Collectors.toList());

			for (Long idComprobanteNoRepetido : idsComprobanteNoRepetido) {
				long cantidad = IntStream.range(0, idsComprobantesOriginales.size()).filter(i -> idComprobanteNoRepetido.equals(idsComprobantesOriginales.get(i))).count();
				if (cantidad == 0) {
					Comprobante_Entrada entradaNueva = new Comprobante_Entrada();
					entradaNueva.IdComprobante = idComprobanteNoRepetido;
					in.P_COMPROBANTEAANULAR.add(entradaNueva);
				}
			}
		}
		
				
		for (Comprobante_Entrada cmbte : in.P_COMPROBANTEAANULAR ) {
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
		
		
		List<Long> eventoCapitalNC= new ArrayList<>();
		if(dataComprobantesaReferencia.size()>0) {
			List<Long> idsComprobanteReferencia = new ArrayList<>(dataComprobantesaReferencia.values());
			
			List<Long> idsConEventoCapital = new ArrayList<>();
			List<EventoCapital_Proceso> listEventoCapital = null;
			HashMap<String,Object> paramEventoCapital = new HashMap<>();
			paramEventoCapital.put("idsComprobante",idsComprobanteReferencia);
			listEventoCapital = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, paramEventoCapital, EventoCapital_Proceso.class);
			listEventoCapital.forEach(_cmp ->{
												if(_cmp.IdComprobanteFac!=null) {
													idsConEventoCapital.add(_cmp.IdComprobanteFac);
												}
				
			} );
			
			if(idsConEventoCapital.size()>0) {
				eventoCapitalNC=dataComprobantesaReferencia.entrySet().stream()
					    .filter(entry -> idsConEventoCapital.contains(entry.getValue()))
					    .map(Map.Entry::getKey)
					    .collect(Collectors.toList());
			}
			


		}

		
		
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
			ProcesaRequisito(conn, in.p_IdEmpresa, _c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante) 
					&& (!_c.tiposAnticipoSimultanea.contains(infoComprobante.IdTipoOperacion) &&infoComprobante.IdTipoProducto.equalsIgnoreCase("INSI")), 
					"CMBTE_TIPO_COMPROBANTE_ANULAR_NOTA_CREDITO", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
			
			ProcesaRequisito(conn, in.p_IdEmpresa, _c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante) 
					&& (infoComprobante.IdTipoProducto.equalsIgnoreCase("EVCA")), 
					"CMBTE_TIPO_COMPROBANTE_ANULAR_NOTA_CREDITO", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);


		}
		

		//TODO VALIDACIONES
		//SI ES VENTA CORTA NO PUEDE ESTAR ASIGNADA
		//COMPROBANTE PENDIENTE DE ANULACION
		//SI ES PERU, EL PERIODO NO DEBE ESTAR CERRADO	
		
		
		limpiaComprobantes(comprobantes, comprobantesConError, out.p_out_ResultadoProceso);
		if (comprobantes.size() == 0) {
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
		//para buscar los movs asociados a los comprobantes
		List<Long> idsComprobante = new ArrayList<>();
		for (Comprobante_Proceso cmbte : comprobantes ) {
			idsComprobante.add(cmbte.IdComprobante);
		}
		
		List<Long> idsComprobanteNC = new ArrayList<>();
		for (Comprobante_Proceso cmbte : comprobantes ) {
			Comprobante infoComprobante = (Comprobante) hashComprobantes.get(cmbte.IdComprobante);
			if (AnularNotaCredito(infoComprobante,eventoCapitalNC)) {
			    idsComprobanteNC.add(cmbte.IdComprobante);
			    idsComprobante.remove(cmbte.IdComprobante);
			}		
		}
		
		if(idsComprobanteNC.size()>0){
			List<ComprobanteNC_Entrada> compNC = new ArrayList<>();
			for (Comprobante_Proceso cmbte : comprobantes ) {
				if(idsComprobanteNC.contains(cmbte.IdComprobante)) {		
					ComprobanteNC_Entrada arg0 = new ComprobanteNC_Entrada();
					arg0.IdComprobante = cmbte.IdComprobante;		
					compNC.add(arg0);
				}				
			}
			
			comprobantes=comprobantes.stream().filter(ca -> !idsComprobanteNC.contains(ca.IdComprobante)).collect(Collectors.toList());
			
			AnularComprobanteNotaDeCredito_Entrada entradaAnularComprobanteNotaDeCredito = new AnularComprobanteNotaDeCredito_Entrada();
			entradaAnularComprobanteNotaDeCredito.p_IdEmpresa=in.p_IdEmpresa;
			entradaAnularComprobanteNotaDeCredito.p_IdUsuario=in.p_IdUsuario;
			entradaAnularComprobanteNotaDeCredito.p_IdModulo=in.p_IdModulo;
			entradaAnularComprobanteNotaDeCredito.p_simularOperacion =in.p_simularOperacion;
			entradaAnularComprobanteNotaDeCredito.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaAnularComprobanteNotaDeCredito.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
			entradaAnularComprobanteNotaDeCredito.p_EjecutaAutorizacion = in.p_EjecutaAutorizacion;
			entradaAnularComprobanteNotaDeCredito.p_OmiteRegistroSinAutorizar = in.p_OmiteRegistroSinAutorizar;
			entradaAnularComprobanteNotaDeCredito.p_Glosa = in.p_Glosa;
			entradaAnularComprobanteNotaDeCredito.P_COMPROBANTEAANULAR=compNC;
			
			AnularComprobanteNotaDeCredito anularComprobanteNotaDeCreditoService = new AnularComprobanteNotaDeCredito();
			AnularComprobanteNotaDeCredito_Salida salidaAnularComprobanteNotaDeCredito = anularComprobanteNotaDeCreditoService.logica(conn, entradaAnularComprobanteNotaDeCredito);
			logger.debug("Salida AnularComprobanteNotaDeCredito: " + salidaAnularComprobanteNotaDeCredito.toString());
			out.P_OUT_MENSAJES.addAll(salidaAnularComprobanteNotaDeCredito.P_OUT_MENSAJES);
			for (AnularComprobanteNotaDeCredito.ResultadoProceso_Salida cte : salidaAnularComprobanteNotaDeCredito.p_out_ResultadoProceso) {
					ResultadoProceso_Salida res = new ResultadoProceso_Salida();
					res.Campo = cte.Campo;
					res.Id = cte.Id;
					res.Servicio = cte.Servicio;
					res.Resultado = cte.Resultado;
					out.p_out_ResultadoProceso.add(res);
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
			params.put("idsComprobante",idsComprobante);
			List<MovimientoCarteraEnTransito_Proceso> transitos = _sql.query(conn, SQL_SELECT_CUSTODIA_TRANSITOS, params, MovimientoCarteraEnTransito_Proceso.class);
			
			List<ObjMovimientosAActualizar> movimientosCarteraEnTransito = new ArrayList<>();
			for (MovimientoCarteraEnTransito_Proceso transito : transitos) {
				
				Comprobante_Proceso cmbte = (Comprobante_Proceso) hashComprobantesProceso.get(transito.FolioComprobante);
				if (cmbte != null) {
					cmbte.movsCarteraTransito.add(transito);
				}
				
				ObjMovimientosAActualizar arg0 = new ObjMovimientosAActualizar();
				arg0.EsAnulacion = "S";
				arg0.EsCertificacion = "N";
				arg0.IdMovimientoCarteraEnTransito = transito.IdMovimientoCarteraEnTransito;
				arg0.IdUsuario = in.p_IdUsuario;
				movimientosCarteraEnTransito.add(arg0 );
			}
			if (movimientosCarteraEnTransito.size() > 0 && in.p_simularOperacion.equalsIgnoreCase("N")) {
				boolean flag= GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, movimientosCarteraEnTransito );
				if(flag) {
					for(ObjMovimientosAActualizar objList:movimientosCarteraEnTransito ) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(objList.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_OK,"COMPROBANTE_ANULADO","El movimiento en transito "+objList.IdMovimientoCarteraEnTransito+" fue anulado con exito"));
					}
					
				}
			}
		}
		
		//OBTIENE REGISTROS PARA ANULAR EN CUSTODIA
		{
			Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
			HashMap<String,Object> params = new HashMap<>();
			params.put("idsComprobante",idsComprobante);
			carterasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, params, Cartera_Proceso.class);
			garantiaCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_GARANTIAS_COMPRAS, params, Cartera_Proceso.class);
			
			if(pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S")) {
				RetiroCustodia retiroCustodiaService = new RetiroCustodia();
				RetiroCustodiaEntrada retiroCustodiaEntrada = new RetiroCustodiaEntrada();
				
				for (Cartera_Proceso cartera : carterasCompra) {
					if(cartera.IdTipoOperacion.equalsIgnoreCase("COMSIM")) {
						List<RetiroCustodia.ObjDatosEntrada> p_DatosGarantia = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
						for(Cartera_Proceso garantia : garantiaCompra) {
							if(cartera.FolioComprobante.equals(garantia.FolioComprobante) && garantia.IdTipoOperacion.equalsIgnoreCase("COMSIM")) {
								RetiroCustodia.ObjDatosEntrada RetiroGar = new RetiroCustodia.ObjDatosEntrada();
								RetiroGar.IdGenerarMovimientoCustodia = garantia.IdGarantia;
								RetiroGar.TablaAfectada = "GARANTIA";
								RetiroGar.AccionOperacion = "ANULACION";
								RetiroGar.IdCustodia = garantia.IdGarantia;
								RetiroGar.FechaRetiroRegistro = fechaHoraSistema;
								RetiroGar.FechaOperacion = Fecha.toDate(fechaSistema);
								RetiroGar.IdInstrumento = garantia.IdInstrumento;										
								RetiroGar.Cantidad = garantia.Cantidad;
								RetiroGar.ValorRescate = garantia.ValorRescate;
								RetiroGar.IdCliente = garantia.IdCliente;
								RetiroGar.IdPortafolio = garantia.IdPortafolio;
								RetiroGar.PrecioCompra = garantia.INGPrecioCompra;
								RetiroGar.IdOrigenGarantia = cartera.IdOrigenGarantia;
								RetiroGar.IdConcepto = garantia.IdConcepto;
								RetiroGar.IdTipoOperacion = garantia.IdTipoOperacion;
								RetiroGar.EsFungible = garantia.EsFungible;
								RetiroGar.IdEstado = garantia.IdEstado;
								p_DatosGarantia.add(RetiroGar);
							}
						}
						retiroCustodiaEntrada.p_IdEmpresa			= in.p_IdEmpresa;
						retiroCustodiaEntrada.p_IdModulo			= in.p_IdModulo;
						retiroCustodiaEntrada.p_IdUsuario			= in.p_IdUsuario;
						retiroCustodiaEntrada.p_SimularOperacion	= "N";
						retiroCustodiaEntrada.p_TicketAtencion		= 0L;
						retiroCustodiaEntrada.p_DatosEntrada		= p_DatosGarantia;
						garantiaCompra.removeIf(gar -> gar.FolioComprobante.equals(cartera.FolioComprobante) && gar.IdTipoOperacion.equalsIgnoreCase("COMSIM"));
					}
				}
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
				carterasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, params, Cartera_Proceso.class);
			}
			
			carterasVenta = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_VENTAS, params, Cartera_Proceso.class);
			carterasNegativas = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_NEGATIVAS, params, Cartera_Proceso.class);
			garantiaAnticipo = _sql.query(conn, SQL_SELECT_CUSTODIA_GARANTIAS_ANTICIPO, params, Cartera_Proceso.class);
			subyacentes = _sql.query(conn, SQL_SELECT_CUSTODIA_SUBYACENTES, params, Subyacente_Proceso.class);
			
			if (carterasVenta.size() > 0) {
				carterasVenta.removeIf(c -> !(idsComprobante.contains(c.EGRFolioComprobante) || "VIG".equals(c.IdEstado))
											&& ("COMSIM".equals(c.IdTipoOperacion) || "VENSIM".equals(c.IdTipoOperacion)));

				subyacentes.removeIf(sub -> !(idsComprobante.contains(sub.EGRFolioComprobante) || "VIG".equals(sub.IdEstadoSubyacente))
											&& ("COMSIM".equals(sub.IdTipoOperacion) || "VENSIM".equals(sub.IdTipoOperacion)));
			}
			
			HashMap<Long,List<AnulacionCompraNormalCustodia>> hashComprobantesAnulacionCompras = new HashMap<>();
			HashMap<Long,List<AnulacionVentaNormalCustodia>> hashComprobantesAnulacionVentas = new HashMap<>();
			HashMap<Long,List<AnulacionVentaSimultanea>> hashComprobantesAnulacionSimultanea = new HashMap<>();

			HashMap<Long,RetiroCustodia.ObjDatosEntrada> carterasAnular = new HashMap<Long,RetiroCustodia.ObjDatosEntrada>();
			HashMap<Long,IngresoCustodia.ObjDatosEntrada> carterasIngresos = new HashMap<Long,IngresoCustodia.ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> carterasRetiro = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			List<IngresoCustodia.ObjDatosEntrada> carterasReingreso = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			HashMap<Long,Long> folioCarteraComprobante = new HashMap<Long,Long>();
			
			
			//VALIDAMOS LAS CARTERAS ANTES DE ANULAR
			Map<Long, List<Cartera_Proceso>> carterasCompraAgrupadasPorIdComprobante = carterasCompra.stream().collect(Collectors.groupingBy(cartera->cartera.FolioComprobante));
			for (Comprobante_Proceso cmbte : comprobantes) {
				List<Cartera_Proceso> carterasCompraComprobante = carterasCompraAgrupadasPorIdComprobante.get(cmbte.IdComprobante);
				if(carterasCompraComprobante != null) {
					for (Cartera_Proceso cartera : carterasCompraComprobante) {
						ProcesaRequisito(conn, in.p_IdEmpresa, !"VIG".equalsIgnoreCase(cartera.IdEstado), "ANULAR_COMPROBANTE_CARTERA_NO_VIGENTE", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
						ProcesaRequisito(conn, in.p_IdEmpresa, "VIG".equalsIgnoreCase(cartera.IdEstado) && cartera.Cantidad.compareTo(cartera.INGCantidad) != 0, "ANULAR_COMPROBANTE_CARTERA_SALDO_INFUCIENTE", MensajeSalida.MENSAJE_ERROR, cmbte, out.P_OUT_MENSAJES);
					}
				}
			}
			
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
				
				out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
				out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				logger.info("Fin");
				return out;
			}
			
			
			//ANULACION DE CUSTODIA
			carterasCompra.addAll(garantiaCompra);
			
			for (Cartera_Proceso cartera : carterasCompra) {
				
				Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(cartera.FolioComprobante);
				if (info != null) {
					info.carteras.add(cartera);
				}
				
				if (cartera.FolioComprobante.equals(info.IdComprobante)) {
					ObjDatosEntrada anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
					
					anulacionCartera.IdGenerarMovimientoCustodia = (!cartera.TipoCartera.equals("GARANTIA")? cartera.IdCartera: cartera.IdGarantia);
					anulacionCartera.TablaAfectada = cartera.TipoCartera;
					anulacionCartera.IdCustodia = (!cartera.TipoCartera.equals("GARANTIA")) ? cartera.IdCartera : cartera.IdGarantia;
					anulacionCartera.FolioCustodia = (!cartera.TipoCartera.equals("GARANTIA")? cartera.FolioCartera: 0L);
					anulacionCartera.AccionOperacion = "ANULACION";
					anulacionCartera.FechaRetiroRegistro = fechaHoraSistema;
					anulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
					anulacionCartera.IdInstrumento = cartera.IdInstrumento;										
					anulacionCartera.Cantidad = cartera.Cantidad;
					anulacionCartera.ValorRescate = cartera.ValorRescate;
					anulacionCartera.IdCliente = cartera.IdCliente;
					anulacionCartera.IdPortafolio = cartera.IdPortafolio;
					anulacionCartera.PrecioCompra = cartera.INGPrecioCompra;
					anulacionCartera.IdOrigenGarantia = cartera.IdOrigenGarantia;
					anulacionCartera.IdConcepto = cartera.IdConcepto;
					anulacionCartera.IdTipoOperacion = cartera.IdTipoOperacion;
					anulacionCartera.EsFungible = cartera.EsFungible;
					
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
			}
			
			for (Cartera_Proceso cartera : garantiaAnticipo) {

				//Valida existencia de garantia anticipada
				Cartera_Proceso carteraAnticipada = null;
				for (Cartera_Proceso carteraAnt : garantiaAnticipo) {
					if (carteraAnt.IdEstado.equalsIgnoreCase("ANT") && carteraAnt.FolioCartera.compareTo(cartera.FolioCartera) == 0) {
						carteraAnticipada = carteraAnt;
						break;
					}
				}
				if(carteraAnticipada != null) {

					if ("ANT".equalsIgnoreCase(cartera.IdEstado)) {
						//BUSCAMOS LA VIG PARA ANULARLA
						Cartera_Proceso carteraVigente = null;
						for (Cartera_Proceso carteraVig : garantiaAnticipo) {
							if (carteraVig.IdEstado.equalsIgnoreCase("VIG") && carteraVig.FolioCartera.compareTo(cartera.FolioCartera) == 0) {
								carteraVigente = carteraVig;
								break;
							}
						}
						
						if(carteraVigente != null && carterasAnular.get(carteraVigente.FolioCartera) == null){
							IngresoCustodia.ObjDatosEntrada	reingresoCartera = carterasIngresos.get(cartera.IdGarantia);

							if(reingresoCartera == null) {
								reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
								reingresoCartera.IdGenerarMovimientoCustodia	= cartera.IdGarantia;
								reingresoCartera.TablaAfectada 					= "GARANTIA";
								reingresoCartera.AccionOperacion				= "INGRESO";		
								reingresoCartera.IdCliente						= cartera.IdCliente;
								reingresoCartera.IdPortafolio					= cartera.IdPortafolio;
								reingresoCartera.FolioCustodia					= cartera.FolioCartera;
								reingresoCartera.IdCustodia						= carteraVigente.IdGarantia;
								reingresoCartera.Cantidad						= cartera.EGRCantidad.add(carteraVigente.INGCantidad);
								reingresoCartera.IdInstrumento					= cartera.IdInstrumento;
								reingresoCartera.ValorRescate					= cartera.INGValorRescate;
								reingresoCartera.FechaOperacion					= Fecha.toDate(fechaSistema);
								reingresoCartera.IdOrigenGarantia 				= cartera.IdOrigenGarantia;
								reingresoCartera.PrecioCompra 					= cartera.INGPrecioCompra;
								reingresoCartera.TasaCompra						= cartera.TasaCompra;
								reingresoCartera.EsRegistroNuevo				= "N";
								reingresoCartera.GenerarNuevoFolioCustodia		= "N";
								carterasReingreso.add(reingresoCartera);
							}
							
						}
						
					}
				}
				
			}
			
			for (Cartera_Proceso cartera : carterasVenta) {
				
				//SOLO PROCESAMOS DIRECTAMENTE LAS RPA Y LIQ, LAS VIG SON INTERNAS
				if (cartera.IdEstado.equalsIgnoreCase("RPA") || cartera.IdEstado.equalsIgnoreCase("LIQ") || cartera.IdEstado.equalsIgnoreCase("ANT") ) {
					
					Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(cartera.EGRFolioComprobante);
					
					
					if (info != null) {
						info.carteras.add(cartera);
					}
					else {
						continue;
					}
					
					RetiroCustodia.ObjDatosEntrada anulacionCartera = null;
					
					if ("RPA".equalsIgnoreCase(cartera.IdEstado)||"ANT".equalsIgnoreCase(cartera.IdEstado)) {
						//BUSCAMOS LA VIG PARA ANULARLA
						Cartera_Proceso carteraVigente = null;
						for (Cartera_Proceso carteraVig : carterasVenta) {
							if (carteraVig.IdEstado.equalsIgnoreCase("VIG") && carteraVig.FolioCartera.compareTo(cartera.FolioCartera) == 0) {
								carteraVigente = carteraVig;
								break;
							}
						}
						
						if(carteraVigente != null && carterasAnular.get(carteraVigente.FolioCartera) == null){
							anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
							anulacionCartera.IdGenerarMovimientoCustodia	= cartera.IdCartera * -1;
							anulacionCartera.TablaAfectada 					= "CARTERA";
							anulacionCartera.AccionOperacion				= "ANULACION";		
							anulacionCartera.IdCliente						= carteraVigente.IdCliente;
							anulacionCartera.IdPortafolio					= carteraVigente.IdPortafolio;
							anulacionCartera.FolioCustodia					= carteraVigente.FolioCartera;
							anulacionCartera.FechaLiquidacion				= carteraVigente.INGFechaLiquidacion;
							anulacionCartera.IdCustodia						= carteraVigente.IdCartera;
							anulacionCartera.Cantidad						= carteraVigente.INGCantidad;
							anulacionCartera.IdInstrumento					= carteraVigente.IdInstrumento;
							anulacionCartera.ValorRescate					= carteraVigente.INGValorRescate;
							anulacionCartera.IdModulo						= "FAC";
							anulacionCartera.FechaOperacion					= Fecha.toDate(fechaSistema);
							anulacionCartera.PrecioCompra                   = carteraVigente.INGPrecioCompra;
							anulacionCartera.PlazoEnDiasOperacion           = carteraVigente.PlazoEnDiasOperacion;
							anulacionCartera.IdEmisor                       = carteraVigente.IdEmisor;
							anulacionCartera.IdTipoReajusteInstrumento      = carteraVigente.IdTipoReajuste; //aqui
							anulacionCartera.IdMonedaEmision                = carteraVigente.IdMonedaEmision;
							anulacionCartera.PrecioAPlazo					= carteraVigente.PrecioAPlazo;
							anulacionCartera.MontoAPlazo					= carteraVigente.MontoAPlazo;
							carterasAnular.put(anulacionCartera.FolioCustodia, anulacionCartera);
						}
						
					}
					
					IngresoCustodia.ObjDatosEntrada	reingresoCartera = carterasIngresos.get(cartera.FolioCartera);
					if(reingresoCartera == null) {
						reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
						reingresoCartera.IdGenerarMovimientoCustodia	= cartera.IdCartera;
						reingresoCartera.TablaAfectada 					= "CARTERA";
						reingresoCartera.AccionOperacion				= "INGRESO";		
						reingresoCartera.IdCliente						= cartera.IdCliente;
						reingresoCartera.IdPortafolio					= cartera.IdPortafolio;
						reingresoCartera.FolioCustodia					= cartera.FolioCartera;
						reingresoCartera.IdCustodia						= cartera.IdCartera;
						reingresoCartera.Cantidad						= cartera.EGRCantidad;
						reingresoCartera.IdInstrumento					= cartera.IdInstrumento;
						reingresoCartera.ValorRescate					= cartera.EGRValorRescate;
						reingresoCartera.FechaOperacion					= Fecha.toDate(fechaSistema);
						reingresoCartera.EsRegistroNuevo				= "N";
						reingresoCartera.GenerarNuevoFolioCustodia		= "N";
						reingresoCartera.PlazoEnDiasOperacion			= cartera.PlazoEnDiasOperacion;
						reingresoCartera.IdEmisor                       = cartera.IdEmisor; ///aqui
						reingresoCartera.IdTipoReajusteInstrumento      = cartera.IdTipoReajuste;
						reingresoCartera.IdMonedaEmision                = cartera.IdMonedaEmision;		
						reingresoCartera.PrecioCompra                   = cartera.INGPrecioCompra;
						reingresoCartera.FechaLiquidacion				= cartera.INGFechaLiquidacion;
						reingresoCartera.TasaCompra						= cartera.TasaCompra;
						reingresoCartera.PrecioAPlazo					= cartera.PrecioAPlazo;
						reingresoCartera.MontoAPlazo					= cartera.MontoAPlazo;
						
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

			for (Subyacente_Proceso suby : subyacentes) {
				
				Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(suby.FolioComprobante);
				if (info != null) {
					info.subyacentes.add(suby);
				}
				
				ObjDatosEntrada anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
				ObjDatosEntrada anulacionSubyacente = new RetiroCustodia.ObjDatosEntrada();
				
				anulacionSubyacente.IdGenerarMovimientoCustodia = suby.IdSubyacente;
				anulacionSubyacente.TablaAfectada = "SUBYACENTE";
				anulacionSubyacente.IdCustodia = suby.IdSubyacente;
				anulacionSubyacente.FolioCustodia = suby.FolioSubyacente;
				anulacionSubyacente.IdTipoOperacion = suby.IdTipoOperacionAnulacion;
				anulacionSubyacente.AccionOperacion = "ANULACION";
				anulacionSubyacente.FechaRetiroRegistro = fechaHoraSistema;
				anulacionSubyacente.FechaOperacion = Fecha.toDate(fechaSistema);
				anulacionSubyacente.IdInstrumento = suby.IdInstrumento;
				anulacionSubyacente.Cantidad = suby.INGCantidad;
				anulacionSubyacente.ValorRescate = suby.INGValorRescate;
				anulacionSubyacente.IdCliente = suby.IdCliente;
				anulacionSubyacente.IdPortafolio = suby.IdPortafolio;

				List<AnulacionVentaSimultanea> lista = hashComprobantesAnulacionSimultanea.get(info.IdComprobante);
				
				if(lista != null) {
					for (AnulacionVentaSimultanea obj : lista) {
						if (obj.FolioCartera.compareTo(suby.FolioCarteraRelacionada) == 0) {
							obj.pasoRetiroSubyacente.add(anulacionSubyacente);
						}
					}	
				}

				if("ANT".equalsIgnoreCase(suby.IdEstadoSubyacente) ) {

					//BUSCAMOS LA VIG PARA RETIRARLA
					Subyacente_Proceso subyacenteVigente = null;
					for (Subyacente_Proceso subyacenteVig : subyacentes) {
						if (subyacenteVig.IdEstadoSubyacente.equalsIgnoreCase("VIG") && suby.FolioSubyacente.compareTo(subyacenteVig.FolioSubyacente) == 0) {
							subyacenteVigente = subyacenteVig;
							break;
						}
					}

					if(subyacenteVigente != null && ("COMSIM".equals(suby.IdTipoOperacion) || ("ANVSIM").equals(suby.IdTipoOperacionAnulacion)) ) { //se consideran las anulaciones de ANTICIPO VENTA SIMULTANEA
						anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
						anulacionCartera.IdGenerarMovimientoCustodia	= 1L;
						anulacionCartera.TablaAfectada 					= "SUBYACENTE";
						anulacionCartera.AccionOperacion				= "ANULACION";		
						anulacionCartera.IdCliente						= subyacenteVigente.IdCliente;
						anulacionCartera.IdPortafolio					= subyacenteVigente.IdPortafolio;
						anulacionCartera.FolioCustodia					= subyacenteVigente.FolioSubyacente;
						anulacionCartera.FechaLiquidacion				= carterasIngresos.get(suby.FolioCartera).FechaLiquidacion;
						anulacionCartera.IdCustodia						= subyacenteVigente.IdSubyacente;
						anulacionCartera.Cantidad						= subyacenteVigente != null ? subyacenteVigente.INGCantidad : suby.INGCantidad;
						anulacionCartera.IdInstrumento					= suby.IdInstrumento;
						anulacionCartera.ValorRescate					= subyacenteVigente != null ? suby.INGValorRescate.subtract(subyacenteVigente.INGValorRescate) : suby.INGValorRescate ;
						anulacionCartera.IdModulo						= "FAC";
						anulacionCartera.FechaOperacion					= Fecha.toDate(fechaSistema);
						anulacionCartera.PrecioCompra                   = carterasIngresos.get(suby.FolioCartera).PrecioCompra;
						anulacionCartera.PlazoEnDiasOperacion           = carterasIngresos.get(suby.FolioCartera).PlazoEnDiasOperacion;
						anulacionCartera.IdEmisor                       = suby.IdEmisor;
						anulacionCartera.IdTipoReajusteInstrumento      = carterasIngresos.get(suby.FolioCartera).IdTipoReajusteInstrumento; //aqui
						anulacionCartera.IdMonedaEmision                = carterasIngresos.get(suby.FolioCartera).IdMonedaEmision;
						anulacionCartera.SubyacenteRetornaCartera		= !("ANVSIM").equals(suby.IdTipoOperacionAnulacion) ? "S" : "N";
					}

					
		
					IngresoCustodia.ObjDatosEntrada	reingresoCartera = carterasIngresos.get(suby.IdSubyacente);
					if(reingresoCartera == null) {
						reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
						reingresoCartera.IdGenerarMovimientoCustodia	= suby.IdSubyacente;
						reingresoCartera.TablaAfectada 					= "SUBYACENTE";
						reingresoCartera.AccionOperacion				= "INGRESO";		
						reingresoCartera.IdCliente						= suby.IdCliente;
						reingresoCartera.IdPortafolio					= suby.IdPortafolio;
						reingresoCartera.FolioCustodia					= suby.FolioCartera;
						reingresoCartera.IdCustodia						= suby.IdSubyacente;
						reingresoCartera.Cantidad						= suby.INGCantidad;
						reingresoCartera.IdInstrumento					= suby.IdInstrumento;
						reingresoCartera.ValorRescate					= suby.INGValorRescate;
						reingresoCartera.FechaOperacion					= Fecha.toDate(fechaSistema);
						reingresoCartera.EsRegistroNuevo				= "N";
						reingresoCartera.GenerarNuevoFolioCustodia		= "N";
						folioCarteraComprobante.put(reingresoCartera.FolioCustodia, info.IdComprobante);
						
					}

					List<AnulacionVentaNormalCustodia> listaVentas = hashComprobantesAnulacionVentas.get(suby.IdSubyacente);
					if(listaVentas == null) {
						listaVentas = new ArrayList<>();
					}
					AnulacionVentaNormalCustodia anulacion = new AnulacionVentaNormalCustodia(
							subyacenteVigente != null && ("COMSIM".equals(suby.IdTipoOperacion) || ("ANVSIM").equals(suby.IdTipoOperacionAnulacion)) ? anulacionCartera : null,reingresoCartera  //en el ternario se consideran tambien las anulaciones de ANTICIPO VENTA SIMULTANEA
							);
					listaVentas.add(anulacion);
					hashComprobantesAnulacionVentas.put(suby.IdSubyacente,listaVentas);
					
					if(!"COMSIM".equals(suby.IdTipoOperacion)) {
						anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
						anulacionCartera.IdGenerarMovimientoCustodia	= suby.IdCarteraRetirada;
						anulacionCartera.TablaAfectada 					= "CARTERA";
						anulacionCartera.AccionOperacion				= "RETIRO";		
						anulacionCartera.IdCliente						= suby.IdCliente;
						anulacionCartera.IdPortafolio					= suby.IdPortafolio;
						anulacionCartera.FolioCustodia					= !("ANVSIM").equals(suby.IdTipoOperacionAnulacion) ? 0L : suby.FolioCarteraRetirada;
						anulacionCartera.FechaLiquidacion				= carterasIngresos.get(suby.FolioCartera).FechaLiquidacion;
						anulacionCartera.IdCustodia						= 0L;
						anulacionCartera.Cantidad						= subyacenteVigente != null ? suby.INGCantidad.subtract(subyacenteVigente.INGCantidad) : suby.INGCantidad;
						anulacionCartera.IdInstrumento					= suby.IdInstrumento;
						anulacionCartera.ValorRescate					= subyacenteVigente != null ? suby.INGValorRescate.subtract(subyacenteVigente.INGValorRescate) : suby.INGValorRescate ;
						anulacionCartera.IdModulo						= "FAC";
						anulacionCartera.FechaOperacion					= Fecha.toDate(fechaSistema);
						anulacionCartera.PrecioCompra                   = carterasIngresos.get(suby.FolioCartera).PrecioCompra;
						anulacionCartera.PlazoEnDiasOperacion           = carterasIngresos.get(suby.FolioCartera).PlazoEnDiasOperacion;
						anulacionCartera.IdEmisor                       = suby.IdEmisor;
						anulacionCartera.IdTipoReajusteInstrumento      = carterasIngresos.get(suby.FolioCartera).IdTipoReajusteInstrumento; //aqui
						anulacionCartera.IdMonedaEmision                = carterasIngresos.get(suby.FolioCartera).IdMonedaEmision;
						carterasRetiro.add(anulacionCartera);
					}
				}
				
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
			
			//anulacion carteras negativas
			List<CarteraNegativa_Entrada> cneg_list = new ArrayList<>();
			for (Cartera_Proceso cneg : carterasNegativas) {
				CarteraNegativa_Entrada n = new CarteraNegativa_Entrada();
				n.IdCarteraNegativa = cneg.IdCartera;
				cneg_list.add(n);
			}
			
			if (cneg_list.size() > 0) {
				AnularCarteraNegativa anulacionCNeg = new AnularCarteraNegativa();
				AnularCarteraNegativa_Entrada cneg_in = new AnularCarteraNegativa_Entrada();
				cneg_in.p_IdEmpresa = in.p_IdEmpresa;
				cneg_in.p_IdModulo = in.p_IdModulo;
				cneg_in.p_IdUsuario = in.p_IdUsuario;
				cneg_in.p_simularOperacion = in.p_simularOperacion;
				cneg_in.p_CarterasNegativas = cneg_list;
				AnularCarteraNegativa_Salida cneg_out = anulacionCNeg.logica(conn, cneg_in);
				out.P_OUT_MENSAJES.addAll(cneg_out.P_OUT_MENSAJES);
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
			_sql.batch(conn, SQL_UPDATE_ANTICIPO_CARTERA, carterasVenta, Cartera_Proceso.class);
			_sql.batch(conn, SQL_UPDATE_ANTICIPO_SUBYACENTE, subyacentes, Subyacente_Proceso.class);
			_sql.batch(conn, SQL_UPDATE_ANTICIPO_GARANTIA, garantiaAnticipo, Cartera_Proceso.class);
					
			//ANULAR MOVIMIENTO COMPROBANTE -> PARAMETRO
			{
				AnularMovimientoComprobante_Entrada entradaAnularMovComprobante = new AnularMovimientoComprobante_Entrada();
				idsComprobante.clear();
				for (Comprobante c : comprobantes) {
					idsComprobante.add(c.IdComprobante);
				}
				
				idsComprobanteNC.clear();
				for (Comprobante_Proceso cmbte : comprobantes ) {
					Comprobante infoComprobante = (Comprobante) hashComprobantes.get(cmbte.IdComprobante);
					if (AnularNotaCredito(infoComprobante,eventoCapitalNC)){
					    idsComprobanteNC.add(cmbte.IdComprobante);
					    idsComprobante.remove(cmbte.IdComprobante);
					}							
				}
			
				if(idsComprobante.size()>0){
					HashMap<String,Object> params = new HashMap<>();
					params.put("idsComprobante",idsComprobante);
					List<MovimientoComprobante> movsAnular = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE, params , MovimientoComprobante.class);
					
					if (movsAnular.size() > 0) {
						for (MovimientoComprobante mov : movsAnular) {
							Movimiento_Entrada arg0 = new Movimiento_Entrada();
							arg0.IdMovimientoComprobante = mov.IdMovimientoComprobante;
							entradaAnularMovComprobante.p_MovimientoAAnular.add(arg0);
						
						}
						
						_sql.batch(conn, SQL_UPDATE_MOVIMIENTO_COMPROBANTE, movsAnular, MovimientoComprobante.class);					
					}
					
					entradaAnularMovComprobante.p_EsIngresoAutomatico = "S";
					entradaAnularMovComprobante.p_Glosa = in.p_Glosa;
					entradaAnularMovComprobante.p_IdEmpresa = in.p_IdEmpresa;
					entradaAnularMovComprobante.p_IdModulo = in.p_IdModulo;
					entradaAnularMovComprobante.p_IdUsuario = in.p_IdUsuario;
					entradaAnularMovComprobante.p_simularOperacion = in.p_simularOperacion;
					entradaAnularMovComprobante.p_UsaReglasNegocio = "S";
					
					AnularMovimientoComprobante anularMovComprobanteService = new AnularMovimientoComprobante();
					AnularMovimientoComprobante_Salida salidaAnularMovComprobante = anularMovComprobanteService.logica(conn, entradaAnularMovComprobante );
					logger.debug("Salida AnularMovimientoComprobante: " + salidaAnularMovComprobante.toString());
					
					out.P_OUT_MENSAJES.addAll(salidaAnularMovComprobante.P_OUT_MENSAJES);
					
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
			if(!idsComprobanteNC.contains(cte.IdComprobante)) {
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
			
		}
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		List<UpdateEventoCapitalCliente> upds = new ArrayList<UpdateEventoCapitalCliente>();
		UpdateEventoCapitalCliente upd = new UpdateEventoCapitalCliente();
		
		upd = new UpdateEventoCapitalCliente();
		upd.IdEventoCapitalCliente = 0L;		
		upd.IdComprobanteFac = in.P_COMPROBANTEAANULAR.get(0).IdComprobante;
		upds.add(upd);
		
		_sql.batch(conn, SQL_UPDATE_ESTADO_EVENTO_CAPITAL_CLIENTE, upds, UpdateEventoCapitalCliente.class);
		
		if(out.P_OUT_ERRORESCONTADOR == 0L) {
			AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa,"ANUFACT","Refresco Anulacion Facturacion" );
		}
		
		logger.info("Fin AnularComprobanteFinal");
		return out;
	}
	

	private boolean AnularNotaCredito(Comprobante infoComprobante,List<Long>eventoCapitalNC) {
	
	    boolean esProductoExcluido =
	    			"INSI".equalsIgnoreCase(infoComprobante.IdTipoProducto)
	    			|| "EVCA".equalsIgnoreCase(infoComprobante.IdTipoProducto);

	    boolean esTipoNotaCredito =_c.tiposNotaCredito.contains(infoComprobante.IdTipoComprobante);
	    boolean esNEEValida =
	    		"NEE".equalsIgnoreCase(infoComprobante.IdTipoComprobante)
	    		&& !eventoCapitalNC.contains(infoComprobante.IdComprobante);	
	    boolean esNEO =
	    		"NEO".equalsIgnoreCase(infoComprobante.IdTipoComprobante);
	
	    return !esProductoExcluido && (esTipoNotaCredito || esNEEValida || esNEO);
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
		public Long FolioCartera;
		public List<RetiroCustodia.ObjDatosEntrada> pasoRetiroGarantia;
		
		public AnulacionCompraNormalCustodia(RetiroCustodia.ObjDatosEntrada dato) {
			this.pasoAnulacionCartera = dato;
			this.FolioCartera = dato.FolioCustodia;
			this.pasoRetiroGarantia = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	// CLASES IN
	// ------------------------------------------------------------------------------
	public static class Comprobante_Entrada {
		public Long IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class AnularComprobante_Entrada {
	
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_simularOperacion = "N";
		public String 	p_UsaReglasNegocio = "S";
		public String	p_EsIngresoAutomatico = "N";
		public String	p_EjecutaAutorizacion = "S";
		public String	p_OmiteRegistroSinAutorizar = "S";
		public String 	p_Glosa = "";
		
		public List<Comprobante_Entrada> P_COMPROBANTEAANULAR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public AnularComprobante_Entrada() {
			P_COMPROBANTEAANULAR = new ArrayList<Comprobante_Entrada>();
		}
	}

	// CLASES OUT
	// ------------------------------------------------------------------------------	
	public static class AnularComprobante_Salida {
		public List<Comprobante_Salida>					P_OUT_COMPROBANTEANULADO;
		public List<PlataformaUtils.MensajeSalida> 		P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> 			p_out_ResultadoProceso;
		
		public AnularComprobante_Salida() {
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
					+ "    \"p_IdUsuario\": 9787,\r\n"
					+ "    \"p_IdModulo\": \"FAC\",\r\n"
					+ "    \"p_simularOperacion\": \"N\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_EsIngresoAutomatico\": \"N\",\r\n"
					+ "    \"p_EjecutaAutorizacion\": \"N\",\r\n"
					+ "    \"p_OmiteRegistroSinAutorizar\": \"S\",\r\n"
					+ "    \"p_Glosa\": \"Anulado por Sistema\",\r\n"
					+ "    \"P_COMPROBANTEAANULAR\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdComprobante\": 2703204,\r\n"
					+ "            \"IdPortafolio\": 2225,\r\n"
					+ "            \"IdCliente\": 4846,\r\n"
					+ "            \"IdTipoComprobante\": \"NCR\"\r\n"
					+ "        },\r\n"
					+ "		{\r\n"
					+ "            \"IdComprobante\": 2703218,\r\n"
					+ "            \"IdPortafolio\": 1445,\r\n"
					+ "            \"IdCliente\": 3189,\r\n"
					+ "            \"IdTipoComprobante\": \"NEE\"\r\n"
					+ "        },\r\n"
					+ "		{\r\n"
					+ "            \"IdComprobante\": 2703237,\r\n"
					+ "            \"IdPortafolio\": 1445,\r\n"
					+ "            \"IdCliente\": 3189,\r\n"
					+ "            \"IdTipoComprobante\": \"NEE\"\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			AnularComprobante_Entrada Objin = gson.fromJson(json, AnularComprobante_Entrada.class);
			AnularComprobanteFinal demo=new AnularComprobanteFinal();
 			AnularComprobante_Salida ObjOut=demo.logica(con, Objin);
 			System.out.println(ObjOut);
 			//con.commit();
 			con.rollback();
	    }

		System.out.println("== END ==");
	}

	
}

	