package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerNemoSVS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class EstandarizacionRegistroCustodia implements JavaCall{		

	private final String SQL_Instrumento = loadResourceToString("cl/bcs/custodia/sql/EstandarizacionRegistroCustodia_SEL_Instrumento.sql");
	private final String SQL_PlantillaMovimientoCustodia = loadResourceToString("cl/bcs/custodia/sql/EstandarizacionRegistroCustodia_SEL_PlantillaMovimientoCustodia.sql");	
	
	public static class ObjPlantillaMovimientoCustodia {
		public Long			IdPlantillaCustodia; 
		public String		IdAccionOperacionLlave; 
		public String		IdTipoProductoLlave; 
		public String		IdTipoOperacionLlave; 
		public String		IdClasificacionTipoInstrumentoLlave; 
		public String		IdTipoInstrumentoLlave; 
		public String		IdInstrumentoLlave; 
		public String		IdOrigenInstrumentoLlave; 
		public String		EsGarantiaLlave;
		//-----------------------------------------------------
		public String		IdEmisor;
		public String		Nemotecnico;
		public String		NemoSVS;		
		public String		IdTipoReajuste; 
		public String		IdMonedaEmision;
		public String		IdMonedaInstrumento;		 			
		public String		EsFungible;
		public String		FechaEmisionInstrumento;
		public String		FechaVencimientoInstrumento;
		public String		Tamano;
		public String		ISIN;
		public String		TieneCorteDeCupon;
		public String		IndArt104;
		public String		TasaFiscal;					
		public String		TipoValorizacion;					
		public String		ValorizaPor;		
		//-----------------------------------------------------
		public String		IdBolsa;
		public String		IdBoveda; 
		
		/*
		public String		TablaAfectadaBusqueda; 
		public String		IdCustodiaBusqueda; 
		public String		FolioComprobanteBusqueda; 
		public String		FolioCarteraGarantizada; 
		public String		IdInstrumentoCarteraGarantizada; 
		public String		EsEspecifica; 
		public String		IdContraparteGarantia; 
		public String		IdOrigenGarantia; 
		public String		IdEspecieGarantia; 
		public String		FechaOperacion; 
		public String		FechaVencimientoOperacion; 
		public String		PlazoEnDiasOperacion; 
		public String		IdTipoProducto; 
		public String		IdTipoCanalIngreso; 
		public String		IdMonedaSistema; 
		public String		IdMonedaAdicional; 
		public String		FechaFactorMonedaAdicional; 
		public String		ValorFactorMonedaAdicional; 
		public String		CodigoIFRS; 
		public String		IdConcepto; 
		public String		IdTipoOperacion; 
		public String		IdGrupoOperacion; 
		public String		IdOperacion; 
		public String		FolioComprobante; 
		public String		FolioExternoReferencia; 
		public String		IdMesa; 
		public String		IdBolsa; 
		public String		IdSucursal; 
		public String		IdAgente; 
		public String		IdCondicionLiquidacion; 
		public String		FechaLiquidacion; 
		public String		EsIntraday; 
		public String		UsaGarantiaEspecifica; 
		public String		EsVentaCorta; 
		public String		CobraPrimaAlInicio; 
		public String		IdTipoPrestamo; 
		public String		Glosa; 
		public String		IdUsuario; 
		public String		IdModulo; 
		public String		TipoCarteraNormativa; 
		public String		FechaCurse; 
		public String		TipoFactorAjuste; 
		public String		ValorFactorAjusteaFechaCurse; 
		public String		CostoEnFactorAjuste; 
		public String		IdClasificacionTipoInstrumento; 
		public String		IdTipoInstrumento; 
		public String		IdInstrumento; 		
		public String		IdBovedaEsIncluyente; 
		public String		FechaFactorMonedaInstrumento; 
		public String		ValorFactorMonedaInstrumento; 
		public String		Cantidad; 
		public String		CorteMinimo; 
		public String		ValorRescate; 
		public String		PrecioCompra; 
		public String		TasaCompra; 
		public String		MontoCompra; 
		public String		MontoCompraMonedaInstrumento; 
		public String		MontoCompraMonedaAdicional; 
		public String		TasaAgente; 
		public String		PrecioAPlazo; 
		public String		MontoAPlazo; 
		public String		MontoAPlazoMonedaInstrumento; 
		public String		MontoAPlazoMonedaAdicional; 
		public String		INGCantidad; 
		public String		INGValorRescate; 		
		public String		MueveGarantiaAutomatica;
		public String		FechaEfectiva; 
		public String		IdPlaza; 
		public String		IdFixing; 
		public String		FechaFixing; 
		public String		HoraFixing; 
		public String		IdReferenciaMercado; 
		public String		IdMonedaPrincipal; 
		public String		IdMonedaSecundaria; 
		public String		ValorTipoCambioCierre; 
		public String		ValorParidadCierre; 
		public String		ValorTipoCambioCosto; 
		public String		ValorParidadCosto; 
		public String		IdSpotAsociado; 
		public String		IdModalidadCumplimiento; 
		public String		MontoMonedaPrincipal; 
		public String		MontoMonedaSecundaria; 
		public String		IdMonedaLiquidacion; 
		public String		MontoLiquidacion; 
		public String		MontoLiquidacionMonedaPrincipal; 
		public String		MontoDiferido; 
		public String		MontoUtilidadDiferida; 
		public String		MontoPerdidaDiferida; 
		public String		MontoMargen; 
		public String		MontoValorRazonableActivo; 
		public String		MontoValorRazonablePasivo; 
		public String		MontoValorRazonableNeto; 
		public String		TasaValorRazonableActivo; 
		public String		TasaValorRazonablePasivo; 
		public String		MontoDevengoTipoCambio; 
		public String		MontoDevengoTipoCambioAnterior; 
		public String		MontoDiferidoDiario; 
		public String		MontoDiferidoAcumulado; 
		public String		ValorTipoCambioSpot; 
		public String		ValorTipoCambioCotizador; 
		public String		EsPuntos; 
		public String		TasaPuntosMonedaPrincipal; 
		public String		TasaPuntosMonedaSecundaria; 
		public String		MontoMatriz; 
		public String		RazonSocialContraparteGarantia; 
		public String		CuentaContraparteGarantia; 
		public String		CuentaBoveda; 
		public String		CuentaBovedaRelacionado; 
		public String		IdTipoComprobante; 
		public String		FechaComprobante; 
		public String		FolioOperacionRelacionada; 
		public String		IdTipoComprobanteBusqueda; 
		public String		TasaDescuentoAnticipo; 
		public String		TipoCambioAnticipo; 
		public String		MontoARecibirAnticipo; 
		public String		TipoCambioObservado; 
		public String		FolioCustodiaBusqueda; 
		public String		TipoCarteraCorredor; 
		public String		IdEstado; 
		public String		IdUsuarioModificacionRegistro; 
		public Stringstamp	FechaModificacionRegistro; 
		*/
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjResultPlanillaMayorSimilitud{
		public Long IdGenerarMovimientoCustodia;
		public Long IdPlantillaMovimientoCustodia;
		public Long MayorSimilitud;
	};
	
	public static class ObjInstrumento {
		public Long					IdInstrumento;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public String				Nemotecnico;
		public String 				ISIN;
		public String				IdEmisor;
		public String				IdTipoReajuste;
		public String				IdMonedaEmision;
		public java.sql.Date		FechaEmisionInstrumento;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String				IdMonedaInstrumento;
		public BigDecimal			CorteMinimo;
		public Long					Tamano;
		public String				IndArt104;
		public BigDecimal			TasaFiscal;
		public String				ValorizaPor;
		public String				TipoValorizacion;
		public String				TieneCupon;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	////////********************************************************************************************************************************************////////
	public static class ObjRegistroAProcesar {	
		public Long					IdGenerarMovimientoCustodia;	//ID Interno del proceso
		public String				TablaAfectada; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
		public Long					IdCustodia;
		public Long					FolioCustodia;
		
		/////////////////////////// OPERACION
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public Long					IdCuentaCustodioPortafolio;
		public Long					IdClienteRelacionado; 					//----------------------------------------------------
		public Long					IdPortafolioRelacionado; 				//----------------------------------------------------
		public Long					IdCuentaCustodioPortafolioRelacionado;	//----------------------------------------------------	
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public String				IdTipoOperacion;
		public String				AccionOperacion;
		public Long					IdGrupoOperacion; 
		public Long					IdOperacion;
		public String				IdConcepto;
		public Long					FolioOperacionRelacionada;
		public String				IdCondicionLiquidacion; 
		public java.sql.Date		FechaLiquidacion;		
		public String				IdTipoProducto; 
		public String				IdTipoCanalIngreso; 
		public String				CodigoIFRS;		 		 
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante; 
		public String				FolioExternoReferencia; 
		public Long					IdMesa; 
		public String				IdBolsa;
		public String				EsIntraday; 
		public String				TipoCarteraCorredor;
		public String				UsaGarantiaEspecifica;
		public String				CobraPrimaAlInicio; 
		public String				IdTipoPrestamo;	
		public String 				IdMonedaAdicional;
		public String 				FolioSistemaExterno;
		
		/////////////////////////// GARANTIAS
		public Long					FolioCarteraGarantizada; 
		public Long					IdInstrumentoCarteraGarantizada; 		
		public Long					IdContraparteGarantia; 
		public String				RazonSocialContraparteGarantia; 
		public String				CuentaContraparteGarantia; 
		public String				IdOrigenGarantia;
		public Long					IdEspecieGarantia;
		public String				EsEspecifica;
		
		/////////////////////////// INSTRUMENTO
		public Long					IdInstrumento;
		public String				Nemotecnico;
		public Long					IdInstrumentoAnterior;
		public String				IdMonedaInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				IdEmisor; 		
		public String				EsFungible;
		public String				IdBoveda;
		public String				CuentaBoveda;
		public String				EsNominal;
		public java.sql.Date		FechaVencimientoInstrumento;		//SOLO PARA IF
		public java.sql.Date		FechaEmisionInstrumento;			//SOLO PARA IF
		public String 				NemoSVS; 							//SOLO PARA IF
		
		/////////////////////////// CANTIDADES Y MONTOS
		public String				IdMonedaEmision; 
		public java.sql.Date		FechaFactorMonedaInstrumento; 
		public BigDecimal			ValorFactorMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			CorteMinimo; 
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra; 
		public BigDecimal			MontoCompra; 
		public BigDecimal			MontoCompraMonedaInstrumento;
		public BigDecimal			TasaAgente; 
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			MontoAPlazo; 
		public BigDecimal			MontoAPlazoMonedaInstrumento;
		
		/////////////////////////// FONDOS MUTUOS
		public String				TipoCarteraNormativa; 
		public java.sql.Date		FechaCurse; 
		public String				TipoFactorAjuste; 
		public BigDecimal			ValorFactorAjusteaFechaCurse; 
		public BigDecimal			CostoEnFactorAjuste;
		public String				IdRegimenFondoMutuo;
		public java.sql.Date		FechaOrigenCompra;
		public String				OrdenaFechaOrigenCompra;
		/////////////////////////// FORWARD
		public Long					IdSpotAsociado; 
		public Long					IdModalidadCumplimiento; 
		public BigDecimal			MontoMonedaPrincipal; 
		public BigDecimal			MontoMonedaSecundaria; 
		public String				IdMonedaLiquidacion; 
		public BigDecimal			MontoLiquidacion; 
		public BigDecimal			MontoLiquidacionMonedaPrincipal; 
		public BigDecimal			MontoDiferido; 
		public BigDecimal			MontoUtilidadDiferida; 
		public BigDecimal			MontoPerdidaDiferida; 
		public BigDecimal			MontoMargen; 
		public BigDecimal			MontoValorRazonableActivo; 
		public BigDecimal			MontoValorRazonablePasivo; 
		public BigDecimal			MontoValorRazonableNeto; 
		public BigDecimal			TasaValorRazonableActivo; 
		public BigDecimal			TasaValorRazonablePasivo; 
		public BigDecimal			MontoDevengoTipoCambio; 
		public BigDecimal			MontoDevengoTipoCambioAnterior; 
		public BigDecimal			MontoDiferidoDiario; 
		public BigDecimal			MontoDiferidoAcumulado; 
		public BigDecimal			ValorTipoCambioSpot; 
		public BigDecimal			ValorTipoCambioCotizador; 
		public String				EsPuntos; 
		public BigDecimal			TasaPuntosMonedaPrincipal; 
		public BigDecimal			TasaPuntosMonedaSecundaria; 
		public BigDecimal			MontoMatriz; 
		public BigDecimal			TasaDescuentoAnticipo; 
		public BigDecimal			TipoCambioAnticipo; 
		public BigDecimal			MontoARecibirAnticipo; 
		public BigDecimal			TipoCambioObservado;		
		public java.sql.Date		FechaEfectiva; // RF
		public Long					IdPlaza; // RF
		public Long					IdFixing; // RF
		public java.sql.Date		FechaFixing; // RF
		public java.sql.Time		HoraFixing; // RF
		public Long					IdReferenciaMercado; // RF
		public String				IdMonedaPrincipal; // RF
		public String				IdMonedaSecundaria; // RF
		public BigDecimal			ValorTipoCambioCierre; // RF
		public BigDecimal			ValorParidadCierre; // RF
		public BigDecimal			ValorTipoCambioCosto; // RF
		public BigDecimal			ValorParidadCosto;// RF
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjRegistroProcesado {
		public Long					IdGenerarMovimientoCustodia;	//ID Interno del proceso
		public Long					IdPlantillaMovimientoCustodia;
		public String				TablaAfectada; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
		public Long					IdCustodia;
		public Long					FolioCustodia;
		
		/////////////////////////// INFORMACION DE SISTEMA
		public Long					IdPais;
		public String				IdMonedaSistema; 
		public String				IdMonedaAdicional;	
		
		/////////////////////////// OPERACION
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public Long					IdCuentaCustodioPortafolio;
		public Long					IdClienteRelacionado; 					//----------------------------------------------------
		public Long					IdPortafolioRelacionado; 				//----------------------------------------------------
		public Long					IdCuentaCustodioPortafolioRelacionado;	//----------------------------------------------------	
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public String				IdTipoOperacion;
		public String				IdAccionOperacion;
		public Long					IdGrupoOperacion; 
		public Long					IdOperacion;
		public String				IdConcepto;
		public Long					FolioOperacionRelacionada;
		public String				IdCondicionLiquidacion; 
		public java.sql.Date		FechaLiquidacion;		
		public String				IdTipoProducto; 
		public String				IdTipoCanalIngreso; 		
		public String				CodigoIFRS;		 		 
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante; 
		public String				FolioExternoReferencia; 
		public Long					IdMesa; 
		public String				IdBolsa;
		public String				EsIntraday; 
		public String				TipoCarteraCorredor;
		public String				UsaGarantiaEspecifica;
		public String				CobraPrimaAlInicio; 
		public String				IdTipoPrestamo;		
		public String 				FolioSistemaExterno;
		
		/////////////////////////// GARANTIAS
		public Long					FolioCarteraGarantizada; 
		public Long					IdInstrumentoCarteraGarantizada; 		
		public Long					IdContraparteGarantia; 
		public String				RazonSocialContraparteGarantia; 
		public String				CuentaContraparteGarantia; 
		public String				IdOrigenGarantia;
		public Long					IdEspecieGarantia;
		public String				EsEspecifica;
		
		/////////////////////////// INSTRUMENTO
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento;
		public Long					IdInstrumentoAnterior;
		public String				Nemotecnico; 
		public String				IdEmisor; 
		public String				NemoSVS;
		public String				IdMonedaInstrumento;
		public String				IdTipoReajuste; 
		public java.sql.Date		FechaEmisionInstrumento; 
		public java.sql.Date		FechaVencimientoInstrumento; 
		public String				EsFungible; 		
		public Long					Tamano; 		
		public String				IdBoveda;
		public String				IdBovedaEsIncluyente;
		public String				CuentaBoveda; 		
		public String				EsNominal;
		public String				ISIN;
		public String				IndArt104;
		public BigDecimal			TasaFiscal;
		public String				TipoValorizacion;
		public String				TieneCorteDeCupon;
		public String				ValorizaPor;

		/////////////////////////// CANTIDADES Y MONTOS
		public String				IdMonedaEmision; 
		public java.sql.Date		FechaFactorMonedaInstrumento; 
		public BigDecimal			ValorFactorMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			CorteMinimo; 
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra; 
		public BigDecimal			MontoCompra; 
		public BigDecimal			MontoCompraMonedaInstrumento; 
		public BigDecimal			TasaAgente; 
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			MontoAPlazo; 
		public BigDecimal			MontoAPlazoMonedaInstrumento; 
		
		/////////////////////////// FONDOS MUTUOS
		public String				TipoCarteraNormativa; 
		public java.sql.Date		FechaCurse; 
		public String				TipoFactorAjuste; 
		public BigDecimal			ValorFactorAjusteaFechaCurse; 
		public BigDecimal			CostoEnFactorAjuste;
		public String               IdRegimenFondoMutuo;
		public java.sql.Date		FechaOrigenCompra;
		public String				OrdenaFechaOrigenCompra;
		/////////////////////////// FORWARD
		public Long					IdSpotAsociado; 
		public Long					IdModalidadCumplimiento; 
		public BigDecimal			MontoMonedaPrincipal; 
		public BigDecimal			MontoMonedaSecundaria; 
		public String				IdMonedaLiquidacion; 
		public BigDecimal			MontoLiquidacion; 
		public BigDecimal			MontoLiquidacionMonedaPrincipal; 
		public BigDecimal			MontoDiferido; 
		public BigDecimal			MontoUtilidadDiferida; 
		public BigDecimal			MontoPerdidaDiferida; 
		public BigDecimal			MontoMargen; 
		public BigDecimal			MontoValorRazonableActivo; 
		public BigDecimal			MontoValorRazonablePasivo; 
		public BigDecimal			MontoValorRazonableNeto; 
		public BigDecimal			TasaValorRazonableActivo; 
		public BigDecimal			TasaValorRazonablePasivo; 
		public BigDecimal			MontoDevengoTipoCambio; 
		public BigDecimal			MontoDevengoTipoCambioAnterior; 
		public BigDecimal			MontoDiferidoDiario; 
		public BigDecimal			MontoDiferidoAcumulado; 
		public BigDecimal			ValorTipoCambioSpot; 
		public BigDecimal			ValorTipoCambioCotizador; 
		public String				EsPuntos; 
		public BigDecimal			TasaPuntosMonedaPrincipal; 
		public BigDecimal			TasaPuntosMonedaSecundaria; 
		public BigDecimal			MontoMatriz; 
		public BigDecimal			TasaDescuentoAnticipo; 
		public BigDecimal			TipoCambioAnticipo; 
		public BigDecimal			MontoARecibirAnticipo; 
		public BigDecimal			TipoCambioObservado;		
		public java.sql.Date		FechaEfectiva; // RF
		public Long					IdPlaza; // RF
		public Long					IdFixing; // RF
		public java.sql.Date		FechaFixing; // RF
		public java.sql.Time		HoraFixing; // RF
		public Long					IdReferenciaMercado; // RF
		public String				IdMonedaPrincipal; // RF
		public String				IdMonedaSecundaria; // RF
		public BigDecimal			ValorTipoCambioCierre; // RF
		public BigDecimal			ValorParidadCierre; // RF
		public BigDecimal			ValorTipoCambioCosto; // RF
		public BigDecimal			ValorParidadCosto;// RF
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class EstandarizacionRegistroCustodiaEntrada {
	    public Long							p_IdEmpresa;
	    public List<ObjRegistroAProcesar>	p_RegistrosAProcesar;
	    
	    public EstandarizacionRegistroCustodiaEntrada() {
	    	p_RegistrosAProcesar	= new ArrayList<ObjRegistroAProcesar>();
	    };
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class EstandarizacionRegistroCustodiaSalida {		
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjRegistroProcesado>	p_RegistrosProcesados;

		public EstandarizacionRegistroCustodiaSalida (){
			p_RegistrosProcesados	= new ArrayList<ObjRegistroProcesado>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	////////********************************************************************************************************************************************////////
	@Trace
	public EstandarizacionRegistroCustodiaSalida logica(Connection con, EstandarizacionRegistroCustodiaEntrada in) throws Exception {
		EstandarizacionRegistroCustodiaSalida out = new EstandarizacionRegistroCustodiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		
		try {
			//Configuracion sistema
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String IdMonedaSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
			String IdMonedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional");
			
			
			
			//Obtiene Plantilla
			HashMap<String,Object> paramsPlantillaMovimientoCustodia = new HashMap<>();
			//paramsPlantillaMovimientoCustodia.put("IdEmpresa", in.p_IdEmpresa);
			List<ObjPlantillaMovimientoCustodia> PlantillaMovimientoCustodia = sql.query(con, SQL_PlantillaMovimientoCustodia, paramsPlantillaMovimientoCustodia, ObjPlantillaMovimientoCustodia.class);
			HashMap<String,Object> PlantillaMovCustodia = Utils.List2Hash(PlantillaMovimientoCustodia, "IdPlantillaCustodia");			
			
			
			//Obtiene Informacion adicional instrumento
			HashMap<String,Object> paramsInstrumento = new HashMap<>();
			paramsInstrumento.put("IdInstrumento", in.p_RegistrosAProcesar.stream().map(inst -> inst.IdInstrumento).distinct().collect(Collectors.toList()));
			
			List<ObjInstrumento> INS = sql.query(con, SQL_Instrumento, paramsInstrumento, ObjInstrumento.class);
			HashMap<String,Object> Instrumentos = Utils.List2Hash(INS, "IdInstrumento");
			
			
			//*********************************************************************************************
			//** ESTANDARIZAR LOS REGISTROS
			//*********************************************************************************************
			for (ObjRegistroAProcesar DatosEntrada: in.p_RegistrosAProcesar) {
				ObjInstrumento Instrumento = (ObjInstrumento) Instrumentos.get(DatosEntrada.IdInstrumento);
				ObjRegistroProcesado RegistroProcesado = new ObjRegistroProcesado();
				Utils.copyPojo(DatosEntrada, RegistroProcesado);
				RegistroProcesado.IdTipoReajuste = DatosEntrada.IdTipoReajusteInstrumento;

				String IdAccionOperacion = "";
				switch (DatosEntrada.AccionOperacion) {					 
			        case "INGRESO": IdAccionOperacion = "ING"; break;
			        case "CERTIFICACION": IdAccionOperacion = "CER"; break;
			        case "RETIRO": IdAccionOperacion = "RET"; break;
			        case "VENCIMIENTO": IdAccionOperacion = "VEN"; break;
			        case "ANTICIPO": IdAccionOperacion = "ANT"; break;
			        case "ANULACION": IdAccionOperacion = "ANU"; break;			        
			        case "MODIFICACION": IdAccionOperacion = "MOD"; break;
				};
				
				if (Instrumento != null ){
					RegistroProcesado.IdClasificacionTipoInstrumento	= Instrumento.IdClasificacionTipoInstrumento;
					RegistroProcesado.IdTipoInstrumento 				= Instrumento.IdTipoInstrumento;
				};
				//RegistroProcesado.IdInstrumento = (DatosEntrada.IdInstrumento != null && !DatosEntrada.IdInstrumento.equals(0L)) ? RegistroProcesado.IdInstrumento : null;  
				
				
				//** RECUPERAR PLANTILLA MAS ACORDE A TIPO OPERACION
				ObjResultPlanillaMayorSimilitud rp = new ObjResultPlanillaMayorSimilitud();
				for (ObjPlantillaMovimientoCustodia plantillaDato: PlantillaMovimientoCustodia){
					Long Posicion = 0L;
					if (!IdAccionOperacion.equalsIgnoreCase(plantillaDato.IdAccionOperacionLlave)) continue;
										
					if (RegistroProcesado.IdClasificacionTipoInstrumento != null && RegistroProcesado.IdClasificacionTipoInstrumento.equalsIgnoreCase(plantillaDato.IdClasificacionTipoInstrumentoLlave)){ Posicion = Posicion + 200L; };
					if (RegistroProcesado.IdClasificacionTipoInstrumento != null && plantillaDato.IdClasificacionTipoInstrumentoLlave == null){ Posicion = Posicion + 100L; };				
					
					if (RegistroProcesado.IdTipoInstrumento != null && RegistroProcesado.IdTipoInstrumento.equalsIgnoreCase(plantillaDato.IdTipoInstrumentoLlave)){ Posicion = Posicion + 220L; };
					if (RegistroProcesado.IdTipoInstrumento != null && plantillaDato.IdTipoInstrumentoLlave == null){ Posicion = Posicion + 110L; };
					
					if (Instrumento != null && Instrumento.equals(plantillaDato.IdInstrumentoLlave)){ Posicion = Posicion + 230L; };
					if (Instrumento != null && plantillaDato.IdInstrumentoLlave == null){ Posicion = Posicion + 130L; };
					
					if (RegistroProcesado.IdTipoProducto != null && RegistroProcesado.IdTipoProducto.equalsIgnoreCase(plantillaDato.IdTipoProductoLlave)){ Posicion = Posicion + 20L;};
					if (RegistroProcesado.IdTipoProducto != null && plantillaDato.IdTipoProductoLlave == null){ Posicion = Posicion + 10L; };
					
					if (RegistroProcesado.IdTipoOperacion != null && RegistroProcesado.IdTipoOperacion.equalsIgnoreCase(plantillaDato.IdTipoOperacionLlave)){ Posicion = Posicion + 20L; };
					if (RegistroProcesado.IdTipoOperacion != null && plantillaDato.IdTipoOperacionLlave == null){ Posicion = Posicion + 10L; };
					
					if (RegistroProcesado.TablaAfectada != null && RegistroProcesado.TablaAfectada.equalsIgnoreCase("GARANTIA") && plantillaDato.EsGarantiaLlave.equals("S")){ Posicion = Posicion + 20L; };
					if (RegistroProcesado.TablaAfectada != null && !RegistroProcesado.TablaAfectada.equalsIgnoreCase("GARANTIA") && plantillaDato.EsGarantiaLlave.equals("N")){ Posicion = Posicion + 10L; };
					
					if (RegistroProcesado.IdOrigenGarantia != null && RegistroProcesado.IdOrigenGarantia.equalsIgnoreCase(plantillaDato.IdOrigenInstrumentoLlave)){ Posicion = Posicion + 20L; };
					if (RegistroProcesado.IdOrigenGarantia != null && plantillaDato.IdOrigenInstrumentoLlave == null){ Posicion = Posicion + 10L; };
					

					if (rp.IdGenerarMovimientoCustodia == null){
						rp.IdGenerarMovimientoCustodia = RegistroProcesado.IdGenerarMovimientoCustodia;
						rp.IdPlantillaMovimientoCustodia = plantillaDato.IdPlantillaCustodia;
						rp.MayorSimilitud = Posicion;
					};
					
					if (RegistroProcesado.IdGenerarMovimientoCustodia!=null &&RegistroProcesado.IdGenerarMovimientoCustodia.equals(rp.IdGenerarMovimientoCustodia) && Posicion > rp.MayorSimilitud){
						rp.IdGenerarMovimientoCustodia = RegistroProcesado.IdGenerarMovimientoCustodia;
						rp.IdPlantillaMovimientoCustodia = plantillaDato.IdPlantillaCustodia;
						rp.MayorSimilitud = Posicion;
					};
				};
				
				
				//** (2) APLICAR PLANTILLA
				if (rp.IdGenerarMovimientoCustodia != null){
					ObjPlantillaMovimientoCustodia Plantilla = (ObjPlantillaMovimientoCustodia) PlantillaMovCustodia.get(rp.IdPlantillaMovimientoCustodia);
					
					//(
					//	- : String con error
					//	0001-12-01 : Fecha con error
					//)
					
					//** SETEO INFORMACION INSTRUMENTO   					
					RegistroProcesado.Nemotecnico 					= Plantilla.Nemotecnico != null ? (Plantilla.Nemotecnico.equalsIgnoreCase("!") || RegistroProcesado.Nemotecnico != null ? RegistroProcesado.Nemotecnico : (Instrumento != null ? Instrumento.Nemotecnico : "-") ) : RegistroProcesado.Nemotecnico != null?RegistroProcesado.Nemotecnico:null;
					RegistroProcesado.IdEmisor 						= Plantilla.IdEmisor != null ? (Plantilla.IdEmisor.equalsIgnoreCase("!") || RegistroProcesado.IdEmisor != null ? RegistroProcesado.IdEmisor :  (Instrumento != null ? Instrumento.IdEmisor : "-" ) ) : RegistroProcesado.IdEmisor != null?RegistroProcesado.IdEmisor:null;
					RegistroProcesado.EsFungible 					= Plantilla.EsFungible != null ? (Plantilla.EsFungible.equalsIgnoreCase("!") || RegistroProcesado.EsFungible != null  ? RegistroProcesado.EsFungible : Plantilla.EsFungible) : RegistroProcesado.EsFungible != null?RegistroProcesado.EsFungible:null;					
					RegistroProcesado.IdTipoReajuste 				= Plantilla.IdTipoReajuste != null ? (Plantilla.IdTipoReajuste.equalsIgnoreCase("!") ? RegistroProcesado.IdTipoReajuste : ( Instrumento != null ? Instrumento.IdTipoReajuste : "-") ) : null;
					RegistroProcesado.IdMonedaInstrumento 			= Plantilla.IdMonedaInstrumento != null ? (Plantilla.IdMonedaInstrumento.equalsIgnoreCase("!") ? RegistroProcesado.IdMonedaInstrumento : ( Instrumento != null ? Instrumento.IdMonedaInstrumento : "-") ) : null;					
					RegistroProcesado.IdMonedaEmision    			= Plantilla.IdMonedaEmision != null ? (Plantilla.IdMonedaEmision.equalsIgnoreCase("!") ? RegistroProcesado.IdMonedaEmision : ( Instrumento != null ? Instrumento.IdMonedaEmision : "-") ) : null;
					RegistroProcesado.FechaEmisionInstrumento 		= Plantilla.FechaEmisionInstrumento != null ? (Plantilla.FechaEmisionInstrumento.equalsIgnoreCase("") && Instrumento != null ? Instrumento.FechaEmisionInstrumento : (Plantilla.FechaEmisionInstrumento.equalsIgnoreCase("!") ? RegistroProcesado.FechaEmisionInstrumento : Fecha.toDate( (Plantilla.FechaEmisionInstrumento.equalsIgnoreCase("")?"0001-01-01":Plantilla.FechaEmisionInstrumento) )) )   : null;
					RegistroProcesado.FechaVencimientoInstrumento 	= Plantilla.FechaVencimientoInstrumento != null ? (Plantilla.FechaVencimientoInstrumento.equalsIgnoreCase("!") || RegistroProcesado.FechaVencimientoInstrumento != null ? RegistroProcesado.FechaVencimientoInstrumento : ( Instrumento != null ? Instrumento.FechaVencimientoInstrumento : Fecha.toDate("0001-12-01")) ) : null;

					if (RegistroProcesado.IdClasificacionTipoInstrumento != null && RegistroProcesado.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")){
						RegistroProcesado.FechaVencimientoInstrumento = Fecha.toDate("0001-01-01"); 
					}
					
					RegistroProcesado.Tamano 						= Plantilla.Tamano != null && Instrumento != null ? Instrumento.Tamano : null;
					RegistroProcesado.ISIN 							= Plantilla.ISIN != null && Instrumento != null ? Instrumento.ISIN : null;
					RegistroProcesado.TieneCorteDeCupon 			= Plantilla.TieneCorteDeCupon != null && Instrumento != null ? Instrumento.TieneCupon : null;
					RegistroProcesado.IndArt104 					= Plantilla.IndArt104 != null && Instrumento != null ? Instrumento.IndArt104 : null;
					RegistroProcesado.TasaFiscal 					= Plantilla.TasaFiscal != null && Instrumento != null ? Instrumento.TasaFiscal : null;					
					RegistroProcesado.TipoValorizacion 				= Plantilla.TipoValorizacion != null && Instrumento != null ? Instrumento.TipoValorizacion : null;					
					RegistroProcesado.ValorizaPor 					= Plantilla.ValorizaPor != null && Instrumento != null ? (Instrumento.ValorizaPor != null ? Instrumento.ValorizaPor : "P") : null;
					
					if (RegistroProcesado.IdClasificacionTipoInstrumento!=null && RegistroProcesado.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
						if ( Plantilla.NemoSVS != null && Plantilla.NemoSVS.equalsIgnoreCase("!") && RegistroProcesado.FechaVencimientoInstrumento != null){  //localizacionSistema.equals("CL")
							if (RegistroProcesado.NemoSVS == null || RegistroProcesado.NemoSVS.equalsIgnoreCase("") ) {             // si no viene NemoSVS, lo calcula
								ObtenerNemoSVS.ObtenerNemoSVS_Entrada InIngresoCustodia = new ObtenerNemoSVS.ObtenerNemoSVS_Entrada();				
								ObtenerNemoSVS.Instrumento_Entrada IN = new ObtenerNemoSVS.Instrumento_Entrada();
								IN.IdInterno		= DatosEntrada.IdGenerarMovimientoCustodia;
								IN.FechaVencimiento	= RegistroProcesado.FechaVencimientoInstrumento.toString();
								IN.Plazo			= RegistroProcesado.PlazoEnDiasOperacion;
								IN.IdEmisor			= RegistroProcesado.IdEmisor;
								IN.Nemotecnico		= RegistroProcesado.Nemotecnico;
								IN.IdTipoReajuste	= RegistroProcesado.IdTipoReajuste;
								IN.PagaderoEn		= RegistroProcesado.IdMonedaInstrumento;
								InIngresoCustodia.p_Instrumentos.add(IN);
								
								ObtenerNemoSVS ONS = new ObtenerNemoSVS();
								ObtenerNemoSVS.ObtenerNemoSVS_Salida OUT = ONS.logica(con, InIngresoCustodia);
								for (ObtenerNemoSVS.Instrumento_Salida  result : OUT.p_out_Instrumento){
									RegistroProcesado.NemoSVS = result.NemoSVS;
								};
							}
						} else {
							RegistroProcesado.NemoSVS = null;
						};						
					} else {
						RegistroProcesado.NemoSVS = RegistroProcesado.Nemotecnico;
					};
					
					//** para RF e IF, de la cuenta propia asignar la mesa x defecto para la Cta Propia 
					

					
					
					//** SETEO INFORMACION OPERACION
					RegistroProcesado.IdBolsa	= Plantilla.IdBolsa != null ? (Plantilla.IdBolsa.equalsIgnoreCase("!") ? RegistroProcesado.IdBolsa : "-" ) : null;
					RegistroProcesado.IdBoveda	= Plantilla.IdBoveda != null ? (Plantilla.IdBoveda.equalsIgnoreCase("!") ? RegistroProcesado.IdBoveda : "-" ) : null;
					
				}; //FIN APLICAR PLANTILLA
				
				
				//** (3) SE ESTANDARIZA INFORMACION DE SISTEMA
				RegistroProcesado.IdMonedaSistema 	= IdMonedaSistema;
				RegistroProcesado.IdMonedaAdicional = localizacionSistema.equalsIgnoreCase("CL") && RegistroProcesado.IdMonedaAdicional != null?RegistroProcesado.IdMonedaAdicional:IdMonedaAdicional;
				
				
				//** (4) SE SETEAN ALGUNOS VALORES NULL
				if ( DatosEntrada.IdTipoComprobante == null) RegistroProcesado.IdTipoComprobante = "CUS";
				if ( DatosEntrada.FechaComprobante == null) RegistroProcesado.FechaComprobante = DatosEntrada.FechaOperacion;
								
				out.p_RegistrosProcesados.add(RegistroProcesado);
			};//FIN ESTANDARIZACION DATOS DE ENTRADA
		}
		catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
			throw e;
		}
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EstandarizacionRegistroCustodiaEntrada jin=gson.fromJson(in.toString() ,EstandarizacionRegistroCustodiaEntrada.class);
		EstandarizacionRegistroCustodiaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_RegistrosAProcesar\":[{\"IdGenerarMovimientoCustodia\":1834,\"TablaAfectada\":\"CARTERA\",\"IdCustodia\":1524348,\"FolioCustodia\":20685998,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"COMPAC\",\"AccionOperacion\":\"ANTICIPO\",\"IdGrupoOperacion\":null,\"IdOperacion\":null,\"IdConcepto\":\"ANPCME\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":null,\"FechaLiquidacion\":\"2018-07-31 00:00:00.000\",\"IdTipoProducto\":\"MERF\",\"IdTipoCanalIngreso\":null,\"CodigoIFRS\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":null,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdMesa\":null,\"IdBolsa\":null,\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdMonedaAdicional\":null,\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":null,\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":346378,\"Nemotecnico\":null,\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"\",\"EsFungible\":\"N\",\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":null,\"IdMonedaEmision\":\"CLP\",\"FechaFactorMonedaInstrumento\":null,\"ValorFactorMonedaInstrumento\":null,\"Cantidad\":1,\"CorteMinimo\":null,\"ValorRescate\":5000.00000000,\"PrecioSucio\":null,\"PrecioCompra\":null,\"TasaCompra\":null,\"MontoCompra\":null,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":2.5,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":5125,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null},{\"IdGenerarMovimientoCustodia\":1682,\"TablaAfectada\":\"SUBYACENTE\",\"IdCustodia\":1682,\"FolioCustodia\":255093,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"COMPAC\",\"AccionOperacion\":\"ANTICIPO\",\"IdGrupoOperacion\":null,\"IdOperacion\":null,\"IdConcepto\":\"ANPCME\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":null,\"FechaLiquidacion\":\"2018-07-31 00:00:00.000\",\"IdTipoProducto\":\"MERF\",\"IdTipoCanalIngreso\":null,\"CodigoIFRS\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":null,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdMesa\":null,\"IdBolsa\":null,\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdMonedaAdicional\":null,\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":null,\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":2224,\"Nemotecnico\":null,\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"ABN AMRO\",\"EsFungible\":\"S\",\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":null,\"IdMonedaEmision\":\"CLP\",\"FechaFactorMonedaInstrumento\":null,\"ValorFactorMonedaInstrumento\":null,\"Cantidad\":1.00000000,\"CorteMinimo\":null,\"ValorRescate\":5320.00000000,\"PrecioSucio\":null,\"PrecioCompra\":null,\"TasaCompra\":null,\"MontoCompra\":null,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null},{\"IdGenerarMovimientoCustodia\":1524349,\"TablaAfectada\":\"CARTERA\",\"IdCustodia\":1524349,\"FolioCustodia\":20685999,\"IdCliente\":249212,\"IdPortafolio\":1451214,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"COMPAC\",\"AccionOperacion\":\"ANTICIPO\",\"IdGrupoOperacion\":null,\"IdOperacion\":null,\"IdConcepto\":\"ANPCME\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":null,\"FechaLiquidacion\":\"2018-07-31 00:00:00.000\",\"IdTipoProducto\":\"MERF\",\"IdTipoCanalIngreso\":null,\"CodigoIFRS\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":null,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdMesa\":null,\"IdBolsa\":null,\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdMonedaAdicional\":null,\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":null,\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":2224,\"Nemotecnico\":null,\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"ABN AMRO\",\"EsFungible\":\"S\",\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":null,\"IdMonedaEmision\":\"CLP\",\"FechaFactorMonedaInstrumento\":null,\"ValorFactorMonedaInstrumento\":null,\"Cantidad\":1.00000000,\"CorteMinimo\":null,\"ValorRescate\":20000.00000000,\"PrecioSucio\":null,\"PrecioCompra\":null,\"TasaCompra\":null,\"MontoCompra\":null,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null}]}";
				String json2 = "{\"p_IdEmpresa\":1,\"p_RegistrosAProcesar\":[{\"IdGenerarMovimientoCustodia\":1682,\"TablaAfectada\":\"CARTERA\",\"IdCustodia\":1524346,\"FolioCustodia\":null,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":null,\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":null,\"AccionOperacion\":\"INGRESO\",\"IdGrupoOperacion\":null,\"IdOperacion\":null,\"IdConcepto\":null,\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":null,\"FechaLiquidacion\":null,\"IdTipoProducto\":null,\"IdTipoCanalIngreso\":null,\"CodigoIFRS\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":null,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdMesa\":null,\"IdBolsa\":null,\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdMonedaAdicional\":null,\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":null,\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":2224,\"Nemotecnico\":null,\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":null,\"IdTipoReajusteInstrumento\":null,\"IdEmisor\":null,\"EsFungible\":null,\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":null,\"IdMonedaEmision\":null,\"FechaFactorMonedaInstrumento\":null,\"ValorFactorMonedaInstrumento\":null,\"Cantidad\":1.00000000,\"CorteMinimo\":null,\"ValorRescate\":5320.00000000,\"PrecioSucio\":null,\"PrecioCompra\":0E-8,\"TasaCompra\":3.20000000,\"MontoCompra\":null,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null}]}";
				EstandarizacionRegistroCustodiaEntrada Objin = gson.fromJson(json, EstandarizacionRegistroCustodiaEntrada.class);
				
				EstandarizacionRegistroCustodia  kk = new EstandarizacionRegistroCustodia();
				EstandarizacionRegistroCustodiaSalida Objout = kk.logica(con, Objin);				
				System.out.println(Objout.toString());
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}
