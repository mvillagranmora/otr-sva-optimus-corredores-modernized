SET SCHEMA "BSCL";


DROP PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ModificarPrestamo";


CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ModificarPrestamo" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',
	IN p_IdCartera 							BIGINT,
	IN p_IdSubyacente 						BIGINT,
    
	IN p_FechaVencimiento 					NVARCHAR(10) 	default '0001-01-01',
	IN p_PrecioInstrumento					DECIMAL(26,8) 	default 0,
	IN p_TasaInstrumento					DECIMAL(26,8) 	default 0,
	IN p_Monto								DECIMAL(26,8) 	default 0,
	IN p_MontoMdaInstr						DECIMAL(26,8) 	default 0,
	IN p_MontoPrima							DECIMAL(26,8) 	default 0,
	IN p_TasaPrima							DECIMAL(26,8) 	default 0,
	IN p_MontoAPlazo						DECIMAL(26,8) 	default 0,
	IN p_PlazoEnDias 						BIGINT 			default 0,
	
	   
	OUT p_out_NewIdCartera bigint,
	OUT p_out_NewIdSubyacente bigint,
	OUT p_out_ErroresContador int,
	OUT p_out_ErrCode NVARCHAR(5),
	OUT p_out_ErrExt NVARCHAR(200)
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		
		p_out_NewIdCartera := 0;	
		p_out_NewIdSubyacente := 0;
		p_out_ErroresContador := 1;
		
		IF ::SQL_ERROR_CODE > 10000 THEN
			SELECT ::SQL_ERROR_CODE, ::SQL_ERROR_MESSAGE INTO p_out_ErrCode, p_out_ErrExt  FROM DUMMY;
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;

	END;


	p_out_ErroresContador		:= 0;
	
	BEGIN
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
		
		SELECT "BSCL.Custodia.BD.Secuencias::IdCartera".NEXTVAL INTO p_out_NewIdCartera FROM DUMMY;
		
		INSERT INTO "BSCL.Custodia.BD::CUS.Cartera" ("IdCartera", "FolioCartera", "FolioCarteraRelacionada", "FolioGarantiaOrigen", "FechaIngresoRegistro", "FechaModificacionRegistro", "IdEmpresa", "FechaOperacion", 
			"FechaVencimientoOperacion", "PlazoEnDiasOperacion", "IdCliente", "IdPortafolio","IdCuentaCustodioPortafolio", "IdClienteRelacionado", "IdPortafolioRelacionado", "IdEstado", "IdTipoProducto", "IdTipoCanalIngreso", 
			"IdMonedaSistema", "IdMonedaAdicional", "FechaFactorMonedaAdicional", "ValorFactorMonedaAdicional","IdMovimientoCarteraEnTransito", "IdConcepto", "IdTipoOperacion", "IdOperacion", "IdTipoComprobante", "FolioComprobante", 
			"FechaComprobante", "FolioExternoReferencia", "IdMesa", "IdBolsa", "IdSucursal", "IdAgente", "IdCondicionLiquidacion", "FechaLiquidacion", "EsIntraday", "UsaGarantiaEspecifica", "CobraPrimaAlInicio", "IdTipoPrestamo", 
			"Glosa", "IdUsuario", "IdModulo", "TipoCarteraCorredor", "TipoCarteraNormativa", "FechaCurseInversion", "TipoFactorAjuste", "ValorFactorAjusteaFechaCurse", "CostoInversionEnFactorAjuste", "IdClasificacionTipoInstrumento", 
			"IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "EsPagaderoEn", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", 
			"TipoValorizacion", "TieneCorteDeCupon", "CodigoIFRS", "IdBoveda", "CuentaBoveda", "IdMonedaInstrumento", "Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", 
			"Cantidad", "CorteMinimo", "ValorRescate", "PrecioCompra", "TasaCompra", "MontoCompra", "MontoCompraMonedaInstrumento", "MontoCompraMonedaAdicional", "TasaAgente", "PrecioAPlazo", "MontoAPlazo", "MontoAPlazoMonedaInstrumento", 
			"MontoAPlazoMonedaAdicional", "FechaEfectiva", "IdPlaza", "IdFixing", "FechaFixing", "HoraFixing", "IdModalidadCumplimiento", "IdReferenciaMercado", "IdMonedaPrincipal", "IdMonedaSecundaria", "ValorTipoCambioCierre", 
			"ValorParidadCierre", "ValorTipoCambioCosto", "ValorParidadCosto", "MontoMonedaPrincipal", "MontoMonedaSecundaria", "IdCarga", "IdSecuenciaCarteraMigracion", "CodigoPeriodo", "INGGlosa", "INGTasaAgente", "INGFechaOperacion", 
			"INGIdCondicionLiquidacion", "INGFechaLiquidacion", "INGCantidad", "INGValorRescate", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra", "INGMontoPrecioPromedioMonedaInstrumento", "INGMontoPrecioPromedioMonedaAdicional", 
			"INGPrecioAPlazo", "INGMontoAPlazo", "INGMontoAPlazoMonedaInstrumento", "INGMontoAPlazoMonedaAdicional", "INGEsPuntos", "INGValorTipoCambioSpot", "INGValorTipoCambioCotizador", "INGTasaPuntosMonedaPrincipal", 
			"INGTasaPuntosMonedaSecundaria", "INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionCartera", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", 
			"INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", "INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", 
			"INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", "INGValorFactorMonedaAdicionalActual", 
			"INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", "INGPrecioCompra", "INGTasaCompra", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", 
			"INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", "INGMontoReajusteCompraAnteriorMonedaInstrumento", "INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", 
			"INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", 
			"INGMontoCompraAnteriorPibMasMonedaAdicional", "INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", "INGMontoCompraAnteriorPibMenosMonedaAdicional", "INGMontoATasaCompraActual", 
			"INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", "INGMontoReajusteCorridoCompraActual", 
			"INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", "INGMontoReajusteCorridoCompraActualMonedaAdicional", 
			"INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", "INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", 
			"INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", "INGMontoInteresCompraProximoMonedaInstrumento",
			"INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", 
			"INGConvexidadCompraProximo", "INGFechaPrecioTasaMercadoAnterior", "INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", "INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", 
			"INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", "INGMontoInteresMercadoAnteriorMonedaInstrumento", "INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", 
			"INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", 
			"INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", "INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", 
			"INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", 
			"INGDurationMercadoActual", "INGConvexidadMercadoActual", "INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", "INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", 
			"INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", "INGMontoMercadoActualPibMasMonedaAdicional", 
			"INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", 
			"INGTasaMercadoProximo", "INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", 
			"INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", "INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", 
			"INGConvexidadMercadoProximo", "INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", 
			"INGIdMonedaPagoCupon", "INGMontoCorteCupon", "INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", 
			"INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", "INGMontoInteresCorteCuponMonedaAdicional", 
			"INGMontoReajusteCorteCuponMonedaAdicional", "INGMontoPresenteTIF", "INGMontoInteresTIF", "INGMontoReajusteTIF", "INGMontoInteresAcumTIF", "INGMontoReajusteAcumTIF", "INGMontoPresenteTIFMonedaInstrumento", "INGMontoInteresTIFMonedaInstrumento", 
			"INGMontoReajusteTIFMonedaInstrumento", "INGMontoInteresAcumTIFMonedaInstrumento", "INGMontoReajusteAcumTIFMonedaInstrumento", "INGMontoPresenteTIFMonedaAdicional", "INGMontoInteresTIFMonedaAdicional", "INGMontoReajusteTIFMonedaAdicional", 
			"INGMontoInteresAcumTIFMonedaAdicional", "INGMontoReajusteAcumTIFMonedaAdicional", "INGMontoMonedaPrincipal", "INGMontoMonedaSecundaria", "INGMontoDiferido", "INGMontoUtilidadDiferida", "INGMontoPerdidaDiferida", "INGMontoMargen", 
			"INGMontoValorRazonableActivo", "INGMontoValorRazonablePasivo", "INGMontoValorRazonableNeto", "INGTasaValorRazonableActivo", "INGTasaValorRazonablePasivo", "INGMontoDevengoTipoCambio", "INGMontoDevengoTipoCambioAnterior", 
			"INGMontoDiferidoDiario", "INGMontoDiferidoAcumulado", "INGMontoMatriz") 
		
		SELECT p_out_NewIdCartera AS "IdCartera", "FolioCartera", "FolioCarteraRelacionada", "FolioGarantiaOrigen", CURRENT_TIMESTAMP AS "FechaIngresoRegistro", CURRENT_TIMESTAMP AS "FechaModificacionRegistro", "IdEmpresa", "FechaOperacion", 
			p_FechaVencimiento AS "FechaVencimientoOperacion", p_PlazoEnDias AS "PlazoEnDiasOperacion", "IdCliente", "IdPortafolio","IdCuentaCustodioPortafolio", "IdClienteRelacionado", "IdPortafolioRelacionado", 'VIG' AS "IdEstado", 
			"IdTipoProducto", "IdTipoCanalIngreso", 
			"IdMonedaSistema", "IdMonedaAdicional", "FechaFactorMonedaAdicional", "ValorFactorMonedaAdicional","IdMovimientoCarteraEnTransito", "IdConcepto", "IdTipoOperacion", "IdOperacion", "IdTipoComprobante", "FolioComprobante", 
			"FechaComprobante", "FolioExternoReferencia", "IdMesa", "IdBolsa", "IdSucursal", "IdAgente", "IdCondicionLiquidacion", "FechaLiquidacion", "EsIntraday", "UsaGarantiaEspecifica", "CobraPrimaAlInicio", "IdTipoPrestamo", 
			"Glosa", p_IdUsuario AS "IdUsuario", "IdModulo", "TipoCarteraCorredor", "TipoCarteraNormativa", "FechaCurseInversion", "TipoFactorAjuste", "ValorFactorAjusteaFechaCurse", "CostoInversionEnFactorAjuste", "IdClasificacionTipoInstrumento", 
			"IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "EsPagaderoEn", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", 
			"TipoValorizacion", "TieneCorteDeCupon", "CodigoIFRS", "IdBoveda", "CuentaBoveda", "IdMonedaInstrumento", "Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", 
			"Cantidad", "CorteMinimo", "ValorRescate", p_PrecioInstrumento AS "PrecioCompra", p_TasaPrima AS "TasaCompra", p_Monto AS "MontoCompra", p_MontoMdaInstr AS "MontoCompraMonedaInstrumento", "MontoCompraMonedaAdicional", "TasaAgente", p_MontoPrima AS "PrecioAPlazo", 
			p_MontoAPlazo AS "MontoAPlazo", p_MontoAPlazo AS "MontoAPlazoMonedaInstrumento", 
			"MontoAPlazoMonedaAdicional", "FechaEfectiva", "IdPlaza", "IdFixing", "FechaFixing", "HoraFixing", "IdModalidadCumplimiento", "IdReferenciaMercado", "IdMonedaPrincipal", "IdMonedaSecundaria", "ValorTipoCambioCierre", 
			"ValorParidadCierre", "ValorTipoCambioCosto", "ValorParidadCosto", "MontoMonedaPrincipal", "MontoMonedaSecundaria", "IdCarga", "IdSecuenciaCarteraMigracion", "CodigoPeriodo", "INGGlosa", "INGTasaAgente", "INGFechaOperacion", 
			"INGIdCondicionLiquidacion", "INGFechaLiquidacion", "INGCantidad", "INGValorRescate", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra", "INGMontoPrecioPromedioMonedaInstrumento", "INGMontoPrecioPromedioMonedaAdicional", 
			p_MontoPrima AS "INGPrecioAPlazo", p_MontoAPlazo AS "INGMontoAPlazo", p_MontoAPlazo AS "INGMontoAPlazoMonedaInstrumento", "INGMontoAPlazoMonedaAdicional", "INGEsPuntos", "INGValorTipoCambioSpot", "INGValorTipoCambioCotizador", "INGTasaPuntosMonedaPrincipal", 
			"INGTasaPuntosMonedaSecundaria", 0, "INGValorizacionConProblemas", "INGFechaValorizacionCartera", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", 
			"INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", "INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", 
			"INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", "INGValorFactorMonedaAdicionalActual", 
			"INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", p_PrecioInstrumento AS "INGPrecioCompra", p_TasaPrima AS "INGTasaCompra", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", 
			"INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", "INGMontoReajusteCompraAnteriorMonedaInstrumento", "INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", 
			"INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", 
			"INGMontoCompraAnteriorPibMasMonedaAdicional", "INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", "INGMontoCompraAnteriorPibMenosMonedaAdicional", p_Monto AS "INGMontoATasaCompraActual", 
			p_Monto AS "INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", "INGMontoReajusteCorridoCompraActual", 
			"INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", "INGMontoReajusteCorridoCompraActualMonedaAdicional", 
			"INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", "INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", 
			"INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", "INGMontoInteresCompraProximoMonedaInstrumento",
			"INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", 
			"INGConvexidadCompraProximo", "INGFechaPrecioTasaMercadoAnterior", "INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", "INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", 
			"INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", "INGMontoInteresMercadoAnteriorMonedaInstrumento", "INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", 
			"INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", 
			"INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", "INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", 
			"INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", 
			"INGDurationMercadoActual", "INGConvexidadMercadoActual", "INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", "INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", 
			"INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", "INGMontoMercadoActualPibMasMonedaAdicional", 
			"INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", 
			"INGTasaMercadoProximo", "INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", 
			"INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", "INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", 
			"INGConvexidadMercadoProximo", "INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", 
			"INGIdMonedaPagoCupon", "INGMontoCorteCupon", "INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", 
			"INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", "INGMontoInteresCorteCuponMonedaAdicional", 
			"INGMontoReajusteCorteCuponMonedaAdicional", "INGMontoPresenteTIF", "INGMontoInteresTIF", "INGMontoReajusteTIF", "INGMontoInteresAcumTIF", "INGMontoReajusteAcumTIF", "INGMontoPresenteTIFMonedaInstrumento", "INGMontoInteresTIFMonedaInstrumento", 
			"INGMontoReajusteTIFMonedaInstrumento", "INGMontoInteresAcumTIFMonedaInstrumento", "INGMontoReajusteAcumTIFMonedaInstrumento", "INGMontoPresenteTIFMonedaAdicional", "INGMontoInteresTIFMonedaAdicional", "INGMontoReajusteTIFMonedaAdicional", 
			"INGMontoInteresAcumTIFMonedaAdicional", "INGMontoReajusteAcumTIFMonedaAdicional", "INGMontoMonedaPrincipal", "INGMontoMonedaSecundaria", "INGMontoDiferido", "INGMontoUtilidadDiferida", "INGMontoPerdidaDiferida", "INGMontoMargen", 
			"INGMontoValorRazonableActivo", "INGMontoValorRazonablePasivo", "INGMontoValorRazonableNeto", "INGTasaValorRazonableActivo", "INGTasaValorRazonablePasivo", "INGMontoDevengoTipoCambio", "INGMontoDevengoTipoCambioAnterior", 
			"INGMontoDiferidoDiario", "INGMontoDiferidoAcumulado", "INGMontoMatriz"
		FROM "BSCL.Custodia.BD::CUS.Cartera" 
		WHERE "IdCartera" = p_IdCartera 
				AND "IdEstado" = 'VIG';

		
		SELECT "BSCL.Custodia.BD.Secuencias::IdSubyacente".NEXTVAL INTO p_out_NewIdSubyacente FROM DUMMY;

		INSERT INTO "BSCL.Custodia.BD::CUS.Subyacente" ("IdSubyacente", "FolioSubyacente", "FolioCarteraRelacionada", "FechaIngresoRegistro", "IdCarteraRetirada", "IdMovimientoCarteraEnTransito", 
			"PrecioCompraOperacionRelacionada", "TasaCompraOperacionRelacionada", "PrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada", 
			"MontoPrecioPromedioMonedaAdicionalOperacionRelacionada", "IdEstado", "IdTipoCanalIngreso", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", 
			"IdTipoReajuste", "EsPagaderoEn", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", "IdMonedaInstrumento", "Tamano", "TIFAfectoBeneficio", "TIFTasa", 
			ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", "Cantidad", "CorteMinimo", "ValorRescate", "PrecioSubyacente", "TasaSubyacente", "MontoSubyacente", "MontoSubyacenteMonedaInstrumento", "MontoSubyacenteMonedaAdicional", 
			"INGFechaOperacion", "INGCantidad", "INGValorRescate", "INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionSubyacente", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", 
			"INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", "INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", 
			"INGValorFactorMonedaInstrumentoProximo", "INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", "INGValorFactorMonedaAdicionalActual", "INGFechaFactorMonedaAdicionalProximo", 
			"INGValorFactorMonedaAdicionalProximo", "INGPrecioSubyacente", "INGTasaSubyacente", "INGMontoATasaSubyacenteAnterior", "INGMontoInteresSubyacenteAnterior", "INGMontoReajusteSubyacenteAnterior", "INGMontoSubyacenteAnteriorMonedaInstrumento", 
			"INGMontoInteresSubyacenteAnteriorMonedaInstrumento", "INGMontoReajusteSubyacenteAnteriorMonedaInstrumento", "INGMontoSubyacenteAnteriorMonedaAdicional", "INGMontoInteresSubyacenteAnteriorMonedaAdicional", 
			"INGMontoReajusteSubyacenteAnteriorMonedaAdicional", "INGDurationSubyacenteAnterior", "INGConvexidadSubyacenteAnterior", "INGMontoSubyacenteAnteriorPibMas", "INGMontoSubyacenteAnteriorPibMasMonedaInstrumento", 
			"INGMontoSubyacenteAnteriorPibMasMonedaAdicional", "INGMontoSubyacenteAnteriorPibMenos", "INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMenosMonedaAdicional", "INGMontoATasaSubyacenteActual", 
			"INGMontoSubyacenteActualMonedaInstrumento", "INGMontoSubyacenteActualMonedaAdicional", "INGDurationSubyacenteActual", "INGConvexidadSubyacenteActual", "INGMontoInteresCorridoSubyacenteActual", "INGMontoReajusteCorridoSubyacenteActual", 
			"INGMontoInteresCorridoSubyacenteActualMonedaInstrumento", "INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento", "INGMontoInteresCorridoSubyacenteActualMonedaAdicional", "INGMontoReajusteCorridoSubyacenteActualMonedaAdicional", 
			"INGMontoSubyacenteActualPibMas", "INGMontoSubyacenteActualPibMasMonedaInstrumento", "INGMontoSubyacenteActualPibMasMonedaAdicional", "INGMontoSubyacenteActualPibMenos", "INGMontoSubyacenteActualPibMenosMonedaInstrumento", 
			"INGMontoSubyacenteActualPibMenosMonedaAdicional", "INGMontoATasaSubyacenteProximo", "INGMontoInteresSubyacenteProximo", "INGMontoReajusteSubyacenteProximo", "INGMontoSubyacenteProximoMonedaInstrumento", 
			"INGMontoInteresSubyacenteProximoMonedaInstrumento", "INGMontoReajusteSubyacenteProximoMonedaInstrumento", "INGMontoSubyacenteProximoMonedaAdicional", "INGMontoInteresSubyacenteProximoMonedaAdicional", 
			"INGMontoReajusteSubyacenteProximoMonedaAdicional", "INGDurationSubyacenteProximo", "INGConvexidadSubyacenteProximo", "INGFechaPrecioTasaMercadoAnterior", "INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", 
			"INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", "INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", "INGMontoInteresMercadoAnteriorMonedaInstrumento", 
			"INGMontoReajusteMercadoAnteriorMonedaInstrumento", "INGMontoMercadoAnteriorMonedaAdicional", "INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", 
			"INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", "INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", "INGMontoMercadoAnteriorPibMenos", 
			"INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "INGTasaMercadoActual", 
			"INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", "INGDurationMercadoActual", "INGConvexidadMercadoActual", "INGMontoInteresCorridoMercadoActual", 
			"INGMontoReajusteCorridoMercadoActual", "INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", "INGMontoInteresCorridoMercadoActualMonedaAdicional", 
			"INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", "INGMontoMercadoActualPibMasMonedaAdicional", "INGMontoMercadoActualPibMenos", 
			"INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", "INGTasaMercadoProximo", 
			"INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", "INGMontoReajusteMercadoProximoMonedaInstrumento", 
			"INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", "INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", "INGConvexidadMercadoProximo", "INGMontoATasaCompraAnterior", 
			"INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", "INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", "INGMontoReajusteCompraAnteriorMonedaInstrumento", 
			"INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", "INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", "INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", 
			"INGMontoCompraAnteriorPibMasMonedaInstrumento", "INGMontoCompraAnteriorPibMasMonedaAdicional", "INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", "INGMontoCompraAnteriorPibMenosMonedaAdicional", 
			"INGMontoATasaCompraActual", "INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", "INGMontoReajusteCorridoCompraActual", 
			"INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", "INGMontoReajusteCorridoCompraActualMonedaAdicional", 
			"INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", "INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", 
			"INGMontoCompraActualPibMenosMonedaAdicional", "INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", "INGMontoInteresCompraProximoMonedaInstrumento", 
			"INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", "INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", 
			"INGConvexidadCompraProximo", "INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", "INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", 
			"INGIdMonedaPagoCupon", "INGMontoCorteCupon", "INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", 
			"INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", "INGMontoCapitalCorteCuponMonedaAdicional", "INGMontoInteresCorteCuponMonedaAdicional", 
			"INGMontoReajusteCorteCuponMonedaAdicional", "INGMontoPresenteTIF", "INGMontoInteresTIF", "INGMontoReajusteTIF", "INGMontoInteresAcumTIF", "INGMontoReajusteAcumTIF", "INGMontoPresenteTIFMonedaInstrumento", "INGMontoInteresTIFMonedaInstrumento", 
			"INGMontoReajusteTIFMonedaInstrumento", "INGMontoInteresAcumTIFMonedaInstrumento", "INGMontoReajusteAcumTIFMonedaInstrumento", "INGMontoPresenteTIFMonedaAdicional", "INGMontoInteresTIFMonedaAdicional", "INGMontoReajusteTIFMonedaAdicional", 
			"INGMontoInteresAcumTIFMonedaAdicional", "INGMontoReajusteAcumTIFMonedaAdicional")

				
		SELECT p_out_NewIdSubyacente AS "IdSubyacente", "FolioSubyacente", "FolioCarteraRelacionada", CURRENT_TIMESTAMP AS "FechaIngresoRegistro", "IdCarteraRetirada", "IdMovimientoCarteraEnTransito", p_PrecioInstrumento AS "PrecioCompraOperacionRelacionada", 
			p_TasaPrima AS "TasaCompraOperacionRelacionada", "PrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioCompraOperacionRelacionada", "MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada", "MontoPrecioPromedioMonedaAdicionalOperacionRelacionada", 
			'VIG' AS "IdEstado", "IdTipoCanalIngreso", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoAnterior", "Nemotecnico", "IdEmisor", "NemoSVS", "IdTipoReajuste", "EsPagaderoEn", "FechaEmisionInstrumento", 
			"FechaVencimientoInstrumento", "EsFungible", "ValorizaPor", "TipoValorizacion", "TieneCorteDeCupon", "IdMonedaInstrumento", "Tamano", "TIFAfectoBeneficio", "TIFTasa", ISIN, "FechaFactorMonedaInstrumento", "ValorFactorMonedaInstrumento", 
			"Cantidad", "CorteMinimo", "ValorRescate", p_PrecioInstrumento AS "PrecioSubyacente", p_TasaInstrumento AS "TasaSubyacente", p_Monto AS "MontoSubyacente", p_MontoMdaInstr AS "MontoSubyacenteMonedaInstrumento", "MontoSubyacenteMonedaAdicional", 
			"INGFechaOperacion", "INGCantidad", "INGValorRescate", 0 AS "INGTicketValorizacion", "INGValorizacionConProblemas", "INGFechaValorizacionSubyacente", "INGFechaValorizacionAnterior", "INGFechaValorizacionActual", "INGFechaValorizacionProximo", 
			"INGFechaFactorMonedaInstrumentoAnterior", "INGValorFactorMonedaInstrumentoAnterior", "INGFechaFactorMonedaInstrumentoActual", "INGValorFactorMonedaInstrumentoActual", "INGFechaFactorMonedaInstrumentoProximo", "INGValorFactorMonedaInstrumentoProximo", 
			"INGFechaFactorMonedaAdicionalAnterior", "INGValorFactorMonedaAdicionalAnterior", "INGFechaFactorMonedaAdicionalActual", "INGValorFactorMonedaAdicionalActual", "INGFechaFactorMonedaAdicionalProximo", "INGValorFactorMonedaAdicionalProximo", 
			p_PrecioInstrumento AS "INGPrecioSubyacente", p_TasaInstrumento AS "INGTasaSubyacente", p_Monto AS "INGMontoATasaSubyacenteAnterior", "INGMontoInteresSubyacenteAnterior", "INGMontoReajusteSubyacenteAnterior", p_MontoMdaInstr AS "INGMontoSubyacenteAnteriorMonedaInstrumento", 
			"INGMontoInteresSubyacenteAnteriorMonedaInstrumento", "INGMontoReajusteSubyacenteAnteriorMonedaInstrumento", "INGMontoSubyacenteAnteriorMonedaAdicional", "INGMontoInteresSubyacenteAnteriorMonedaAdicional", "INGMontoReajusteSubyacenteAnteriorMonedaAdicional", 
			"INGDurationSubyacenteAnterior", "INGConvexidadSubyacenteAnterior", "INGMontoSubyacenteAnteriorPibMas", "INGMontoSubyacenteAnteriorPibMasMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMasMonedaAdicional", "INGMontoSubyacenteAnteriorPibMenos", 
			"INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento", "INGMontoSubyacenteAnteriorPibMenosMonedaAdicional", p_Monto AS "INGMontoATasaSubyacenteActual", p_MontoMdaInstr AS "INGMontoSubyacenteActualMonedaInstrumento", "INGMontoSubyacenteActualMonedaAdicional", 
			"INGDurationSubyacenteActual", "INGConvexidadSubyacenteActual", "INGMontoInteresCorridoSubyacenteActual", "INGMontoReajusteCorridoSubyacenteActual", "INGMontoInteresCorridoSubyacenteActualMonedaInstrumento", "INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento", 
			"INGMontoInteresCorridoSubyacenteActualMonedaAdicional", "INGMontoReajusteCorridoSubyacenteActualMonedaAdicional", "INGMontoSubyacenteActualPibMas", "INGMontoSubyacenteActualPibMasMonedaInstrumento", "INGMontoSubyacenteActualPibMasMonedaAdicional", 
			"INGMontoSubyacenteActualPibMenos", "INGMontoSubyacenteActualPibMenosMonedaInstrumento", "INGMontoSubyacenteActualPibMenosMonedaAdicional", "INGMontoATasaSubyacenteProximo", "INGMontoInteresSubyacenteProximo", "INGMontoReajusteSubyacenteProximo", 
			"INGMontoSubyacenteProximoMonedaInstrumento", "INGMontoInteresSubyacenteProximoMonedaInstrumento", "INGMontoReajusteSubyacenteProximoMonedaInstrumento", "INGMontoSubyacenteProximoMonedaAdicional", "INGMontoInteresSubyacenteProximoMonedaAdicional", 
			"INGMontoReajusteSubyacenteProximoMonedaAdicional", "INGDurationSubyacenteProximo", "INGConvexidadSubyacenteProximo", "INGFechaPrecioTasaMercadoAnterior", "INGFuenteTasaPrecioMercadoAnterior", "INGPrecioMercadoAnterior", 
			"INGTasaMercadoAnterior", "INGMontoATasaMercadoAnterior", "INGMontoInteresMercadoAnterior", "INGMontoReajusteMercadoAnterior", "INGMontoMercadoAnteriorMonedaInstrumento", "INGMontoInteresMercadoAnteriorMonedaInstrumento", "INGMontoReajusteMercadoAnteriorMonedaInstrumento", 
			"INGMontoMercadoAnteriorMonedaAdicional", "INGMontoInteresMercadoAnteriorMonedaAdicional", "INGMontoReajusteMercadoAnteriorMonedaAdicional", "INGDurationMercadoAnterior", "INGConvexidadMercadoAnterior", "INGMontoMercadoAnteriorPibMas", 
			"INGMontoMercadoAnteriorPibMasMonedaInstrumento", "INGMontoMercadoAnteriorPibMasMonedaAdicional", "INGMontoMercadoAnteriorPibMenos", "INGMontoMercadoAnteriorPibMenosMonedaInstrumento", "INGMontoMercadoAnteriorPibMenosMonedaAdicional", 
			"INGFechaPrecioTasaMercadoActual", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGMontoMercadoActualMonedaAdicional", 
			"INGDurationMercadoActual", "INGConvexidadMercadoActual", "INGMontoInteresCorridoMercadoActual", "INGMontoReajusteCorridoMercadoActual", "INGMontoInteresCorridoMercadoActualMonedaInstrumento", "INGMontoReajusteCorridoMercadoActualMonedaInstrumento", 
			"INGMontoInteresCorridoMercadoActualMonedaAdicional", "INGMontoReajusteCorridoMercadoActualMonedaAdicional", "INGMontoMercadoActualPibMas", "INGMontoMercadoActualPibMasMonedaInstrumento", "INGMontoMercadoActualPibMasMonedaAdicional", 
			"INGMontoMercadoActualPibMenos", "INGMontoMercadoActualPibMenosMonedaInstrumento", "INGMontoMercadoActualPibMenosMonedaAdicional", "INGFechaPrecioTasaMercadoProximo", "INGFuenteTasaPrecioMercadoProximo", "INGPrecioMercadoProximo", 
			"INGTasaMercadoProximo", "INGMontoATasaMercadoProximo", "INGMontoInteresMercadoProximo", "INGMontoReajusteMercadoProximo", "INGMontoMercadoProximoMonedaInstrumento", "INGMontoInteresMercadoProximoMonedaInstrumento", 
			"INGMontoReajusteMercadoProximoMonedaInstrumento", "INGMontoMercadoProximoMonedaAdicional", "INGMontoInteresMercadoProximoMonedaAdicional", "INGMontoReajusteMercadoProximoMonedaAdicional", "INGDurationMercadoProximo", 
			"INGConvexidadMercadoProximo", "INGMontoATasaCompraAnterior", "INGMontoInteresCompraAnterior", "INGMontoReajusteCompraAnterior", "INGMontoCompraAnteriorMonedaInstrumento", "INGMontoInteresCompraAnteriorMonedaInstrumento", 
			"INGMontoReajusteCompraAnteriorMonedaInstrumento", "INGMontoCompraAnteriorMonedaAdicional", "INGMontoInteresCompraAnteriorMonedaAdicional", "INGMontoReajusteCompraAnteriorMonedaAdicional", "INGDurationCompraAnterior", 
			"INGConvexidadCompraAnterior", "INGMontoCompraAnteriorPibMas", "INGMontoCompraAnteriorPibMasMonedaInstrumento", "INGMontoCompraAnteriorPibMasMonedaAdicional", "INGMontoCompraAnteriorPibMenos", "INGMontoCompraAnteriorPibMenosMonedaInstrumento", "INGMontoCompraAnteriorPibMenosMonedaAdicional", 
			p_Monto AS "INGMontoATasaCompraActual", p_MontoMdaInstr" as INGMontoCompraActualMonedaInstrumento", "INGMontoCompraActualMonedaAdicional", "INGDurationCompraActual", "INGConvexidadCompraActual", "INGMontoInteresCorridoCompraActual", 
			"INGMontoReajusteCorridoCompraActual", "INGMontoInteresCorridoCompraActualMonedaInstrumento", "INGMontoReajusteCorridoCompraActualMonedaInstrumento", "INGMontoInteresCorridoCompraActualMonedaAdicional", "INGMontoReajusteCorridoCompraActualMonedaAdicional", 
			"INGMontoCompraActualPibMas", "INGMontoCompraActualPibMasMonedaInstrumento", "INGMontoCompraActualPibMasMonedaAdicional", "INGMontoCompraActualPibMenos", "INGMontoCompraActualPibMenosMonedaInstrumento", "INGMontoCompraActualPibMenosMonedaAdicional", 
			"INGMontoATasaCompraProximo", "INGMontoInteresCompraProximo", "INGMontoReajusteCompraProximo", "INGMontoCompraProximoMonedaInstrumento", "INGMontoInteresCompraProximoMonedaInstrumento", "INGMontoReajusteCompraProximoMonedaInstrumento", "INGMontoCompraProximoMonedaAdicional", 
			"INGMontoInteresCompraProximoMonedaAdicional", "INGMontoReajusteCompraProximoMonedaAdicional", "INGDurationCompraProximo", "INGConvexidadCompraProximo", "INGTasaCorredora", "INGFechaTasaCorredora", "INGMontoATasaCorredora", 
			"INGMontoATasaCorredoraMonedaInstrumento", "INGMontoATasaCorredoraMonedaAdicional", "INGFechaCorteCupon", "INGNumeroCupon", "INGIdMonedaPagoCupon", "INGMontoCorteCupon", "INGFechaPagoCorteCupon", "INGMontoCapitalCorteCupon", 
			"INGMontoInteresCorteCupon", "INGMontoReajusteCorteCupon", "INGMontoCorteCuponMonedaInstrumento", "INGMontoCapitalCorteCuponMonedaInstrumento", "INGMontoInteresCorteCuponMonedaInstrumento", "INGMontoReajusteCorteCuponMonedaInstrumento", "INGMontoCorteCuponMonedaAdicional", 
			"INGMontoCapitalCorteCuponMonedaAdicional", "INGMontoInteresCorteCuponMonedaAdicional", "INGMontoReajusteCorteCuponMonedaAdicional", "INGMontoPresenteTIF", "INGMontoInteresTIF", "INGMontoReajusteTIF", "INGMontoInteresAcumTIF", "INGMontoReajusteAcumTIF", 
			"INGMontoPresenteTIFMonedaInstrumento", "INGMontoInteresTIFMonedaInstrumento", "INGMontoReajusteTIFMonedaInstrumento", "INGMontoInteresAcumTIFMonedaInstrumento", "INGMontoReajusteAcumTIFMonedaInstrumento", "INGMontoPresenteTIFMonedaAdicional", 
			"INGMontoInteresTIFMonedaAdicional", "INGMontoReajusteTIFMonedaAdicional", "INGMontoInteresAcumTIFMonedaAdicional", "INGMontoReajusteAcumTIFMonedaAdicional"
		FROM "BSCL.Custodia.BD::CUS.Subyacente"
		WHERE "IdSubyacente" = p_IdSubyacente
			AND "IdEstado" = 'VIG';

			
		UPDATE "BSCL.Custodia.BD::CUS.Subyacente" SET "IdEstado" = 'MOD', "FechaModificacionRegistro" = CURRENT_TIMESTAMP WHERE "IdSubyacente" = p_IdSubyacente AND "IdEstado" = 'VIG';
		UPDATE "BSCL.Custodia.BD::CUS.Cartera" SET "IdEstado" = 'MOD', "FechaModificacionRegistro" = CURRENT_TIMESTAMP WHERE "IdCartera" = p_IdCartera AND "IdEstado" = 'VIG';
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => :v_fechaHoraSistema);
		        
		v_DetalleLog = 	SELECT 'MODIFICACION' AS "SubRegistro", 
		        		'IdCartera' AS "CampoRegistro", 
		        		:p_IdCartera AS "ValorAntiguo", 
		        		:p_out_NewIdCartera AS "ValorNuevo" FROM dummy;
		        		

		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'CUS' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa
					,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'MODIFICARPRESTAMO'
					,p_IdRegistro => :p_out_NewIdCartera 
					,p_DetalleLog => :v_DetalleLog  --hacia arriba obligatorios
					,p_NombreProceso => 'BSCL.Custodia.BD.Servicios::PRT.ModificarPrestamo' -- hacia abajo opcionales
					,p_EstadoProceso => ''
					,p_Cantidad => 0
					,p_Nemo => '');

	END;
END; 

