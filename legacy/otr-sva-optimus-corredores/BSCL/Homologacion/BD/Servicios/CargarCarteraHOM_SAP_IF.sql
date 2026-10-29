SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraHOM_SAP_IF";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraHOM_SAP_IF"
	(  
	 	IN  p_IdCarga 	       			BIGINT          -- ID de la carga 
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(5000); --MENSAJE SALIDA
	    DECLARE v_erroroutmsg  			nvarchar(5000);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    -- Para LOg
	    DECLARE v_out_refID					BIGINT;    
	    DECLARE v_out_ErroresContador		BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo"  NVARCHAR(5000),"Descripcion" nvarchar(5000));	
	    DECLARE v_FechaSistema  			NVARCHAR(5000);
	    DECLARE v_MaxPeriodo  				NVARCHAR(5000);
	    DECLARE v_FechaInicioPeriodo 		NVARCHAR(5000);
	    DECLARE v_RutCli 					NVARCHAR(11):='0-0';
	    DECLARE v_MonedaSistema 			NVARCHAR(5000);
	        
		--------- CONTROL DE ERRORES
		--- Controlado
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :v_out_ErroresContador = 0 THEN	 
		 	    v_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt  NVARCHAR(5000);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				v_out_ErroresDetalle = select * from :v_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into v_out_ErroresContador 
				from :v_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)
				v_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		v_out_ErroresContador		:= 0;
		v_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        
      -----------------------------------------------
      -- INSERTAMOS EN TABLAS DEL MODELO DE SAP -----
      -----------------------------------------------

			INSERT INTO "BSCL"."BSCL.Custodia.BD::CUS.Cartera"(
						 "IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","FechaIngresoRegistro","FechaModificacionRegistro","IdEmpresa","FechaOperacion"
						,"FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","IdClienteRelacionado","IdPortafolioRelacionado"
						,"IdEstado","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","IdMovimientoCarteraEnTransito"
						,"IdConcepto","IdTipoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal"
						,"IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo"
						,"TipoCarteraCorredor","TipoCarteraNormativa","FechaCurseInversion","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoInversionEnFactorAjuste","IdClasificacionTipoInstrumento"
						,"IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento"
						,"EsFungible","ValorizaPor","TipoValorizacion","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio"
						,"TIFTasa","ISIN","FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra"
						,"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional"
						,"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdModalidadCumplimiento","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria"
						,"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto","MontoMonedaPrincipal","MontoMonedaSecundaria","IdCarga","IdSecuenciaCarteraMigracion"
						,"CodigoPeriodo","INGGlosa","INGTasaAgente","INGFechaOperacion","INGIdCondicionLiquidacion","INGFechaLiquidacion","INGCantidad","INGValorRescate"
						,"INGPrecioPromedioCompra","INGMontoPrecioPromedioCompra","INGMontoPrecioPromedioMonedaInstrumento","INGMontoPrecioPromedioMonedaAdicional","INGPrecioAPlazo"
						,"INGMontoAPlazo","INGMontoAPlazoMonedaInstrumento","INGMontoAPlazoMonedaAdicional","INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador","INGTasaPuntosMonedaPrincipal"
						,"INGTasaPuntosMonedaSecundaria","INGTicketValorizacion","INGValorizacionConProblemas","INGFechaValorizacionCartera","INGFechaValorizacionAnterior","INGFechaValorizacionActual"
						,"INGFechaValorizacionProximo","INGFechaFactorMonedaInstrumentoAnterior","INGValorFactorMonedaInstrumentoAnterior","INGFechaFactorMonedaInstrumentoActual","INGValorFactorMonedaInstrumentoActual"
						,"INGFechaFactorMonedaInstrumentoProximo","INGValorFactorMonedaInstrumentoProximo","INGFechaFactorMonedaAdicionalAnterior","INGValorFactorMonedaAdicionalAnterior","INGFechaFactorMonedaAdicionalActual"
						,"INGValorFactorMonedaAdicionalActual","INGFechaFactorMonedaAdicionalProximo","INGValorFactorMonedaAdicionalProximo","INGPrecioCompra","INGTasaCompra","INGMontoATasaCompraAnterior"
						,"INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior","INGMontoCompraAnteriorMonedaInstrumento","INGMontoInteresCompraAnteriorMonedaInstrumento","INGMontoReajusteCompraAnteriorMonedaInstrumento"
						,"INGMontoCompraAnteriorMonedaAdicional","INGMontoInteresCompraAnteriorMonedaAdicional","INGMontoReajusteCompraAnteriorMonedaAdicional","INGDurationCompraAnterior","INGConvexidadCompraAnterior"
						,"INGMontoCompraAnteriorPibMas","INGMontoCompraAnteriorPibMasMonedaInstrumento","INGMontoCompraAnteriorPibMasMonedaAdicional","INGMontoCompraAnteriorPibMenos","INGMontoCompraAnteriorPibMenosMonedaInstrumento"
						,"INGMontoCompraAnteriorPibMenosMonedaAdicional","INGMontoATasaCompraActual","INGMontoCompraActualMonedaInstrumento","INGMontoCompraActualMonedaAdicional","INGDurationCompraActual"
						,"INGConvexidadCompraActual","INGMontoInteresCorridoCompraActual","INGMontoReajusteCorridoCompraActual","INGMontoInteresCorridoCompraActualMonedaInstrumento","INGMontoReajusteCorridoCompraActualMonedaInstrumento"
						,"INGMontoInteresCorridoCompraActualMonedaAdicional","INGMontoReajusteCorridoCompraActualMonedaAdicional","INGMontoCompraActualPibMas","INGMontoCompraActualPibMasMonedaInstrumento"
						,"INGMontoCompraActualPibMasMonedaAdicional","INGMontoCompraActualPibMenos","INGMontoCompraActualPibMenosMonedaInstrumento","INGMontoCompraActualPibMenosMonedaAdicional","INGMontoATasaCompraProximo"
						,"INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGMontoCompraProximoMonedaInstrumento","INGMontoInteresCompraProximoMonedaInstrumento","INGMontoReajusteCompraProximoMonedaInstrumento"
						,"INGMontoCompraProximoMonedaAdicional","INGMontoInteresCompraProximoMonedaAdicional","INGMontoReajusteCompraProximoMonedaAdicional","INGDurationCompraProximo","INGConvexidadCompraProximo"
						,"INGFechaPrecioTasaMercadoAnterior","INGFuenteTasaPrecioMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior","INGMontoATasaMercadoAnterior","INGMontoInteresMercadoAnterior"
						,"INGMontoReajusteMercadoAnterior","INGMontoMercadoAnteriorMonedaInstrumento","INGMontoInteresMercadoAnteriorMonedaInstrumento","INGMontoReajusteMercadoAnteriorMonedaInstrumento","INGMontoMercadoAnteriorMonedaAdicional"
						,"INGMontoInteresMercadoAnteriorMonedaAdicional","INGMontoReajusteMercadoAnteriorMonedaAdicional","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGMontoMercadoAnteriorPibMas"
						,"INGMontoMercadoAnteriorPibMasMonedaInstrumento","INGMontoMercadoAnteriorPibMasMonedaAdicional","INGMontoMercadoAnteriorPibMenos","INGMontoMercadoAnteriorPibMenosMonedaInstrumento","INGMontoMercadoAnteriorPibMenosMonedaAdicional"
						,"INGFechaPrecioTasaMercadoActual","INGFuenteTasaPrecioMercadoActual","INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGMontoMercadoActualMonedaInstrumento"
						,"INGMontoMercadoActualMonedaAdicional","INGDurationMercadoActual","INGConvexidadMercadoActual","INGMontoInteresCorridoMercadoActual","INGMontoReajusteCorridoMercadoActual"
						,"INGMontoInteresCorridoMercadoActualMonedaInstrumento","INGMontoReajusteCorridoMercadoActualMonedaInstrumento","INGMontoInteresCorridoMercadoActualMonedaAdicional","INGMontoReajusteCorridoMercadoActualMonedaAdicional"
						,"INGMontoMercadoActualPibMas","INGMontoMercadoActualPibMasMonedaInstrumento","INGMontoMercadoActualPibMasMonedaAdicional","INGMontoMercadoActualPibMenos","INGMontoMercadoActualPibMenosMonedaInstrumento"
						,"INGMontoMercadoActualPibMenosMonedaAdicional","INGFechaPrecioTasaMercadoProximo","INGFuenteTasaPrecioMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo","INGMontoATasaMercadoProximo"
						,"INGMontoInteresMercadoProximo","INGMontoReajusteMercadoProximo","INGMontoMercadoProximoMonedaInstrumento","INGMontoInteresMercadoProximoMonedaInstrumento","INGMontoReajusteMercadoProximoMonedaInstrumento"
						,"INGMontoMercadoProximoMonedaAdicional","INGMontoInteresMercadoProximoMonedaAdicional","INGMontoReajusteMercadoProximoMonedaAdicional","INGDurationMercadoProximo","INGConvexidadMercadoProximo"
						,"INGTasaCorredora","INGFechaTasaCorredora","INGMontoATasaCorredora","INGMontoATasaCorredoraMonedaInstrumento","INGMontoATasaCorredoraMonedaAdicional","INGFechaCorteCupon"
						,"INGNumeroCupon","INGIdMonedaPagoCupon","INGMontoCorteCupon","INGFechaPagoCorteCupon","INGMontoCapitalCorteCupon","INGMontoInteresCorteCupon","INGMontoReajusteCorteCupon"
						,"INGMontoCorteCuponMonedaInstrumento","INGMontoCapitalCorteCuponMonedaInstrumento","INGMontoInteresCorteCuponMonedaInstrumento","INGMontoReajusteCorteCuponMonedaInstrumento","INGMontoCorteCuponMonedaAdicional"
						,"INGMontoCapitalCorteCuponMonedaAdicional","INGMontoInteresCorteCuponMonedaAdicional","INGMontoReajusteCorteCuponMonedaAdicional","INGMontoPresenteTIF","INGMontoInteresTIF","INGMontoReajusteTIF"
						,"INGMontoInteresAcumTIF","INGMontoReajusteAcumTIF","INGMontoPresenteTIFMonedaInstrumento","INGMontoInteresTIFMonedaInstrumento","INGMontoReajusteTIFMonedaInstrumento","INGMontoInteresAcumTIFMonedaInstrumento"
						,"INGMontoReajusteAcumTIFMonedaInstrumento","INGMontoPresenteTIFMonedaAdicional","INGMontoInteresTIFMonedaAdicional","INGMontoReajusteTIFMonedaAdicional","INGMontoInteresAcumTIFMonedaAdicional"
						,"INGMontoReajusteAcumTIFMonedaAdicional","INGMontoMonedaPrincipal","INGMontoMonedaSecundaria","INGMontoDiferido","INGMontoUtilidadDiferida","INGMontoPerdidaDiferida","INGMontoMargen"
						,"INGMontoValorRazonableActivo","INGMontoValorRazonablePasivo","INGMontoValorRazonableNeto","INGTasaValorRazonableActivo","INGTasaValorRazonablePasivo","INGMontoDevengoTipoCambio"
						,"INGMontoDevengoTipoCambioAnterior","INGMontoDiferidoDiario","INGMontoDiferidoAcumulado","INGMontoMatriz","EGRTicketValorizacion","EGRValorizacionConProblemas","EGRFechaFactorMonedaInstrumento"
						,"EGRValorFactorMonedaInstrumento","EGRFechaFactorMonedaAdicional","EGRValorFactorMonedaAdicional","EGRGlosa","EGRFechaOperacion","EGRIdTipoOperacion","EGRIdOperacion","EGRIdMovimientoCarteraEnTransito"
						,"EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion","EGRIdTipoComprobante","EGRFolioComprobante","EGRFechaComprobante","EGRFolioExternoReferencia","EGRIdModulo"
						,"EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente","EGRFechaCurseRetiro","EGRCostoRetiroenFactorAjuste","EGRValorFactorAjusteaFechaRetiro","EGRCantidad"
						,"EGRValorRescate","EGRPrecio","EGRTasa","EGRMonto","EGRMontoMonedaInstrumento","EGRMontoMonedaAdicional","EGRSpread","EGRFechaPrecioTasaMercado","EGRFuenteTasaPrecioMercado"
						,"EGRPrecioMercado","EGRTasaMercado","EGRMontoATasaMercado","EGRMontoMercadoMonedaInstrumento","EGRMontoMercadoMonedaAdicional","EGRSpreadMercado","EGRPrecioAPlazo","EGRMontoAPlazo"
						,"EGRMontoAPlazoMonedaInstrumento","EGRMontoAPlazoMonedaAdicional","EGRMontoPresenteTIF","EGRMontoInteresTIF","EGRMontoReajusteTIF","EGRMontoInteresAcumTIF","EGRMontoReajusteAcumTIF"
						,"EGRMontoPresenteTIFMonedaInstrumento","EGRMontoInteresTIFMonedaInstrumento","EGRMontoReajusteTIFMonedaInstrumento","EGRMontoInteresAcumTIFMonedaInstrumento","EGRMontoReajusteAcumTIFMonedaInstrumento"
						,"EGRMontoPresenteTIFMonedaAdicional","EGRMontoInteresTIFMonedaAdicional","EGRMontoReajusteTIFMonedaAdicional","EGRMontoInteresAcumTIFMonedaAdicional","EGRMontoReajusteAcumTIFMonedaAdicional"
						,"EGRIdSpotAsociado","EGRMontoMonedaPrincipal","EGRMontoMonedaSecundaria","EGRIdMonedaLiquidacion","EGRMontoLiquidacion","EGRMontoLiquidacionMonedaPrincipal","EGRValorTipoCambioSpot"
						,"EGRValorParidad","EGRValorTipoCambioCotizador","EGREsPuntos","EGRTasaPuntosMonedaPrincipal","EGRTasaPuntosMonedaSecundaria","EGRTasaDescuentoAnticipo","EGRTipoCambioAnticipo"
						,"EGRMontoARecibirAnticipo","EGRTipoCambioObservado")
						select 
						 Cartera_Hom."IdCartera",Cartera_Hom."FolioCartera",Cartera_Hom."FolioCarteraRelacionada",Cartera_Hom."FolioGarantiaOrigen",Cartera_Hom."FechaIngresoRegistro",Cartera_Hom."FechaModificacionRegistro",Cartera_Hom."IdEmpresa",Cartera_Hom."FechaOperacion"
						,Cartera_Hom."FechaVencimientoOperacion",Cartera_Hom."PlazoEnDiasOperacion",Cartera_Hom."IdCliente",Cartera_Hom."IdPortafolio",Cartera_Hom."IdCuentaCustodioPortafolio",Cartera_Hom."IdClienteRelacionado",Cartera_Hom."IdPortafolioRelacionado"
						,Cartera_Hom."IdEstado",Cartera_Hom."IdTipoProducto",Cartera_Hom."IdTipoCanalIngreso",Cartera_Hom."IdMonedaSistema",Cartera_Hom."IdMonedaAdicional",Cartera_Hom."FechaFactorMonedaAdicional",Cartera_Hom."ValorFactorMonedaAdicional",Cartera_Hom."IdMovimientoCarteraEnTransito"
						,Cartera_Hom."IdConcepto",Cartera_Hom."IdTipoOperacion",Cartera_Hom."IdOperacion",Cartera_Hom."IdTipoComprobante",Cartera_Hom."FolioComprobante",Cartera_Hom."FechaComprobante",Cartera_Hom."FolioExternoReferencia",Cartera_Hom."IdMesa",Cartera_Hom."IdBolsa",Cartera_Hom."IdSucursal"
						,Cartera_Hom."IdAgente",Cartera_Hom."IdCondicionLiquidacion",Cartera_Hom."FechaLiquidacion",Cartera_Hom."EsIntraday",Cartera_Hom."UsaGarantiaEspecifica",Cartera_Hom."CobraPrimaAlInicio",Cartera_Hom."IdTipoPrestamo",Cartera_Hom."Glosa",Cartera_Hom."IdUsuario",Cartera_Hom."IdModulo"
						,Cartera_Hom."TipoCarteraCorredor",Cartera_Hom."TipoCarteraNormativa",Cartera_Hom."FechaCurseInversion",Cartera_Hom."TipoFactorAjuste",Cartera_Hom."ValorFactorAjusteaFechaCurse",Cartera_Hom."CostoInversionEnFactorAjuste",Cartera_Hom."IdClasificacionTipoInstrumento"
						,Cartera_Hom."IdTipoInstrumento",Cartera_Hom."IdInstrumento",Cartera_Hom."IdInstrumentoAnterior",Cartera_Hom."Nemotecnico",Cartera_Hom."IdEmisor",Cartera_Hom."NemoSVS",Cartera_Hom."IdTipoReajuste",Cartera_Hom."EsPagaderoEn",Cartera_Hom."FechaEmisionInstrumento",Cartera_Hom."FechaVencimientoInstrumento"
						,Cartera_Hom."EsFungible",Cartera_Hom."ValorizaPor",Cartera_Hom."TipoValorizacion",Cartera_Hom."CodigoIFRS",Cartera_Hom."IdBoveda",Cartera_Hom."CuentaBoveda",Cartera_Hom."IdMonedaInstrumento",Cartera_Hom."Tamano",Cartera_Hom."TIFAfectoBeneficio"
						,Cartera_Hom."TIFTasa",Cartera_Hom."ISIN",Cartera_Hom."FechaFactorMonedaInstrumento",Cartera_Hom."ValorFactorMonedaInstrumento",Cartera_Hom."Cantidad",Cartera_Hom."CorteMinimo",Cartera_Hom."ValorRescate",Cartera_Hom."PrecioCompra",Cartera_Hom."TasaCompra",Cartera_Hom."MontoCompra"
						,Cartera_Hom."MontoCompraMonedaInstrumento",Cartera_Hom."MontoCompraMonedaAdicional",Cartera_Hom."TasaAgente",Cartera_Hom."PrecioAPlazo",Cartera_Hom."MontoAPlazo",Cartera_Hom."MontoAPlazoMonedaInstrumento",Cartera_Hom."MontoAPlazoMonedaAdicional"
						,Cartera_Hom."FechaEfectiva",Cartera_Hom."IdPlaza",Cartera_Hom."IdFixing",Cartera_Hom."FechaFixing",Cartera_Hom."HoraFixing",Cartera_Hom."IdModalidadCumplimiento",Cartera_Hom."IdReferenciaMercado",Cartera_Hom."IdMonedaPrincipal",Cartera_Hom."IdMonedaSecundaria"
						,Cartera_Hom."ValorTipoCambioCierre",Cartera_Hom."ValorParidadCierre",Cartera_Hom."ValorTipoCambioCosto",Cartera_Hom."ValorParidadCosto",Cartera_Hom."MontoMonedaPrincipal",Cartera_Hom."MontoMonedaSecundaria",Cartera_Hom."IdCarga",Cartera_Hom."IdSecuenciaCarteraMigracion"
						,Cartera_Hom."CodigoPeriodo",Cartera_Hom."INGGlosa",Cartera_Hom."INGTasaAgente",Cartera_Hom."INGFechaOperacion",Cartera_Hom."INGIdCondicionLiquidacion",Cartera_Hom."INGFechaLiquidacion",Cartera_Hom."INGCantidad",Cartera_Hom."INGValorRescate"
						,Cartera_Hom."INGPrecioPromedioCompra",Cartera_Hom."INGMontoPrecioPromedioCompra",Cartera_Hom."INGMontoPrecioPromedioMonedaInstrumento",Cartera_Hom."INGMontoPrecioPromedioMonedaAdicional",Cartera_Hom."INGPrecioAPlazo"
						,Cartera_Hom."INGMontoAPlazo",Cartera_Hom."INGMontoAPlazoMonedaInstrumento",Cartera_Hom."INGMontoAPlazoMonedaAdicional",Cartera_Hom."INGEsPuntos",Cartera_Hom."INGValorTipoCambioSpot",Cartera_Hom."INGValorTipoCambioCotizador",Cartera_Hom."INGTasaPuntosMonedaPrincipal"
						,Cartera_Hom."INGTasaPuntosMonedaSecundaria",Cartera_Hom."INGTicketValorizacion",Cartera_Hom."INGValorizacionConProblemas",Cartera_Hom."INGFechaValorizacionCartera",Cartera_Hom."INGFechaValorizacionAnterior",Cartera_Hom."INGFechaValorizacionActual"
						,Cartera_Hom."INGFechaValorizacionProximo",Cartera_Hom."INGFechaFactorMonedaInstrumentoAnterior",Cartera_Hom."INGValorFactorMonedaInstrumentoAnterior",Cartera_Hom."INGFechaFactorMonedaInstrumentoActual",Cartera_Hom."INGValorFactorMonedaInstrumentoActual"
						,Cartera_Hom."INGFechaFactorMonedaInstrumentoProximo",Cartera_Hom."INGValorFactorMonedaInstrumentoProximo",Cartera_Hom."INGFechaFactorMonedaAdicionalAnterior",Cartera_Hom."INGValorFactorMonedaAdicionalAnterior",Cartera_Hom."INGFechaFactorMonedaAdicionalActual"
						,Cartera_Hom."INGValorFactorMonedaAdicionalActual",Cartera_Hom."INGFechaFactorMonedaAdicionalProximo",Cartera_Hom."INGValorFactorMonedaAdicionalProximo",Cartera_Hom."INGPrecioCompra",Cartera_Hom."INGTasaCompra",Cartera_Hom."INGMontoATasaCompraAnterior"
						,Cartera_Hom."INGMontoInteresCompraAnterior",Cartera_Hom."INGMontoReajusteCompraAnterior",Cartera_Hom."INGMontoCompraAnteriorMonedaInstrumento",Cartera_Hom."INGMontoInteresCompraAnteriorMonedaInstrumento",Cartera_Hom."INGMontoReajusteCompraAnteriorMonedaInstrumento"
						,Cartera_Hom."INGMontoCompraAnteriorMonedaAdicional",Cartera_Hom."INGMontoInteresCompraAnteriorMonedaAdicional",Cartera_Hom."INGMontoReajusteCompraAnteriorMonedaAdicional",Cartera_Hom."INGDurationCompraAnterior",Cartera_Hom."INGConvexidadCompraAnterior"
						,Cartera_Hom."INGMontoCompraAnteriorPibMas",Cartera_Hom."INGMontoCompraAnteriorPibMasMonedaInstrumento",Cartera_Hom."INGMontoCompraAnteriorPibMasMonedaAdicional",Cartera_Hom."INGMontoCompraAnteriorPibMenos",Cartera_Hom."INGMontoCompraAnteriorPibMenosMonedaInstrumento"
						,Cartera_Hom."INGMontoCompraAnteriorPibMenosMonedaAdicional",Cartera_Hom."INGMontoATasaCompraActual",Cartera_Hom."INGMontoCompraActualMonedaInstrumento",Cartera_Hom."INGMontoCompraActualMonedaAdicional",Cartera_Hom."INGDurationCompraActual"
						,Cartera_Hom."INGConvexidadCompraActual",Cartera_Hom."INGMontoInteresCorridoCompraActual",Cartera_Hom."INGMontoReajusteCorridoCompraActual",Cartera_Hom."INGMontoInteresCorridoCompraActualMonedaInstrumento",Cartera_Hom."INGMontoReajusteCorridoCompraActualMonedaInstrumento"
						,Cartera_Hom."INGMontoInteresCorridoCompraActualMonedaAdicional",Cartera_Hom."INGMontoReajusteCorridoCompraActualMonedaAdicional",Cartera_Hom."INGMontoCompraActualPibMas",Cartera_Hom."INGMontoCompraActualPibMasMonedaInstrumento"
						,Cartera_Hom."INGMontoCompraActualPibMasMonedaAdicional",Cartera_Hom."INGMontoCompraActualPibMenos",Cartera_Hom."INGMontoCompraActualPibMenosMonedaInstrumento",Cartera_Hom."INGMontoCompraActualPibMenosMonedaAdicional",Cartera_Hom."INGMontoATasaCompraProximo"
						,Cartera_Hom."INGMontoInteresCompraProximo",Cartera_Hom."INGMontoReajusteCompraProximo",Cartera_Hom."INGMontoCompraProximoMonedaInstrumento",Cartera_Hom."INGMontoInteresCompraProximoMonedaInstrumento",Cartera_Hom."INGMontoReajusteCompraProximoMonedaInstrumento"
						,Cartera_Hom."INGMontoCompraProximoMonedaAdicional",Cartera_Hom."INGMontoInteresCompraProximoMonedaAdicional",Cartera_Hom."INGMontoReajusteCompraProximoMonedaAdicional",Cartera_Hom."INGDurationCompraProximo",Cartera_Hom."INGConvexidadCompraProximo"
						,Cartera_Hom."INGFechaPrecioTasaMercadoAnterior",Cartera_Hom."INGFuenteTasaPrecioMercadoAnterior",Cartera_Hom."INGPrecioMercadoAnterior",Cartera_Hom."INGTasaMercadoAnterior",Cartera_Hom."INGMontoATasaMercadoAnterior",Cartera_Hom."INGMontoInteresMercadoAnterior"
						,Cartera_Hom."INGMontoReajusteMercadoAnterior",Cartera_Hom."INGMontoMercadoAnteriorMonedaInstrumento",Cartera_Hom."INGMontoInteresMercadoAnteriorMonedaInstrumento",Cartera_Hom."INGMontoReajusteMercadoAnteriorMonedaInstrumento",Cartera_Hom."INGMontoMercadoAnteriorMonedaAdicional"
						,Cartera_Hom."INGMontoInteresMercadoAnteriorMonedaAdicional",Cartera_Hom."INGMontoReajusteMercadoAnteriorMonedaAdicional",Cartera_Hom."INGDurationMercadoAnterior",Cartera_Hom."INGConvexidadMercadoAnterior",Cartera_Hom."INGMontoMercadoAnteriorPibMas"
						,Cartera_Hom."INGMontoMercadoAnteriorPibMasMonedaInstrumento",Cartera_Hom."INGMontoMercadoAnteriorPibMasMonedaAdicional",Cartera_Hom."INGMontoMercadoAnteriorPibMenos",Cartera_Hom."INGMontoMercadoAnteriorPibMenosMonedaInstrumento",Cartera_Hom."INGMontoMercadoAnteriorPibMenosMonedaAdicional"
						,Cartera_Hom."INGFechaPrecioTasaMercadoActual",Cartera_Hom."INGFuenteTasaPrecioMercadoActual",Cartera_Hom."INGPrecioMercadoActual",Cartera_Hom."INGTasaMercadoActual",Cartera_Hom."INGMontoATasaMercadoActual",Cartera_Hom."INGMontoMercadoActualMonedaInstrumento"
						,Cartera_Hom."INGMontoMercadoActualMonedaAdicional",Cartera_Hom."INGDurationMercadoActual",Cartera_Hom."INGConvexidadMercadoActual",Cartera_Hom."INGMontoInteresCorridoMercadoActual",Cartera_Hom."INGMontoReajusteCorridoMercadoActual"
						,Cartera_Hom."INGMontoInteresCorridoMercadoActualMonedaInstrumento",Cartera_Hom."INGMontoReajusteCorridoMercadoActualMonedaInstrumento",Cartera_Hom."INGMontoInteresCorridoMercadoActualMonedaAdicional",Cartera_Hom."INGMontoReajusteCorridoMercadoActualMonedaAdicional"
						,Cartera_Hom."INGMontoMercadoActualPibMas",Cartera_Hom."INGMontoMercadoActualPibMasMonedaInstrumento",Cartera_Hom."INGMontoMercadoActualPibMasMonedaAdicional",Cartera_Hom."INGMontoMercadoActualPibMenos",Cartera_Hom."INGMontoMercadoActualPibMenosMonedaInstrumento"
						,Cartera_Hom."INGMontoMercadoActualPibMenosMonedaAdicional",Cartera_Hom."INGFechaPrecioTasaMercadoProximo",Cartera_Hom."INGFuenteTasaPrecioMercadoProximo",Cartera_Hom."INGPrecioMercadoProximo",Cartera_Hom."INGTasaMercadoProximo",Cartera_Hom."INGMontoATasaMercadoProximo"
						,Cartera_Hom."INGMontoInteresMercadoProximo",Cartera_Hom."INGMontoReajusteMercadoProximo",Cartera_Hom."INGMontoMercadoProximoMonedaInstrumento",Cartera_Hom."INGMontoInteresMercadoProximoMonedaInstrumento",Cartera_Hom."INGMontoReajusteMercadoProximoMonedaInstrumento"
						,Cartera_Hom."INGMontoMercadoProximoMonedaAdicional",Cartera_Hom."INGMontoInteresMercadoProximoMonedaAdicional",Cartera_Hom."INGMontoReajusteMercadoProximoMonedaAdicional",Cartera_Hom."INGDurationMercadoProximo",Cartera_Hom."INGConvexidadMercadoProximo"
						,Cartera_Hom."INGTasaCorredora",Cartera_Hom."INGFechaTasaCorredora",Cartera_Hom."INGMontoATasaCorredora",Cartera_Hom."INGMontoATasaCorredoraMonedaInstrumento",Cartera_Hom."INGMontoATasaCorredoraMonedaAdicional",Cartera_Hom."INGFechaCorteCupon"
						,Cartera_Hom."INGNumeroCupon",Cartera_Hom."INGIdMonedaPagoCupon",Cartera_Hom."INGMontoCorteCupon",Cartera_Hom."INGFechaPagoCorteCupon",Cartera_Hom."INGMontoCapitalCorteCupon",Cartera_Hom."INGMontoInteresCorteCupon",Cartera_Hom."INGMontoReajusteCorteCupon"
						,Cartera_Hom."INGMontoCorteCuponMonedaInstrumento",Cartera_Hom."INGMontoCapitalCorteCuponMonedaInstrumento",Cartera_Hom."INGMontoInteresCorteCuponMonedaInstrumento",Cartera_Hom."INGMontoReajusteCorteCuponMonedaInstrumento",Cartera_Hom."INGMontoCorteCuponMonedaAdicional"
						,Cartera_Hom."INGMontoCapitalCorteCuponMonedaAdicional",Cartera_Hom."INGMontoInteresCorteCuponMonedaAdicional",Cartera_Hom."INGMontoReajusteCorteCuponMonedaAdicional",Cartera_Hom."INGMontoPresenteTIF",Cartera_Hom."INGMontoInteresTIF",Cartera_Hom."INGMontoReajusteTIF"
						,Cartera_Hom."INGMontoInteresAcumTIF",Cartera_Hom."INGMontoReajusteAcumTIF",Cartera_Hom."INGMontoPresenteTIFMonedaInstrumento",Cartera_Hom."INGMontoInteresTIFMonedaInstrumento",Cartera_Hom."INGMontoReajusteTIFMonedaInstrumento",Cartera_Hom."INGMontoInteresAcumTIFMonedaInstrumento"
						,Cartera_Hom."INGMontoReajusteAcumTIFMonedaInstrumento",Cartera_Hom."INGMontoPresenteTIFMonedaAdicional",Cartera_Hom."INGMontoInteresTIFMonedaAdicional",Cartera_Hom."INGMontoReajusteTIFMonedaAdicional",Cartera_Hom."INGMontoInteresAcumTIFMonedaAdicional"
						,Cartera_Hom."INGMontoReajusteAcumTIFMonedaAdicional",Cartera_Hom."INGMontoMonedaPrincipal",Cartera_Hom."INGMontoMonedaSecundaria",Cartera_Hom."INGMontoDiferido",Cartera_Hom."INGMontoUtilidadDiferida",Cartera_Hom."INGMontoPerdidaDiferida",Cartera_Hom."INGMontoMargen"
						,Cartera_Hom."INGMontoValorRazonableActivo",Cartera_Hom."INGMontoValorRazonablePasivo",Cartera_Hom."INGMontoValorRazonableNeto",Cartera_Hom."INGTasaValorRazonableActivo",Cartera_Hom."INGTasaValorRazonablePasivo",Cartera_Hom."INGMontoDevengoTipoCambio"
						,Cartera_Hom."INGMontoDevengoTipoCambioAnterior",Cartera_Hom."INGMontoDiferidoDiario",Cartera_Hom."INGMontoDiferidoAcumulado",Cartera_Hom."INGMontoMatriz",Cartera_Hom."EGRTicketValorizacion",Cartera_Hom."EGRValorizacionConProblemas",Cartera_Hom."EGRFechaFactorMonedaInstrumento"
						,Cartera_Hom."EGRValorFactorMonedaInstrumento",Cartera_Hom."EGRFechaFactorMonedaAdicional",Cartera_Hom."EGRValorFactorMonedaAdicional",Cartera_Hom."EGRGlosa",Cartera_Hom."EGRFechaOperacion",Cartera_Hom."EGRIdTipoOperacion",Cartera_Hom."EGRIdOperacion",Cartera_Hom."EGRIdMovimientoCarteraEnTransito"
						,Cartera_Hom."EGRIdTipoCanalIngreso",Cartera_Hom."EGRIdCondicionLiquidacion",Cartera_Hom."EGRFechaLiquidacion",Cartera_Hom."EGRIdTipoComprobante",Cartera_Hom."EGRFolioComprobante",Cartera_Hom."EGRFechaComprobante",Cartera_Hom."EGRFolioExternoReferencia",Cartera_Hom."EGRIdModulo"
						,Cartera_Hom."EGRIdConcepto",Cartera_Hom."EGRIdUsuario",Cartera_Hom."EGRIdSucursal",Cartera_Hom."EGRIdAgente",Cartera_Hom."EGRFechaCurseRetiro",Cartera_Hom."EGRCostoRetiroenFactorAjuste",Cartera_Hom."EGRValorFactorAjusteaFechaRetiro",Cartera_Hom."EGRCantidad"
						,Cartera_Hom."EGRValorRescate",Cartera_Hom."EGRPrecio",Cartera_Hom."EGRTasa",Cartera_Hom."EGRMonto",Cartera_Hom."EGRMontoMonedaInstrumento",Cartera_Hom."EGRMontoMonedaAdicional",Cartera_Hom."EGRSpread",Cartera_Hom."EGRFechaPrecioTasaMercado",Cartera_Hom."EGRFuenteTasaPrecioMercado"
						,Cartera_Hom."EGRPrecioMercado",Cartera_Hom."EGRTasaMercado",Cartera_Hom."EGRMontoATasaMercado",Cartera_Hom."EGRMontoMercadoMonedaInstrumento",Cartera_Hom."EGRMontoMercadoMonedaAdicional",Cartera_Hom."EGRSpreadMercado",Cartera_Hom."EGRPrecioAPlazo",Cartera_Hom."EGRMontoAPlazo"
						,Cartera_Hom."EGRMontoAPlazoMonedaInstrumento",Cartera_Hom."EGRMontoAPlazoMonedaAdicional",Cartera_Hom."EGRMontoPresenteTIF",Cartera_Hom."EGRMontoInteresTIF",Cartera_Hom."EGRMontoReajusteTIF",Cartera_Hom."EGRMontoInteresAcumTIF",Cartera_Hom."EGRMontoReajusteAcumTIF"
						,Cartera_Hom."EGRMontoPresenteTIFMonedaInstrumento",Cartera_Hom."EGRMontoInteresTIFMonedaInstrumento",Cartera_Hom."EGRMontoReajusteTIFMonedaInstrumento",Cartera_Hom."EGRMontoInteresAcumTIFMonedaInstrumento",Cartera_Hom."EGRMontoReajusteAcumTIFMonedaInstrumento"
						,Cartera_Hom."EGRMontoPresenteTIFMonedaAdicional",Cartera_Hom."EGRMontoInteresTIFMonedaAdicional",Cartera_Hom."EGRMontoReajusteTIFMonedaAdicional",Cartera_Hom."EGRMontoInteresAcumTIFMonedaAdicional",Cartera_Hom."EGRMontoReajusteAcumTIFMonedaAdicional"
						,Cartera_Hom."EGRIdSpotAsociado",Cartera_Hom."EGRMontoMonedaPrincipal",Cartera_Hom."EGRMontoMonedaSecundaria",Cartera_Hom."EGRIdMonedaLiquidacion",Cartera_Hom."EGRMontoLiquidacion",Cartera_Hom."EGRMontoLiquidacionMonedaPrincipal",Cartera_Hom."EGRValorTipoCambioSpot"
						,Cartera_Hom."EGRValorParidad",Cartera_Hom."EGRValorTipoCambioCotizador",Cartera_Hom."EGREsPuntos",Cartera_Hom."EGRTasaPuntosMonedaPrincipal",Cartera_Hom."EGRTasaPuntosMonedaSecundaria",Cartera_Hom."EGRTasaDescuentoAnticipo",Cartera_Hom."EGRTipoCambioAnticipo"
						,Cartera_Hom."EGRMontoARecibirAnticipo",Cartera_Hom."EGRTipoCambioObservado"
				FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera_Hom
				LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.Cartera" AS Cartera_Cus
					ON Cartera_Cus."IdCartera" =  Cartera_Hom."IdCartera"
				WHERE 	Cartera_Cus."IdCartera" IS NULL 
					AND Cartera_Hom."IdCarga"=p_IdCarga
					AND Cartera_Hom."IdClasificacionTipoInstrumento" = 'IF';
					
				INSERT INTO "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"(	
					"IdMovimientoCarteraEnTransito","IdMovimientoCarteraEnTransitoRelacionado","FechaIngresoRegistro","FechaCertificacionRegistro","FechaAnulacionRegistro"
					,"IdUsuarioIngreso","IdUsuarioCertificacion","IdUsuarioAnulacion","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","FolioCustodia"
					,"IdTipoComprobante","FolioComprobante","EsRegistroGeneradoPorSistema","MueveCustodia","PermiteCertificacionPorUsuario","OrigenCustodia","IdCustodiaOrigen"
					,"IdTipoOperacionOrigen","FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia"
					,"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia","IdEmpresa","IdPais","EsNominal","FolioExternoReferencia","IdCliente","IdPortafolio"
					,"IdCuentaCustodioPortafolio","IdEstado","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional"
					,"CodigoIFRS","IdConcepto","IdTipoOperacion","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica"
					,"Glosa","IdUsuario","IdModulo","TipoCarteraCorredor","TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste"
					,"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn"
					,"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano"
					,"IdBoveda","CuentaBoveda","ISIN","FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra"
					,"TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
					)
				SELECT 
				   Cartera_Hom."IdMovimientoCarteraEnTransito",Cartera_Hom."IdMovimientoCarteraEnTransitoRelacionado",Cartera_Hom."FechaIngresoRegistro",Cartera_Hom."FechaCertificacionRegistro",Cartera_Hom."FechaAnulacionRegistro"
					,Cartera_Hom."IdUsuarioIngreso",Cartera_Hom."IdUsuarioCertificacion",Cartera_Hom."IdUsuarioAnulacion",Cartera_Hom."FechaOperacion",Cartera_Hom."FechaVencimientoOperacion",Cartera_Hom."PlazoEnDiasOperacion",Cartera_Hom."FolioCustodia"
					,Cartera_Hom."IdTipoComprobante",Cartera_Hom."FolioComprobante",Cartera_Hom."EsRegistroGeneradoPorSistema",Cartera_Hom."MueveCustodia",Cartera_Hom."PermiteCertificacionPorUsuario",Cartera_Hom."OrigenCustodia",Cartera_Hom."IdCustodiaOrigen"
					,Cartera_Hom."IdTipoOperacionOrigen",Cartera_Hom."FolioCarteraGarantizada",Cartera_Hom."IdInstrumentoCarteraGarantizada",Cartera_Hom."EsEspecifica",Cartera_Hom."IdContraparteGarantia",Cartera_Hom."RazonSocialContraparteGarantia"
					,Cartera_Hom."CuentaContraparteGarantia",Cartera_Hom."IdOrigenGarantia",Cartera_Hom."IdEspecieGarantia",Cartera_Hom."IdEmpresa",Cartera_Hom."IdPais",Cartera_Hom."EsNominal",Cartera_Hom."FolioExternoReferencia",Cartera_Hom."IdCliente",Cartera_Hom."IdPortafolio"
					,Cartera_Hom."IdCuentaCustodioPortafolio",Cartera_Hom."IdEstado",Cartera_Hom."IdTipoCanalIngreso",Cartera_Hom."IdMonedaSistema",Cartera_Hom."IdMonedaAdicional",Cartera_Hom."FechaFactorMonedaAdicional",Cartera_Hom."ValorFactorMonedaAdicional"
					,Cartera_Hom."CodigoIFRS",Cartera_Hom."IdConcepto",Cartera_Hom."IdTipoOperacion",Cartera_Hom."IdMesa",Cartera_Hom."IdBolsa",Cartera_Hom."IdSucursal",Cartera_Hom."IdAgente",Cartera_Hom."IdCondicionLiquidacion",Cartera_Hom."FechaLiquidacion",Cartera_Hom."EsIntraday",Cartera_Hom."UsaGarantiaEspecifica"
					,Cartera_Hom."Glosa",Cartera_Hom."IdUsuario",Cartera_Hom."IdModulo",Cartera_Hom."TipoCarteraCorredor",Cartera_Hom."TipoCarteraNormativa",Cartera_Hom."FechaCurse",Cartera_Hom."TipoFactorAjuste",Cartera_Hom."ValorFactorAjusteaFechaCurse",Cartera_Hom."CostoEnFactorAjuste"
					,Cartera_Hom."IdClasificacionTipoInstrumento",Cartera_Hom."IdTipoInstrumento",Cartera_Hom."IdInstrumento",Cartera_Hom."IdInstrumentoAnterior",Cartera_Hom."Nemotecnico",Cartera_Hom."IdEmisor",Cartera_Hom."NemoSVS",Cartera_Hom."IdTipoReajuste",Cartera_Hom."EsPagaderoEn"
					,Cartera_Hom."FechaEmisionInstrumento",Cartera_Hom."FechaVencimientoInstrumento",Cartera_Hom."EsFungible",Cartera_Hom."ValorizaPor",Cartera_Hom."TipoValorizacion",Cartera_Hom."TieneCorteDeCupon",Cartera_Hom."IdMonedaInstrumento",Cartera_Hom."Tamano"
					,Cartera_Hom."IdBoveda",Cartera_Hom."CuentaBoveda",Cartera_Hom."ISIN",Cartera_Hom."FechaFactorMonedaInstrumento",Cartera_Hom."ValorFactorMonedaInstrumento",Cartera_Hom."Cantidad",Cartera_Hom."CorteMinimo",Cartera_Hom."ValorRescate",Cartera_Hom."PrecioCompra"
					,Cartera_Hom."TasaCompra",Cartera_Hom."MontoCompra",Cartera_Hom."MontoCompraMonedaInstrumento",Cartera_Hom."MontoCompraMonedaAdicional",Cartera_Hom."IdCarga",Cartera_Hom."IdSecuenciaCarteraMigracion",Cartera_Hom."CodigoPeriodo"
			    FROM "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" AS Cartera_Hom
				LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS Cartera_Cus
					ON Cartera_Cus."IdMovimientoCarteraEnTransito" =  Cartera_Hom."IdMovimientoCarteraEnTransito"
				WHERE Cartera_Cus."IdMovimientoCarteraEnTransito" IS NULL 
				 AND Cartera_Hom."IdCarga" = p_IdCarga
				 AND Cartera_Hom."IdClasificacionTipoInstrumento" = 'IF';
 
				--SELECT COUNT(*) FROM "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" WHERE "IdCarga"=p_IdCarga;
				--SELECT COUNT(*) FROM "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" WHERE "IdCarga"=p_IdCarga;
				  
				--SELECT COUNT(*) FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera" WHERE "IdCarga"=p_IdCarga;
				--SELECT COUNT(*) FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" WHERE "IdCarga"=p_IdCarga;

		   SIGNAL SALIDA_CONTROLADA;         

-- ENB 2017-06-22 23:45:00
END;
