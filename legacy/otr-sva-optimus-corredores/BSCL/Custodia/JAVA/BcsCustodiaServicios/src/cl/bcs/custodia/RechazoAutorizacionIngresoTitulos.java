package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class RechazoAutorizacionIngresoTitulos implements JavaCall{	
	
	private final String SQL_UPDATE_ESTADO = loadResourceToString("cl/bcs/custodia/sql/RechazoAutorizacionIngresoTitulos_UpdateEstado.sql");
	
	public static class EntradaRechazoAutorizacionIngresoTitulos{
	    public Long						p_IdEmpresa;
	    public String					p_IdModulo;
	    public Long						p_IdUsuario;
	    public List<Long>				IdMovimientoCarteraEnTransito;
    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaRechazoAutorizacionIngresoTitulos {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public SalidaRechazoAutorizacionIngresoTitulos (){
			P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class MovimientoEnTransito{
		public Long IdMovimientoCarteraEnTransito;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaRechazoAutorizacionIngresoTitulos jin=gson.fromJson(in.toString() ,EntradaRechazoAutorizacionIngresoTitulos.class);
		SalidaRechazoAutorizacionIngresoTitulos out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaRechazoAutorizacionIngresoTitulos logica(Connection con, EntradaRechazoAutorizacionIngresoTitulos entrada) throws Exception {
		SalidaRechazoAutorizacionIngresoTitulos out = new SalidaRechazoAutorizacionIngresoTitulos();
		SQL sql = new SQL();
		out.P_OUT_ERRORESCONTADOR = 0L;		

		List<MovimientoEnTransito> params = new ArrayList<>();
		for(Long movimientoEntransito : entrada.IdMovimientoCarteraEnTransito) {
			MovimientoEnTransito movimiento = new MovimientoEnTransito();
			movimiento.IdMovimientoCarteraEnTransito = movimientoEntransito;
			params.add(movimiento);
		}
		int[] resUpdateEstado = sql.batch(con, SQL_UPDATE_ESTADO, params, MovimientoEnTransito.class);
		
		for(int estado : resUpdateEstado) {
			if(estado == 1){
				out.P_OUT_ERRORESCONTADOR = 0L;
			}else{
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_NO_SE_PUEDE_ANULAR","Operacion no se puede Anular"));
			}
		}
		
		return out;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_IdUsuario\":0,\"p_SimularOperacion\":\"N\",\"p_TicketAtencion\":0,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":0,\"TablaAfectada\":\"CARTERA\",\"EsRegistroNuevo\":\"S\",\"IdCustodia\":null,\"FolioCustodia\":null,\"GenerarNuevoFolioCustodia\":\"N\",\"MantieneFolioCustodia\":\"N\",\"IdCarteraOrigen\":null,\"FolioCarteraRelacionada\":null,\"FolioCarteraOrigen\":null,\"IdGarantiaOrigen\":null,\"FolioGarantiaOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdMovimientoCarteraEnTransitoRelacionado\":null,\"IdTipoOperacionGestora\":null,\"IdCustodiaGestor\":null,\"AccionOperacion\":\"INGRESO\",\"IdCliente\":100477,\"IdPortafolio\":100866,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-02-08 00:00:00.000\",\"FechaOperacionOriginal\":\"2018-02-08 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"INGTIT\",\"IdGrupoOperacion\":null,\"IdOperacion\":901809,\"IdConcepto\":\"CARSIN\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-02-08 00:00:00.000\",\"IdTipoProducto\":\"INFM\",\"IdTipoCanalIngreso\":\"AGE\",\"CodigoIFRS\":null,\"IdTipoComprobante\":\"CUS\",\"FolioComprobante\":901809,\"FechaComprobante\":\"2018-02-08 00:00:00.000\",\"FolioExternoReferencia\":\"901809\",\"IdMesa\":null,\"IdBolsa\":\"BCS\",\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdUsuario\":0,\"IdSucursal\":0,\"IdAgente\":20180,\"Glosa\":\"\",\"PermiteCertificacionPorUsuario\":null,\"IdMonedaAdicional\":\"CLP\",\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":\"CUS\",\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":133264,\"Nemotecnico\":\"LIQUIDEZB\",\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"CREDICORPAGF\",\"EsFungible\":\"N\",\"IdBoveda\":\"COR\",\"CuentaBoveda\":null,\"EsNominal\":\"N\",\"EsVentaDescubierta\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":\"LIQUIDEZB\",\"Cantidad\":3.70999999999999996447286321199499070644378662109375,\"CorteMinimo\":1,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":34.210000,\"TasaCompra\":0.0000,\"MontoCompra\":127,\"MontoCompraMonedaInstrumento\":127,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdRegimenFondoMutuo\":\"\",\"FechaOrigenCompra\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null,\"IdCarga\":678,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":\"2018-02\",\"EsVentaCorta\":null},{\"IdGenerarMovimientoCustodia\":1,\"TablaAfectada\":\"CARTERA\",\"EsRegistroNuevo\":\"S\",\"IdCustodia\":null,\"FolioCustodia\":null,\"GenerarNuevoFolioCustodia\":\"N\",\"MantieneFolioCustodia\":\"N\",\"IdCarteraOrigen\":null,\"FolioCarteraRelacionada\":null,\"FolioCarteraOrigen\":null,\"IdGarantiaOrigen\":null,\"FolioGarantiaOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdMovimientoCarteraEnTransitoRelacionado\":null,\"IdTipoOperacionGestora\":null,\"IdCustodiaGestor\":null,\"AccionOperacion\":\"INGRESO\",\"IdCliente\":100477,\"IdPortafolio\":100866,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-02-08 00:00:00.000\",\"FechaOperacionOriginal\":\"2018-02-08 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"INGTIT\",\"IdGrupoOperacion\":null,\"IdOperacion\":906139,\"IdConcepto\":\"CARSIN\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-02-08 00:00:00.000\",\"IdTipoProducto\":\"INFM\",\"IdTipoCanalIngreso\":\"AGE\",\"CodigoIFRS\":null,\"IdTipoComprobante\":\"CUS\",\"FolioComprobante\":906139,\"FechaComprobante\":\"2018-02-08 00:00:00.000\",\"FolioExternoReferencia\":\"906139\",\"IdMesa\":null,\"IdBolsa\":\"BCS\",\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdUsuario\":0,\"IdSucursal\":0,\"IdAgente\":20180,\"Glosa\":\"\",\"PermiteCertificacionPorUsuario\":null,\"IdMonedaAdicional\":\"CLP\",\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":\"CUS\",\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":133264,\"Nemotecnico\":\"LIQUIDEZB\",\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"CREDICORPAGF\",\"EsFungible\":\"N\",\"IdBoveda\":\"COR\",\"CuentaBoveda\":null,\"EsNominal\":\"N\",\"EsVentaDescubierta\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":\"LIQUIDEZB\",\"Cantidad\":0.450000000000000011102230246251565404236316680908203125,\"CorteMinimo\":1,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":33.330000,\"TasaCompra\":0.0000,\"MontoCompra\":15,\"MontoCompraMonedaInstrumento\":15,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdRegimenFondoMutuo\":\"\",\"FechaOrigenCompra\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null,\"IdCarga\":678,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":\"2018-02\",\"EsVentaCorta\":null}],\"p_ValorizacionAutomatica\":\"N\",\"p_idPais\":null,\"FolioSistemaExterno\":\"61-0000000018122\"}";
				EntradaRechazoAutorizacionIngresoTitulos Objin = gson.fromJson(json, EntradaRechazoAutorizacionIngresoTitulos.class);
							
				RechazoAutorizacionIngresoTitulos  kk = new RechazoAutorizacionIngresoTitulos();
				SalidaRechazoAutorizacionIngresoTitulos Objout = kk.logica(con, Objin);
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
}