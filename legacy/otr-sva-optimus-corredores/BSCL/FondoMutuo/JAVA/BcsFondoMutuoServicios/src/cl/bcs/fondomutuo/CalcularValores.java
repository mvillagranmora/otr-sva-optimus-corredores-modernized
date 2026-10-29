package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularValores implements JavaCall {
	private final Log log = new Log("FM.CalcularValores");

	public static class EntradaCalcularValores{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public List<IngresoCustodia.ObjTablaCartera> p_IngresoCustodia;
		public List<RetiroCustodia.ObjTablaCartera> p_RetiroCustodia;
		public HashMap<Long,Object> p_Paridades; // idInterno es la fecha en Long para UF y *-1 para UTM
		
		public EntradaCalcularValores() {
			this.p_SimularOperacion = "N";
			this.p_IngresoCustodia = new ArrayList<IngresoCustodia.ObjTablaCartera>();
			this.p_RetiroCustodia = new ArrayList<RetiroCustodia.ObjTablaCartera>();
			this.p_Paridades = new HashMap<Long,Object>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularValores {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<IngresoCustodia.ObjTablaCartera> p_IngresoCustodia;
		public List<RetiroCustodia.ObjTablaCartera> p_RetiroCustodia;

		public SalidaCalcularValores() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.p_IngresoCustodia = new ArrayList<IngresoCustodia.ObjTablaCartera>();
			this.p_RetiroCustodia = new ArrayList<RetiroCustodia.ObjTablaCartera>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjFFMMOperaciones{
		public long IdOperacion;
		public long IdCarga;
		public long perId;
		public BigDecimal secMovto;
		public String codMovto;
		public BigDecimal cantidad;
		public String emisor;
		public String fechaIngresoMvto;
		public String fechaLiquidacion;
		public BigDecimal monto;
		public BigDecimal precio;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaCalcularValores jin = gson.fromJson(in.toString(), EntradaCalcularValores.class);
		SalidaCalcularValores out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaCalcularValores logica(Connection con, EntradaCalcularValores entrada) throws Exception {
		log.write("INICIO CalcularValores");
		SalidaCalcularValores out = new SalidaCalcularValores();
		
		//ingresos
		if(entrada.p_IngresoCustodia.size() > 0) {
			for(IngresoCustodia.ObjTablaCartera obj : entrada.p_IngresoCustodia) {
				String fecha = Fecha.toStringDate(obj.FechaOperacion);
				Long longFecha = Long.parseLong(fecha.replaceAll("\\-", ""));
				//si es de la carga calculamos el costoUF
				if(obj.IdCarga != null) {
					if(obj.CostoInversionEnFactorAjuste == null) {
						BigDecimal valorUF = BigDecimal.ONE;
						CalculoSalida resultadoUF = (CalculoSalida) entrada.p_Paridades.get(longFecha);
						if(resultadoUF != null && resultadoUF.MontoDestino != null) valorUF = resultadoUF.MontoDestino;
						
						if(obj.IdClasificacionTipoInstrumento.equals("FM") && (obj.IdConcepto.equals("APORFM") || obj.IdConcepto.equals("APOREI"))) {
							//costoUF
							if(valorUF.compareTo(BigDecimal.ZERO) != 0) {
								obj.CostoInversionEnFactorAjuste = obj.MontoCompra.divide(valorUF, MathContext.DECIMAL128);
								obj.INGCostoInversionEnFactorAjuste = obj.MontoCompra.divide(valorUF, MathContext.DECIMAL128);
							}
						}
					}
					else {
						obj.INGCostoInversionEnFactorAjuste = obj.CostoInversionEnFactorAjuste;
					}	
				}
				//para compras RV o aportes FM
				if(obj.IdClasificacionTipoInstrumento.equals("RV") || obj.IdClasificacionTipoInstrumento.equals("FM")) {
					BigDecimal valorUTM = BigDecimal.ONE;
					
					CalculoSalida resultadoUTM = (CalculoSalida) entrada.p_Paridades.get(longFecha * -1);
					if(resultadoUTM != null && resultadoUTM.MontoDestino != null) valorUTM = resultadoUTM.MontoDestino;
					
					//rentabilidad
					if(valorUTM.compareTo(BigDecimal.ZERO) != 0) {
						obj.TipoFactor 				= "UTM";
						obj.FechaFactor 			= obj.FechaOperacion;
						obj.ValorFactor 			= valorUTM;
						obj.MontoCompraFactor		= obj.MontoCompra.divide(valorUTM, MathContext.DECIMAL128);
					}
				}
			}
		}
		
		out.p_IngresoCustodia = entrada.p_IngresoCustodia;
		out.p_RetiroCustodia = entrada.p_RetiroCustodia;
		log.write("FIN CalcularValores");
		return out;
	}
	
	//funcion para obtener paridad de UF y UTM
	public CalcularParidadSalida valoresReferencia(Connection con, EntradaCalcularValores entrada, Long IdInterno, Date FechaOperacion, String MonedaDestino) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		CalcularParidad paridad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 2001L;
		
		CalculoEntrada c1 = new CalculoEntrada();
		c1.Fecha = dateFormat.format(FechaOperacion);
		c1.IdInterno = IdInterno;
		c1.IdMonedaOrigen = MonedaDestino;
		c1.IdMonedaDestino = "CLP";
		c1.IdTrabajo = 2001L;
		c1.MontoOrigen = BigDecimal.ONE;
		c1.IdUso = "CARTERA";
		calcularParidadEntrada.p_CalculosEntrada.add(c1);
		CalcularParidadSalida calcularParidadSalida = paridad.logica(con, calcularParidadEntrada);
		return calcularParidadSalida;
	}
	
	//main
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			CalcularValores demo = new CalcularValores();
			Gson gson = Utils.getGsonBuilder();
			String jsonIngresos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"CUS\",\"p_SimularOperacion\":\"N\",\"p_IngresoCustodia\":[{\"IdCartera\":8457,\"FolioCartera\":56284,\"FolioCarteraRelacionada\":null,\"FolioGarantiaOrigen\":null,\"FechaIngresoRegistro\":null,\"FechaModificacionRegistro\":null,\"IdEmpresa\":1,\"FechaOperacion\":\"2018-02-08 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdCliente\":100477,\"IdPortafolio\":100866,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdEstado\":\"VIG\",\"IdTipoProducto\":\"INFM\",\"IdTipoCanalIngreso\":\"AGE\",\"IdMonedaSistema\":\"CLP\",\"IdMonedaAdicional\":\"CLP\",\"FechaFactorMonedaAdicional\":null,\"ValorFactorMonedaAdicional\":null,\"IdMovimientoCarteraEnTransito\":null,\"IdConcepto\":\"CARSIN\",\"IdTipoOperacion\":\"INGTIT\",\"IdOperacion\":901809,\"IdTipoComprobante\":\"CUS\",\"FolioComprobante\":19774,\"FechaComprobante\":\"2018-02-08 00:00:00.000\",\"FolioExternoReferencia\":\"901809\",\"IdMesa\":null,\"IdBolsa\":\"BCS\",\"IdSucursal\":0,\"IdAgente\":20180,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-02-08 00:00:00.000\",\"EsIntraday\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"Glosa\":\"\",\"IdUsuario\":0,\"IdModulo\":\"CUS\",\"TipoCarteraCorredor\":null,\"TipoCarteraNormativa\":null,\"FechaCurseInversion\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoInversionEnFactorAjuste\":null,\"IdClasificacionTipoInstrumento\":\"FM\",\"IdTipoInstrumento\":\"FFM\",\"IdInstrumento\":133264,\"IdInstrumentoAnterior\":133264,\"Nemotecnico\":\"LIQUIDEZB\",\"IdEmisor\":\"CREDICORPAGF\",\"NemoSVS\":\"LIQUIDEZB\",\"IdTipoReajuste\":null,\"IdMonedaEmision\":\"CLP\",\"FechaEmisionInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"EsFungible\":\"N\",\"ValorizaPor\":\"P\",\"TipoValorizacion\":null,\"TieneCorteDeCupon\":\"N\",\"CodigoIFRS\":null,\"IdBoveda\":\"COR\",\"CuentaBoveda\":null,\"IdMonedaInstrumento\":\"CLP\",\"Tamano\":0,\"TIFAfectoBeneficio\":null,\"TIFTasa\":null,\"ISIN\":\"\",\"FechaFactorMonedaInstrumento\":null,\"ValorFactorMonedaInstrumento\":null,\"Cantidad\":3.70999999999999996447286321199499070644378662109375,\"CorteMinimo\":1,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":34.210000,\"TasaCompra\":0.0000,\"MontoCompra\":127,\"MontoCompraMonedaInstrumento\":127,\"MontoCompraMonedaAdicional\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"MontoAPlazoMonedaAdicional\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdModalidadCumplimiento\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"TipoFactor\":null,\"FechaFactor\":null,\"ValorFactor\":null,\"MontoCompraFactor\":null,\"IdRegimenFondoMutuo\":\"\",\"FechaOrigenCompra\":null,\"IdCarga\":678,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":\"2018-02\",\"INGIdTipoOperacion\":\"INGTIT\",\"INGGlosa\":\"\",\"INGTasaAgente\":null,\"INGFechaOperacion\":\"2018-02-08 00:00:00.000\",\"INGIdCondicionLiquidacion\":\"T0\",\"INGFechaLiquidacion\":\"2018-02-08 00:00:00.000\",\"INGCantidad\":3.70999999999999996447286321199499070644378662109375,\"INGValorRescate\":0,\"INGPrecioPromedioCompra\":null,\"INGMontoPrecioPromedioCompra\":null,\"INGMontoPrecioPromedioMonedaInstrumento\":null,\"INGMontoPrecioPromedioMonedaAdicional\":null,\"INGPrecioAPlazo\":null,\"INGMontoAPlazo\":null,\"INGMontoAPlazoMonedaInstrumento\":null,\"INGMontoAPlazoMonedaAdicional\":null,\"INGEsPuntos\":null,\"INGValorTipoCambioSpot\":null,\"INGValorTipoCambioCotizador\":null,\"INGTasaPuntosMonedaPrincipal\":null,\"INGTasaPuntosMonedaSecundaria\":null,\"INGTicketValorizacion\":null,\"INGValorizacionConProblemas\":null,\"INGFechaValorizacionCartera\":null,\"INGFechaValorizacionAnterior\":null,\"INGFechaValorizacionActual\":null,\"INGFechaValorizacionProximo\":null,\"INGFechaFactorMonedaInstrumentoAnterior\":null,\"INGValorFactorMonedaInstrumentoAnterior\":null,\"INGFechaFactorMonedaInstrumentoActual\":null,\"INGValorFactorMonedaInstrumentoActual\":null,\"INGFechaFactorMonedaInstrumentoProximo\":null,\"INGValorFactorMonedaInstrumentoProximo\":null,\"INGFechaFactorMonedaAdicionalAnterior\":null,\"INGValorFactorMonedaAdicionalAnterior\":null,\"INGFechaFactorMonedaAdicionalActual\":null,\"INGValorFactorMonedaAdicionalActual\":null,\"INGFechaFactorMonedaAdicionalProximo\":null,\"INGValorFactorMonedaAdicionalProximo\":null,\"INGPrecioSucio\":null,\"INGPrecioCompra\":34.210000,\"INGTasaCompra\":0.0000,\"INGMontoATasaCompraAnterior\":null,\"INGMontoInteresCompraAnterior\":null,\"INGMontoReajusteCompraAnterior\":null,\"INGMontoCompraAnteriorMonedaInstrumento\":null,\"INGMontoInteresCompraAnteriorMonedaInstrumento\":null,\"INGMontoReajusteCompraAnteriorMonedaInstrumento\":null,\"INGMontoCompraAnteriorMonedaAdicional\":null,\"INGMontoInteresCompraAnteriorMonedaAdicional\":null,\"INGMontoReajusteCompraAnteriorMonedaAdicional\":null,\"INGDurationCompraAnterior\":null,\"INGConvexidadCompraAnterior\":null,\"INGMontoCompraAnteriorPibMas\":null,\"INGMontoCompraAnteriorPibMasMonedaInstrumento\":null,\"INGMontoCompraAnteriorPibMasMonedaAdicional\":null,\"INGMontoCompraAnteriorPibMenos\":null,\"INGMontoCompraAnteriorPibMenosMonedaInstrumento\":null,\"INGMontoCompraAnteriorPibMenosMonedaAdicional\":null,\"INGMontoATasaCompraActual\":127,\"INGMontoCompraActualMonedaInstrumento\":127,\"INGMontoCompraActualMonedaAdicional\":null,\"INGDurationCompraActual\":null,\"INGConvexidadCompraActual\":null,\"INGMontoInteresCompraActual\":null,\"INGMontoReajusteCompraActual\":null,\"INGMontoInteresCompraActualMonedaInstrumento\":null,\"INGMontoReajusteCompraActualMonedaInstrumento\":null,\"INGMontoInteresCompraActualMonedaAdicional\":null,\"INGMontoReajusteCompraActualMonedaAdicional\":null,\"INGMontoInteresCorridoCompraActual\":null,\"INGMontoReajusteCorridoCompraActual\":null,\"INGMontoInteresCorridoCompraActualMonedaInstrumento\":null,\"INGMontoReajusteCorridoCompraActualMonedaInstrumento\":null,\"INGMontoInteresCorridoCompraActualMonedaAdicional\":null,\"INGMontoReajusteCorridoCompraActualMonedaAdicional\":null,\"INGMontoCompraActualPibMas\":null,\"INGMontoCompraActualPibMasMonedaInstrumento\":null,\"INGMontoCompraActualPibMasMonedaAdicional\":null,\"INGMontoCompraActualPibMenos\":null,\"INGMontoCompraActualPibMenosMonedaInstrumento\":null,\"INGMontoCompraActualPibMenosMonedaAdicional\":null,\"INGMontoATasaCompraProximo\":null,\"INGMontoInteresCompraProximo\":null,\"INGMontoReajusteCompraProximo\":null,\"INGMontoCompraProximoMonedaInstrumento\":null,\"INGMontoInteresCompraProximoMonedaInstrumento\":null,\"INGMontoReajusteCompraProximoMonedaInstrumento\":null,\"INGMontoCompraProximoMonedaAdicional\":null,\"INGMontoInteresCompraProximoMonedaAdicional\":null,\"INGMontoReajusteCompraProximoMonedaAdicional\":null,\"INGDurationCompraProximo\":null,\"INGConvexidadCompraProximo\":null,\"INGFechaPrecioTasaMercadoAnterior\":null,\"INGFuenteTasaPrecioMercadoAnterior\":null,\"INGPrecioMercadoAnterior\":null,\"INGTasaMercadoAnterior\":null,\"INGMontoATasaMercadoAnterior\":null,\"INGMontoInteresMercadoAnterior\":null,\"INGMontoReajusteMercadoAnterior\":null,\"INGMontoMercadoAnteriorMonedaInstrumento\":null,\"INGMontoInteresMercadoAnteriorMonedaInstrumento\":null,\"INGMontoReajusteMercadoAnteriorMonedaInstrumento\":null,\"INGMontoMercadoAnteriorMonedaAdicional\":null,\"INGMontoInteresMercadoAnteriorMonedaAdicional\":null,\"INGMontoReajusteMercadoAnteriorMonedaAdicional\":null,\"INGDurationMercadoAnterior\":null,\"INGConvexidadMercadoAnterior\":null,\"INGMontoMercadoAnteriorPibMas\":null,\"INGMontoMercadoAnteriorPibMasMonedaInstrumento\":null,\"INGMontoMercadoAnteriorPibMasMonedaAdicional\":null,\"INGMontoMercadoAnteriorPibMenos\":null,\"INGMontoMercadoAnteriorPibMenosMonedaInstrumento\":null,\"INGMontoMercadoAnteriorPibMenosMonedaAdicional\":null,\"INGFechaPrecioTasaMercadoActual\":null,\"INGFuenteTasaPrecioMercadoActual\":null,\"INGPrecioMercadoActual\":null,\"INGTasaMercadoActual\":null,\"INGMontoATasaMercadoActual\":null,\"INGMontoMercadoActualMonedaInstrumento\":null,\"INGMontoMercadoActualMonedaAdicional\":null,\"INGDurationMercadoActual\":null,\"INGConvexidadMercadoActual\":null,\"INGMontoInteresCorridoMercadoActual\":null,\"INGMontoReajusteCorridoMercadoActual\":null,\"INGMontoInteresCorridoMercadoActualMonedaInstrumento\":null,\"INGMontoReajusteCorridoMercadoActualMonedaInstrumento\":null,\"INGMontoInteresCorridoMercadoActualMonedaAdicional\":null,\"INGMontoReajusteCorridoMercadoActualMonedaAdicional\":null,\"INGMontoMercadoActualPibMas\":null,\"INGMontoMercadoActualPibMasMonedaInstrumento\":null,\"INGMontoMercadoActualPibMasMonedaAdicional\":null,\"INGMontoMercadoActualPibMenos\":null,\"INGMontoMercadoActualPibMenosMonedaInstrumento\":null,\"INGMontoMercadoActualPibMenosMonedaAdicional\":null,\"INGFechaPrecioTasaMercadoProximo\":null,\"INGFuenteTasaPrecioMercadoProximo\":null,\"INGPrecioMercadoProximo\":null,\"INGTasaMercadoProximo\":null,\"INGMontoATasaMercadoProximo\":null,\"INGMontoInteresMercadoProximo\":null,\"INGMontoReajusteMercadoProximo\":null,\"INGMontoMercadoProximoMonedaInstrumento\":null,\"INGMontoInteresMercadoProximoMonedaInstrumento\":null,\"INGMontoReajusteMercadoProximoMonedaInstrumento\":null,\"INGMontoMercadoProximoMonedaAdicional\":null,\"INGMontoInteresMercadoProximoMonedaAdicional\":null,\"INGMontoReajusteMercadoProximoMonedaAdicional\":null,\"INGDurationMercadoProximo\":null,\"INGConvexidadMercadoProximo\":null,\"INGTasaCorredora\":null,\"INGFechaTasaCorredora\":null,\"INGMontoATasaCorredora\":null,\"INGMontoATasaCorredoraMonedaInstrumento\":null,\"INGMontoATasaCorredoraMonedaAdicional\":null,\"INGFechaCorteCupon\":null,\"INGNumeroCupon\":null,\"INGIdMonedaPagoCupon\":null,\"INGMontoCorteCupon\":null,\"INGFechaPagoCorteCupon\":null,\"INGMontoCapitalCorteCupon\":null,\"INGMontoInteresCorteCupon\":null,\"INGMontoReajusteCorteCupon\":null,\"INGMontoCorteCuponMonedaInstrumento\":null,\"INGMontoCapitalCorteCuponMonedaInstrumento\":null,\"INGMontoInteresCorteCuponMonedaInstrumento\":null,\"INGMontoReajusteCorteCuponMonedaInstrumento\":null,\"INGMontoCorteCuponMonedaAdicional\":null,\"INGMontoCapitalCorteCuponMonedaAdicional\":null,\"INGMontoInteresCorteCuponMonedaAdicional\":null,\"INGMontoReajusteCorteCuponMonedaAdicional\":null,\"INGMontoPresenteTIF\":null,\"INGMontoInteresTIF\":null,\"INGMontoReajusteTIF\":null,\"INGMontoInteresAcumTIF\":null,\"INGMontoReajusteAcumTIF\":null,\"INGMontoPresenteTIFMonedaInstrumento\":null,\"INGMontoInteresTIFMonedaInstrumento\":null,\"INGMontoReajusteTIFMonedaInstrumento\":null,\"INGMontoInteresAcumTIFMonedaInstrumento\":null,\"INGMontoReajusteAcumTIFMonedaInstrumento\":null,\"INGMontoPresenteTIFMonedaAdicional\":null,\"INGMontoInteresTIFMonedaAdicional\":null,\"INGMontoReajusteTIFMonedaAdicional\":null,\"INGMontoInteresAcumTIFMonedaAdicional\":null,\"INGMontoReajusteAcumTIFMonedaAdicional\":null,\"INGMontoMonedaPrincipal\":null,\"INGMontoMonedaSecundaria\":null,\"INGMontoDiferido\":null,\"INGMontoUtilidadDiferida\":null,\"INGMontoPerdidaDiferida\":null,\"INGMontoMargen\":null,\"INGMontoValorRazonableActivo\":null,\"INGMontoValorRazonablePasivo\":null,\"INGMontoValorRazonableNeto\":null,\"INGTasaValorRazonableActivo\":null,\"INGTasaValorRazonablePasivo\":null,\"INGMontoDevengoTipoCambio\":null,\"INGMontoDevengoTipoCambioAnterior\":null,\"INGMontoDiferidoDiario\":null,\"INGMontoDiferidoAcumulado\":null,\"INGMontoMatriz\":null,\"EGRTicketValorizacion\":null,\"EGRValorizacionConProblemas\":null,\"EGRFechaFactorMonedaInstrumento\":null,\"EGRValorFactorMonedaInstrumento\":null,\"EGRFechaFactorMonedaAdicional\":null,\"EGRValorFactorMonedaAdicional\":null,\"EGRGlosa\":null,\"EGRFechaOperacion\":null,\"EGRIdTipoOperacion\":null,\"EGRIdOperacion\":null,\"EGRIdMovimientoCarteraEnTransito\":null,\"EGRIdTipoCanalIngreso\":null,\"EGRIdCondicionLiquidacion\":null,\"EGRFechaLiquidacion\":null,\"EGRIdTipoComprobante\":null,\"EGRFolioComprobante\":null,\"EGRFechaComprobante\":null,\"EGRFolioExternoReferencia\":null,\"EGRIdModulo\":null,\"EGRIdConcepto\":null,\"EGRIdUsuario\":null,\"EGRIdSucursal\":null,\"EGRIdAgente\":null,\"EGRFechaCurseRetiro\":null,\"EGRCostoRetiroenFactorAjuste\":null,\"EGRValorFactorAjusteaFechaRetiro\":null,\"EGRCantidad\":null,\"EGRValorRescate\":null,\"EGRPrecioSucio\":null,\"EGRPrecio\":null,\"EGRTasa\":null,\"EGRMonto\":null,\"EGRMontoMonedaInstrumento\":null,\"EGRMontoMonedaAdicional\":null,\"EGRSpread\":null,\"EGRFechaPrecioTasaMercado\":null,\"EGRFuenteTasaPrecioMercado\":null,\"EGRPrecioMercado\":null,\"EGRTasaMercado\":null,\"EGRMontoATasaMercado\":null,\"EGRMontoMercadoMonedaInstrumento\":null,\"EGRMontoMercadoMonedaAdicional\":null,\"EGRSpreadMercado\":null,\"EGRPrecioAPlazo\":null,\"EGRMontoAPlazo\":null,\"EGRMontoAPlazoMonedaInstrumento\":null,\"EGRMontoAPlazoMonedaAdicional\":null,\"EGRMontoPresenteTIF\":null,\"EGRMontoPresenteTIFMonedaInstrumento\":null,\"EGRMontoPresenteTIFMonedaAdicional\":null,\"EGRIdSpotAsociado\":null,\"EGRMontoMonedaPrincipal\":null,\"EGRMontoMonedaSecundaria\":null,\"EGRIdMonedaLiquidacion\":null,\"EGRMontoLiquidacion\":null,\"EGRMontoLiquidacionMonedaPrincipal\":null,\"EGRValorTipoCambioSpot\":null,\"EGRValorParidad\":null,\"EGRValorTipoCambioCotizador\":null,\"EGREsPuntos\":null,\"EGRTasaPuntosMonedaPrincipal\":null,\"EGRTasaPuntosMonedaSecundaria\":null,\"EGRTasaDescuentoAnticipo\":null,\"EGRTipoCambioAnticipo\":null,\"EGRMontoARecibirAnticipo\":null,\"EGRTipoCambioObservado\":null}],\"p_RetiroCustodia\":[]}";
			EntradaCalcularValores in = gson.fromJson(jsonIngresos, EntradaCalcularValores.class);
			try {
 				SalidaCalcularValores out=demo.logica(con, in);
 				con.rollback();
 				System.out.println(out.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
