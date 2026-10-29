package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaDetallado;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.TramoComision;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularComisiones implements JavaCall {

	static final Logger logger = LogManager.getLogger(CalcularComisiones.class);

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_TRAMO_COMISION = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTramoComision.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	
	public static class EntradaCalcularComisiones{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InCustodia> p_DatosCustodia;

		public EntradaCalcularComisiones() {
			this.p_DatosCustodia = new ArrayList<InCustodia>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class InCustodia {
		public Long IdInterno;
		public String IdSerieFondo;
		public Long IdInstrumento;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal Cantidad;
		public String IdMonedaFondo;
		public String IdMonedaOperacion;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String FechaValorCuota;
		public String FechaCurse;
		public String EsTraspaso;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaCalcularComisiones {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoCalcularComision> P_OUT_RESULTADO;

		public SalidaCalcularComisiones() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCalcularComision>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ResultadoCalcularComision {

		public Long IdInterno;
		public String IdSerieFondo;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal Comision;
		public BigDecimal ComisionLocal;
		public BigDecimal ComisionMonedaSistema;
		public BigDecimal ValorPivoteLocalConversionLocal;
		public BigDecimal ValorPivoteSistemaConversionSistema;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String EsAPV;
		public String EsTraspaso;
		public int Flujo;
		public BigDecimal CantidadComision;
		public BigDecimal MontoComisionMonedaLocal;
		public String TipoComision;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CarterasProceso {
		public Long IdInterno;
		public Long IdInstrumento;
		public String IdSerieFondo;
		public BigDecimal CantidadCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public Date FechaIngresoCartera;
		public Date FechaIngresoInversion;
		public String IdConcepto;
		public BigDecimal ValorCuota;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ValorCuota {
		public String IdSerieFondo;
		public BigDecimal ValorCuota;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaCalcularComisiones jin = gson.fromJson(in.toString(), EntradaCalcularComisiones.class);
		SalidaCalcularComisiones out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaCalcularComisiones logica(Connection con, EntradaCalcularComisiones entrada) throws Exception {
		logger.trace("INICIO CalcularComisiones");
		SQL sql = new SQL();
		SalidaCalcularComisiones out = new SalidaCalcularComisiones();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String usoValorCuotaActual;
		try {
			usoValorCuotaActual = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_VALOR_CUOTA_ACTUAL");
		}
		catch (Exception e) {
			usoValorCuotaActual = "S";
		}
		
		Date dFechaSistema = Fecha.toDate(fechaSistema);
		ConsultarFlujoCustodia flujoCustodia = new ConsultarFlujoCustodia();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		FlujoCustodiaEntrada flujoCustodiaEntrada = new FlujoCustodiaEntrada();
		flujoCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		flujoCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
		flujoCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
		flujoCustodiaEntrada.p_FechaSaldo = fechaSistema;
		flujoCustodiaEntrada.p_IdPais = idPaisEmpresa;
		flujoCustodiaEntrada.p_DiasProyeccion = 0;
		flujoCustodiaEntrada.p_DiaHabil = "S";
		flujoCustodiaEntrada.p_ValorizarFlujo = "N";

		List<Long> clientes = new ArrayList<Long>();
		List<Long> portafolios = new ArrayList<Long>();
		List<Long> instrumentos = new ArrayList<Long>();
		List<String> series = new ArrayList<String>();

		for (InCustodia cartera : entrada.p_DatosCustodia) {
			FlujoUso uso = new FlujoUso();
			uso.UsoTipoOperacion = "RESCFM";
			uso.IdCliente = cartera.IdCliente;
			uso.IdPortafolio = cartera.IdPortafolio;
			uso.BusquedaEspecifica = "";
			uso.IdTipoInstrumentoEspecifico = "";
			FlujoFiltrosBusqueda busqueda = new FlujoFiltrosBusqueda();
			busqueda.IdClasificacionTipoInstrumento = "FM";
			busqueda.IdInstrumento = cartera.IdInstrumento.intValue() == 0 ? null : cartera.IdInstrumento;
			flujoCustodiaEntrada.p_FiltrosBusqueda.add(busqueda);
			flujoCustodiaEntrada.p_Uso.add(uso);
			if (cartera.IdInstrumento != null && !instrumentos.contains(cartera.IdInstrumento)) {
				instrumentos.add(cartera.IdInstrumento);
			}
			if (!clientes.contains(cartera.IdCliente)) {
				clientes.add(cartera.IdCliente);
			}
			if (!portafolios.contains(cartera.IdPortafolio)) {
				portafolios.add(cartera.IdPortafolio);
			}
			if (!series.contains(cartera.IdSerieFondo)) {
				series.add(cartera.IdSerieFondo);
			}
		}

		FlujoEsAgrupadoPor agrupador = new FlujoEsAgrupadoPor();
		agrupador.ClasificacionTipoInstrumento = "S";
		agrupador.TipoInstrumento = "S";
		agrupador.Instrumento = "S";
		agrupador.Emisor = "N";
		agrupador.Nemotecnico = "N";
		agrupador.NemoSVS = "N";
		agrupador.FechaEmisionInstrumento = "N";
		agrupador.FechaVencimientoInstrumento = "N";
		agrupador.FechaLiquidacionInstrumento = "N";
		agrupador.TipoReajuste = "N";
		agrupador.EsFungible = "N";
		agrupador.CodigoIFRS = "N";
		agrupador.Boveda = "N";
		agrupador.Bolsa = "N";
		agrupador.TipoOperacion = "N";
		agrupador.Mesa = "N";
		agrupador.FolioRegistro = "N";
		agrupador.ItemOrigen = "N";
		flujoCustodiaEntrada.p_EsAgrupadoPor.add(agrupador);

		FlujoCustodiaSalida salida = flujoCustodia.logica(con, flujoCustodiaEntrada);
		
		salida.p_out_FlujoDetallado.sort(new Comparator<FlujoCustodiaDetallado>() {
		    @Override
		    public int compare(FlujoCustodiaDetallado mov1, FlujoCustodiaDetallado mov2) {
		        if(mov1.FechaOrigenCompra.compareTo(mov2.FechaOrigenCompra) == 0){
		            return 0;
		        }
		        return mov1.FechaOrigenCompra.compareTo(mov2.FechaOrigenCompra) == 1 ? 1 : -1;
		     }
		});;

		List<CarterasProceso> carteras = new ArrayList<CarterasProceso>();
		for(InCustodia cartera : entrada.p_DatosCustodia){
			for(FlujoCustodiaDetallado detalle : salida.p_out_FlujoDetallado){
				if(cartera.IdCliente.longValue() == detalle.IdCliente.longValue() &&
						cartera.IdPortafolio.longValue() == detalle.IdPortafolio.longValue() &&
						cartera.IdInstrumento.longValue() == detalle.IdInstrumento.longValue()){
					BigDecimal cuotaRetirar = BigDecimal.ZERO;
					if(cartera.Cantidad.compareTo(detalle.Cantidad) != -1){
						cuotaRetirar = detalle.Cantidad;
						cartera.Cantidad = cartera.Cantidad.subtract(detalle.Cantidad, MathContext.DECIMAL128);
					} else if (cartera.Cantidad.compareTo(detalle.Cantidad) == -1) {
						cuotaRetirar = cartera.Cantidad;
						cartera.Cantidad = BigDecimal.ZERO;
					}
					CarterasProceso cc = new CarterasProceso();
					cc.IdInterno = cartera.IdInterno;
					cc.IdCliente = cartera.IdCliente;
					cc.IdPortafolio = cartera.IdPortafolio;
					cc.IdInstrumento = cartera.IdInstrumento;
					cc.IdSerieFondo = cartera.IdSerieFondo;					
					cc.FechaIngresoCartera = detalle.FechaOrigenCompra;
					cc.FechaIngresoInversion = detalle.FechaOperacion;
					cc.IdConcepto = detalle.IdConcepto;
					cc.CantidadCartera = cuotaRetirar;
					cc.ValorCuota = detalle.PrecioCompra;
					carteras.add(cc);
					if(cartera.Cantidad.compareTo(BigDecimal.ZERO) == -1 || cartera.Cantidad.compareTo(BigDecimal.ZERO) == 0){
						break;
					}
				}
			}
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdSerieFondo", series);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_TRAMO_COMISION");
		List<TramoComision> tramosComisiones = sql.query(con, SQL_SELECT_TRAMO_COMISION, params, TramoComision.class);
		logger.debug("FIN::SQL_SELECT_TRAMO_COMISION");
		params.clear();
		params.put("IdSerieFondo", series);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> listaSeries = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		Map<String, SerieFondoSolicitudes> informacionSeries = listaSeries.parallelStream().collect(Collectors.toMap(serie->serie.IdSerieFondo, serie->serie));
		
		Map<Long,EntregarPreciosTasas_Salida_PreciosTasas> valoresCuota = obtenerValorCuota(con,entrada,informacionSeries);

		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();

		// Cantidad de mapeos + 1 (paridad local, paridad sistema, tipo de cambio fondo-local relativo a local y tipo de cambio fondo-sistema relativo sistema)
		final int pendiente =  5;
		for (InCustodia custodia : entrada.p_DatosCustodia) {
			SerieFondoSolicitudes serie = informacionSeries.get(custodia.IdSerieFondo);
			ResultadoCalcularComision comision = new ResultadoCalcularComision();
			comision.IdInterno = custodia.IdInterno;
			comision.IdSerieFondo = custodia.IdSerieFondo;
			comision.IdCliente = custodia.IdCliente;
			comision.IdPortafolio = custodia.IdPortafolio;
			comision.Comision = BigDecimal.ZERO;
			comision.ComisionLocal = BigDecimal.ZERO;
			comision.ComisionMonedaSistema = BigDecimal.ZERO;
			comision.TipoSolicitud = custodia.TipoSolicitud;
			comision.IdTipoMovimientoFondoMutuo = custodia.IdTipoMovimientoFondoMutuo;
			comision.EsAPV = serie != null ? serie.EsAPV : _c.NO;
			comision.EsTraspaso = _c.NO;
			EntregarPreciosTasas_Salida_PreciosTasas precioTasas = valoresCuota.get(custodia.IdInterno);
			BigDecimal valorCuota = precioTasas != null && precioTasas.Precio != null ? precioTasas.Precio : BigDecimal.ZERO;
			String tipoComision = _c.TipoSolicitudAporte.equalsIgnoreCase(comision.TipoSolicitud) ? _c.ComisionEntrada
					: _c.ComisionSalida;
			if(!_c.Reinversion.equals(comision.IdTipoMovimientoFondoMutuo) && _c.NO.equals(comision.EsAPV ) && _c.NO.equals(comision.EsTraspaso)) {
				int CantidadCarteras = carteras.size();
				BigDecimal CantidadCarteraTotal = BigDecimal.ZERO; 
				for (CarterasProceso cartera : carteras) {					
					if (cartera.IdCliente.longValue() == custodia.IdCliente.longValue()
							&& cartera.IdPortafolio.longValue() == custodia.IdPortafolio.longValue()
							&& cartera.IdInstrumento.longValue() == custodia.IdInstrumento.longValue()
							&& cartera.IdInterno.longValue() == custodia.IdInterno.longValue()) {
						
						if (_c.NO.equalsIgnoreCase(usoValorCuotaActual) && custodia.TipoSolicitud.equalsIgnoreCase("RES")){
							valorCuota = cartera.ValorCuota;
						}
						for (TramoComision tramo : tramosComisiones) {
							if (tramo.TipoComision.equalsIgnoreCase(tipoComision)
									&& tramo.IdSerieFondo.equalsIgnoreCase(comision.IdSerieFondo)
									&& !tramo.TipoComision.equalsIgnoreCase("PER")) {
								int dias = 0;
								if (cartera.IdConcepto.equals("APOREI")) {
									dias = numeroDiasEntreDosFechas(cartera.FechaIngresoCartera, Date.valueOf(custodia.FechaCurse));
								}else {
									if (custodia.FechaCurse != null && !custodia.FechaCurse.isEmpty()) {
										dias = numeroDiasEntreDosFechas(cartera.FechaIngresoInversion, Date.valueOf(custodia.FechaCurse));
									}else {
										dias = numeroDiasEntreDosFechas(cartera.FechaIngresoCartera, dFechaSistema);
									}
								}																
									comision.Flujo = dias;																	
								if (dias >= tramo.DiasDesde && dias <= tramo.DiasHasta) {
									BigDecimal porcentaje = tramo.PorcentajeComision.divide(new BigDecimal(100),
											MathContext.DECIMAL128);
									BigDecimal montoComision = cartera.CantidadCartera
											.multiply(valorCuota, MathContext.DECIMAL128)
											.multiply(porcentaje, MathContext.DECIMAL128)
											.setScale(8, RoundingMode.HALF_DOWN);
									comision.Comision = comision.Comision.add(montoComision, MathContext.DECIMAL128);
									comision.ComisionLocal = comision.Comision;
								}
							}
							else if(tramo.IdSerieFondo.equalsIgnoreCase(comision.IdSerieFondo)
									&& tramo.TipoComision.equalsIgnoreCase("PER"))
							{
								CantidadCarteras = CantidadCarteras - 1;
								BigDecimal porcentaje = tramo.PorcentajeComision.divide(new BigDecimal(100),
											MathContext.DECIMAL128);
								
								BigDecimal comisionDiaria = porcentaje
										.divide(new BigDecimal(365), MathContext.DECIMAL128)
										.multiply(new BigDecimal(1.19),MathContext.DECIMAL128)
										.setScale(8, RoundingMode.HALF_DOWN);
								
								int TrimestreActual = trimestreSegunFecha(dFechaSistema);
								int TrimestreCartera = trimestreSegunFecha(cartera.FechaIngresoCartera);
								int diasPermanencia = 0;
								comision.TipoComision = tramo.TipoComision;
								
								if(TrimestreActual != TrimestreCartera && dFechaSistema.compareTo(cartera.FechaIngresoCartera) > 0)
								{
									diasPermanencia = duracionTrimestre(cartera.FechaIngresoCartera);
								}
								else  
								{
									diasPermanencia = numeroDiasEntreDosFechas(cartera.FechaIngresoCartera, dFechaSistema);
								}
								
								comision.CantidadComision = cartera.CantidadCartera
										.multiply(comisionDiaria)
										.multiply(new BigDecimal(diasPermanencia), MathContext.DECIMAL128)
										.setScale(8, RoundingMode.HALF_DOWN);
									
								CantidadCarteraTotal = CantidadCarteraTotal.add(comision.CantidadComision);
								if(CantidadCarteras == 0) {
									comision.Comision = comision.Comision.add(CantidadCarteraTotal
											.multiply(valorCuota, MathContext.DECIMAL128), MathContext.DECIMAL128);
										
									comision.ComisionLocal = comision.Comision;
								}
							}
						}
					}
				}
			}
			
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 1L;
			calculoEntrada.IdInterno = (custodia.IdInterno * pendiente) + 1L;
			calculoEntrada.Fecha = fechaSistema;
			calculoEntrada.IdMonedaOrigen = custodia.IdMonedaFondo;
			calculoEntrada.IdMonedaDestino = custodia.IdMonedaOperacion;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = comision.Comision;
			calculoEntrada.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);

			CalculoEntrada calculoComisionMonedaSistema = new CalculoEntrada();
			calculoComisionMonedaSistema.IdTrabajo = 1L;
			calculoComisionMonedaSistema.IdInterno = (custodia.IdInterno * pendiente) + 2L;
			calculoComisionMonedaSistema.Fecha = fechaSistema;
			calculoComisionMonedaSistema.IdMonedaOrigen = custodia.IdMonedaFondo;
			calculoComisionMonedaSistema.IdMonedaDestino = idMonedaSistema;
			calculoComisionMonedaSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoComisionMonedaSistema.MontoOrigen = comision.Comision;
			calculoComisionMonedaSistema.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoComisionMonedaSistema);

			CalculoEntrada tipoCambioFondoLocalRespectoLocal = new CalculoEntrada();
			tipoCambioFondoLocalRespectoLocal.IdTrabajo = 1L;
			tipoCambioFondoLocalRespectoLocal.IdInterno = (custodia.IdInterno * pendiente) + 3L;
			tipoCambioFondoLocalRespectoLocal.Fecha = fechaSistema;
			tipoCambioFondoLocalRespectoLocal.IdMonedaOrigen = custodia.IdMonedaOperacion;
			tipoCambioFondoLocalRespectoLocal.IdMonedaDestino = custodia.IdMonedaFondo;
			tipoCambioFondoLocalRespectoLocal.ValorCambioLocalModificado = BigDecimal.ZERO;
			tipoCambioFondoLocalRespectoLocal.MontoOrigen = BigDecimal.ONE;
			tipoCambioFondoLocalRespectoLocal.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(tipoCambioFondoLocalRespectoLocal);

			CalculoEntrada tipoCambioFondoSistemaRespectoSistema = new CalculoEntrada();
			tipoCambioFondoSistemaRespectoSistema.IdTrabajo = 1L;
			tipoCambioFondoSistemaRespectoSistema.IdInterno = (custodia.IdInterno * pendiente) + 4L;
			tipoCambioFondoSistemaRespectoSistema.Fecha = fechaSistema;
			tipoCambioFondoSistemaRespectoSistema.IdMonedaOrigen = custodia.IdMonedaFondo;
			tipoCambioFondoSistemaRespectoSistema.IdMonedaDestino = idMonedaSistema;
			tipoCambioFondoSistemaRespectoSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
			tipoCambioFondoSistemaRespectoSistema.MontoOrigen = BigDecimal.ONE;
			tipoCambioFondoSistemaRespectoSistema.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(tipoCambioFondoSistemaRespectoSistema);

			out.P_OUT_RESULTADO.add(comision);
		}

		HashMap<Long, Object> hcalculo = new HashMap<Long, Object>();
		if (calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			hcalculo = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
			for (ResultadoCalcularComision comision : out.P_OUT_RESULTADO) {
				CalculoSalida calculoFondoLocal = (CalculoSalida) hcalculo.get((comision.IdInterno * pendiente) + 1L);
				comision.ComisionLocal = calculoFondoLocal == null ? BigDecimal.ONE : calculoFondoLocal.MontoDestino;

				CalculoSalida calculoFondoSistema = (CalculoSalida) hcalculo.get((comision.IdInterno * pendiente) + 2L);
				comision.ComisionMonedaSistema = calculoFondoSistema == null ? BigDecimal.ONE : calculoFondoSistema.MontoDestino;

				CalculoSalida calculoTipoCambioFondoLocalRespectoLocal = (CalculoSalida) hcalculo.get((comision.IdInterno * pendiente) + 3L);
				comision.ValorPivoteLocalConversionLocal = calculoTipoCambioFondoLocalRespectoLocal == null ? BigDecimal.ONE : calculoTipoCambioFondoLocalRespectoLocal.MontoDestino;

				CalculoSalida calculoTipoCambioFondoSistemaRespectoSistema = (CalculoSalida) hcalculo.get((comision.IdInterno) * pendiente + 4L);
				comision.ValorPivoteSistemaConversionSistema = calculoTipoCambioFondoSistemaRespectoSistema == null ? BigDecimal.ONE : calculoTipoCambioFondoSistemaRespectoSistema.MontoDestino;

			}
		}

		logger.trace("FIN CalcularComisiones");
		return out;
	}

	/*
	 * private Long diferenciaDias(Date fechaIngreso, Date fecha){ final int
	 * MILLSECS_PER_DAY = 24 * 60 * 60 * 1000; return fecha.getTime() -
	 * fechaIngreso.getTime() / MILLSECS_PER_DAY; }
	 */
	private int numeroDiasEntreDosFechas(Date fecha1, Date fecha2) {
		logger.trace("INICIO::CalcularComisiones.numeroDiasEntreDosFechas");
		long startTime = fecha1.getTime();
		long endTime = fecha2.getTime();
		long diffTime = endTime - startTime;
		long diffDays = diffTime / (1000 * 60 * 60 * 24);
		logger.trace("FIN::CalcularComisiones.numeroDiasEntreDosFechas");
		return (int) diffDays;
	}
	
	private int trimestreSegunFecha(Date fecha) {
		logger.trace("INICIO::CALCULARCOMISIONES.trimestreSegunFecha");
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        int mes = calendar.get(Calendar.MONTH) + 1;
	    int trimestre = (mes + 2) / 3;
		logger.trace("FIN::CALCULARCOMISIONES.trimestreSegunFecha");
		return (int) trimestre;
	}
	private int duracionTrimestre(Date date) {
	    Calendar calInicio = Calendar.getInstance();
	    calInicio.setTime(date);
	    calInicio.set(Calendar.DAY_OF_MONTH, 1);
	    calInicio.set(Calendar.MONTH, calInicio.get(Calendar.MONTH)/3 * 3);
	    
	    Calendar calFinal = Calendar.getInstance();
	    calFinal.setTime(date);
	    calFinal.set(Calendar.DAY_OF_MONTH, 1);
	    calFinal.set(Calendar.MONTH, calFinal.get(Calendar.MONTH)/3 * 3 + 2);
	    calFinal.set(Calendar.DAY_OF_MONTH, calFinal.getActualMaximum(Calendar.DAY_OF_MONTH));
	    
	    int diaInicio = calInicio.get(Calendar.DAY_OF_YEAR);
	    int diaFinal = calFinal.get(Calendar.DAY_OF_YEAR);
	    int dias = diaFinal - diaInicio;
	    return dias;
	}

	private Map<Long,EntregarPreciosTasas_Salida_PreciosTasas> obtenerValorCuota(Connection con, EntradaCalcularComisiones entrada, Map<String,SerieFondoSolicitudes> informacionSeries) throws Exception {
		logger.trace("INICIO::CalcularComisiones.obtenerValorCuota");

		EntregarPreciosTasas servicioPreciosTasas = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  entradaPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		entradaPrecioTasa.p_IdEmpresa 	= entrada.p_IdEmpresa;
		entradaPrecioTasa.p_IdModulo 	= entrada.p_IdModulo;
		entradaPrecioTasa.p_IdPais 		= paisSistema;
		entradaPrecioTasa.p_IdUsuario 	= entrada.p_IdUsuario;
		entradaPrecioTasa.p_DatosInstrumentos = entrada.p_DatosCustodia.parallelStream()	
		.map(sol->{
			SerieFondoSolicitudes serie = informacionSeries.get(sol.IdSerieFondo);
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta 		= Fecha.toDate(sol.FechaValorCuota);
			paramsRegIn.IdClasificacionTipoInstrumento = "FM";
			paramsRegIn.IdInstrumento 		= sol.IdInstrumento;
			paramsRegIn.IdMoneda 			= sol.IdMonedaFondo;
			paramsRegIn.IdRegistro 			= sol.IdInterno;
			paramsRegIn.IdTipoCriterioTasa 	= "0";
			paramsRegIn.IdTipoInstrumento 	= serie != null ? serie.IdTipoInstrumento : "";
			paramsRegIn.IdUso 				= "CARTERA";
			paramsRegIn.Nemotecnico 		= sol.IdSerieFondo;
			paramsRegIn.PlazoResidual 		= 0L;
			paramsRegIn.TipoCartera 		= "0";			
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = servicioPreciosTasas.logica(con, entradaPrecioTasa);
		Map<Long,EntregarPreciosTasas_Salida_PreciosTasas> valoresCuota = salidaPrecioTasa.P_OUT_PRECIOSTASAS.parallelStream().filter(precio -> precio.IdInstrumento != null).collect(Collectors.toMap(instrumento->instrumento.IdRegistro, precio-> precio));
		
		logger.trace("FIN::CalcularComisiones.obtenerValorCuota");
		return valoresCuota;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
	//	String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1006,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosCustodia\":[{\"IdInterno\":1,\"IdSerieFondo\":\"00227BCLP\",\"IdInstrumento\":910737,\"IdCliente\":585,\"IdPortafolio\":199050,\"Cantidad\":1000,\"IdMonedaFondo\":\"CLP\",\"IdMonedaOperacion\":\"CLP\",\"TipoSolicitud\":\"RES\",\"IdTipoMovimientoFondoMutuo\":\"RESNORM\",\"FechaValorCuota\":\"2021-11-12\",\"EsTraspaso\":\"N\"}]}";
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosCustodia\":[{\"IdInterno\":1,\"IdSerieFondo\":\"ASD\",\"IdInstrumento\":471793,\"IdCliente\":1425498,\"IdPortafolio\":1453050,\"Cantidad\":1.0000,\"IdMonedaFondo\":\"CLP\",\"IdMonedaOperacion\":\"CLP\",\"TipoSolicitud\":\"RES\",\"IdTipoMovimientoFondoMutuo\":\"RESNORM\",\"FechaValorCuota\":\"2022-01-04\",\"EsTraspaso\":\"N\"}]}";
		//String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosCustodia\":[{\"IdInterno\":1,\"IdSerieFondo\":\"ASD\",\"IdInstrumento\":910737,\"IdCliente\":1425498,\"IdPortafolio\":1453050,\"Cantidad\":1000,\"IdMonedaFondo\":\"CLP\",\"IdMonedaOperacion\":\"CLP\",\"TipoSolicitud\":\"RES\",\"IdTipoMovimientoFondoMutuo\":\"RESNORM\",\"FechaValorCuota\":\"2021-12-30\",\"EsTraspaso\":\"N\"}]}";
		//String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":null,\"p_UsaReglasNegocio\":\"S\",\"p_DatosCustodia\":[{\"IdInterno\":1,\"IdSerieFondo\":\"FMOPTIMUS_A\",\"IdInstrumento\":351321,\"IdCliente\":1425498,\"IdPortafolio\":1453050,\"Cantidad\":0,\"IdMonedaFondo\":\"USD\",\"IdMonedaOperacion\":\"USD\",\"TipoSolicitud\":\"RES\",\"IdTipoMovimientoFondoMutuo\":\"RESNORM\",\"FechaValorCuota\":\"2022-01-04\",\"EsTraspaso\":\"N\"}]}";
		EntradaCalcularComisiones Objin = gson.fromJson(jsonRF, EntradaCalcularComisiones.class);
		CalcularComisiones  demo=new CalcularComisiones();				
		try {
			SalidaCalcularComisiones ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}