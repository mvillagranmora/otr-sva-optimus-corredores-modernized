SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::HIS.RespaldarCustodia";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::HIS.RespaldarCustodia" ( 
    --VARIABLES DEL PROCESO
    IN p_FechaRespaldo							DATE,
    IN p_RespaldaCarteras 						NVARCHAR(1) DEFAULT 'S',
    IN p_RespaldaGarantias 						NVARCHAR(1) DEFAULT 'S',
    IN p_IdClasificacionTipoInstrumento 		NVARCHAR(4) DEFAULT '',
    IN p_IdClient						 		BIGINT DEFAULT 0,
    IN p_IdPortafolio						 	BIGINT DEFAULT 0,
    IN p_IdInstrumento						 	BIGINT DEFAULT 0,
    IN p_TipoInstrumento						NVARCHAR(4) DEFAULT '',
    IN p_IdCarga							 	BIGINT DEFAULT 0,
    
    OUT p_out_countCarteras 	BIGINT,
    OUT p_out_countSubyacentes 	BIGINT,
    OUT p_out_countReservas 	BIGINT,
    OUT p_out_countGarantias 	BIGINT,
    OUT p_out_countMovTransito	BIGINT,
    OUT p_out_countNegativas		BIGINT

)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS

BEGIN
	DECLARE v_countCarteras, v_countSubyacentes, v_countGarantias, v_countReservas,v_countMovTransito,v_countNegativas   BIGINT := 0;
	
	IF IFNULL(p_FechaRespaldo,'0001-01-01') = '0001-01-01' THEN
		p_out_countCarteras := 0;
		p_out_countSubyacentes := 0;
		p_out_countGarantias := 0;
		p_out_countReservas := 0;
		p_out_countNegativas := 0;
		RETURN;
	END IF;
	
	
	/**** inicio carteras, subyacentes y reservas ****/
	IF p_RespaldaCarteras = 'S' THEN
		DELETE FROM "BSCL.Custodia.BD::HIS.Cartera" 
		WHERE "FechaRespaldo" = p_FechaRespaldo  
			AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
			AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
			AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
			AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
			AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
			;
		/**** subyacente, siempre de la mano con las carteras *****/
		DELETE 
		FROM "BSCL.Custodia.BD::HIS.Subyacente" AS S 
		WHERE "FechaRespaldo" = p_FechaRespaldo   
		AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
		AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
		AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
		
		AND NOT EXISTS (
					SELECT * FROM  "BSCL.Custodia.BD::HIS.Cartera" as CUS
 					WHERE S."FolioCarteraRelacionada" = CUS."FolioCartera"
 					AND CUS."FechaRespaldo" = p_FechaRespaldo  
 					AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN (CUS."IdClasificacionTipoInstrumento", '')
 					AND COALESCE( p_TipoInstrumento, '') IN (CUS."IdTipoInstrumento", '')
 					AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
		)	
		;
		INSERT INTO "BSCL.Custodia.BD::HIS.Cartera" (
			 "FechaRespaldo"
			,"IdCartera"
			,"FolioCartera"
			,"FolioCarteraRelacionada"
			,"FechaIngresoRegistro"
			,"FechaModificacionRegistro"
			,"IdUsuarioMod"
			,"IdEmpresa"
			,"FechaOperacion"
			,"FechaVencimientoOperacion"
			,"PlazoEnDiasOperacion"
			,"IdCliente"
			,"IdPortafolio"
			,"IdCuentaCustodioPortafolio"
			,"IdClienteRelacionado"
			,"IdPortafolioRelacionado"
			,"IdEstado"
			,"IdTipoProducto"
			,"IdTipoCanalIngreso"
			,"IdMonedaSistema"
			,"IdMonedaAdicional"
			,"IdConcepto"
			,"IdTipoOperacion"
			,"IdOperacion"
			,"IdTipoComprobante"
			,"FolioComprobante"
			,"FechaComprobante"
			,"FolioExternoReferencia"
			,"IdMesa"
			,"IdBolsa"
			,"IdSucursal"
			,"IdAgente"
			,"IdCondicionLiquidacion"
			,"FechaLiquidacion"
			,"UsaGarantiaEspecifica"
			,"CobraPrimaAlInicio"
			,"Glosa"
			,"IdUsuario"
			,"IdModulo"
			,"TipoCarteraCorredor"
			,"ValorFactorAjusteaFechaCurse"
			,"CostoInversionEnFactorAjuste"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"IdEmisor"
			,"NemoSVS"
			,"IdTipoReajuste"
			,"IdMonedaEmision"
			,"FechaEmisionInstrumento"
			,"FechaVencimientoInstrumento"
			,"EsFungible"
			,"TieneCorteDeCupon"
			,"CodigoIFRS"
			,"IdBoveda"
			,"IdMonedaInstrumento"
			,"Tamano"
			,"ISIN"
			,"ValorFactorMonedaInstrumento"
			,"Cantidad"
			,"CorteMinimo"
			,"ValorRescate"
			,"PrecioCompra"
			,"TasaCompra"
			,"MontoCompra"
			,"MontoCompraMonedaInstrumento"
			,"MontoCompraMonedaAdicional"
			,"PrecioAPlazo"
			,"MontoAPlazo"
			,"MontoAPlazoMonedaInstrumento"
			,"MontoAPlazoMonedaAdicional"
			,"FechaEfectiva"
			,"IdPlaza"
			,"IdFixing"
			,"FechaFixing"
			,"HoraFixing"
			,"IdModalidadCumplimiento"
			,"IdReferenciaMercado"
			,"IdMonedaPrincipal"
			,"IdMonedaSecundaria"
			,"ValorTipoCambioCierre"
			,"ValorParidadCierre"
			,"ValorTipoCambioCosto"
			,"ValorParidadCosto"
			,"MontoMonedaPrincipal"
			,"MontoMonedaSecundaria"
			,"IdCarga"
			,"INGGlosa"
			,"INGTasaAgente"
			,"INGFechaOperacion"
			,"INGIdCondicionLiquidacion"
			,"INGFechaLiquidacion"
			,"INGCantidad"
			,"INGValorRescate"
			,"INGPrecioPromedioCompra"
			,"INGMontoPrecioPromedioCompra"
			,"INGMontoPrecioPromedioMonedaInstrumento"
			,"INGMontoPrecioPromedioMonedaAdicional"
			,"INGPrecioAPlazo"
			,"INGMontoAPlazo"
			,"INGMontoAPlazoMonedaInstrumento"
			,"INGEsPuntos"
			,"INGValorTipoCambioSpot"
			,"INGValorTipoCambioCotizador"
			,"INGTasaPuntosMonedaPrincipal"
			,"INGTasaPuntosMonedaSecundaria"
			,"INGFechaValorizacionCartera"
			,"INGFechaValorizacionAnterior"
			,"INGFechaValorizacionActual"
			,"INGPrecioCompra"
			,"INGTasaCompra"
			,"INGMontoATasaCompraAnterior"
			,"INGMontoInteresCompraAnterior"
			,"INGMontoCompraAnteriorMonedaInstrumento"
			,"INGMontoInteresCompraAnteriorMonedaInstrumento"
			,"INGMontoReajusteCompraAnteriorMonedaInstrumento"
			,"INGMontoInteresCompraAnteriorMonedaAdicional"
			,"INGMontoATasaCompraActual"
			,"INGMontoCompraActualMonedaInstrumento"
			,"INGMontoCompraActualMonedaAdicional"
			,"INGDurationCompraActual"
			,"INGConvexidadCompraActual"
			,"INGMontoInteresCorridoCompraActual"
			,"INGMontoReajusteCorridoCompraActual"
			,"INGMontoInteresCorridoCompraActualMonedaInstrumento"
			,"INGMontoReajusteCorridoCompraActualMonedaInstrumento"
			,"INGMontoInteresCorridoCompraActualMonedaAdicional"
			,"INGMontoATasaCompraProximo"
			,"INGMontoInteresCompraProximo"
			,"INGMontoReajusteCompraProximo"
			,"INGMontoCompraProximoMonedaInstrumento"
			,"INGMontoInteresCompraProximoMonedaInstrumento"
			,"INGMontoReajusteCompraProximoMonedaInstrumento"
			,"INGMontoInteresCompraProximoMonedaAdicional"
			,"INGPrecioMercadoAnterior"
			,"INGTasaMercadoAnterior"
			,"INGMontoATasaMercadoAnterior"
			,"INGMontoInteresMercadoAnterior"
			,"INGMontoMercadoAnteriorMonedaInstrumento"
			,"INGMontoInteresMercadoAnteriorMonedaInstrumento"
			,"INGMontoInteresMercadoAnteriorMonedaAdicional"
			,"INGFechaPrecioTasaMercadoActual"
			,"INGFuenteTasaPrecioMercadoActual"
			,"INGPrecioMercadoActual"
			,"INGTasaMercadoActual"
			,"INGMontoATasaMercadoActual"
			,"INGMontoMercadoActualMonedaInstrumento"
			,"INGMontoMercadoActualMonedaAdicional"
			,"INGDurationMercadoActual"
			,"INGConvexidadMercadoActual"
			,"INGMontoInteresCorridoMercadoActual"
			,"INGMontoInteresCorridoMercadoActualMonedaInstrumento"
			,"INGMontoInteresCorridoMercadoActualMonedaAdicional"
			,"INGPrecioMercadoProximo"
			,"INGTasaMercadoProximo"
			,"INGMontoATasaMercadoProximo"
			,"INGMontoInteresMercadoProximo"
			,"INGMontoReajusteMercadoProximo"
			,"INGMontoMercadoProximoMonedaInstrumento"
			,"INGMontoInteresMercadoProximoMonedaInstrumento"
			,"INGMontoInteresMercadoProximoMonedaAdicional"
			,"INGTasaCorredora"
			,"INGFechaTasaCorredora"
			,"INGMontoATasaCorredora"
			,"INGMontoATasaCorredoraMonedaInstrumento"
			,"INGFechaCorteCupon"
			,"INGNumeroCupon"
			,"INGIdMonedaPagoCupon"
			,"INGMontoCorteCupon"
			,"INGFechaPagoCorteCupon"
			,"INGMontoCapitalCorteCupon"
			,"INGMontoInteresCorteCupon"
			,"INGMontoReajusteCorteCupon"
			,"INGMontoCorteCuponMonedaInstrumento"
			,"INGMontoCapitalCorteCuponMonedaInstrumento"
			,"INGMontoInteresCorteCuponMonedaInstrumento"
			,"INGMontoReajusteCorteCuponMonedaInstrumento"
			,"INGMontoMonedaPrincipal"
			,"INGMontoMonedaSecundaria"
			,"INGMontoDiferido"
			,"INGMontoUtilidadDiferida"
			,"INGMontoPerdidaDiferida"
			,"INGMontoMargen"
			,"INGMontoValorRazonableActivo"
			,"INGMontoValorRazonablePasivo"
			,"INGMontoValorRazonableNeto"
			,"INGTasaValorRazonableActivo"
			,"INGTasaValorRazonablePasivo"
			,"INGMontoDevengoTipoCambio"
			,"INGMontoDevengoTipoCambioAnterior"
			,"INGMontoDiferidoDiario"
			,"INGMontoDiferidoAcumulado"
			,"INGMontoMatriz"
			,"EGRGlosa"
			,"EGRFechaOperacion"
			,"EGRIdTipoOperacion"
			,"EGRIdOperacion"
			,"EGRIdMovimientoCarteraEnTransito"
			,"EGRIdTipoCanalIngreso"
			,"EGRIdCondicionLiquidacion"
			,"EGRFechaLiquidacion"
			,"EGRIdTipoComprobante"
			,"EGRFolioComprobante"
			,"EGRFechaComprobante"
			,"EGRFolioExternoReferencia"
			,"EGRIdModulo"
			,"EGRIdConcepto"
			,"EGRIdUsuario"
			,"EGRIdSucursal"
			,"EGRIdAgente"
			,"EGRFechaCurseRetiro"
			,"EGRCostoRetiroenFactorAjuste"
			,"EGRValorFactorAjusteaFechaRetiro"
			,"EGRCantidad"
			,"EGRValorRescate"
			,"EGRPrecio"
			,"EGRTasa"
			,"EGRMonto"
			,"EGRMontoMonedaInstrumento"
			,"EGRSpread"
			,"EGRPrecioAPlazo"
			,"EGRMontoAPlazo"
			,"EGRMontoAPlazoMonedaInstrumento"
			,"EGRIdSpotAsociado"
			,"EGRMontoMonedaPrincipal"
			,"EGRMontoMonedaSecundaria"
			,"EGRIdMonedaLiquidacion"
			,"EGRMontoLiquidacion"
			,"EGRMontoLiquidacionMonedaPrincipal"
			,"EGRValorParidad"
			,"EGRValorTipoCambioCotizador"
			,"EGRTasaDescuentoAnticipo"
			,"EGRTipoCambioAnticipo"
			,"EGRMontoARecibirAnticipo"
			,"EGRTipoCambioObservado"
			,"INGInteresesPagoCorteCupon"
			,"TipoFactor"
			,"FechaFactor"
			,"ValorFactor"
			,"MontoCompraFactor"
			,"IdRegimenFondoMutuo"
			,"FechaOrigenCompra"
			,"INGCostoInversionEnFactorAjuste"
			,"INGValorFactorAjusteaFechaCurse"
			,"INGValorFactorMonedaInstrumentoProximo"
			,"INGValorFactorMonedaInstrumentoActual"
		)
		SELECT 
			 p_FechaRespaldo
			,"IdCartera"
			,"FolioCartera"
			,"FolioCarteraRelacionada"
			,"FechaIngresoRegistro"
			,"FechaModificacionRegistro"
			,"IdUsuarioMod"
			,"IdEmpresa"
			,"FechaOperacion"
			,"FechaVencimientoOperacion"
			,"PlazoEnDiasOperacion"
			,"IdCliente"
			,"IdPortafolio"
			,"IdCuentaCustodioPortafolio"
			,"IdClienteRelacionado"
			,"IdPortafolioRelacionado"
			,"IdEstado"
			,"IdTipoProducto"
			,"IdTipoCanalIngreso"
			,"IdMonedaSistema"
			,"IdMonedaAdicional"
			,"IdConcepto"
			,"IdTipoOperacion"
			,"IdOperacion"
			,"IdTipoComprobante"
			,"FolioComprobante"
			,"FechaComprobante"
			,"FolioExternoReferencia"
			,"IdMesa"
			,"IdBolsa"
			,"IdSucursal"
			,"IdAgente"
			,"IdCondicionLiquidacion"
			,"FechaLiquidacion"
			,"UsaGarantiaEspecifica"
			,"CobraPrimaAlInicio"
			,"Glosa"
			,"IdUsuario"
			,"IdModulo"
			,"TipoCarteraCorredor"
			,"ValorFactorAjusteaFechaCurse"
			,"CostoInversionEnFactorAjuste"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"IdEmisor"
			,"NemoSVS"
			,"IdTipoReajuste"
			,"IdMonedaEmision"
			,IFNULL("FechaEmisionInstrumento",'0001-01-01')
			,IFNULL("FechaVencimientoInstrumento",'0001-01-01')
			,"EsFungible"
			,"TieneCorteDeCupon"
			,"CodigoIFRS"
			,"IdBoveda"
			,"IdMonedaInstrumento"
			,"Tamano"
			,"ISIN"
			,"ValorFactorMonedaInstrumento"
			,"Cantidad"
			,"CorteMinimo"
			,"ValorRescate"
			,"PrecioCompra"
			,"TasaCompra"
			,"MontoCompra"
			,"MontoCompraMonedaInstrumento"
			,"MontoCompraMonedaAdicional"
			,"PrecioAPlazo"
			,"MontoAPlazo"
			,"MontoAPlazoMonedaInstrumento"
			,"MontoAPlazoMonedaAdicional"
			,"FechaEfectiva"
			,"IdPlaza"
			,"IdFixing"
			,"FechaFixing"
			,"HoraFixing"
			,"IdModalidadCumplimiento"
			,"IdReferenciaMercado"
			,"IdMonedaPrincipal"
			,"IdMonedaSecundaria"
			,"ValorTipoCambioCierre"
			,"ValorParidadCierre"
			,"ValorTipoCambioCosto"
			,"ValorParidadCosto"
			,"MontoMonedaPrincipal"
			,"MontoMonedaSecundaria"
			,"IdCarga"
			,"INGGlosa"
			,"INGTasaAgente"
			,"INGFechaOperacion"
			,"INGIdCondicionLiquidacion"
			,"INGFechaLiquidacion"
			,"INGCantidad"
			,"INGValorRescate"
			,"INGPrecioPromedioCompra"
			,"INGMontoPrecioPromedioCompra"
			,"INGMontoPrecioPromedioMonedaInstrumento"
			,"INGMontoPrecioPromedioMonedaAdicional"
			,"INGPrecioAPlazo"
			,"INGMontoAPlazo"
			,"INGMontoAPlazoMonedaInstrumento"
			,"INGEsPuntos"
			,"INGValorTipoCambioSpot"
			,"INGValorTipoCambioCotizador"
			,"INGTasaPuntosMonedaPrincipal"
			,"INGTasaPuntosMonedaSecundaria"
			,"INGFechaValorizacionCartera"
			,"INGFechaValorizacionAnterior"
			,"INGFechaValorizacionActual"
			,"INGPrecioCompra"
			,"INGTasaCompra"
			,"INGMontoATasaCompraAnterior"
			,"INGMontoInteresCompraAnterior"
			,"INGMontoCompraAnteriorMonedaInstrumento"
			,"INGMontoInteresCompraAnteriorMonedaInstrumento"
			,"INGMontoReajusteCompraAnteriorMonedaInstrumento"
			,"INGMontoInteresCompraAnteriorMonedaAdicional"
			,"INGMontoATasaCompraActual"
			,"INGMontoCompraActualMonedaInstrumento"
			,"INGMontoCompraActualMonedaAdicional"
			,"INGDurationCompraActual"
			,"INGConvexidadCompraActual"
			,"INGMontoInteresCorridoCompraActual"
			,"INGMontoReajusteCorridoCompraActual"
			,"INGMontoInteresCorridoCompraActualMonedaInstrumento"
			,"INGMontoReajusteCorridoCompraActualMonedaInstrumento"
			,"INGMontoInteresCorridoCompraActualMonedaAdicional"
			,"INGMontoATasaCompraProximo"
			,"INGMontoInteresCompraProximo"
			,"INGMontoReajusteCompraProximo"
			,"INGMontoCompraProximoMonedaInstrumento"
			,"INGMontoInteresCompraProximoMonedaInstrumento"
			,"INGMontoReajusteCompraProximoMonedaInstrumento"
			,"INGMontoInteresCompraProximoMonedaAdicional"
			,"INGPrecioMercadoAnterior"
			,"INGTasaMercadoAnterior"
			,"INGMontoATasaMercadoAnterior"
			,"INGMontoInteresMercadoAnterior"
			,"INGMontoMercadoAnteriorMonedaInstrumento"
			,"INGMontoInteresMercadoAnteriorMonedaInstrumento"
			,"INGMontoInteresMercadoAnteriorMonedaAdicional"
			,"INGFechaPrecioTasaMercadoActual"
			,"INGFuenteTasaPrecioMercadoActual"
			,"INGPrecioMercadoActual"
			,"INGTasaMercadoActual"
			,"INGMontoATasaMercadoActual"
			,"INGMontoMercadoActualMonedaInstrumento"
			,"INGMontoMercadoActualMonedaAdicional"
			,"INGDurationMercadoActual"
			,"INGConvexidadMercadoActual"
			,"INGMontoInteresCorridoMercadoActual"
			,"INGMontoInteresCorridoMercadoActualMonedaInstrumento"
			,"INGMontoInteresCorridoMercadoActualMonedaAdicional"
			,"INGPrecioMercadoProximo"
			,"INGTasaMercadoProximo"
			,"INGMontoATasaMercadoProximo"
			,"INGMontoInteresMercadoProximo"
			,"INGMontoReajusteMercadoProximo"
			,"INGMontoMercadoProximoMonedaInstrumento"
			,"INGMontoInteresMercadoProximoMonedaInstrumento"
			,"INGMontoInteresMercadoProximoMonedaAdicional"
			,"INGTasaCorredora"
			,"INGFechaTasaCorredora"
			,"INGMontoATasaCorredora"
			,"INGMontoATasaCorredoraMonedaInstrumento"
			,"INGFechaCorteCupon"
			,"INGNumeroCupon"
			,"INGIdMonedaPagoCupon"
			,"INGMontoCorteCupon"
			,"INGFechaPagoCorteCupon"
			,"INGMontoCapitalCorteCupon"
			,"INGMontoInteresCorteCupon"
			,"INGMontoReajusteCorteCupon"
			,"INGMontoCorteCuponMonedaInstrumento"
			,"INGMontoCapitalCorteCuponMonedaInstrumento"
			,"INGMontoInteresCorteCuponMonedaInstrumento"
			,"INGMontoReajusteCorteCuponMonedaInstrumento"
			,"INGMontoMonedaPrincipal"
			,"INGMontoMonedaSecundaria"
			,"INGMontoDiferido"
			,"INGMontoUtilidadDiferida"
			,"INGMontoPerdidaDiferida"
			,"INGMontoMargen"
			,"INGMontoValorRazonableActivo"
			,"INGMontoValorRazonablePasivo"
			,"INGMontoValorRazonableNeto"
			,"INGTasaValorRazonableActivo"
			,"INGTasaValorRazonablePasivo"
			,"INGMontoDevengoTipoCambio"
			,"INGMontoDevengoTipoCambioAnterior"
			,"INGMontoDiferidoDiario"
			,"INGMontoDiferidoAcumulado"
			,"INGMontoMatriz"
			,"EGRGlosa"
			,"EGRFechaOperacion"
			,"EGRIdTipoOperacion"
			,"EGRIdOperacion"
			,"EGRIdMovimientoCarteraEnTransito"
			,"EGRIdTipoCanalIngreso"
			,"EGRIdCondicionLiquidacion"
			,"EGRFechaLiquidacion"
			,"EGRIdTipoComprobante"
			,"EGRFolioComprobante"
			,"EGRFechaComprobante"
			,"EGRFolioExternoReferencia"
			,"EGRIdModulo"
			,"EGRIdConcepto"
			,"EGRIdUsuario"
			,"EGRIdSucursal"
			,"EGRIdAgente"
			,"EGRFechaCurseRetiro"
			,"EGRCostoRetiroenFactorAjuste"
			,"EGRValorFactorAjusteaFechaRetiro"
			,"EGRCantidad"
			,"EGRValorRescate"
			,"EGRPrecio"
			,"EGRTasa"
			,"EGRMonto"
			,"EGRMontoMonedaInstrumento"
			,"EGRSpread"
			,"EGRPrecioAPlazo"
			,"EGRMontoAPlazo"
			,"EGRMontoAPlazoMonedaInstrumento"
			,"EGRIdSpotAsociado"
			,"EGRMontoMonedaPrincipal"
			,"EGRMontoMonedaSecundaria"
			,"EGRIdMonedaLiquidacion"
			,"EGRMontoLiquidacion"
			,"EGRMontoLiquidacionMonedaPrincipal"
			,"EGRValorParidad"
			,"EGRValorTipoCambioCotizador"
			,"EGRTasaDescuentoAnticipo"
			,"EGRTipoCambioAnticipo"
			,"EGRMontoARecibirAnticipo"
			,"EGRTipoCambioObservado"
			,"INGInteresesPagoCorteCupon"
			,IFNULL("TipoFactor",'')
			,IFNULL("FechaFactor",'0001-01-01')
			,IFNULL("ValorFactor",0)
			,IFNULL("MontoCompraFactor",0)
			,"IdRegimenFondoMutuo"
			,"FechaOrigenCompra"
			,"INGCostoInversionEnFactorAjuste"
			,"INGValorFactorAjusteaFechaCurse"
			,"INGValorFactorMonedaInstrumentoProximo"
			,"INGValorFactorMonedaInstrumentoActual"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE
			COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
			

			AND ("IdEstado" = 'VIG' 
		
				OR EXISTS (SELECT 1 FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
						WHERE 
						GAR."IdEstado" = 'VIG'  
						AND CAR."FolioCartera" = GAR."FolioCarteraOrigen"
						AND  CAR."IdPortafolio" = GAR."IdPortafolio" 
						AND GAR."IdClasificacionTipoInstrumento" = CAR."IdClasificacionTipoInstrumento"
						AND GAR."IdEmpresa" = 1
						
					)
					
				OR EXISTS (
					SELECT  1 FROM "BSCL.Custodia.BD::CUS.Subyacente" AS S
					WHERE  S."IdEstado" = 'VIG'
					AND S."IdClasificacionTipoInstrumento" = CAR."IdClasificacionTipoInstrumento"
					AND S."FolioCarteraRelacionada" = CAR."FolioCartera"
					)
					
				OR EXISTS (
					SELECT  1 FROM "BSCL.Custodia.BD::CUS.Reserva" AS R
					WHERE  R."IdEstado" = 'VIG'
					AND R."IdClasificacionTipoInstrumento" = CAR."IdClasificacionTipoInstrumento"
					AND  CAR."FolioCartera" = R."FolioCarteraOrigen"
					)
					
					
				
		)
			AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
			AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
			AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
			AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0);

		
	
			
			
			
		SELECT ::ROWCOUNT into v_countCarteras FROM DUMMY;
		
		INSERT INTO "BSCL.Custodia.BD::HIS.Subyacente" (
			"FechaRespaldo", "IdSubyacente", "FolioSubyacente", "FolioCarteraRelacionada", "FechaIngresoRegistro", "FechaModificacionRegistro", "IdCarteraRetirada", "IdMovimientoCarteraEnTransito", "PrecioCompraOperacionRelacionada", 
			"TasaCompraOperacionRelacionada", "PrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada", "MontoPrecioPromedioMonedaAdicionalOperacionRelacionada", 
			"IdEstado", "IdTipoCanalIngreso", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "IdMonedaEmision", "FechaEmisionInstrumento", 
			"FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", "IdMonedaInstrumento", "Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", 
			"Cantidad", "CorteMinimo", "ValorRescate", "PrecioSubyacente", "TasaSubyacente", "MontoSubyacente", "MontoSubyacenteMonedaInstrumento", "MontoSubyacenteMonedaAdicional", "INGFechaOperacion", "INGCantidad", "INGValorRescate", 
			"INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionSubyacente", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", "INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", 
			"INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", "INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", 
			"INGValorFactorMonedaAdicionalActual", "INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", "INGPrecioSubyacente", "INGTasaSubyacente", "INGMontoATasaSubyacenteAnterior", "INGMontoInteresSubyacenteAnterior", "INGMontoReajusteSubyacenteAnterior", 
			"INGMontoSubyacenteAnteriorMonedaInstrumento", "INGMontoInteresSubyacenteAnteriorMonedaInstrumento", "INGMontoReajusteSubyacenteAnteriorMonedaInstrumento", "INGMontoSubyacenteAnteriorMonedaAdicional", "INGMontoInteresSubyacenteAnteriorMonedaAdicional", 
			"INGMontoReajusteSubyacenteAnteriorMonedaAdicional", "INGDurationSubyacenteAnterior", "INGConvexidadSubyacenteAnterior", "INGMontoSubyacenteAnteriorPibMas", "INGMontoSubyacenteAnteriorPibMasMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMasMonedaAdicional", 
			"INGMontoSubyacenteAnteriorPibMenos", "INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMenosMonedaAdicional", "INGMontoATasaSubyacenteActual", "INGMontoSubyacenteActualMonedaInstrumento", "INGMontoSubyacenteActualMonedaAdicional", 
			"INGDurationSubyacenteActual", "INGConvexidadSubyacenteActual", "INGMontoInteresCorridoSubyacenteActual", "INGMontoReajusteCorridoSubyacenteActual", "INGMontoInteresCorridoSubyacenteActualMonedaInstrumento", "INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento", 
			"INGMontoInteresCorridoSubyacenteActualMonedaAdicional", "INGMontoReajusteCorridoSubyacenteActualMonedaAdicional", "INGMontoSubyacenteActualPibMas", "INGMontoSubyacenteActualPibMasMonedaInstrumento", "INGMontoSubyacenteActualPibMasMonedaAdicional", "INGMontoSubyacenteActualPibMenos", 
			"INGMontoSubyacenteActualPibMenosMonedaInstrumento", "INGMontoSubyacenteActualPibMenosMonedaAdicional", "INGMontoATasaSubyacenteProximo", "INGMontoInteresSubyacenteProximo", "INGMontoReajusteSubyacenteProximo", "INGMontoSubyacenteProximoMonedaInstrumento", "INGMontoInteresSubyacenteProximoMonedaInstrumento", 
			"INGMontoReajusteSubyacenteProximoMonedaInstrumento", "INGMontoSubyacenteProximoMonedaAdicional", "INGMontoInteresSubyacenteProximoMonedaAdicional", "INGMontoReajusteSubyacenteProximoMonedaAdicional", "INGDurationSubyacenteProximo", "INGConvexidadSubyacenteProximo", "INGFechaPrecioTasaMercadoAnterior", 
			"INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", "INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", "INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", "INGMontoInteresMercadoAnteriorMonedaInstrumento", 
			"INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", "INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", 
			"INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", "INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", 
			"INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", "INGDurationMercadoActual", "INGConvexidadMercadoActual", "INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", 
			"INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", "INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", 
			"INGMontoMercadoActualPibMasMonedaAdicional", "INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", "INGTasaMercadoProximo", 
			"INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", "INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", 
			"INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", "INGConvexidadMercadoProximo", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", "INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", "INGMontoReajusteCompraAnteriorMonedaInstrumento", 
			"INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", "INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", "INGMontoCompraAnteriorPibMasMonedaAdicional", 
			"INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", "INGMontoCompraAnteriorPibMenosMonedaAdicional", "INGMontoATasaCompraActual", "INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", 
			"INGMontoReajusteCorridoCompraActual", "INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", "INGMontoReajusteCorridoCompraActualMonedaAdicional", "INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", 
			"INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", "INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", "INGMontoInteresCompraProximoMonedaInstrumento", 
			"INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", "INGConvexidadCompraProximo",
			"INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", "INGIdMonedaPagoCupon", 			
			"INGMontoCorteCupon", "INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", "INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", 
			"INGMontoInteresCorteCuponMonedaAdicional", "INGMontoReajusteCorteCuponMonedaAdicional", "INGMontoPresenteTIF", "INGMontoInteresTIF", "INGMontoReajusteTIF", "INGMontoInteresAcumTIF", "INGMontoReajusteAcumTIF", "INGMontoPresenteTIFMonedaInstrumento", "INGMontoInteresTIFMonedaInstrumento", "INGMontoReajusteTIFMonedaInstrumento", "INGMontoInteresAcumTIFMonedaInstrumento", "INGMontoReajusteAcumTIFMonedaInstrumento", 
			"INGMontoPresenteTIFMonedaAdicional", "INGMontoInteresTIFMonedaAdicional", "INGMontoReajusteTIFMonedaAdicional", "INGMontoInteresAcumTIFMonedaAdicional", "INGMontoReajusteAcumTIFMonedaAdicional", "EGRTicketValorizacion", "EGRValorizacionConProblemas", "EGRFechaFactorMonedaInstrumento", "EGRValorFactorMonedaInstrumento", "EGRFechaFactorMonedaAdicional", "EGRValorFactorMonedaAdicional", "EGRFechaOperacion", 
			"EGRIdTipoCanalIngreso", "EGRIdCondicionLiquidacion", "EGRFechaLiquidacion", "EGRIdTipoComprobante", "EGRFolioComprobante", "EGRFechaComprobante", "EGRFolioExternoReferencia", "EGRIdModulo", "EGRIdTipoOperacion", "EGRIdOperacion", "EGRIdConcepto", "EGRIdUsuario", "EGRIdSucursal", "EGRIdAgente", "EGRCantidad", "EGRValorRescate", "EGRPrecio", "EGRTasa", "EGRMonto", "EGRMontoMonedaInstrumento", "EGRMontoMonedaAdicional", 
			"EGRSpread", "EGRFechaPrecioTasaMercado", "EGRFuenteTasaPrecioMercado", "EGRPrecioMercado", "EGRTasaMercado", "EGRMontoATasaMercado", "EGRMontoMercadoMonedaInstrumento", "EGRMontoMercadoMonedaAdicional", "EGRSpreadMercado", "EGRMontoPresenteTIF", "EGRMontoPresenteTIFMonedaInstrumento", "EGRMontoPresenteTIFMonedaAdicional","EGRValorPresente")
		SELECT p_FechaRespaldo, "IdSubyacente", "FolioSubyacente", "FolioCarteraRelacionada", "FechaIngresoRegistro", "FechaModificacionRegistro", "IdCarteraRetirada", "IdMovimientoCarteraEnTransito", "PrecioCompraOperacionRelacionada", 
			"TasaCompraOperacionRelacionada", "PrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada", "MontoPrecioPromedioMonedaAdicionalOperacionRelacionada", 
			"IdEstado", "IdTipoCanalIngreso", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "IdMonedaEmision", "FechaEmisionInstrumento", 
			"FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", "IdMonedaInstrumento", "Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", 
			"Cantidad", "CorteMinimo", "ValorRescate", "PrecioSubyacente", "TasaSubyacente", "MontoSubyacente", "MontoSubyacenteMonedaInstrumento", "MontoSubyacenteMonedaAdicional", "INGFechaOperacion", "INGCantidad", "INGValorRescate", 
			"INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionSubyacente", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", "INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", 
			"INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", "INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", 
			"INGValorFactorMonedaAdicionalActual", "INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", "INGPrecioSubyacente", "INGTasaSubyacente", "INGMontoATasaSubyacenteAnterior", "INGMontoInteresSubyacenteAnterior", "INGMontoReajusteSubyacenteAnterior", 
			"INGMontoSubyacenteAnteriorMonedaInstrumento", "INGMontoInteresSubyacenteAnteriorMonedaInstrumento", "INGMontoReajusteSubyacenteAnteriorMonedaInstrumento", "INGMontoSubyacenteAnteriorMonedaAdicional", "INGMontoInteresSubyacenteAnteriorMonedaAdicional", 
			"INGMontoReajusteSubyacenteAnteriorMonedaAdicional", "INGDurationSubyacenteAnterior", "INGConvexidadSubyacenteAnterior", "INGMontoSubyacenteAnteriorPibMas", "INGMontoSubyacenteAnteriorPibMasMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMasMonedaAdicional", 
			"INGMontoSubyacenteAnteriorPibMenos", "INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMenosMonedaAdicional", "INGMontoATasaSubyacenteActual", "INGMontoSubyacenteActualMonedaInstrumento", "INGMontoSubyacenteActualMonedaAdicional", 
			"INGDurationSubyacenteActual", "INGConvexidadSubyacenteActual", "INGMontoInteresCorridoSubyacenteActual", "INGMontoReajusteCorridoSubyacenteActual", "INGMontoInteresCorridoSubyacenteActualMonedaInstrumento", "INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento", 
			"INGMontoInteresCorridoSubyacenteActualMonedaAdicional", "INGMontoReajusteCorridoSubyacenteActualMonedaAdicional", "INGMontoSubyacenteActualPibMas", "INGMontoSubyacenteActualPibMasMonedaInstrumento", "INGMontoSubyacenteActualPibMasMonedaAdicional", "INGMontoSubyacenteActualPibMenos", 
			"INGMontoSubyacenteActualPibMenosMonedaInstrumento", "INGMontoSubyacenteActualPibMenosMonedaAdicional", "INGMontoATasaSubyacenteProximo", "INGMontoInteresSubyacenteProximo", "INGMontoReajusteSubyacenteProximo", "INGMontoSubyacenteProximoMonedaInstrumento", "INGMontoInteresSubyacenteProximoMonedaInstrumento", 
			"INGMontoReajusteSubyacenteProximoMonedaInstrumento", "INGMontoSubyacenteProximoMonedaAdicional", "INGMontoInteresSubyacenteProximoMonedaAdicional", "INGMontoReajusteSubyacenteProximoMonedaAdicional", "INGDurationSubyacenteProximo", "INGConvexidadSubyacenteProximo", "INGFechaPrecioTasaMercadoAnterior", 
			"INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", "INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", "INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", "INGMontoInteresMercadoAnteriorMonedaInstrumento", 
			"INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", "INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", 
			"INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", "INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", 
			"INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", "INGDurationMercadoActual", "INGConvexidadMercadoActual", "INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", 
			"INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", "INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", 
			"INGMontoMercadoActualPibMasMonedaAdicional", "INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", "INGTasaMercadoProximo", 
			"INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", "INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", 
			"INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", "INGConvexidadMercadoProximo", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", "INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", "INGMontoReajusteCompraAnteriorMonedaInstrumento", 
			"INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", "INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", "INGMontoCompraAnteriorPibMasMonedaAdicional", 
			"INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", "INGMontoCompraAnteriorPibMenosMonedaAdicional", "INGMontoATasaCompraActual", "INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", 
			"INGMontoReajusteCorridoCompraActual", "INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", "INGMontoReajusteCorridoCompraActualMonedaAdicional", "INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", 
			"INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", "INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", "INGMontoInteresCompraProximoMonedaInstrumento", 
			"INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", "INGConvexidadCompraProximo", 
			"INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", "INGIdMonedaPagoCupon", 
			"INGMontoCorteCupon", "INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", "INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", 
			"INGMontoInteresCorteCuponMonedaAdicional", "INGMontoReajusteCorteCuponMonedaAdicional", "INGMontoPresenteTIF", "INGMontoInteresTIF", "INGMontoReajusteTIF", "INGMontoInteresAcumTIF", "INGMontoReajusteAcumTIF", "INGMontoPresenteTIFMonedaInstrumento", "INGMontoInteresTIFMonedaInstrumento", "INGMontoReajusteTIFMonedaInstrumento", "INGMontoInteresAcumTIFMonedaInstrumento", "INGMontoReajusteAcumTIFMonedaInstrumento", 
			"INGMontoPresenteTIFMonedaAdicional", "INGMontoInteresTIFMonedaAdicional", "INGMontoReajusteTIFMonedaAdicional", "INGMontoInteresAcumTIFMonedaAdicional", "INGMontoReajusteAcumTIFMonedaAdicional", "EGRTicketValorizacion", "EGRValorizacionConProblemas", "EGRFechaFactorMonedaInstrumento", "EGRValorFactorMonedaInstrumento", "EGRFechaFactorMonedaAdicional", "EGRValorFactorMonedaAdicional", "EGRFechaOperacion", 
			"EGRIdTipoCanalIngreso", "EGRIdCondicionLiquidacion", "EGRFechaLiquidacion", "EGRIdTipoComprobante", "EGRFolioComprobante", "EGRFechaComprobante", "EGRFolioExternoReferencia", "EGRIdModulo", "EGRIdTipoOperacion", "EGRIdOperacion", "EGRIdConcepto", "EGRIdUsuario", "EGRIdSucursal", "EGRIdAgente", "EGRCantidad", "EGRValorRescate", "EGRPrecio", "EGRTasa", "EGRMonto", "EGRMontoMonedaInstrumento", "EGRMontoMonedaAdicional", 
			"EGRSpread", "EGRFechaPrecioTasaMercado", "EGRFuenteTasaPrecioMercado", "EGRPrecioMercado", "EGRTasaMercado", "EGRMontoATasaMercado", "EGRMontoMercadoMonedaInstrumento", "EGRMontoMercadoMonedaAdicional", "EGRSpreadMercado", "EGRMontoPresenteTIF", "EGRMontoPresenteTIFMonedaInstrumento", "EGRMontoPresenteTIFMonedaAdicional","EGRValorPresente"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" AS S
		WHERE "IdEstado" = 'VIG' 
		AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
		AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
		AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
		
		AND  EXISTS (
					SELECT * FROM  "BSCL.Custodia.BD::CUS.Cartera" as CUS
 					WHERE S."FolioCarteraRelacionada" = CUS."FolioCartera"
 					AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN (CUS."IdClasificacionTipoInstrumento", '')
 					AND COALESCE( p_TipoInstrumento, '') IN (CUS."IdTipoInstrumento", '')
 					AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
					AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
					AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
		)	;
		
		
		
		
		
		SELECT ::ROWCOUNT into v_countSubyacentes FROM DUMMY;
		
		
		/**** reservas, tambien siempre de la mano con las carteras *****/
		DELETE FROM "BSCL.Custodia.BD::HIS.Reserva" AS R
		WHERE "FechaRespaldo" = p_FechaRespaldo 
			AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
			AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
			AND NOT EXISTS (
						SELECT * FROM  "BSCL.Custodia.BD::HIS.Cartera" as CUS
	 					WHERE R."FolioCarteraOrigen" = CUS."FolioCartera"
	 					AND CUS."FechaRespaldo" = p_FechaRespaldo  
	 					AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN (CUS."IdClasificacionTipoInstrumento", '')
	 					AND COALESCE( p_TipoInstrumento, '') IN (CUS."IdTipoInstrumento", '')
			)	
		;
		
		
		
		
		
		INSERT INTO "BSCL.Custodia.BD::HIS.Reserva" (
			"FechaRespaldo", "IdReserva", "IdOperacion", "FolioCarteraOrigen", "FolioCarteraCustodia", "FolioOperacion", "IdEstado", "IdUsuarioCreacion", "IdTipoProducto", 
			"IdConcepto", "Tasa", "TasaMercado", "Precio", "PrecioMercado", "MontoValorizado", "IdClasificacionTipoInstrumento", "IdInstrumento", "IdTipoInstrumento", 
			"IdTipoReajuste", "IdEmisor", "FechaVencimiento", "EsFungible", "IdBolsa", "IdBoveda", "CodigoIFRS", "CorteMinimo", "CantidadInicial", "ValorRescateInicial", 
			"MontoValorizadoReserva", "FechaVencimientoReserva", "FechaVencimientoProducto", "FechaCreacion", "IngIdUsuario", "IngCantidad", "IngValorRescate", 
			"IngMontoValorizado", "IngFecha", "EgrIdUsuario", "EgrCantidad", "EgrValorRescate", "EgrMontoValorizado", "EgrFecha", "SecuenciaCompromisoMigracion", "IdCarga"
			,"IdCarteraCustodia","IdMonedaLiquidacionInstrumento","IdMonedaEmisionInstrumento","IdCarteraOrigen","IdMoneda","MontoLiquidacionOperacion","MontoLiquidacion"
			,"IdMonedaLiquidacionOperacion","Nemotecnico","FolioSubyacente")
		SELECT p_FechaRespaldo, "IdReserva", "IdOperacion", "FolioCarteraOrigen", "FolioCarteraCustodia", "FolioOperacion", "IdEstado", "IdUsuarioCreacion", "IdTipoProducto", 
			"IdConcepto", "Tasa", "TasaMercado", "Precio", "PrecioMercado", "MontoValorizado", "IdClasificacionTipoInstrumento", "IdInstrumento", "IdTipoInstrumento", 
			"IdTipoReajuste", "IdEmisor", "FechaVencimiento", "EsFungible", "IdBolsa", "IdBoveda", "CodigoIFRS", "CorteMinimo", "CantidadInicial", "ValorRescateInicial", 
			"MontoValorizadoReserva", "FechaVencimientoReserva", "FechaVencimientoProducto", "FechaCreacion", "IngIdUsuario", "IngCantidad", "IngValorRescate", 
			"IngMontoValorizado", "IngFecha", "EgrIdUsuario", "EgrCantidad", "EgrValorRescate", "EgrMontoValorizado", "EgrFecha", "SecuenciaCompromisoMigracion", "IdCarga"
			,"IdCarteraCustodia","IdMonedaLiquidacionInstrumento","IdMonedaEmisionInstrumento","IdCarteraOrigen","IdMoneda","MontoLiquidacionOperacion","MontoLiquidacion"
			,"IdMonedaLiquidacionOperacion","Nemotecnico","FolioSubyacente"
		FROM "BSCL.Custodia.BD::CUS.Reserva" AS R
		WHERE "IdEstado" = 'VIG' 
		AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
		AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN (R."IdClasificacionTipoInstrumento", '')
		AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
		AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
			AND  EXISTS (
					SELECT * FROM  "BSCL.Custodia.BD::CUS.Cartera" as CUS
 					WHERE CUS."FolioCartera" = R."FolioCarteraOrigen"
 					AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN (CUS."IdClasificacionTipoInstrumento", '')
 					AND COALESCE( p_TipoInstrumento, '') IN (CUS."IdTipoInstrumento", '')
 					AND  COALESCE( p_IdClient, 0) IN (CUS."IdCliente", 0) 
					AND  COALESCE( p_IdPortafolio, 0) IN (CUS."IdPortafolio", 0)
		)	;
		
		
			 
		
		SELECT ::ROWCOUNT into v_countReservas FROM DUMMY;
		
		
	END IF;  
	/*** fin carteras, subyacentes y reservas ****/
	
	
	IF p_RespaldaGarantias = 'S' THEN
		DELETE FROM "BSCL.Custodia.BD::HIS.Garantia" WHERE "FechaRespaldo" = p_FechaRespaldo 
		AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
			AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
			AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
			AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
			AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
			;

		
		
		INSERT INTO "BSCL.Custodia.BD::HIS.Garantia" (
			"FechaRespaldo", "IdGarantia", "FolioGarantia", "FolioCarteraOrigen", "IdCarteraOrigen", "FolioCarteraGarantizada", "IdInstrumentoCarteraGarantizada", "EsEspecifica", "IdContraparteGarantia", "RazonSocialContraparteGarantia", "CuentaContraparteGarantia", "IdOrigenGarantia", "IdEspecieGarantia", "FechaIngresoRegistro", "FechaModificacionRegistro", 
			"PrecioCompraOperacionOrigen", "TasaCompraOperacionOrigen", "PrecioPromedioCompraOperacionOrigen", "MontoPrecioPromedioCompraOperacionOrigen", "MontoPrecioPromedioMonedaInstrumentoOperacionOrigen", "MontoPrecioPromedioMonedaAdicionalOperacionOrigen", "IdEmpresa", "FechaOperacion", "FechaVencimientoOperacion", "PlazoEnDiasOperacion", "IdCliente", "IdPortafolio", 
			"IdCuentaCustodioPortafolio", "IdEstado", "IdTipoCanalIngreso", "IdMonedaSistema", "IdMonedaAdicional", "FechaFactorMonedaAdicional", "ValorFactorMonedaAdicional", "IdConcepto", "IdTipoComprobante", "FolioComprobante", "FechaComprobante", "FolioExternoReferencia", "IdSucursal", "IdAgente", "IdCondicionLiquidacion", "FechaLiquidacion", "IdUsuario", "IdModulo", 
			"IdMovimientoCarteraEnTransito", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "IdMonedaEmision", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", "CodigoIFRS", "IdMonedaInstrumento", 
			"Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", "Cantidad", "CorteMinimo", "ValorRescate", "PrecioGarantia", "TasaGarantia", "MontoGarantia", "MontoGarantiaMonedaInstrumento", "MontoGarantiaMonedaAdicional", "IdCarga", "IdSecuenciaCarteraMigracion", "CodigoPeriodo", "INGFechaOperacion", "INGIdCondicionLiquidacion", "INGFechaLiquidacion", 
			"INGCantidad", "INGValorRescate", "INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionGarantia", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", "INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", "INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", 
			"INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", "INGValorFactorMonedaAdicionalActual", "INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", "INGPrecioGarantia", "INGTasaGarantia", "INGMontoATasaGarantiaAnterior", "INGMontoInteresGarantiaAnterior", "INGMontoReajusteGarantiaAnterior", 
			"INGMontoGarantiaAnteriorMonedaInstrumento", "INGMontoInteresGarantiaAnteriorMonedaInstrumento", "INGMontoReajusteGarantiaAnteriorMonedaInstrumento", "INGMontoGarantiaAnteriorMonedaAdicional", "INGMontoInteresGarantiaAnteriorMonedaAdicional", "INGMontoReajusteGarantiaAnteriorMonedaAdicional", "INGDurationGarantiaAnterior", "INGConvexidadGarantiaAnterior", "INGMontoGarantiaAnteriorPibMas", 
			"INGMontoGarantiaAnteriorPibMasMonedaInstrumento", "INGMontoGarantiaAnteriorPibMasMonedaAdicional", "INGMontoGarantiaAnteriorPibMenos", "INGMontoGarantiaAnteriorPibMenosMonedaInstrumento", "INGMontoGarantiaAnteriorPibMenosMonedaAdicional", "INGMontoATasaGarantiaActual", "INGMontoGarantiaActualMonedaInstrumento", "INGMontoGarantiaActualMonedaAdicional", "INGDurationGarantiaActual", "INGConvexidadGarantiaActual", 
			"INGMontoInteresCorridoGarantiaActual", "INGMontoReajusteCorridoGarantiaActual", "INGMontoInteresCorridoGarantiaActualMonedaInstrumento", "INGMontoReajusteCorridoGarantiaActualMonedaInstrumento", "INGMontoInteresCorridoGarantiaActualMonedaAdicional", "INGMontoReajusteCorridoGarantiaActualMonedaAdicional", "INGMontoGarantiaActualPibMas", "INGMontoGarantiaActualPibMasMonedaInstrumento", "INGMontoGarantiaActualPibMasMonedaAdicional", 
			"INGMontoGarantiaActualPibMenos", "INGMontoGarantiaActualPibMenosMonedaInstrumento", "INGMontoGarantiaActualPibMenosMonedaAdicional", "INGMontoATasaGarantiaProximo", "INGMontoInteresGarantiaProximo", "INGMontoReajusteGarantiaProximo", "INGMontoGarantiaProximoMonedaInstrumento", "INGMontoInteresGarantiaProximoMonedaInstrumento", "INGMontoReajusteGarantiaProximoMonedaInstrumento", "INGMontoGarantiaProximoMonedaAdicional", 
			"INGMontoInteresGarantiaProximoMonedaAdicional", "INGMontoReajusteGarantiaProximoMonedaAdicional", "INGDurationGarantiaProximo", "INGConvexidadGarantiaProximo", "INGFechaPrecioTasaMercadoAnterior", "INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", "INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", "INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", 
			"INGMontoInteresMercadoAnteriorMonedaInstrumento", "INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", "INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", "INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", 
			"INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", "INGDurationMercadoActual", "INGConvexidadMercadoActual", 
			"INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", "INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", "INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", "INGMontoMercadoActualPibMasMonedaAdicional", 
			"INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", "INGTasaMercadoProximo", "INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", 
			"INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", "INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", "INGConvexidadMercadoProximo", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", "INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", 
			"INGMontoReajusteCompraAnteriorMonedaInstrumento", "INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", "INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", "INGMontoCompraAnteriorPibMasMonedaAdicional", "INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", 
			"INGMontoCompraAnteriorPibMenosMonedaAdicional", "INGMontoATasaCompraActual", "INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", "INGMontoReajusteCorridoCompraActual", "INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", 
			"INGMontoReajusteCorridoCompraActualMonedaAdicional", "INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", "INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", "INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", 
			"INGMontoInteresCompraProximoMonedaInstrumento", "INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", "INGConvexidadCompraProximo",
			"INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", "INGIdMonedaPagoCupon", "INGMontoCorteCupon", 
			"INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", "INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", "INGMontoInteresCorteCuponMonedaAdicional", "INGMontoReajusteCorteCuponMonedaAdicional", "EGRTicketValorizacion", 
			"EGRValorizacionConProblemas", "EGRFechaFactorMonedaInstrumento", "EGRValorFactorMonedaInstrumento", "EGRFechaFactorMonedaAdicional", "EGRValorFactorMonedaAdicional", "EGRFechaOperacion", "EGRIdTipoOperacion", "EGRIdOperacion", "EGRIdMovimientoCarteraEnTransito", "EGRIdTipoCanalIngreso", "EGRIdCondicionLiquidacion", "EGRFechaLiquidacion", "EGRIdTipoComprobante", "EGRFolioComprobante", "EGRFechaComprobante", "EGRFolioExternoReferencia", "EGRIdModulo", "EGRIdConcepto", "EGRIdUsuario", 
			"EGRIdSucursal", "EGRIdAgente", "EGRCantidad", "EGRValorRescate", "EGRPrecio", "EGRTasa", "EGRMonto", "EGRMontoMonedaInstrumento", "EGRMontoMonedaAdicional")
		SELECT p_FechaRespaldo, "IdGarantia", "FolioGarantia", "FolioCarteraOrigen", "IdCarteraOrigen", "FolioCarteraGarantizada", "IdInstrumentoCarteraGarantizada", "EsEspecifica", "IdContraparteGarantia", "RazonSocialContraparteGarantia", "CuentaContraparteGarantia", "IdOrigenGarantia", "IdEspecieGarantia", "FechaIngresoRegistro", "FechaModificacionRegistro", 
			"PrecioCompraOperacionOrigen", "TasaCompraOperacionOrigen", "PrecioPromedioCompraOperacionOrigen", "MontoPrecioPromedioCompraOperacionOrigen", "MontoPrecioPromedioMonedaInstrumentoOperacionOrigen", "MontoPrecioPromedioMonedaAdicionalOperacionOrigen", "IdEmpresa", "FechaOperacion", "FechaVencimientoOperacion", "PlazoEnDiasOperacion", "IdCliente", "IdPortafolio", 
			"IdCuentaCustodioPortafolio", "IdEstado", "IdTipoCanalIngreso", "IdMonedaSistema", "IdMonedaAdicional", "FechaFactorMonedaAdicional", "ValorFactorMonedaAdicional", "IdConcepto", "IdTipoComprobante", "FolioComprobante", "FechaComprobante", "FolioExternoReferencia", "IdSucursal", "IdAgente", "IdCondicionLiquidacion", "FechaLiquidacion", "IdUsuario", "IdModulo", 
			"IdMovimientoCarteraEnTransito", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "IdMonedaEmision", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", "CodigoIFRS", "IdMonedaInstrumento", 
			"Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", "Cantidad", "CorteMinimo", "ValorRescate", "PrecioGarantia", "TasaGarantia", "MontoGarantia", "MontoGarantiaMonedaInstrumento", "MontoGarantiaMonedaAdicional", "IdCarga", "IdSecuenciaCarteraMigracion", "CodigoPeriodo", "INGFechaOperacion", "INGIdCondicionLiquidacion", "INGFechaLiquidacion", 
			"INGCantidad", "INGValorRescate", "INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionGarantia", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", "INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", "INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", 
			"INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", "INGValorFactorMonedaAdicionalActual", "INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", "INGPrecioGarantia", "INGTasaGarantia", "INGMontoATasaGarantiaAnterior", "INGMontoInteresGarantiaAnterior", "INGMontoReajusteGarantiaAnterior", 
			"INGMontoGarantiaAnteriorMonedaInstrumento", "INGMontoInteresGarantiaAnteriorMonedaInstrumento", "INGMontoReajusteGarantiaAnteriorMonedaInstrumento", "INGMontoGarantiaAnteriorMonedaAdicional", "INGMontoInteresGarantiaAnteriorMonedaAdicional", "INGMontoReajusteGarantiaAnteriorMonedaAdicional", "INGDurationGarantiaAnterior", "INGConvexidadGarantiaAnterior", "INGMontoGarantiaAnteriorPibMas", 
			"INGMontoGarantiaAnteriorPibMasMonedaInstrumento", "INGMontoGarantiaAnteriorPibMasMonedaAdicional", "INGMontoGarantiaAnteriorPibMenos", "INGMontoGarantiaAnteriorPibMenosMonedaInstrumento", "INGMontoGarantiaAnteriorPibMenosMonedaAdicional", "INGMontoATasaGarantiaActual", "INGMontoGarantiaActualMonedaInstrumento", "INGMontoGarantiaActualMonedaAdicional", "INGDurationGarantiaActual", "INGConvexidadGarantiaActual", 
			"INGMontoInteresCorridoGarantiaActual", "INGMontoReajusteCorridoGarantiaActual", "INGMontoInteresCorridoGarantiaActualMonedaInstrumento", "INGMontoReajusteCorridoGarantiaActualMonedaInstrumento", "INGMontoInteresCorridoGarantiaActualMonedaAdicional", "INGMontoReajusteCorridoGarantiaActualMonedaAdicional", "INGMontoGarantiaActualPibMas", "INGMontoGarantiaActualPibMasMonedaInstrumento", "INGMontoGarantiaActualPibMasMonedaAdicional", 
			"INGMontoGarantiaActualPibMenos", "INGMontoGarantiaActualPibMenosMonedaInstrumento", "INGMontoGarantiaActualPibMenosMonedaAdicional", "INGMontoATasaGarantiaProximo", "INGMontoInteresGarantiaProximo", "INGMontoReajusteGarantiaProximo", "INGMontoGarantiaProximoMonedaInstrumento", "INGMontoInteresGarantiaProximoMonedaInstrumento", "INGMontoReajusteGarantiaProximoMonedaInstrumento", "INGMontoGarantiaProximoMonedaAdicional", 
			"INGMontoInteresGarantiaProximoMonedaAdicional", "INGMontoReajusteGarantiaProximoMonedaAdicional", "INGDurationGarantiaProximo", "INGConvexidadGarantiaProximo", "INGFechaPrecioTasaMercadoAnterior", "INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", "INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", "INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", 
			"INGMontoInteresMercadoAnteriorMonedaInstrumento", "INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", "INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", "INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", 
			"INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", "INGDurationMercadoActual", "INGConvexidadMercadoActual", 
			"INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", "INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", "INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", "INGMontoMercadoActualPibMasMonedaAdicional", 
			"INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", "INGTasaMercadoProximo", "INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", 
			"INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", "INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", "INGConvexidadMercadoProximo", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", "INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", 
			"INGMontoReajusteCompraAnteriorMonedaInstrumento", "INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", "INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", "INGMontoCompraAnteriorPibMasMonedaAdicional", "INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", 
			"INGMontoCompraAnteriorPibMenosMonedaAdicional", "INGMontoATasaCompraActual", "INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", "INGMontoReajusteCorridoCompraActual", "INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", 
			"INGMontoReajusteCorridoCompraActualMonedaAdicional", "INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", "INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", "INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", 
			"INGMontoInteresCompraProximoMonedaInstrumento", "INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", "INGConvexidadCompraProximo",
			"INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", "INGIdMonedaPagoCupon", "INGMontoCorteCupon", 
			"INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", "INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", "INGMontoInteresCorteCuponMonedaAdicional", "INGMontoReajusteCorteCuponMonedaAdicional", "EGRTicketValorizacion", 
			"EGRValorizacionConProblemas", "EGRFechaFactorMonedaInstrumento", "EGRValorFactorMonedaInstrumento", "EGRFechaFactorMonedaAdicional", "EGRValorFactorMonedaAdicional", "EGRFechaOperacion", "EGRIdTipoOperacion", "EGRIdOperacion", "EGRIdMovimientoCarteraEnTransito", "EGRIdTipoCanalIngreso", "EGRIdCondicionLiquidacion", "EGRFechaLiquidacion", "EGRIdTipoComprobante", "EGRFolioComprobante", "EGRFechaComprobante", "EGRFolioExternoReferencia", "EGRIdModulo", "EGRIdConcepto", "EGRIdUsuario", 
			"EGRIdSucursal", "EGRIdAgente", "EGRCantidad", "EGRValorRescate", "EGRPrecio", "EGRTasa", "EGRMonto", "EGRMontoMonedaInstrumento", "EGRMontoMonedaAdicional"
		FROM "BSCL.Custodia.BD::GTA.Garantia"
		WHERE "IdEstado" = 'VIG' 
			AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
			AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
			AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
			AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
			AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
			;

		SELECT ::ROWCOUNT into v_countGarantias FROM DUMMY;
	END IF;

	
	

	DELETE FROM  "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" WHERE "FechaRespaldo" = p_FechaRespaldo 
	AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
		AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
		AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
		AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
		AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
		AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
		;

		
			
	
	INSERT INTO "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito"
		( "FechaRespaldo", "IdMovimientoCarteraEnTransito", 
		"IdMovimientoCarteraEnTransitoRelacionado", "FechaIngresoRegistro", "FechaCertificacionRegistro", 
		"FechaAnulacionRegistro", "IdUsuarioIngreso", "IdUsuarioCertificacion", "IdUsuarioAnulacion", 
		"FechaOperacion", "FechaVencimientoOperacion", "PlazoEnDiasOperacion", "FolioCustodia", "IdTipoComprobante", 
		"FolioComprobante", "EsRegistroGeneradoPorSistema", "MueveCustodia", "PermiteCertificacionPorUsuario", 
		"OrigenCustodia", "IdCustodiaOrigen", "IdTipoOperacionOrigen", "FolioCarteraGarantizada", "IdInstrumentoCarteraGarantizada",
		"EsEspecifica", "IdContraparteGarantia", "RazonSocialContraparteGarantia", "CuentaContraparteGarantia", "IdOrigenGarantia", 
		"IdEspecieGarantia", "IdEmpresa", "IdPais", "EsNominal", "FolioExternoReferencia", "IdCliente", "IdPortafolio", 
		"IdCuentaCustodioPortafolio", "IdEstado", "IdTipoCanalIngreso", "IdMonedaSistema", "IdMonedaAdicional", "FechaFactorMonedaAdicional",
		"ValorFactorMonedaAdicional", "CodigoIFRS", "IdConcepto", "IdTipoOperacion", "IdMesa", "IdBolsa", "IdSucursal", "IdAgente", 
		"IdCondicionLiquidacion", "FechaLiquidacion", "EsIntraday", "UsaGarantiaEspecifica", "Glosa", "IdUsuario", "IdModulo", 
		"TipoCarteraCorredor", "TipoCarteraNormativa", "FechaCurse", "TipoFactorAjuste", "ValorFactorAjusteaFechaCurse", 
		"CostoEnFactorAjuste", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior",
		"Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "IdMonedaEmision", "FechaEmisionInstrumento", 
		"FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", 
		"IdMonedaInstrumento", "Tamano", "IdBoveda", "CuentaBoveda", ISIN, "FechaFactorMonedaInstrumento", 
		"ValorFactorMonedaInstrumento", "Cantidad", "CorteMinimo", "ValorRescate", "PrecioCompra", "TasaCompra", 
		"MontoCompra", "MontoCompraMonedaInstrumento", "MontoCompraMonedaAdicional", "IdCarga", "IdSecuenciaCarteraMigracion", 
		"CodigoPeriodo", "IdOperacion", "IdClienteRelacionado", "IdPortafolioRelacionado", "IdCuentaCustodioPortafolioRelacionado", 
		"PrecioSucio", "PrecioAPlazo", "EsVentaCorta", "MontoAPlazo", "MontoAPlazoMonedaInstrumento", "IdTipoProducto", 
		"FechaOperacionOriginal", "IdRegimenFondoMutuo", "FechaOrigenCompra")

	
	SELECT 
 		p_FechaRespaldo, "IdMovimientoCarteraEnTransito", 
		"IdMovimientoCarteraEnTransitoRelacionado", "FechaIngresoRegistro", "FechaCertificacionRegistro", 
		"FechaAnulacionRegistro", "IdUsuarioIngreso", "IdUsuarioCertificacion", "IdUsuarioAnulacion", 
		"FechaOperacion", "FechaVencimientoOperacion", "PlazoEnDiasOperacion", "FolioCustodia", "IdTipoComprobante", 
		"FolioComprobante", "EsRegistroGeneradoPorSistema", "MueveCustodia", "PermiteCertificacionPorUsuario", 
		"OrigenCustodia", "IdCustodiaOrigen", "IdTipoOperacionOrigen", "FolioCarteraGarantizada", "IdInstrumentoCarteraGarantizada",
		"EsEspecifica", "IdContraparteGarantia", "RazonSocialContraparteGarantia", "CuentaContraparteGarantia", "IdOrigenGarantia", 
		"IdEspecieGarantia", "IdEmpresa", "IdPais", "EsNominal", "FolioExternoReferencia", "IdCliente", "IdPortafolio", 
		"IdCuentaCustodioPortafolio", "IdEstado", "IdTipoCanalIngreso", "IdMonedaSistema", "IdMonedaAdicional", "FechaFactorMonedaAdicional",
		"ValorFactorMonedaAdicional", "CodigoIFRS", "IdConcepto", "IdTipoOperacion", "IdMesa", "IdBolsa", "IdSucursal", "IdAgente", 
		"IdCondicionLiquidacion", "FechaLiquidacion", "EsIntraday", "UsaGarantiaEspecifica", "Glosa", "IdUsuario", "IdModulo", 
		"TipoCarteraCorredor", "TipoCarteraNormativa", "FechaCurse", "TipoFactorAjuste", "ValorFactorAjusteaFechaCurse", 
		"CostoEnFactorAjuste", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior",
		"Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "IdMonedaEmision", "FechaEmisionInstrumento", 
		"FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", 
		"IdMonedaInstrumento", "Tamano", "IdBoveda", "CuentaBoveda", ISIN, "FechaFactorMonedaInstrumento", 
		"ValorFactorMonedaInstrumento", "Cantidad", "CorteMinimo", "ValorRescate", "PrecioCompra", "TasaCompra", 
		"MontoCompra", "MontoCompraMonedaInstrumento", "MontoCompraMonedaAdicional", "IdCarga", "IdSecuenciaCarteraMigracion", 
		"CodigoPeriodo", "IdOperacion", "IdClienteRelacionado", "IdPortafolioRelacionado", "IdCuentaCustodioPortafolioRelacionado", 
		"PrecioSucio", "PrecioAPlazo", "EsVentaCorta", "MontoAPlazo", "MontoAPlazoMonedaInstrumento", "IdTipoProducto", 
		"FechaOperacionOriginal", "IdRegimenFondoMutuo", "FechaOrigenCompra"
	FROM  "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
		WHERE "IdEstado" = 'PEN' 
			AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
			AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
			AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
			AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
			AND COALESCE(p_IdCarga ,0)  IN ("IdCarga",0)
			;

	SELECT ::ROWCOUNT into v_countMovTransito FROM DUMMY;

	DELETE FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" 
	WHERE "FechaRespaldo" = p_FechaRespaldo 
	--AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
		--AND COALESCE( p_TipoInstrumento, '') IN ("IdTipoInstrumento", '')
		AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
		AND  COALESCE( p_IdInstrumento, 0) IN ("IdInstrumento", 0)
		AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
		;
	
	INSERT INTO "BSCL.Custodia.BD::HIS.CarteraNegativa"
	("FechaRespaldo", "IdCarteraNegativa", "FechaIngresoRegistro", "FechaNeteo", "IdEstado", "IdUsuarioIngreso", "IdUsuarioNeteo", "FechaOperacion", "IdTipoOperacion", "IdEmpresa", "IdCliente", "IdPortafolio", "IdInstrumento", "Cantidad", "ValorRescate", "IdConcepto", "IdCuentaCustodioPortafolio", "IdCondicionLiquidacion", "FechaLiquidacion", "CodigoIFRS", "IdMesa", "Nemotecnico", "IdEmisor", "FechaVencimientoOperacion", "EsFungible", "IdTipoReajuste", "IdOperacionMesa", "IdTipoComprobante", "FolioComprobante", "IdMovimientoComprobante", "PrecioOperacion", "MontoOperacionMonedaInstrumento", "TasaOperacion", "IdBoveda", "IdTipoProducto", "IdCorredoraContraparte", "FolioTransaccion", "FechaTransaccion", "IdBolsa", "IdOperacion", "IdAgente", "IdMonedaInstrumento", "IdTipoCanalIngreso", "CorteMinimo", "FolioCustodia", "FolioExternoReferencia", "IdSucursal", "Glosa", "NemoSVS", "IdMonedaEmision", "FechaValorizacion", "PrecioMercado", "TasaMercado", "MontoMercadoMonedaSistema", "MontoMercadoMonedaInstrumento", "FechaPrecioTasa", "FuentePrecioTasa","FechaCorteCupon","FechaPagoCorteCupon","MontoCorteCupon","IdMonedaPagoCupon","NumeroCupon","MontoCapitalCorteCupon","MontoInteresCorteCupon","InteresesPagoCorteCupon","MontoCapitalCorteCuponMonedaInstrumento","MontoReajusteCorteCupon","MontoCorteCuponMonedaInstrumento", "IdInstrumentoAnterior")
	SELECT p_FechaRespaldo AS "FechaRespaldo", "IdCarteraNegativa", "FechaIngresoRegistro", "FechaNeteo", NEG."IdEstado", "IdUsuarioIngreso", "IdUsuarioNeteo", "FechaOperacion", "IdTipoOperacion", "IdEmpresa", "IdCliente", "IdPortafolio", NEG."IdInstrumento", "Cantidad", "ValorRescate", "IdConcepto", "IdCuentaCustodioPortafolio", NEG."IdCondicionLiquidacion", "FechaLiquidacion", "CodigoIFRS", "IdMesa", NEG."Nemotecnico", NEG."IdEmisor", NEG."FechaVencimientoOperacion", "EsFungible", NEG."IdTipoReajuste", "IdOperacionMesa", "IdTipoComprobante", "FolioComprobante", "IdMovimientoComprobante", "PrecioOperacion", "MontoOperacionMonedaInstrumento", "TasaOperacion", "IdBoveda", "IdTipoProducto", "IdCorredoraContraparte", "FolioTransaccion", "FechaTransaccion", "IdBolsa", "IdOperacion", "IdAgente", "IdMonedaInstrumento", "IdTipoCanalIngreso", NEG."CorteMinimo", "FolioCustodia", "FolioExternoReferencia", "IdSucursal", "Glosa", "NemoSVS", NEG."IdMonedaEmision", "FechaValorizacion", "PrecioMercado", "TasaMercado", "MontoMercadoMonedaSistema", "MontoMercadoMonedaInstrumento", "FechaPrecioTasa", "FuentePrecioTasa","FechaCorteCupon","FechaPagoCorteCupon","MontoCorteCupon","IdMonedaPagoCupon","NumeroCupon","MontoCapitalCorteCupon","MontoInteresCorteCupon","InteresesPagoCorteCupon","MontoCapitalCorteCuponMonedaInstrumento","MontoReajusteCorteCupon","MontoCorteCuponMonedaInstrumento", "IdInstrumentoAnterior"
	FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" NEG
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INs on NEG."IdInstrumento" = INS."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tINs on tins."IdTipoInstrumento" = INS."IdTipoInstrumento"
	WHERE NEG."IdEstado" = 'VIG' 
			AND COALESCE( p_IdClasificacionTipoInstrumento, '') IN ("IdClasificacionTipoInstrumento", '')
			AND COALESCE( p_TipoInstrumento, '') IN (INS."IdTipoInstrumento", '')
			AND  COALESCE( p_IdClient, 0) IN ("IdCliente", 0) 
			AND  COALESCE( p_IdInstrumento, 0) IN (NEG."IdInstrumento", 0)
			AND  COALESCE( p_IdPortafolio, 0) IN ("IdPortafolio", 0)
			;
	
	SELECT ::ROWCOUNT into v_countNegativas FROM DUMMY;
	
	p_out_countCarteras := v_countCarteras;
	p_out_countSubyacentes := v_countSubyacentes;
	p_out_countReservas := v_countReservas;
	p_out_countGarantias := v_countGarantias;
	p_out_countMovTransito := v_countMovTransito;
	p_out_countNegativas := v_countNegativas;
	
END;