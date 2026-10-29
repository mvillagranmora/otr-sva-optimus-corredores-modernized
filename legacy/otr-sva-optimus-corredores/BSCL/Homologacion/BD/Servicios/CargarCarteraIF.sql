--------------------------------------------------------------------------
-- ENB 2017-06-29 14:45:00
--------------------------------------------------------------------------
SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraIF";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraIF"
	(  
			IN  p_IdCarga 	       		BIGINT,          					-- 	ID de la carga 
			IN  p_Modalidad        	 	NVARCHAR(5000),    					-- 	Ingreso I incremental, 
			IN  p_inversa 				NVARCHAR(5000),	  					-- 	S carga cartera inversa perdiodo anterior N no,
			IN  p_RutCliente       	 	NVARCHAR(5000) default '0-0',    	-- 	rut del cliente sebra
			IN  p_Periodo_Inicial     	NVARCHAR(5000)   					--	YYYY-MM-DD
--			OUT p_out_ErroresContador 	int,
--			OUT p_out_ErroresDetalle 	table ("Codigo" NVARCHAR(5000),"Descripcion" NVARCHAR(5000))
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE  p_IdUsuario			BIGINT DEFAULT 66;					-- 	ID del usuario que solicita el servicio
		DECLARE p_IdModulo         	 	NVARCHAR(5000) DEFAULT 'CUS';    	-- 	modulo 
		DECLARE p_IdEmpresa		   		BIGINT DEFAULT 1;					-- 	ID Empresa destino
		
		DECLARE v_out_Resultado  		CHAR(1); 	   						--	RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(5000); 					--	MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			nvarchar(5000);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle 	table ("Codigo" nvarchar(5000),"Descripcion" nvarchar(5000));	
	    DECLARE v_FechaSistema 			NVARCHAR(5000);
	    DECLARE v_MaxPeriodo			NVARCHAR(5000);
	    DECLARE v_FechaInicioPeriodo 	NVARCHAR(5000);
	    DECLARE v_RutCli 				NVARCHAR(5000):='0-0';
	    DECLARE v_MonedaSistema 		NVARCHAR(5000);
	    DECLARE Concepto_Egre 			NVARCHAR(5000) default 'RETIIF';
	    DECLARE Concepto_Ingre 			NVARCHAR(5000) default 'INTIIF';

		DECLARE in_FechaInicialant      NVARCHAR(5000);
		DECLARE	in_out_Contador 		int;
	    DECLARE v_periodo  				DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01');
		DECLARE v_periodo_ant  			DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01');

		--------------------------
		--- CONTROL DE ERRORES ---
		--------------------------
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :v_out_ErroresContador = 0 THEN	 
		 	    v_out_ErroresDetalle  =  select 'INGRESO OK' as "Codigo",'' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt nvarchar(5000);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				v_out_ErroresDetalle = select * from :v_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into v_out_ErroresContador 
				from :v_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log ser? captado por la capa de XS control)
				v_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		
		v_out_ErroresContador		:= 0;
		v_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
				
		SELECT LPAD(p_RutCliente, 11,' ') into v_RutCli FROM DUMMY;
		SELECT  COALESCE("Valor",'') into v_MonedaSistema 
		  FROM  "BSCL.Plataforma.BD::PLA.Configuracion"
		 WHERE  "IdEmpresa" = p_IdEmpresa
		   AND  "CodigoParametro" like  '%MonedaSistema%';
        		
		IF p_Modalidad = 'R' THEN --REPROCESO
			DELETE FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"
			WHERE "IdCarga" = 	p_IdCarga;	

			DELETE FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera"
			WHERE "IdCarga" = 	p_IdCarga;	
 		END IF;
		
---------------------------------------------			
--- CARGA CATERA INVERSA PERIODO ANTERIOR ---
---------------------------------------------
IF p_inversa = 'S' THEN 
	   SELECT COUNT(*) INTO in_out_Contador 
		 FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" 
		WHERE "COD_PER" = SUBSTRING((SELECT ADD_MONTHS (TO_DATE (v_periodo_ant, 'YYYY-MM-DD'), -1) FROM DUMMY),1,7)
		  AND "RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then "RUT_CLI" ELSE p_RutCliente   END
		  AND (GES_TBCTCIIF."CAN_ACTUAL" < 0  or GES_TBCTCIIF."CAN_ACTUAL" > 0);
										 
   IF in_out_Contador > 0 THEN
  			-----------------------------------------
			--  CARTERAS IIF > 0
			-----------------------------------------
			INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"(
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
						,"EGRMontoARecibirAnticipo","EGRTipoCambioObservado","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN"
						,"SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA","SEC_COMPROM")
						
			SELECT
						  "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL 				as "IdCartera"
						, "BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL 			as "FolioCartera"
						, 0 											as "FolioCarteraRelacionada"
						, 0 											as "FolioGarantiaOrigen"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD'))	as "FechaIngresoRegistro"
						, '0001-01-01' 									as "FechaModificacionRegistro"
						, p_IdEmpresa 									as "IdEmpresa" 
						, GES_TBCTCIIF."FEC_INVER" 						as "FechaOperacion"
						, '0001-01-01' 									as "FechaVencimientoOperacion"
						, GES_TBCTCIIF."PLAZO"  						as "PlazoEnDiasOperacion"
						, Cliente."IdCliente" 							as "IdCliente"
						, Portafolio."IdPortafolio" 					as "IdPortafolio"
						, 0 											as "IdCuentaCustodioPortafolio"
						, 0 											as "IdClienteRelacionado"
						, 0 											as "IdPortafolioRelacionado"
						, 'LIQ' 										as "IdEstado"
						, 'INIF' 										as "IdTipoProducto"
						, 'AGE' 										as "IdTipoCanalIngreso"
						, v_MonedaSistema 								as "IdMonedaSistema" 
						,'' 											as "IdMonedaAdicional"
						,'0001-01-01' 									as "FechaFactorMonedaAdicional"
						,'0' 											as "ValorFactorMonedaAdicional"
						, 0 											as "IdMovimientoCarteraEnTransito"
						, Concepto_Ingre 								as "IdConcepto"
						, 'INGTIT' 										as "IdTipoOperacion"
						, 0 											as "IdOperacion"
						, coalesce(TipoComprobante."ValorDestino",'') 	as "IdTipoComprobante"
						, coalesce(ComprobanteFacturacion."IdComprobante",0) 			as "FolioComprobante" 
						, coalesce(ComprobanteFacturacion."FEC_ING_COMP",'0001-01-01')	as "FechaComprobante"
						, 0 											as "FolioExternoReferencia" 
						, 0 											as "IdMesa" 
						, 'BCS' 										as "IdBolsa"
						, coalesce(EmpleadoEjecutivo."IdSucursal",0) 	as "IdSucursal"  
						, Portafolio."IdEmpleadoEjecutivo" 				as "IdAgente" 
						, 'T0' 											as "IdCondicionLiquidacion"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) as "FechaLiquidacion"
						, '' 											as "EsIntraday"
						, '' 											as "UsaGarantiaEspecifica"
						, '' 											as "CobraPrimaAlInicio"
						, '' 											as "IdTipoPrestamo"
						, '' 											as "Glosa"
						, 0  											as "IdUsuario" 
						, p_IdModulo 									as "IdModulo"  
						, '' 											as "TipoCarteraCorredor"
						, '' 											as "TipoCarteraNormativa"
						, '0001-01-01' 									as "FechaCurseInversion"
						, '' 											as "TipoFactorAjuste"
						, 0 											as "ValorFactorAjusteaFechaCurse"
						, 0 											as "CostoInversionEnFactorAjuste"
						, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
						, COALESCE(Instrumento."IdTipoInstrumento",'') 	as "IdTipoInstrumento"
						, COALESCE(Instrumento."IdInstrumento",0) 		as "IdInstrumento" 
						, 0 											as "IdInstrumentoAnterior"
						, TRIM(GES_TBCTCIIF."NEMO")  					as "Nemotecnico"
						, TRIM(GES_TBCTCIIF."EMISOR") 					as "IdEmisor"
						, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 	as "NemoSVS"
						, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
						, '' 											as "EsPagaderoEn"
						, GES_TBCTCIIF."FEC_EMI"  						as "FechaEmisionInstrumento" -- debo calcularla en caso de IF
						, GES_TBCTCIIF."FEC_VCTO"  						as "FechaVencimientoInstrumento" 
						, GES_TBCTCIIF."IND_FUNGIBILIDAD"				as "EsFungible"
						, Instrumento."ValorizaPor" 					as "ValorizaPor"
						, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) 	as "TipoValorizacion"						
						, 0 											as "CodigoIFRS" 
						, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END AS "IdBoveda" 
						, 0 											as "CuentaBoveda" 
						, COALESCE(Instrumento."IdMoneda",'') 			as "IdMonedaInstrumento"
						, 0 											as "Tamano" 
						, '' 											as "TIFAfectoBeneficio"
						, 0 											as "TIFTasa"
						, COALESCE(Instrumento."ISIN",'') 				as "ISIN"
						, '0001-01-01' 									as "FechaFactorMonedaInstrumento"
						, 0 											as "ValorFactorMonedaInstrumento"
						, GES_TBCTCIIF."CAN_ACTUAL" 					as "Cantidad"
						, 0 											as "CorteMinimo" ---- 
						, GES_TBCTCIIF."VAL_RESC" 						as "ValorRescate"
						, 0 											as "PrecioCompra"
						, GES_TBCTCIIF."TASA_PROM_COMP" 				as "TasaCompra"
						, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompra"
						, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompraMonedaInstrumento"
						, 0 											as "MontoCompraMonedaAdicional"
						, 0 											as "TasaAgente"
						, 0 											as "PrecioAPlazo"
						, 0 											as "MontoAPlazo"
						, 0 											as "MontoAPlazoMonedaInstrumento"
						, 0 											as "MontoAPlazoMonedaAdicional"
						, GES_TBCTCIIF."FEC_INVER"  					as "FechaEfectiva" 
						, 0 											as "IdPlaza"
						, 0 											as "IdFixing"
						, '0001-01-01' 									as "FechaFixing"
						, '0:00:00' 									as "HoraFixing"
						, 0 											as "IdModalidadCumplimiento"
						, 0 											as "IdReferenciaMercado"
						, 0 											as "IdMonedaPrincipal"
						, 0 											as "IdMonedaSecundaria"
						, 0 											as "ValorTipoCambioCierre"
						, 0 											as "ValorParidadCierre"
						, 0 											as "ValorTipoCambioCosto"
						, 0 											as "ValorParidadCosto"
						, 0 											as "MontoMonedaPrincipal"
						, 0 											as "MontoMonedaSecundaria"
						, p_IdCarga 									as "IdCarga"     
						, GES_TBCTCIIF."SEC_CAR_INVER" 					as "IdSecuenciaCarteraMigracion"
						, GES_TBCTCIIF."COD_PER" 						as "CodigoPeriodo"
						, '' 											as "INGGlosa"
						, 0 											as "INGTasaAgente"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) as "INGFechaOperacion"
						, 'T0' 											as "INGIdCondicionLiquidacion"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) as "INGFechaLiquidacion"
						, GES_TBCTCIIF."CAN_ACTUAL" 					as "INGCantidad"
						, GES_TBCTCIIF."VAL_RESC" 						as "INGValorRescate"
						, 0 											as "INGPrecioPromedioCompra"
						, 0 											as "INGMontoPrecioPromedioCompra"
						, 0 											as "INGMontoPrecioPromedioMonedaInstrumento"
						, 0 											as "INGMontoPrecioPromedioMonedaAdicional"
						, 0 											as "INGPrecioAPlazo"
						, 0 											as "INGMontoAPlazo"
						, 0 											as "INGMontoAPlazoMonedaInstrumento"
						, 0 											as "INGMontoAPlazoMonedaAdicional"
						, 0 											as "INGEsPuntos"
						, 0 											as "INGValorTipoCambioSpot"
						, 0 											as "INGValorTipoCambioCotizador"
						, 0 											as "INGTasaPuntosMonedaPrincipal"
						, 0 											as "INGTasaPuntosMonedaSecundaria"
						, 0 											as "INGTicketValorizacion"
						, 'N' 											as "INGValorizacionConProblemas"
						, '0001-01-01' 									as "INGFechaValorizacionCartera"
						, '0001-01-01' 									as "INGFechaValorizacionAnterior"
						, '0001-01-01' 									as "INGFechaValorizacionActual"
						, '0001-01-01' 									as "INGFechaValorizacionProximo"
						, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoAnterior"
						, 0 											as "INGValorFactorMonedaInstrumentoAnterior"
						, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoActual"
						, 0 											as "INGValorFactorMonedaInstrumentoActual"
						, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoProximo"
						, 0 											as "INGValorFactorMonedaInstrumentoProximo"
						, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalAnterior"
						, 0 											as "INGValorFactorMonedaAdicionalAnterior"
						, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalActual"
						, 0 											as "INGValorFactorMonedaAdicionalActual"
						, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalProximo"
						, 0 											as "INGValorFactorMonedaAdicionalProximo"
						, 0 											as "INGPrecioCompra"
						, GES_TBCTCIIF."TASA_PROM_COMP" 				as "INGTasaCompra"
						, 0 											as "INGMontoATasaCompraAnterior"
						, 0 											as "INGMontoInteresCompraAnterior"
						, 0 											as "INGMontoReajusteCompraAnterior"
						, 0 											as "INGMontoCompraAnteriorMonedaInstrumento"
						, 0 											as "INGMontoInteresCompraAnteriorMonedaInstrumento"
						, 0 											as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
						, 0 											as "INGMontoCompraAnteriorMonedaAdicional"
						, 0 											as "INGMontoInteresCompraAnteriorMonedaAdicional"
						, 0 											as "INGMontoReajusteCompraAnteriorMonedaAdicional"
						, 0 											as "INGDurationCompraAnterior"
						, 0 											as "INGConvexidadCompraAnterior"
						, 0 											as "INGMontoCompraAnteriorPibMas"
						, 0 											as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
						, 0 											as "INGMontoCompraAnteriorPibMasMonedaAdicional"
						, 0 											as "INGMontoCompraAnteriorPibMenos"
						, 0 											as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
						, 0 											as "INGMontoCompraAnteriorPibMenosMonedaAdicional"
						, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaCompraActual"
						, 0 											as "INGMontoCompraActualMonedaInstrumento"
						, 0 											as "INGMontoCompraActualMonedaAdicional"
						, 0 											as "INGDurationCompraActual"
						, 0 											as "INGConvexidadCompraActual"
						, 0 											as "INGMontoInteresCorridoCompraActual"
						, 0 											as "INGMontoReajusteCorridoCompraActual"
						, 0 											as "INGMontoInteresCorridoCompraActualMonedaInstrumento"
						, 0 											as "INGMontoReajusteCorridoCompraActualMonedaInstrumento"
						, 0 											as "INGMontoInteresCorridoCompraActualMonedaAdicional"
						, 0 											as "INGMontoReajusteCorridoCompraActualMonedaAdicional"
						, 0 											as "INGMontoCompraActualPibMas"
						, 0 											as "INGMontoCompraActualPibMasMonedaInstrumento"
						, 0 											as "INGMontoCompraActualPibMasMonedaAdicional"
						, 0 											as "INGMontoCompraActualPibMenos"
						, 0 											as "INGMontoCompraActualPibMenosMonedaInstrumento"
						, 0 											as "INGMontoCompraActualPibMenosMonedaAdicional"
						, 0 											as "INGMontoATasaCompraProximo"
						, 0 											as "INGMontoInteresCompraProximo"
						, 0 											as "INGMontoReajusteCompraProximo"
						, 0 											as "INGMontoCompraProximoMonedaInstrumento"
						, 0 											as "INGMontoInteresCompraProximoMonedaInstrumento"
						, 0 											as "INGMontoReajusteCompraProximoMonedaInstrumento"
						, 0 											as "INGMontoCompraProximoMonedaAdicional"
						, 0 											as "INGMontoInteresCompraProximoMonedaAdicional"
						, 0 											as "INGMontoReajusteCompraProximoMonedaAdicional"
						, 0 											as "INGDurationCompraProximo"
						, 0 											as "INGConvexidadCompraProximo"
						, '0001-01-01' 									as "INGFechaPrecioTasaMercadoAnterior"
						, 0 											as "INGFuenteTasaPrecioMercadoAnterior"
						, 0 											as "INGPrecioMercadoAnterior"
						, 0 											as "INGTasaMercadoAnterior"
						, 0 											as "INGMontoATasaMercadoAnterior"
						, 0 											as "INGMontoInteresMercadoAnterior"
						, 0 											as "INGMontoReajusteMercadoAnterior"
						, 0 											as "INGMontoMercadoAnteriorMonedaInstrumento"
						, 0 											as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
						, 0 											as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
						, 0 											as "INGMontoMercadoAnteriorMonedaAdicional"
						, 0 											as "INGMontoInteresMercadoAnteriorMonedaAdicional"
						, 0 											as "INGMontoReajusteMercadoAnteriorMonedaAdicional"
						, 0 											as "INGDurationMercadoAnterior"
						, 0 											as "INGConvexidadMercadoAnterior"
						, 0 											as "INGMontoMercadoAnteriorPibMas"
						, 0 											as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
						, 0 											as "INGMontoMercadoAnteriorPibMasMonedaAdicional"
						, 0 											as "INGMontoMercadoAnteriorPibMenos"
						, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
						, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaAdicional"
						, '0001-01-01' 									as "INGFechaPrecioTasaMercadoActual"
						, '' 											as "INGFuenteTasaPrecioMercadoActual"
						, 0												as "INGPrecioMercadoActual"
						, GES_TBCTCIIF."TASA_MED_CIE"					as "INGTasaMercadoActual"
						, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaMercadoActual"
						, 0 											as "INGMontoMercadoActualMonedaInstrumento"
						, 0 											as "INGMontoMercadoActualMonedaAdicional"
						, 0 											as "INGDurationMercadoActual"
						, 0 											as "INGConvexidadMercadoActual"
						, 0 											as	"INGMontoInteresCorridoMercadoActual"
						, 0 											as "INGMontoReajusteCorridoMercadoActual"
						, 0 											as "INGMontoInteresCorridoMercadoActualMonedaInstrumento"
						, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaInstrumento"
						, 0 											as "INGMontoInteresCorridoMercadoActualMonedaAdicional"
						, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaAdicional"
						, 0 											as "INGMontoMercadoActualPibMas"
						, 0 											as "INGMontoMercadoActualPibMasMonedaInstrumento"
						, 0 											as "INGMontoMercadoActualPibMasMonedaAdicional"
						, 0 											as "INGMontoMercadoActualPibMenos"
						, 0 											as "INGMontoMercadoActualPibMenosMonedaInstrumento"
						, 0 											as "INGMontoMercadoActualPibMenosMonedaAdicional"
						, '0001-01-01' 									as "INGFechaPrecioTasaMercadoProximo"
						, 0 											as "INGFuenteTasaPrecioMercadoProximo"
						, 0 											as "INGPrecioMercadoProximo"
						, 0 											as "INGTasaMercadoProximo"
						, 0 											as "INGMontoATasaMercadoProximo"
						, 0 											as "INGMontoInteresMercadoProximo"
						, 0 											as "INGMontoReajusteMercadoProximo"
						, 0 											as "INGMontoMercadoProximoMonedaInstrumento"
						, 0 											as "INGMontoInteresMercadoProximoMonedaInstrumento"
						, 0 											as "INGMontoReajusteMercadoProximoMonedaInstrumento"
						, 0 											as "INGMontoMercadoProximoMonedaAdicional"
						, 0 											as "INGMontoInteresMercadoProximoMonedaAdicional"
						, 0 											as "INGMontoReajusteMercadoProximoMonedaAdicional"
						, 0 											as "INGDurationMercadoProximo"
						, 0 											as "INGConvexidadMercadoProximo"
						, 0 											as "INGTasaCorredora"
						, '0001-01-01' 									as "INGFechaTasaCorredora"
						, 0 											as "INGMontoATasaCorredora"
						, 0 											as "INGMontoATasaCorredoraMonedaInstrumento"
						, 0 											as "INGMontoATasaCorredoraMonedaAdicional"
						, '0001-01-01' 									as "INGFechaCorteCupon"
						, 0 											as "INGNumeroCupon"
						, 0 											as "INGIdMonedaPagoCupon"
						, 0 											as "INGMontoCorteCupon"
						, '0001-01-01' 									as "INGFechaPagoCorteCupon"
						, 0 											as "INGMontoCapitalCorteCupon"
						, 0 											as "INGMontoInteresCorteCupon"
						, 0 											as "INGMontoReajusteCorteCupon"
						, 0 											as "INGMontoCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoCapitalCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoInteresCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoReajusteCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoCorteCuponMonedaAdicional"
						, 0 											as "INGMontoCapitalCorteCuponMonedaAdicional"
						, 0 											as "INGMontoInteresCorteCuponMonedaAdicional"
						, 0 											as "INGMontoReajusteCorteCuponMonedaAdicional"
						, 0 											as "INGMontoPresenteTIF"
						, 0 											as "INGMontoInteresTIF"
						, 0 											as "INGMontoReajusteTIF"
						, 0 											as "INGMontoInteresAcumTIF"
						, 0 											as "INGMontoReajusteAcumTIF"
						, 0 											as "INGMontoPresenteTIFMonedaInstrumento"
						, 0 											as "INGMontoInteresTIFMonedaInstrumento"
						, 0 											as "INGMontoReajusteTIFMonedaInstrumento"
						, 0 											as "INGMontoInteresAcumTIFMonedaInstrumento"
						, 0 											as "INGMontoReajusteAcumTIFMonedaInstrumento"
						, 0 											as "INGMontoPresenteTIFMonedaAdicional"
						, 0 											as "INGMontoInteresTIFMonedaAdicional"
						, 0 											as "INGMontoReajusteTIFMonedaAdicional"
						, 0 											as "INGMontoInteresAcumTIFMonedaAdicional"
						, 0 											as "INGMontoReajusteAcumTIFMonedaAdicional"
						, 0 											as "INGMontoMonedaPrincipal"
						, 0 											as "INGMontoMonedaSecundaria"
						, 0 											as "INGMontoDiferido"
						, 0 											as "INGMontoUtilidadDiferida"
						, 0 											as "INGMontoPerdidaDiferida"
						, 0 											as "INGMontoMargen"
						, 0 											as "INGMontoValorRazonableActivo"
						, 0 											as "INGMontoValorRazonablePasivo"
						, 0 											as "INGMontoValorRazonableNeto"
						, 0 											as "INGTasaValorRazonableActivo"
						, 0 											as "INGTasaValorRazonablePasivo"
						, 0 											as "INGMontoDevengoTipoCambio"
						, 0 											as "INGMontoDevengoTipoCambioAnterior"
						, 0 											as "INGMontoDiferidoDiario"
						, 0 											as "INGMontoDiferidoAcumulado"
						, 0 											as "INGMontoMatriz"
						, 0 											as "EGRTicketValorizacion"
						, ''											as "EGRValorizacionConProblemas"
						, '0001-01-01' 									as "EGRFechaFactorMonedaInstrumento"
						, 0 											as "EGRValorFactorMonedaInstrumento"
						, '0001-01-01' 									as "EGRFechaFactorMonedaAdicional"
						, 0 											as "EGRValorFactorMonedaAdicional"
						, '' 											as "EGRGlosa"
						, ADD_MONTHS(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD'),1) as "EGRFechaOperacion"
						, 'RETTIT' 										as "EGRIdTipoOperacion"
						, 0 											as "EGRIdOperacion"
						, 0 											as "EGRIdMovimientoCarteraEnTransito"
						, 'AGE' 										as "EGRIdTipoCanalIngreso"
						, 'T0'  										as "EGRIdCondicionLiquidacion"
						, ADD_MONTHS(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD'),1) as "EGRFechaLiquidacion"
						, '' 											as "EGRIdTipoComprobante"
						, 0  											as "EGRFolioComprobante"
						, '0001-01-01' 									as "EGRFechaComprobante"
						, 0 											as "EGRFolioExternoReferencia"
						, 'CUS' 										as "EGRIdModulo"
						, Concepto_Egre									as "EGRIdConcepto" -- concepto por el cual ingreso a a la cartera"
						, 0 											as "EGRIdUsuario"
						, coalesce(EmpleadoEjecutivo."IdSucursal",0) 	as "EGRIdSucursal"
						, Portafolio."IdEmpleadoEjecutivo" 				as "EGRIdAgente"
						, '0001-01-01' 									as "EGRFechaCurseRetiro"
						, 0 											as "EGRCostoRetiroenFactorAjuste"
						, 0 											as "EGRValorFactorAjusteaFechaRetiro"
						, GES_TBCTCIIF."CAN_ACTUAL"						as "EGRCantidad"
						, GES_TBCTCIIF."VAL_RESC"						as "EGRValorRescate" 
						, 0 											as "EGRPrecio" --el mismo precio del ingreso"
						, 0 											as "EGRTasa" -- la misma tasa del ingreso"
						, 0 											as "EGRMonto" -- el mismo monto del ingreso"
						, 0 											as "EGRMontoMonedaInstrumento" -- podria ser el mismo monto "
						, 0 											as "EGRMontoMonedaAdicional"
						, 0 											as "EGRSpread" -- revisar si existe este campo"
						, '0001-01-01' 									as "EGRFechaPrecioTasaMercado"
						, ''											as "EGRFuenteTasaPrecioMercado"
						, 0 											as "EGRPrecioMercado"
						, 0 											as "EGRTasaMercado"
						, 0 											as "EGRMontoATasaMercado"
						, 0 											as "EGRMontoMercadoMonedaInstrumento"
						, 0 											as "EGRMontoMercadoMonedaAdicional"
						, 0 											as "EGRSpreadMercado"
						, 0 											as "EGRPrecioAPlazo"
						, 0 											as "EGRMontoAPlazo"
						, 0 											as "EGRMontoAPlazoMonedaInstrumento"
						, 0 											as "EGRMontoAPlazoMonedaAdicional"
						, 0 											as "EGRMontoPresenteTIF"
						, 0 											as "EGRMontoInteresTIF"
						, 0 											as "EGRMontoReajusteTIF"
						, 0 											as "EGRMontoInteresAcumTIF"
						, 0 											as "EGRMontoReajusteAcumTIF"
						, 0 											as "EGRMontoPresenteTIFMonedaInstrumento"
						, 0 											as "EGRMontoInteresTIFMonedaInstrumento"
						, 0 											as "EGRMontoReajusteTIFMonedaInstrumento"
						, 0 											as "EGRMontoInteresAcumTIFMonedaInstrumento"
						, 0 											as "EGRMontoReajusteAcumTIFMonedaInstrumento"
						, 0 											as "EGRMontoPresenteTIFMonedaAdicional"
						, 0 											as "EGRMontoInteresTIFMonedaAdicional"
						, 0 											as "EGRMontoReajusteTIFMonedaAdicional"
						, 0 											as "EGRMontoInteresAcumTIFMonedaAdicional"
						, 0 											as "EGRMontoReajusteAcumTIFMonedaAdicional"
						, 0 											as "EGRIdSpotAsociado"
						, 0 											as "EGRMontoMonedaPrincipal"
						, 0 											as "EGRMontoMonedaSecundaria"
						, 0 											as "EGRIdMonedaLiquidacion"
						, 0 											as "EGRMontoLiquidacion"
						, 0 											as "EGRMontoLiquidacionMonedaPrincipal"
						, 0 											as "EGRValorTipoCambioSpot"
						, 0 											as "EGRValorParidad"
						, 0 											as "EGRValorTipoCambioCotizador"
						, 0 											as "EGREsPuntos"
						, 0 											as "EGRTasaPuntosMonedaPrincipal"
						, 0 											as "EGRTasaPuntosMonedaSecundaria"
						, 0 											as "EGRTasaDescuentoAnticipo"
						, 0 											as "EGRTipoCambioAnticipo"
						, 0 											as "EGRMontoARecibirAnticipo"
						, 0 											as "EGRTipoCambioObservado"
						, GES_TBCTCIIF."RUT_CLI" 						as "RUT_CLI"
						, GES_TBCTCIIF."SEC_RUT_CLI" 					as "SEC_RUT_CLI"
						, GES_TBCTCIIF."SEC_CAR_INVER" 					as "SEC_CAR_INVER"
						, GES_TBCTCIIF."COD_PER" 						as "COD_PER"
						, GES_TBCTCIIF."FOLIO_COMP_ADJ" 				as "FOLIO_COMP_ADJ"
						, GES_TBCTCIIF."TIPO_COMP_ADJ" 					as "TIPO_COMP_ADJ"
						, 0 											as "LIN_COMP_ADJ"
						, 0 											as "LIN_ORDEN"
						, 0 											as "SEC_ORDEN"
						, GES_TBCTCIIF."TIPO_CARTERA" 					as "TIPO_CARTERA"
						, 0 											as "ID_ERROR"
						, 0 											as "SEC_MOVTO"
						, 0 											as "CAN_GARANTIA"
						, 0 											as "SEC_COMPROM"

  		FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
  		INNER JOIN 
       		(SELECT A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
              , SUM(A."CAN_POSITIVO") As "CAN_POS"
              , SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
              , SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
              , SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
          		FROM 
               (SELECT 	PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR", 
						PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
     		      FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
			     WHERE 	PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      		       AND 	PO."CAN_ACTUAL" > 0
      		       
 	             UNION  ALL
       
                SELECT 	NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",   
                		0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
     	          FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
		         WHERE 	NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
				   AND 	NE."CAN_ACTUAL" < 0 
				) AS A	
			GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
			HAVING (SUM(A."CAN_POSITIVO") > 0 AND SUM(A."CAN_NEGATIVO") = 0)
			ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
			) AS CARCLI
 			ON  CARCLI."COD_PER" 			= GES_TBCTCIIF."COD_PER"
			AND CARCLI."RUT_CLI" 			= GES_TBCTCIIF."RUT_CLI" 
			AND CARCLI."SEC_RUT_CLI" 		= GES_TBCTCIIF."SEC_RUT_CLI"
			AND TRIM(CARCLI."NEMO")			= TRIM(GES_TBCTCIIF."NEMO")
			AND TRIM(CARCLI."TIPO_REAJ")	= TRIM(GES_TBCTCIIF."TIPO_REAJ")
			AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
			AND CARCLI."FEC_VCTO" 			= GES_TBCTCIIF."FEC_VCTO"
		
			INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
					ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
					ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
					ON 	Cliente."IdPersona" = Persona."IdPersona"
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
					ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
						Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
			LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
					ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
			LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
					ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
			LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
					ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
					   Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
					   Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
					   Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
			LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
					ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
			LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
					ON  TipoComprobante."TipoValor"  ='TipoComprobante'
						AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
			LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
					ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
						AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
			LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
					ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"

	 		WHERE GES_TBCTCIIF."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
 		 		AND GES_TBCTCIIF."COD_PER" NOT LIKE '%90%'
		 		AND GES_TBCTCIIF."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
		 		AND Cartera."RUT_CLI" IS NULL
		 		AND GES_TBCTCIIF."CAN_ACTUAL" > 0;

			----------------------------------------------------------------
			--  CARTERAS IIF < 0
			----------------------------------------------------------------
			INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito"(	
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
					,"RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR"
					,"SEC_MOVTO","CAN_GARANTIA"
					)
			select
					 "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL 				AS "IdMovimientoCarteraEnTransito"
					,0 																AS "IdMovimientoCarteraEnTransitoRelacionado"
					,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaIngresoRegistro"
					,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaCertificacionRegistro"
					,'0001-01-01' 													AS "FechaAnulacionRegistro"
					, 0 															AS "IdUsuarioIngreso"
					, 0 															AS "IdUsuarioCertificacion"
					, 0 															AS "IdUsuarioAnulacion"
					, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaOperacion"
					, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaOperacion"
					, GES_TBCTCIIF."PLAZO"  										AS "PlazoEnDiasOperacion"
					, 0 															AS "FolioCustodia"
					, coalesce(TipoComprobante."ValorDestino",'') 					AS "IdTipoComprobante"
					, GES_TBCTCIIF.FOLIO_COMP_ADJ 									as "FolioComprobante"
					, 'N' 															AS "EsRegistroGeneradoPorSistema"-- no lo se
					, 'S' 															as "MueveCustodia"
					, 'N' 															as "PermiteCertificacionPorUsuario"
					, 'CUS' 														AS "OrigenCustodia"
					, 0 															AS "IdCustodiaOrigen"
					, 'RETTIT' 														AS "IdTipoOperacionOrigen"
					, 0 															AS "FolioCarteraGarantizada"
					, 0 															AS "IdInstrumentoCarteraGarantizada"
					, 'N' 															AS "EsEspecifica"
					, 0 															AS "IdContraparteGarantia" ---si la operacion fuera de garantia y estuviera en transito
					, '' 															AS "RazonSocialContraparteGarantia" -- si la operacion fuera de garantia
					, '' 															AS "CuentaContraparteGarantia" -- si la operacion fuera de garantia
					, 0 															AS "IdOrigenGarantia" 
					, 0 															AS "IdEspecieGarantia"
					, p_IdEmpresa 													AS "IdEmpresa"
					, 0 															AS "IdPais"-- DEBERIA SER CHILE O EL PAIS DEL INSTRUMENTO
					, 'N' 															AS "EsNominal" -- depende de la operacion no tengo acceso
					, 0 															AS "FolioExternoReferencia"
					, Cliente."IdCliente" 											AS "IdCliente"
					, Portafolio."IdPortafolio" 									AS "IdPortafolio"
					, 0 															as "IdCuentaCustodioPortafolio"
					, 'CRT' 														AS "IdEstado"
					, 'AGE' 														as "IdTipoCanalIngreso"
					, v_MonedaSistema 												AS "IdMonedaSistema"
					, ''  															AS "IdMonedaAdicional"
					, '0001-01-01' 													as "FechaFactorMonedaAdicional"
					, 0 															as "ValorFactorMonedaAdicional"
					, 0 															AS "CodigoIFRS" 
					, Concepto_Egre 												AS "IdConcepto"
					, 'RETTIT' 														AS "IdTipoOperacion"
					, 0 															as "IdMesa"
					, '' 															AS "IdBolsa" -- debo encontrar como sacarla"
					, coalesce(EmpleadoEjecutivo."IdSucursal",0) 					as "IdSucursal" -- lo estamos sacndo del ejecutivo del portafolio
					, Portafolio."IdEmpleadoEjecutivo" 								as "IdAgente"-- lo sacamos del ejecutivo del portafolio
					, 'T0' 															AS "IdCondicionLiquidacion"
					, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaLiquidacion"
					, 'N' 															as "EsIntraday"
					, 'N' 															AS "UsaGarantiaEspecifica"
					, '' 															AS "Glosa"
					, 0 															AS "IdUsuario"
					, p_IdModulo 													AS "IdModulo"
					, '' 															As "TipoCarteraCorredor"
					, '' 															AS "TipoCarteraNormativa"
					, '0001-01-01' 													AS "FechaCurse"
					, '' 															AS "TipoFactorAjuste"
					, 0 															AS "ValorFactorAjusteaFechaCurse"
					, 0 															AS "CostoEnFactorAjuste"
					, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
					, COALESCE(Instrumento."IdTipoInstrumento",'') 					as "IdTipoInstrumento"
					, COALESCE(Instrumento."IdInstrumento",0) 						as "IdInstrumento"
					, 0 															as "IdInstrumentoAnterior"
					, TRIM(GES_TBCTCIIF."NEMO")  									AS "Nemotecnico"
					, TRIM(GES_TBCTCIIF."EMISOR") 									as "IdEmisor"
					, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 					AS "NemoSVS" -- 
					, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
					,'' 															as "EsPagaderoEn"
					, GES_TBCTCIIF."FEC_EMI"  										as "FechaEmisionInstrumento"
					, GES_TBCTCIIF."FEC_VCTO"  										as "FechaVencimientoInstrumento"
					, GES_TBCTCIIF."IND_FUNGIBILIDAD"								AS "EsFungible"
					, Instrumento."ValorizaPor" 									AS "ValorizaPor"
					, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) AS "TipoValorizacion"
					,'N' 															as "TieneCorteDeCupon"
					, COALESCE(Instrumento."IdMoneda",'') 							as "IdMonedaInstrumento"
					, 0 															AS "Tamano"
					, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END AS "IdBoveda"
					, 0 															AS "CuentaBoveda"
					, COALESCE(Instrumento."ISIN",'0') 								as "ISIN"
					, '0001-01-01' 													as "FechaFactorMonedaInstrumento"
					, 0																as "ValorFactorMonedaInstrumento"
					, ABS(GES_TBCTCIIF."CAN_ACTUAL") 								AS "Cantidad" -- la cantidad quedo en negativa ????"
					, 0 															AS "CorteMinimo"
					, GES_TBCTCIIF."VAL_RESC" 										AS "ValorRescate"
					, 0 															AS "PrecioCompra"
					, GES_TBCTCIIF."TASA_PROM_COMP" 								as "TasaCompra"
					, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompra"
					, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompraMonedaInstrumento"
					, 0 															as "MontoCompraMonedaAdicional"
					, p_IdCarga 													AS "IdCarga"
					, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdSecuenciaCarteraMigracion"
					, GES_TBCTCIIF."COD_PER" 										AS "CodigoPeriodo"
					, GES_TBCTCIIF."RUT_CLI" 										AS "RUT_CLI"
					, GES_TBCTCIIF."SEC_RUT_CLI" 									AS "SEC_RUT_CLI"
					, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "SEC_CAR_INVER"
					, GES_TBCTCIIF."COD_PER" 										AS "COD_PER"
					, GES_TBCTCIIF."FOLIO_COMP_ADJ" 								AS "FOLIO_COMP_ADJ"
					, GES_TBCTCIIF."TIPO_COMP_ADJ" 									AS "TIPO_COMP_ADJ"
					, 0 															AS "LIN_COMP_ADJ"
					, 0 															AS "LIN_ORDEN"
					, 0 															AS "SEC_ORDEN"
					, GES_TBCTCIIF."TIPO_CARTERA" 									as "TIPO_CARTERA"
					, 0 															AS "ID_ERROR"
					, 0 															AS "SEC_MOVTO"
					, 0 															AS "CAN_GARANTIA"
			FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
				INNER JOIN 
					(SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
							, SUM(A."CAN_POSITIVO") As "CAN_POS"
							, SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
							, SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
							, SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
					FROM 
						(SELECT 	PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR", 
									PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
						FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
						WHERE 	PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
							AND 	PO."CAN_ACTUAL" > 0
						UNION  ALL
						SELECT 	NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",   
									0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
						FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
						WHERE 	NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
							AND 	NE."CAN_ACTUAL" < 0
						) AS A	
					GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
					HAVING (SUM(A."CAN_POSITIVO") = 0 AND SUM(A."CAN_NEGATIVO") < 0)
					ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
					) AS CARCLI
				ON  CARCLI."COD_PER" 		= GES_TBCTCIIF."COD_PER"
				AND CARCLI."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 
				AND CARCLI."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI"
				AND TRIM(CARCLI."NEMO")		= TRIM(GES_TBCTCIIF."NEMO")
				AND TRIM(CARCLI."TIPO_REAJ")= TRIM(GES_TBCTCIIF."TIPO_REAJ")
				AND TRIM(CARCLI."EMISOR")	= TRIM(GES_TBCTCIIF."EMISOR")
				AND CARCLI."FEC_VCTO" 		= GES_TBCTCIIF."FEC_VCTO"
				
			INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
					ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
					ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
					ON 	Cliente."IdPersona" = Persona."IdPersona"
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
					ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
						Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
			LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
					ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
			LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
					ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
			LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
					ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
					   Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
					   Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
					   Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
			LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
					ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
			LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
					ON  TipoComprobante."TipoValor"  ='TipoComprobante'
						AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
			LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
					ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
						AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
			LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
					ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"

			WHERE GES_TBCTCIIF."COD_PER" 	= SUBSTRING(p_Periodo_Inicial,1,7)
			  AND GES_TBCTCIIF."COD_PER" 	NOT LIKE '%90%'
		 	  AND GES_TBCTCIIF."RUT_CLI" 	=  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
		 	  AND Cartera."RUT_CLI" 		IS NULL
		 	  AND GES_TBCTCIIF."CAN_ACTUAL" < 0;

			----------------------------------------------------------------
			--  CARTERA IIF acumulada > 0     							----
			----------------------------------------------------------------
			INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"(
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
						,"EGRMontoARecibirAnticipo","EGRTipoCambioObservado","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN"
						,"SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA","SEC_COMPROM")
			SELECT
						  "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL 			as "IdCartera"
						, "BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL 		as "FolioCartera"
						, 0 											as "FolioCarteraRelacionada"
						, 0 											as "FolioGarantiaOrigen"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) as "FechaIngresoRegistro"
						, '0001-01-01' 									as "FechaModificacionRegistro"
						, p_IdEmpresa 									as "IdEmpresa" 
						, GES_TBCTCIIF."FEC_INVER" 						as "FechaOperacion"
						, '0001-01-01' 									as "FechaVencimientoOperacion"
						, GES_TBCTCIIF."PLAZO" 							as "PlazoEnDiasOperacion"
						, Cliente."IdCliente" 							as "IdCliente"
						, Portafolio."IdPortafolio" 					as "IdPortafolio"
						, 0 											as "IdCuentaCustodioPortafolio"
						, 0 											as "IdClienteRelacionado"
						, 0 											as "IdPortafolioRelacionado"
						, 'LIQ' 										as "IdEstado"
						, 'INIF' 										as "IdTipoProducto"
						, 'AGE' 										as "IdTipoCanalIngreso"
						, v_MonedaSistema 								as "IdMonedaSistema" 
						,'' 											as "IdMonedaAdicional"
						,'0001-01-01' 									as "FechaFactorMonedaAdicional"
						,'0' 											as "ValorFactorMonedaAdicional"
						, 0 											as "IdMovimientoCarteraEnTransito"
						, Concepto_Ingre 								as "IdConcepto"
						, 'INGTIT' 										as "IdTipoOperacion"
						, 0 											as "IdOperacion"
						, coalesce(TipoComprobante."ValorDestino",'') 	as "IdTipoComprobante"
						, coalesce(ComprobanteFacturacion."IdComprobante",0) 			as "FolioComprobante" 
						, coalesce(ComprobanteFacturacion."FEC_ING_COMP",'0001-01-01')	as "FechaComprobante"
						, 0 											as "FolioExternoReferencia" 
						, 0 											as "IdMesa" 
						, 'BCS' 										as "IdBolsa"
						, coalesce(EmpleadoEjecutivo."IdSucursal",0) 	as "IdSucursal"  
						, Portafolio."IdEmpleadoEjecutivo" 				as "IdAgente" 
						, 'T0' 											as "IdCondicionLiquidacion"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "FechaLiquidacion"
						, '' 											as "EsIntraday"
						, '' 											as "UsaGarantiaEspecifica"
						, '' 											as "CobraPrimaAlInicio"
						, '' 											as "IdTipoPrestamo"
						, '' 											as "Glosa"
						, 0  											as "IdUsuario" 
						, p_IdModulo 									as "IdModulo"  
						, '' 											as "TipoCarteraCorredor"
						, '' 											as "TipoCarteraNormativa"
						, '0001-01-01' 									as "FechaCurseInversion"
						, '' 											as "TipoFactorAjuste"
						, 0 											as "ValorFactorAjusteaFechaCurse"
						, 0 											as "CostoInversionEnFactorAjuste"
						, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
						, COALESCE(Instrumento."IdTipoInstrumento",'') 	as "IdTipoInstrumento"
						, COALESCE(Instrumento."IdInstrumento",0) 		as "IdInstrumento" 
						, 0 											as "IdInstrumentoAnterior"
						, TRIM(GES_TBCTCIIF."NEMO")  					as "Nemotecnico"
						, TRIM(GES_TBCTCIIF."EMISOR") 					as "IdEmisor"
						, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 	as "NemoSVS" -- 
						, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
						, '' 											as "EsPagaderoEn"
						, GES_TBCTCIIF."FEC_EMI"  						as "FechaEmisionInstrumento" -- debo calcularla en caso de IF
						, GES_TBCTCIIF."FEC_VCTO"  						as "FechaVencimientoInstrumento" 
						, GES_TBCTCIIF."IND_FUNGIBILIDAD" 				as "EsFungible"
						, Instrumento."ValorizaPor" 					as "ValorizaPor"
						, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) 	as "TipoValorizacion"						
						, 0 											as "CodigoIFRS" 
						, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END 	AS "IdBoveda" 
						, 0 											as "CuentaBoveda" 
						, COALESCE(Instrumento."IdMoneda",'') 			as "IdMonedaInstrumento"
						, 0 											as "Tamano" 
						, '' 											as "TIFAfectoBeneficio"
						, 0 											as "TIFTasa"
						, COALESCE(Instrumento."ISIN",'') 				as "ISIN"
						, '0001-01-01' 									as "FechaFactorMonedaInstrumento"
						, 0 											as "ValorFactorMonedaInstrumento"
						, CARCLI."CAN_RES"								as "Cantidad"
						, 0 											as "CorteMinimo" 
						, GES_TBCTCIIF."VAL_RESC" 						as "ValorRescate"
						, 0 											as "PrecioCompra"
						, GES_TBCTCIIF."TASA_PROM_COMP" 				as "TasaCompra"
						, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompra"
						, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompraMonedaInstrumento"
						, 0 											as "MontoCompraMonedaAdicional"
						, 0 											as "TasaAgente"
						, 0 											as "PrecioAPlazo"
						, 0 											as "MontoAPlazo"
						, 0 											as "MontoAPlazoMonedaInstrumento"
						, 0 											as "MontoAPlazoMonedaAdicional"
						, GES_TBCTCIIF."FEC_INVER"  					as "FechaEfectiva" 
						, 0 											as "IdPlaza"
						, 0 											as "IdFixing"
						, '0001-01-01' 									as "FechaFixing"
						, '0:00:00' 									as "HoraFixing"
						, 0 											as "IdModalidadCumplimiento"
						, 0 											as "IdReferenciaMercado"
						, 0 											as "IdMonedaPrincipal"
						, 0 											as "IdMonedaSecundaria"
						, 0 											as "ValorTipoCambioCierre"
						, 0 											as "ValorParidadCierre"
						, 0 											as "ValorTipoCambioCosto"
						, 0 											as "ValorParidadCosto"
						, 0 											as "MontoMonedaPrincipal"
						, 0 											as "MontoMonedaSecundaria"
						, p_IdCarga 									as "IdCarga"     
						, GES_TBCTCIIF."SEC_CAR_INVER" 					as "IdSecuenciaCarteraMigracion"
						, GES_TBCTCIIF."COD_PER" 						as "CodigoPeriodo"
						, '' 											as "INGGlosa"
						, 0 											as "INGTasaAgente"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "INGFechaOperacion"
						, 'T0' 											as "INGIdCondicionLiquidacion"
						, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "INGFechaLiquidacion"
						, CARCLI."CAN_RES"  							as "INGCantidad"
						, CARCLI."VAL_RES" 								as "INGValorRescate"
						, 0 											as "INGPrecioPromedioCompra"
						, 0 											as "INGMontoPrecioPromedioCompra"
						, 0 											as "INGMontoPrecioPromedioMonedaInstrumento"
						, 0 											as "INGMontoPrecioPromedioMonedaAdicional"
						, 0 											as "INGPrecioAPlazo"
						, 0 											as "INGMontoAPlazo"
						, 0 											as "INGMontoAPlazoMonedaInstrumento"
						, 0 											as "INGMontoAPlazoMonedaAdicional"
						, 0 											as "INGEsPuntos"
						, 0 											as "INGValorTipoCambioSpot"
						, 0 											as "INGValorTipoCambioCotizador"
						, 0 											as "INGTasaPuntosMonedaPrincipal"
						, 0 											as "INGTasaPuntosMonedaSecundaria"
						, 0 											as "INGTicketValorizacion"
						, 'N' 											as "INGValorizacionConProblemas"
						, '0001-01-01' 									as "INGFechaValorizacionCartera"
						, '0001-01-01' 									as "INGFechaValorizacionAnterior"
						, '0001-01-01' 									as "INGFechaValorizacionActual"
						, '0001-01-01' 									as "INGFechaValorizacionProximo"
						, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoAnterior"
						, 0 											as "INGValorFactorMonedaInstrumentoAnterior"
						, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoActual"
						, 0 											as "INGValorFactorMonedaInstrumentoActual"
						, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoProximo"
						, 0 											as "INGValorFactorMonedaInstrumentoProximo"
						, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalAnterior"
						, 0 											as "INGValorFactorMonedaAdicionalAnterior"
						, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalActual"
						, 0 											as "INGValorFactorMonedaAdicionalActual"
						, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalProximo"
						, 0 											as "INGValorFactorMonedaAdicionalProximo"
						, 0 											as "INGPrecioCompra"
						, GES_TBCTCIIF."TASA_PROM_COMP" 				as "INGTasaCompra"
						, 0 											as "INGMontoATasaCompraAnterior"
						, 0 											as "INGMontoInteresCompraAnterior"
						, 0 											as "INGMontoReajusteCompraAnterior"
						, 0 											as "INGMontoCompraAnteriorMonedaInstrumento"
						, 0 											as "INGMontoInteresCompraAnteriorMonedaInstrumento"
						, 0 											as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
						, 0 											as "INGMontoCompraAnteriorMonedaAdicional"
						, 0 											as "INGMontoInteresCompraAnteriorMonedaAdicional"
						, 0 											as "INGMontoReajusteCompraAnteriorMonedaAdicional"
						, 0 											as "INGDurationCompraAnterior"
						, 0 											as "INGConvexidadCompraAnterior"
						, 0 											as "INGMontoCompraAnteriorPibMas"
						, 0 											as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
						, 0 											as "INGMontoCompraAnteriorPibMasMonedaAdicional"
						, 0 											as "INGMontoCompraAnteriorPibMenos"
						, 0 											as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
						, 0 											as "INGMontoCompraAnteriorPibMenosMonedaAdicional"
						, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaCompraActual"
						, 0 											as "INGMontoCompraActualMonedaInstrumento"
						, 0 											as "INGMontoCompraActualMonedaAdicional"
						, 0 											as "INGDurationCompraActual"
						, 0 											as "INGConvexidadCompraActual"
						, 0 											as "INGMontoInteresCorridoCompraActual"
						, 0 											as "INGMontoReajusteCorridoCompraActual"
						, 0 											as "INGMontoInteresCorridoCompraActualMonedaInstrumento"
						, 0 											as "INGMontoReajusteCorridoCompraActualMonedaInstrumento"
						, 0 											as "INGMontoInteresCorridoCompraActualMonedaAdicional"
						, 0 											as "INGMontoReajusteCorridoCompraActualMonedaAdicional"
						, 0 											as "INGMontoCompraActualPibMas"
						, 0 											as "INGMontoCompraActualPibMasMonedaInstrumento"
						, 0 											as "INGMontoCompraActualPibMasMonedaAdicional"
						, 0 											as "INGMontoCompraActualPibMenos"
						, 0 											as "INGMontoCompraActualPibMenosMonedaInstrumento"
						, 0 											as "INGMontoCompraActualPibMenosMonedaAdicional"
						, 0 											as "INGMontoATasaCompraProximo"
						, 0 											as "INGMontoInteresCompraProximo"
						, 0 											as "INGMontoReajusteCompraProximo"
						, 0 											as "INGMontoCompraProximoMonedaInstrumento"
						, 0 											as "INGMontoInteresCompraProximoMonedaInstrumento"
						, 0 											as "INGMontoReajusteCompraProximoMonedaInstrumento"
						, 0 											as "INGMontoCompraProximoMonedaAdicional"
						, 0 											as "INGMontoInteresCompraProximoMonedaAdicional"
						, 0 											as "INGMontoReajusteCompraProximoMonedaAdicional"
						, 0 											as "INGDurationCompraProximo"
						, 0 											as "INGConvexidadCompraProximo"
						, '0001-01-01' 									as "INGFechaPrecioTasaMercadoAnterior"
						, 0 											as "INGFuenteTasaPrecioMercadoAnterior"
						, 0 											as "INGPrecioMercadoAnterior"
						, 0 											as "INGTasaMercadoAnterior"
						, 0 											as "INGMontoATasaMercadoAnterior"
						, 0 											as "INGMontoInteresMercadoAnterior"
						, 0 											as "INGMontoReajusteMercadoAnterior"
						, 0 											as "INGMontoMercadoAnteriorMonedaInstrumento"
						, 0 											as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
						, 0 											as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
						, 0 											as "INGMontoMercadoAnteriorMonedaAdicional"
						, 0 											as "INGMontoInteresMercadoAnteriorMonedaAdicional"
						, 0 											as "INGMontoReajusteMercadoAnteriorMonedaAdicional"
						, 0 											as "INGDurationMercadoAnterior"
						, 0 											as "INGConvexidadMercadoAnterior"
						, 0 											as "INGMontoMercadoAnteriorPibMas"
						, 0 											as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
						, 0 											as "INGMontoMercadoAnteriorPibMasMonedaAdicional"
						, 0 											as "INGMontoMercadoAnteriorPibMenos"
						, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
						, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaAdicional"
						, '0001-01-01' 									as "INGFechaPrecioTasaMercadoActual"
						, '' 											as "INGFuenteTasaPrecioMercadoActual"
						, 0												as "INGPrecioMercadoActual"
						, GES_TBCTCIIF."TASA_MED_CIE"					as "INGTasaMercadoActual"
						, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaMercadoActual"
						, 0 											as "INGMontoMercadoActualMonedaInstrumento"
						, 0 											as "INGMontoMercadoActualMonedaAdicional"
						, 0 											as "INGDurationMercadoActual"
						, 0 											as "INGConvexidadMercadoActual"
						, 0 											as	"INGMontoInteresCorridoMercadoActual"
						, 0 											as "INGMontoReajusteCorridoMercadoActual"
						, 0 											as "INGMontoInteresCorridoMercadoActualMonedaInstrumento"
						, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaInstrumento"
						, 0 											as "INGMontoInteresCorridoMercadoActualMonedaAdicional"
						, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaAdicional"
						, 0 											as "INGMontoMercadoActualPibMas"
						, 0 											as "INGMontoMercadoActualPibMasMonedaInstrumento"
						, 0 											as "INGMontoMercadoActualPibMasMonedaAdicional"
						, 0 											as "INGMontoMercadoActualPibMenos"
						, 0 											as "INGMontoMercadoActualPibMenosMonedaInstrumento"
						, 0 											as "INGMontoMercadoActualPibMenosMonedaAdicional"
						, '0001-01-01' 									as "INGFechaPrecioTasaMercadoProximo"
						, 0 											as "INGFuenteTasaPrecioMercadoProximo"
						, 0 											as "INGPrecioMercadoProximo"
						, 0 											as "INGTasaMercadoProximo"
						, 0 											as "INGMontoATasaMercadoProximo"
						, 0 											as "INGMontoInteresMercadoProximo"
						, 0 											as "INGMontoReajusteMercadoProximo"
						, 0 											as "INGMontoMercadoProximoMonedaInstrumento"
						, 0 											as "INGMontoInteresMercadoProximoMonedaInstrumento"
						, 0 											as "INGMontoReajusteMercadoProximoMonedaInstrumento"
						, 0 											as "INGMontoMercadoProximoMonedaAdicional"
						, 0 											as "INGMontoInteresMercadoProximoMonedaAdicional"
						, 0 											as "INGMontoReajusteMercadoProximoMonedaAdicional"
						, 0 											as "INGDurationMercadoProximo"
						, 0 											as "INGConvexidadMercadoProximo"
						, 0 											as "INGTasaCorredora"
						, '0001-01-01' 									as "INGFechaTasaCorredora"
						, 0 											as "INGMontoATasaCorredora"
						, 0 											as "INGMontoATasaCorredoraMonedaInstrumento"
						, 0 											as "INGMontoATasaCorredoraMonedaAdicional"
						, '0001-01-01' 									as "INGFechaCorteCupon"
						, 0 											as "INGNumeroCupon"
						, 0 											as "INGIdMonedaPagoCupon"
						, 0 											as "INGMontoCorteCupon"
						, '0001-01-01' 									as "INGFechaPagoCorteCupon"
						, 0 											as "INGMontoCapitalCorteCupon"
						, 0 											as "INGMontoInteresCorteCupon"
						, 0 											as "INGMontoReajusteCorteCupon"
						, 0 											as "INGMontoCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoCapitalCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoInteresCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoReajusteCorteCuponMonedaInstrumento"
						, 0 											as "INGMontoCorteCuponMonedaAdicional"
						, 0 											as "INGMontoCapitalCorteCuponMonedaAdicional"
						, 0 											as "INGMontoInteresCorteCuponMonedaAdicional"
						, 0 											as "INGMontoReajusteCorteCuponMonedaAdicional"
						, 0 											as "INGMontoPresenteTIF"
						, 0 											as "INGMontoInteresTIF"
						, 0 											as "INGMontoReajusteTIF"
						, 0 											as "INGMontoInteresAcumTIF"
						, 0 											as "INGMontoReajusteAcumTIF"
						, 0 											as "INGMontoPresenteTIFMonedaInstrumento"
						, 0 											as "INGMontoInteresTIFMonedaInstrumento"
						, 0 											as "INGMontoReajusteTIFMonedaInstrumento"
						, 0 											as "INGMontoInteresAcumTIFMonedaInstrumento"
						, 0 											as "INGMontoReajusteAcumTIFMonedaInstrumento"
						, 0 											as "INGMontoPresenteTIFMonedaAdicional"
						, 0 											as "INGMontoInteresTIFMonedaAdicional"
						, 0 											as "INGMontoReajusteTIFMonedaAdicional"
						, 0 											as "INGMontoInteresAcumTIFMonedaAdicional"
						, 0 											as "INGMontoReajusteAcumTIFMonedaAdicional"
						, 0 											as "INGMontoMonedaPrincipal"
						, 0 											as "INGMontoMonedaSecundaria"
						, 0 											as "INGMontoDiferido"
						, 0 											as "INGMontoUtilidadDiferida"
						, 0 											as "INGMontoPerdidaDiferida"
						, 0 											as "INGMontoMargen"
						, 0 											as "INGMontoValorRazonableActivo"
						, 0 											as "INGMontoValorRazonablePasivo"
						, 0 											as "INGMontoValorRazonableNeto"
						, 0 											as "INGTasaValorRazonableActivo"
						, 0 											as "INGTasaValorRazonablePasivo"
						, 0 											as "INGMontoDevengoTipoCambio"
						, 0 											as "INGMontoDevengoTipoCambioAnterior"
						, 0 											as "INGMontoDiferidoDiario"
						, 0 											as "INGMontoDiferidoAcumulado"
						, 0 											as "INGMontoMatriz"
						, 0 											as "EGRTicketValorizacion"
						, ''											as "EGRValorizacionConProblemas"
						, '0001-01-01' 									as "EGRFechaFactorMonedaInstrumento"
						, 0 											as "EGRValorFactorMonedaInstrumento"
						, '0001-01-01' 									as "EGRFechaFactorMonedaAdicional"
						, 0 											as "EGRValorFactorMonedaAdicional"
						, '' 											as "EGRGlosa"
						, ADD_MONTHS(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD'),1) as "EGRFechaOperacion"
						, 'RETTIT' 										as "EGRIdTipoOperacion"
						, 0 											as "EGRIdOperacion"
						, 0 											as "EGRIdMovimientoCarteraEnTransito"
						, 'AGE' 										as "EGRIdTipoCanalIngreso"
						, 'T0'  										as "EGRIdCondicionLiquidacion"
						, ADD_MONTHS(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD'),1) as "EGRFechaLiquidacion"
						, '' 											as "EGRIdTipoComprobante"
						, 0  											as "EGRFolioComprobante"
						, '0001-01-01' 									as "EGRFechaComprobante"
						, 0 											as "EGRFolioExternoReferencia"
						, 'CUS' 										as "EGRIdModulo"
						, Concepto_Egre									as "EGRIdConcepto" 
						, 0 as "EGRIdUsuario"-- ver si podemos agregar el dato"
						, coalesce(EmpleadoEjecutivo."IdSucursal",0) 	as "EGRIdSucursal" -- ver si podemos agregar el dato"
						, Portafolio."IdEmpleadoEjecutivo" 				as "EGRIdAgente"-- ver si podemos agregar el dato"
						, '0001-01-01' 									as "EGRFechaCurseRetiro"
						, 0 											as "EGRCostoRetiroenFactorAjuste"
						, 0 											as "EGRValorFactorAjusteaFechaRetiro"
						, GES_TBCTCIIF."CAN_ACTUAL"						as "EGRCantidad"
						, GES_TBCTCIIF."VAL_RESC"						as "EGRValorRescate" 
						, 0 											as "EGRPrecio" --el mismo precio del ingreso"
						, 0 											as "EGRTasa" -- la misma tasa del ingreso"
						, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "EGRMonto" -- el mismo monto del ingreso"
						, 0 											as "EGRMontoMonedaInstrumento" -- podria ser el mismo monto "
						, 0 											as "EGRMontoMonedaAdicional"
						, 0 											as "EGRSpread" -- revisar si existe este campo"
						, '0001-01-01' 									as "EGRFechaPrecioTasaMercado"
						, ''											as "EGRFuenteTasaPrecioMercado"
						, 0 											as "EGRPrecioMercado"
						, 0 											as "EGRTasaMercado"
						, 0 											as "EGRMontoATasaMercado"
						, 0 											as "EGRMontoMercadoMonedaInstrumento"
						, 0 											as "EGRMontoMercadoMonedaAdicional"
						, 0 											as "EGRSpreadMercado"
						, 0 											as "EGRPrecioAPlazo"
						, 0 											as "EGRMontoAPlazo"
						, 0 											as "EGRMontoAPlazoMonedaInstrumento"
						, 0 											as "EGRMontoAPlazoMonedaAdicional"
						, 0 											as "EGRMontoPresenteTIF"
						, 0 											as "EGRMontoInteresTIF"
						, 0 											as "EGRMontoReajusteTIF"
						, 0 											as "EGRMontoInteresAcumTIF"
						, 0 											as "EGRMontoReajusteAcumTIF"
						, 0 											as "EGRMontoPresenteTIFMonedaInstrumento"
						, 0 											as "EGRMontoInteresTIFMonedaInstrumento"
						, 0 											as "EGRMontoReajusteTIFMonedaInstrumento"
						, 0 											as "EGRMontoInteresAcumTIFMonedaInstrumento"
						, 0 											as "EGRMontoReajusteAcumTIFMonedaInstrumento"
						, 0 											as "EGRMontoPresenteTIFMonedaAdicional"
						, 0 											as "EGRMontoInteresTIFMonedaAdicional"
						, 0 											as "EGRMontoReajusteTIFMonedaAdicional"
						, 0 											as "EGRMontoInteresAcumTIFMonedaAdicional"
						, 0 											as "EGRMontoReajusteAcumTIFMonedaAdicional"
						, 0 											as "EGRIdSpotAsociado"
						, 0 											as "EGRMontoMonedaPrincipal"
						, 0 											as "EGRMontoMonedaSecundaria"
						, 0 											as "EGRIdMonedaLiquidacion"
						, 0 											as "EGRMontoLiquidacion"
						, 0 											as "EGRMontoLiquidacionMonedaPrincipal"
						, 0 											as "EGRValorTipoCambioSpot"
						, 0 											as "EGRValorParidad"
						, 0 											as "EGRValorTipoCambioCotizador"
						, 0 											as "EGREsPuntos"
						, 0 											as "EGRTasaPuntosMonedaPrincipal"
						, 0 											as "EGRTasaPuntosMonedaSecundaria"
						, 0 											as "EGRTasaDescuentoAnticipo"
						, 0 											as "EGRTipoCambioAnticipo"
						, 0 											as "EGRMontoARecibirAnticipo"
						, 0 											as "EGRTipoCambioObservado"
						, GES_TBCTCIIF."RUT_CLI" 						as "RUT_CLI"
						, GES_TBCTCIIF."SEC_RUT_CLI" 					as "SEC_RUT_CLI"
						, GES_TBCTCIIF."SEC_CAR_INVER" 					as "SEC_CAR_INVER"
						, GES_TBCTCIIF."COD_PER" 						as "COD_PER"
						, GES_TBCTCIIF."FOLIO_COMP_ADJ" 				as "FOLIO_COMP_ADJ"
						, GES_TBCTCIIF."TIPO_COMP_ADJ" 					as "TIPO_COMP_ADJ"
						, 0 											as "LIN_COMP_ADJ"
						, 0 											as "LIN_ORDEN"
						, 0 											as "SEC_ORDEN"
						, GES_TBCTCIIF."TIPO_CARTERA" 					as "TIPO_CARTERA"
						, 0 											as "ID_ERROR"
						, 0 											as "SEC_MOVTO"
						, 0 											as "CAN_GARANTIA"
						, 0 											as "SEC_COMPROM"

			FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
				INNER JOIN 
				(SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR",MAX(A."SEC_CAR_INVER") AS "SEC"
						, SUM(A."CAN_POSITIVO") As "CAN_POS"
						, SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
						, SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
						, SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
						, SUM(A."CAN_POSITIVO" + A."CAN_NEGATIVO") AS "CAN_RES"
						, SUM(A."VALRES_POSITIVO" - A."VALRES_NEGATIVO") AS "VAL_RES"
					FROM 
						(SELECT PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR",PO."SEC_CAR_INVER",
									PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
						FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
							WHERE PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
						AND PO."CAN_ACTUAL" > 0
						
							UNION  ALL
				
						SELECT NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",NE."SEC_CAR_INVER",
								0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
						FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
							WHERE NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
							AND NE."CAN_ACTUAL" < 0
						) AS A	
					GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
					HAVING SUM(A."CAN_POSITIVO") + SUM(A."CAN_NEGATIVO") >  0
					AND SUM(A."CAN_POSITIVO") > 0 
					AND SUM(A."CAN_NEGATIVO") < 0
					ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
				) AS CARCLI
					ON  CARCLI."COD_PER" 		= GES_TBCTCIIF."COD_PER"
					AND CARCLI."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 
					AND CARCLI."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI"
					AND TRIM(CARCLI."NEMO")		= TRIM(GES_TBCTCIIF."NEMO")
					AND TRIM(CARCLI."TIPO_REAJ")		= TRIM(GES_TBCTCIIF."TIPO_REAJ")
					AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
					AND CARCLI."FEC_VCTO" 	= GES_TBCTCIIF."FEC_VCTO"
				
				INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
						ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
						ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
				INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
						ON 	Cliente."IdPersona" = Persona."IdPersona"
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
							Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
						ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
				LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
						ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
						ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
						Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
						Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
						Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
				LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
						ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
						ON  TipoComprobante."TipoValor"  ='TipoComprobante'
							AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
				LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
						ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
							AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
				LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
						ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"
				
			WHERE 	GES_TBCTCIIF."COD_PER" 		= SUBSTRING(p_Periodo_Inicial,1,7)
			  AND 	GES_TBCTCIIF."COD_PER" 		NOT LIKE '%90%'
			  AND 	GES_TBCTCIIF."RUT_CLI" 		=  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
			  AND 	Cartera."RUT_CLI" 			IS NULL
			  AND 	GES_TBCTCIIF."SEC_CAR_INVER" = CARCLI."SEC"		 ;


			----------------------------------------------------------------
			--  CARTERA IIF acumulada < 0     	    					----
			----------------------------------------------------------------
			INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito"(	
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
					,"RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR"
					,"SEC_MOVTO","CAN_GARANTIA"
					)
			SELECT
					"BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL AS "IdMovimientoCarteraEnTransito"
					,0 																AS "IdMovimientoCarteraEnTransitoRelacionado"
					,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaIngresoRegistro"
					,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaCertificacionRegistro"
					,'0001-01-01' 													AS "FechaAnulacionRegistro"
					, 0 															AS "IdUsuarioIngreso"
					, 0 															AS "IdUsuarioCertificacion"
					, 0 															AS "IdUsuarioAnulacion"
					, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaOperacion"
					, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaVencimientoOperacion"
					, GES_TBCTCIIF."PLAZO"  										AS "PlazoEnDiasOperacion"
					, 0 															AS "FolioCustodia"
					, coalesce(TipoComprobante."ValorDestino",'') 					AS "IdTipoComprobante"
					, GES_TBCTCIIF.FOLIO_COMP_ADJ 									as "FolioComprobante"
					, 'N' 															AS "EsRegistroGeneradoPorSistema"-- no lo se
					, 'S' 															as "MueveCustodia"
					, 'N' 															as "PermiteCertificacionPorUsuario"
					, 'CUS' 														AS "OrigenCustodia"
					, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdCustodiaOrigen"
					, 'RETTIT' 														AS "IdTipoOperacionOrigen"
					, 0 															AS "FolioCarteraGarantizada"
					, 0 															AS "IdInstrumentoCarteraGarantizada"
					, 'N' 															AS "EsEspecifica"
					, 0 															AS "IdContraparteGarantia" ---si la operacion fuera de garantia y estuviera en transito
					, '' 															AS "RazonSocialContraparteGarantia" -- si la operacion fuera de garantia
					, '' 															AS "CuentaContraparteGarantia" -- si la operacion fuera de garantia
					, 0 															AS "IdOrigenGarantia" 
					, 0 															AS "IdEspecieGarantia"
					, p_IdEmpresa 													AS "IdEmpresa"
					, 0 															AS "IdPais"-- DEBERIA SER CHILE O EL PAIS DEL INSTRUMENTO
					, 'N' 															AS "EsNominal" -- depende de la operacion no tengo acceso
					, 0 															AS "FolioExternoReferencia"
					, Cliente."IdCliente" 											AS "IdCliente"
					, Portafolio."IdPortafolio" 									AS "IdPortafolio"
					, 0 															as "IdCuentaCustodioPortafolio"
					, 'CRT' 														AS "IdEstado"
					, 'AGE' 														as "IdTipoCanalIngreso"
					, v_MonedaSistema 												AS "IdMonedaSistema"
					, ''  															AS "IdMonedaAdicional"
					, '0001-01-01' 													as "FechaFactorMonedaAdicional"
					, 0 															as "ValorFactorMonedaAdicional"
					, 0 															AS "CodigoIFRS" 
					, Concepto_Egre 												AS "IdConcepto"
					, 'RETTIT' 														AS "IdTipoOperacion"
					, 0 															as "IdMesa"
					, '' 															AS "IdBolsa" 		-- debo encontrar como sacarla"
					, coalesce(EmpleadoEjecutivo."IdSucursal",0) 					as "IdSucursal" 	-- lo estamos sacndo del ejecutivo del portafolio
					, Portafolio."IdEmpleadoEjecutivo" 								as "IdAgente"		-- lo sacamos del ejecutivo del portafolio
					, 'T0' 															AS "IdCondicionLiquidacion"
					, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaLiquidacion"
					, 'N' 															as "EsIntraday"
					, 'N' 															AS "UsaGarantiaEspecifica"
					, '' 															AS "Glosa"
					, 0 															AS "IdUsuario"
					, p_IdModulo 													AS "IdModulo"
					, '' 															As "TipoCarteraCorredor"
					, '' 															AS "TipoCarteraNormativa"
					, '0001-01-01' 													AS "FechaCurse"
					, '' 															AS "TipoFactorAjuste"
					, 0 															AS "ValorFactorAjusteaFechaCurse"
					, 0 															AS "CostoEnFactorAjuste"
					, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
					, COALESCE(Instrumento."IdTipoInstrumento",'') 					as "IdTipoInstrumento"
					, COALESCE(Instrumento."IdInstrumento",0) 						as "IdInstrumento"
					, 0 															as "IdInstrumentoAnterior"
					, TRIM(GES_TBCTCIIF."NEMO")  									AS "Nemotecnico"
					, TRIM(GES_TBCTCIIF."EMISOR") 									as "IdEmisor"
					, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 					AS "NemoSVS"  
					, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
					,'' 															as "EsPagaderoEn"
					, GES_TBCTCIIF."FEC_EMI"  										as "FechaEmisionInstrumento"
					, GES_TBCTCIIF."FEC_VCTO"  										as "FechaVencimientoInstrumento"
					, GES_TBCTCIIF."IND_FUNGIBILIDAD"								AS "EsFungible"
					, Instrumento."ValorizaPor" 									AS "ValorizaPor"
					, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) AS "TipoValorizacion"
					,'N' 															as "TieneCorteDeCupon"
					, COALESCE(Instrumento."IdMoneda",'') 							as "IdMonedaInstrumento"
					, 0 															AS "Tamano"
					, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END AS "IdBoveda"
					, 0 															AS "CuentaBoveda"
					, COALESCE(Instrumento."ISIN",'0') 								as "ISIN"
					, '0001-01-01' 													as "FechaFactorMonedaInstrumento"
					, 0																as "ValorFactorMonedaInstrumento"
					, ABS(CARCLI."CAN_RES") 										AS "Cantidad" 
					, 0 															AS "CorteMinimo"
					, ABS(CARCLI."VAL_RES")											AS "ValorRescate"
					, 0 															AS "PrecioCompra"
					, GES_TBCTCIIF."TASA_PROM_COMP" 								as "TasaCompra"
					, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompra"
					, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompraMonedaInstrumento"
					, 0 															as "MontoCompraMonedaAdicional"
					, p_IdCarga 													AS "IdCarga"
					, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdSecuenciaCarteraMigracion"
					, GES_TBCTCIIF."COD_PER" 										AS "CodigoPeriodo"
					, GES_TBCTCIIF."RUT_CLI" 										AS "RUT_CLI"
					, GES_TBCTCIIF."SEC_RUT_CLI" 									AS "SEC_RUT_CLI"
					, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "SEC_CAR_INVER"
					, GES_TBCTCIIF."COD_PER" 										AS "COD_PER"
					, GES_TBCTCIIF."FOLIO_COMP_ADJ" 								AS "FOLIO_COMP_ADJ"
					, GES_TBCTCIIF."TIPO_COMP_ADJ" 									AS "TIPO_COMP_ADJ"
					, 0 															AS "LIN_COMP_ADJ"
					, 0 															AS "LIN_ORDEN"
					, 0 															AS "SEC_ORDEN"
					, GES_TBCTCIIF."TIPO_CARTERA" 									as "TIPO_CARTERA"
					, 0 															AS "ID_ERROR"
					, 0 															AS "SEC_MOVTO"
					, 0 															AS "CAN_GARANTIA"
			FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
				INNER JOIN 
					(SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR",MAX(A."SEC_CAR_INVER") AS "SEC"
							, SUM(A."CAN_POSITIVO") As "CAN_POS"
							, SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
							, SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
							, SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
							, SUM(A."CAN_POSITIVO" + A."CAN_NEGATIVO") AS "CAN_RES"
							, SUM(A."VALRES_POSITIVO" - A."VALRES_NEGATIVO") AS "VAL_RES"
						FROM 
							(SELECT PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR",PO."SEC_CAR_INVER",
										PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
							FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
							WHERE PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
								AND PO."CAN_ACTUAL" > 0
								
								UNION  ALL
					
							SELECT NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",NE."SEC_CAR_INVER",
									0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
							FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
							WHERE NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
								AND NE."CAN_ACTUAL" < 0
							) AS A	
					GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
					HAVING SUM(A."CAN_POSITIVO") + SUM(A."CAN_NEGATIVO") <  0
						AND SUM(A."CAN_POSITIVO") > 0 
						AND SUM(A."CAN_NEGATIVO") < 0
						ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
					) AS CARCLI
					ON  CARCLI."COD_PER" 				= GES_TBCTCIIF."COD_PER"
					AND CARCLI."RUT_CLI" 				= GES_TBCTCIIF."RUT_CLI" 
					AND CARCLI."SEC_RUT_CLI" 		= GES_TBCTCIIF."SEC_RUT_CLI"
					AND TRIM(CARCLI."NEMO")			= TRIM(GES_TBCTCIIF."NEMO")
					AND TRIM(CARCLI."TIPO_REAJ")= TRIM(GES_TBCTCIIF."TIPO_REAJ")
					AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
					AND CARCLI."FEC_VCTO" 			= GES_TBCTCIIF."FEC_VCTO"
				
				INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
						ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
						ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
				INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
						ON 	Cliente."IdPersona" = Persona."IdPersona"
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
							Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
						ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
				LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
						ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
						ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
						Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
						Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
						Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
				LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
						ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
						ON  TipoComprobante."TipoValor"  ='TipoComprobante'
							AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
				LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
						ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
							AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
				LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
						ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"											
				
			WHERE GES_TBCTCIIF."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
			  AND GES_TBCTCIIF."COD_PER" NOT LIKE '%90%'
			  AND GES_TBCTCIIF."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
			  AND Cartera."RUT_CLI" IS NULL
			  AND GES_TBCTCIIF."SEC_CAR_INVER" = CARCLI."SEC" ;

	 END IF;
END IF;
----------------------------------------------------------------
---              Fin de carga Carteras Inversas              ---
----------------------------------------------------------------


----------------------------------------------------
--    CARTERAS IIF > 0   -----
----------------------------------------------------
IF p_inversa = 'N' THEN 
INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"(
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
			,"EGRMontoARecibirAnticipo","EGRTipoCambioObservado","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN"
			,"SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA","SEC_COMPROM")
SELECT		  
			  "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL 		as "IdCartera"
			, "BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL 	as "FolioCartera"
			, 0 											as "FolioCarteraRelacionada"
			, 0 											as "FolioGarantiaOrigen"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) as "FechaIngresoRegistro"
			, '0001-01-01' 									as "FechaModificacionRegistro"
			, p_IdEmpresa 									as "IdEmpresa" 
			, GES_TBCTCIIF."FEC_INVER" 						as "FechaOperacion"
			, '0001-01-01' 									as "FechaVencimientoOperacion"
			, GES_TBCTCIIF."PLAZO" 							as "PlazoEnDiasOperacion"
			, Cliente."IdCliente" 							as "IdCliente"
			, Portafolio."IdPortafolio" 					as "IdPortafolio"
			, 0 											as "IdCuentaCustodioPortafolio"
			, 0 											as "IdClienteRelacionado"
			, 0 											as "IdPortafolioRelacionado"
			, 'VIG' 										as "IdEstado"
			, 'INIF' 										as "IdTipoProducto"
			, 'AGE' 										as "IdTipoCanalIngreso"
			, v_MonedaSistema 								as "IdMonedaSistema" 
			,'' 											as "IdMonedaAdicional"
			,'0001-01-01' 									as "FechaFactorMonedaAdicional"
			,'0' 											as "ValorFactorMonedaAdicional"
			, 0 											as "IdMovimientoCarteraEnTransito"
			, Concepto_Ingre 								as "IdConcepto"
			, 'INGTIT' 										as "IdTipoOperacion"
			, 0 											as "IdOperacion"
			, coalesce(TipoComprobante."ValorDestino",'') 	as "IdTipoComprobante"
			, coalesce(ComprobanteFacturacion."IdComprobante",0) 			as "FolioComprobante" 
			, coalesce(ComprobanteFacturacion."FEC_ING_COMP",'0001-01-01')	as "FechaComprobante"
			, 0 											as "FolioExternoReferencia" 
			, 0 											as "IdMesa" 
			, 'BCS' 										as "IdBolsa"
			, coalesce(EmpleadoEjecutivo."IdSucursal",0) 	as "IdSucursal"  
			, Portafolio."IdEmpleadoEjecutivo" 				as "IdAgente" 
			, 'T0' 											as "IdCondicionLiquidacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "FechaLiquidacion"
			, '' 											as "EsIntraday"
			, '' 											as "UsaGarantiaEspecifica"
			, '' 											as "CobraPrimaAlInicio"
			, '' 											as "IdTipoPrestamo"
			, '' 											as "Glosa"
			, 0  											as "IdUsuario" 
			, p_IdModulo 									as "IdModulo"  
			, '' 											as "TipoCarteraCorredor"
			, '' 											as "TipoCarteraNormativa"
			, '0001-01-01' 									as "FechaCurseInversion"
			, '' 											as "TipoFactorAjuste"
			, 0 											as "ValorFactorAjusteaFechaCurse"
			, 0 											as "CostoInversionEnFactorAjuste"
			, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
			, COALESCE(Instrumento."IdTipoInstrumento",'') 	as "IdTipoInstrumento"
			, COALESCE(Instrumento."IdInstrumento",0) 		as "IdInstrumento" 
			, 0 											as "IdInstrumentoAnterior"
			, TRIM(GES_TBCTCIIF."NEMO")  					as "Nemotecnico"
			, TRIM(GES_TBCTCIIF."EMISOR") 					as "IdEmisor"
			, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 	as "NemoSVS" -- 
			, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
			, '' 											as "EsPagaderoEn"
			, GES_TBCTCIIF."FEC_EMI"  						as "FechaEmisionInstrumento" -- debo calcularla en caso de IF
			, GES_TBCTCIIF."FEC_VCTO"  						as "FechaVencimientoInstrumento" 
			, GES_TBCTCIIF."IND_FUNGIBILIDAD" 				as "EsFungible"
			, Instrumento."ValorizaPor" 					as "ValorizaPor"
			, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) 	as "TipoValorizacion"						
			, 0 											as "CodigoIFRS" 
			, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END 	AS "IdBoveda" 
			, 0 											as "CuentaBoveda" 
			, COALESCE(Instrumento."IdMoneda",'') 			as "IdMonedaInstrumento"
			, 0 											as "Tamano" 
			, '' 											as "TIFAfectoBeneficio"
			, 0 											as "TIFTasa"
			, COALESCE(Instrumento."ISIN",'') 				as "ISIN"
			, '0001-01-01' 									as "FechaFactorMonedaInstrumento"
			, 0 											as "ValorFactorMonedaInstrumento"
			, GES_TBCTCIIF."CAN_ACTUAL"						as "Cantidad"
			, 0 											as "CorteMinimo" 
			, GES_TBCTCIIF."VAL_RESC" 						as "ValorRescate"
			, 0 											as "PrecioCompra"
			, GES_TBCTCIIF."TASA_PROM_COMP" 				as "TasaCompra"
			, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompra"
			, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompraMonedaInstrumento"
			, 0 											as "MontoCompraMonedaAdicional"
			, 0 											as "TasaAgente"
			, 0 											as "PrecioAPlazo"
			, 0 											as "MontoAPlazo"
			, 0 											as "MontoAPlazoMonedaInstrumento"
			, 0 											as "MontoAPlazoMonedaAdicional"
			, GES_TBCTCIIF."FEC_INVER"  					as "FechaEfectiva" 
			, 0 											as "IdPlaza"
			, 0 											as "IdFixing"
			, '0001-01-01' 									as "FechaFixing"
			, '0:00:00' 									as "HoraFixing"
			, 0 											as "IdModalidadCumplimiento"
			, 0 											as "IdReferenciaMercado"
			, 0 											as "IdMonedaPrincipal"
			, 0 											as "IdMonedaSecundaria"
			, 0 											as "ValorTipoCambioCierre"
			, 0 											as "ValorParidadCierre"
			, 0 											as "ValorTipoCambioCosto"
			, 0 											as "ValorParidadCosto"
			, 0 											as "MontoMonedaPrincipal"
			, 0 											as "MontoMonedaSecundaria"
			, p_IdCarga 									as "IdCarga"     
			, GES_TBCTCIIF."SEC_CAR_INVER" 					as "IdSecuenciaCarteraMigracion"
			, GES_TBCTCIIF."COD_PER" 						as "CodigoPeriodo"
			, '' 											as "INGGlosa"
			, 0 											as "INGTasaAgente"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "INGFechaOperacion"
			, 'T0' 											as "INGIdCondicionLiquidacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "INGFechaLiquidacion"
			, GES_TBCTCIIF."CAN_ACTUAL"  					as "INGCantidad"
			, GES_TBCTCIIF."VAL_RESC"						as "INGValorRescate"
			, 0 											as "INGPrecioPromedioCompra"
			, 0 											as "INGMontoPrecioPromedioCompra"
			, 0 											as "INGMontoPrecioPromedioMonedaInstrumento"
			, 0 											as "INGMontoPrecioPromedioMonedaAdicional"
			, 0 											as "INGPrecioAPlazo"
			, 0 											as "INGMontoAPlazo"
			, 0 											as "INGMontoAPlazoMonedaInstrumento"
			, 0 											as "INGMontoAPlazoMonedaAdicional"
			, 0 											as "INGEsPuntos"
			, 0 											as "INGValorTipoCambioSpot"
			, 0 											as "INGValorTipoCambioCotizador"
			, 0 											as "INGTasaPuntosMonedaPrincipal"
			, 0 											as "INGTasaPuntosMonedaSecundaria"
			, 0 											as "INGTicketValorizacion"
			, 'N' 											as "INGValorizacionConProblemas"
			, '0001-01-01' 									as "INGFechaValorizacionCartera"
			, '0001-01-01' 									as "INGFechaValorizacionAnterior"
			, '0001-01-01' 									as "INGFechaValorizacionActual"
			, '0001-01-01' 									as "INGFechaValorizacionProximo"
			, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoAnterior"
			, 0 											as "INGValorFactorMonedaInstrumentoAnterior"
			, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoActual"
			, 0 											as "INGValorFactorMonedaInstrumentoActual"
			, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoProximo"
			, 0 											as "INGValorFactorMonedaInstrumentoProximo"
			, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalAnterior"
			, 0 											as "INGValorFactorMonedaAdicionalAnterior"
			, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalActual"
			, 0 											as "INGValorFactorMonedaAdicionalActual"
			, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalProximo"
			, 0 											as "INGValorFactorMonedaAdicionalProximo"
			, 0 											as "INGPrecioCompra"
			, GES_TBCTCIIF."TASA_PROM_COMP" 				as "INGTasaCompra"
			, 0 											as "INGMontoATasaCompraAnterior"
			, 0 											as "INGMontoInteresCompraAnterior"
			, 0 											as "INGMontoReajusteCompraAnterior"
			, 0 											as "INGMontoCompraAnteriorMonedaInstrumento"
			, 0 											as "INGMontoInteresCompraAnteriorMonedaInstrumento"
			, 0 											as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
			, 0 											as "INGMontoCompraAnteriorMonedaAdicional"
			, 0 											as "INGMontoInteresCompraAnteriorMonedaAdicional"
			, 0 											as "INGMontoReajusteCompraAnteriorMonedaAdicional"
			, 0 											as "INGDurationCompraAnterior"
			, 0 											as "INGConvexidadCompraAnterior"
			, 0 											as "INGMontoCompraAnteriorPibMas"
			, 0 											as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
			, 0 											as "INGMontoCompraAnteriorPibMasMonedaAdicional"
			, 0 											as "INGMontoCompraAnteriorPibMenos"
			, 0 											as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
			, 0 											as "INGMontoCompraAnteriorPibMenosMonedaAdicional"
			, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaCompraActual"
			, 0 											as "INGMontoCompraActualMonedaInstrumento"
			, 0 											as "INGMontoCompraActualMonedaAdicional"
			, 0 											as "INGDurationCompraActual"
			, 0 											as "INGConvexidadCompraActual"
			, 0 											as "INGMontoInteresCorridoCompraActual"
			, 0 											as "INGMontoReajusteCorridoCompraActual"
			, 0 											as "INGMontoInteresCorridoCompraActualMonedaInstrumento"
			, 0 											as "INGMontoReajusteCorridoCompraActualMonedaInstrumento"
			, 0 											as "INGMontoInteresCorridoCompraActualMonedaAdicional"
			, 0 											as "INGMontoReajusteCorridoCompraActualMonedaAdicional"
			, 0 											as "INGMontoCompraActualPibMas"
			, 0 											as "INGMontoCompraActualPibMasMonedaInstrumento"
			, 0 											as "INGMontoCompraActualPibMasMonedaAdicional"
			, 0 											as "INGMontoCompraActualPibMenos"
			, 0 											as "INGMontoCompraActualPibMenosMonedaInstrumento"
			, 0 											as "INGMontoCompraActualPibMenosMonedaAdicional"
			, 0 											as "INGMontoATasaCompraProximo"
			, 0 											as "INGMontoInteresCompraProximo"
			, 0 											as "INGMontoReajusteCompraProximo"
			, 0 											as "INGMontoCompraProximoMonedaInstrumento"
			, 0 											as "INGMontoInteresCompraProximoMonedaInstrumento"
			, 0 											as "INGMontoReajusteCompraProximoMonedaInstrumento"
			, 0 											as "INGMontoCompraProximoMonedaAdicional"
			, 0 											as "INGMontoInteresCompraProximoMonedaAdicional"
			, 0 											as "INGMontoReajusteCompraProximoMonedaAdicional"
			, 0 											as "INGDurationCompraProximo"
			, 0 											as "INGConvexidadCompraProximo"
			, '0001-01-01' 									as "INGFechaPrecioTasaMercadoAnterior"
			, 0 											as "INGFuenteTasaPrecioMercadoAnterior"
			, 0 											as "INGPrecioMercadoAnterior"
			, 0 											as "INGTasaMercadoAnterior"
			, 0 											as "INGMontoATasaMercadoAnterior"
			, 0 											as "INGMontoInteresMercadoAnterior"
			, 0 											as "INGMontoReajusteMercadoAnterior"
			, 0 											as "INGMontoMercadoAnteriorMonedaInstrumento"
			, 0 											as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
			, 0 											as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
			, 0 											as "INGMontoMercadoAnteriorMonedaAdicional"
			, 0 											as "INGMontoInteresMercadoAnteriorMonedaAdicional"
			, 0 											as "INGMontoReajusteMercadoAnteriorMonedaAdicional"
			, 0 											as "INGDurationMercadoAnterior"
			, 0 											as "INGConvexidadMercadoAnterior"
			, 0 											as "INGMontoMercadoAnteriorPibMas"
			, 0 											as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
			, 0 											as "INGMontoMercadoAnteriorPibMasMonedaAdicional"
			, 0 											as "INGMontoMercadoAnteriorPibMenos"
			, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
			, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaAdicional"
			, '0001-01-01' 									as "INGFechaPrecioTasaMercadoActual"
			, '' 											as "INGFuenteTasaPrecioMercadoActual"
			, 0												as "INGPrecioMercadoActual"
			, GES_TBCTCIIF."TASA_MED_CIE"					as "INGTasaMercadoActual"
			, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaMercadoActual"
			, 0 											as "INGMontoMercadoActualMonedaInstrumento"
			, 0 											as "INGMontoMercadoActualMonedaAdicional"
			, 0 											as "INGDurationMercadoActual"
			, 0 											as "INGConvexidadMercadoActual"
			, 0 											as	"INGMontoInteresCorridoMercadoActual"
			, 0 											as "INGMontoReajusteCorridoMercadoActual"
			, 0 											as "INGMontoInteresCorridoMercadoActualMonedaInstrumento"
			, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaInstrumento"
			, 0 											as "INGMontoInteresCorridoMercadoActualMonedaAdicional"
			, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaAdicional"
			, 0 											as "INGMontoMercadoActualPibMas"
			, 0 											as "INGMontoMercadoActualPibMasMonedaInstrumento"
			, 0 											as "INGMontoMercadoActualPibMasMonedaAdicional"
			, 0 											as "INGMontoMercadoActualPibMenos"
			, 0 											as "INGMontoMercadoActualPibMenosMonedaInstrumento"
			, 0 											as "INGMontoMercadoActualPibMenosMonedaAdicional"
			, '0001-01-01' 									as "INGFechaPrecioTasaMercadoProximo"
			, 0 											as "INGFuenteTasaPrecioMercadoProximo"
			, 0 											as "INGPrecioMercadoProximo"
			, 0 											as "INGTasaMercadoProximo"
			, 0 											as "INGMontoATasaMercadoProximo"
			, 0 											as "INGMontoInteresMercadoProximo"
			, 0 											as "INGMontoReajusteMercadoProximo"
			, 0 											as "INGMontoMercadoProximoMonedaInstrumento"
			, 0 											as "INGMontoInteresMercadoProximoMonedaInstrumento"
			, 0 											as "INGMontoReajusteMercadoProximoMonedaInstrumento"
			, 0 											as "INGMontoMercadoProximoMonedaAdicional"
			, 0 											as "INGMontoInteresMercadoProximoMonedaAdicional"
			, 0 											as "INGMontoReajusteMercadoProximoMonedaAdicional"
			, 0 											as "INGDurationMercadoProximo"
			, 0 											as "INGConvexidadMercadoProximo"
			, 0 											as "INGTasaCorredora"
			, '0001-01-01' 									as "INGFechaTasaCorredora"
			, 0 											as "INGMontoATasaCorredora"
			, 0 											as "INGMontoATasaCorredoraMonedaInstrumento"
			, 0 											as "INGMontoATasaCorredoraMonedaAdicional"
			, '0001-01-01' 									as "INGFechaCorteCupon"
			, 0 											as "INGNumeroCupon"
			, 0 											as "INGIdMonedaPagoCupon"
			, 0 											as "INGMontoCorteCupon"
			, '0001-01-01' 									as "INGFechaPagoCorteCupon"
			, 0 											as "INGMontoCapitalCorteCupon"
			, 0 											as "INGMontoInteresCorteCupon"
			, 0 											as "INGMontoReajusteCorteCupon"
			, 0 											as "INGMontoCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoCapitalCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoInteresCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoReajusteCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoCorteCuponMonedaAdicional"
			, 0 											as "INGMontoCapitalCorteCuponMonedaAdicional"
			, 0 											as "INGMontoInteresCorteCuponMonedaAdicional"
			, 0 											as "INGMontoReajusteCorteCuponMonedaAdicional"
			, 0 											as "INGMontoPresenteTIF"
			, 0 											as "INGMontoInteresTIF"
			, 0 											as "INGMontoReajusteTIF"
			, 0 											as "INGMontoInteresAcumTIF"
			, 0 											as "INGMontoReajusteAcumTIF"
			, 0 											as "INGMontoPresenteTIFMonedaInstrumento"
			, 0 											as "INGMontoInteresTIFMonedaInstrumento"
			, 0 											as "INGMontoReajusteTIFMonedaInstrumento"
			, 0 											as "INGMontoInteresAcumTIFMonedaInstrumento"
			, 0 											as "INGMontoReajusteAcumTIFMonedaInstrumento"
			, 0 											as "INGMontoPresenteTIFMonedaAdicional"
			, 0 											as "INGMontoInteresTIFMonedaAdicional"
			, 0 											as "INGMontoReajusteTIFMonedaAdicional"
			, 0 											as "INGMontoInteresAcumTIFMonedaAdicional"
			, 0 											as "INGMontoReajusteAcumTIFMonedaAdicional"
			, 0 											as "INGMontoMonedaPrincipal"
			, 0 											as "INGMontoMonedaSecundaria"
			, 0 											as "INGMontoDiferido"
			, 0 											as "INGMontoUtilidadDiferida"
			, 0 											as "INGMontoPerdidaDiferida"
			, 0 											as "INGMontoMargen"
			, 0 											as "INGMontoValorRazonableActivo"
			, 0 											as "INGMontoValorRazonablePasivo"
			, 0 											as "INGMontoValorRazonableNeto"
			, 0 											as "INGTasaValorRazonableActivo"
			, 0 											as "INGTasaValorRazonablePasivo"
			, 0 											as "INGMontoDevengoTipoCambio"
			, 0 											as "INGMontoDevengoTipoCambioAnterior"
			, 0 											as "INGMontoDiferidoDiario"
			, 0 											as "INGMontoDiferidoAcumulado"
			, 0 											as "INGMontoMatriz"
			, 0 											as "EGRTicketValorizacion"
			, ''											as "EGRValorizacionConProblemas"
			, '0001-01-01' 									as "EGRFechaFactorMonedaInstrumento"
			, 0 											as "EGRValorFactorMonedaInstrumento"
			, '0001-01-01' 									as "EGRFechaFactorMonedaAdicional"
			, 0 											as "EGRValorFactorMonedaAdicional"
			, '' 											as "EGRGlosa"
			, '0001-01-01'									as "EGRFechaOperacion"
			, '' 											as "EGRIdTipoOperacion"
			, 0 											as "EGRIdOperacion"
			, 0 											as "EGRIdMovimientoCarteraEnTransito"
			, '' 											as "EGRIdTipoCanalIngreso"
			, ''  											as "EGRIdCondicionLiquidacion"
			, '0001-01-01'									as "EGRFechaLiquidacion"
			, '' 											as "EGRIdTipoComprobante"
			, 0  											as "EGRFolioComprobante"
			, '0001-01-01' 									as "EGRFechaComprobante"
			, 0 											as "EGRFolioExternoReferencia"
			, '' 											as "EGRIdModulo"
			, ''											as "EGRIdConcepto" 
			, 0 											as "EGRIdUsuario"
			, 0 											as "EGRIdSucursal" 
			, 0												as "EGRIdAgente"
			, '0001-01-01' 									as "EGRFechaCurseRetiro"
			, 0 											as "EGRCostoRetiroenFactorAjuste"
			, 0 											as "EGRValorFactorAjusteaFechaRetiro"
			, 0												as "EGRCantidad"
			, 0												as "EGRValorRescate" 
			, 0 											as "EGRPrecio" 
			, 0 											as "EGRTasa" 
			, 0 											as "EGRMonto" 
			, 0 											as "EGRMontoMonedaInstrumento" 
			, 0 											as "EGRMontoMonedaAdicional"
			, 0 											as "EGRSpread" 
			, '0001-01-01' 									as "EGRFechaPrecioTasaMercado"
			, ''											as "EGRFuenteTasaPrecioMercado"
			, 0 											as "EGRPrecioMercado"
			, 0 											as "EGRTasaMercado"
			, 0 											as "EGRMontoATasaMercado"
			, 0 											as "EGRMontoMercadoMonedaInstrumento"
			, 0 											as "EGRMontoMercadoMonedaAdicional"
			, 0 											as "EGRSpreadMercado"
			, 0 											as "EGRPrecioAPlazo"
			, 0 											as "EGRMontoAPlazo"
			, 0 											as "EGRMontoAPlazoMonedaInstrumento"
			, 0 											as "EGRMontoAPlazoMonedaAdicional"
			, 0 											as "EGRMontoPresenteTIF"
			, 0 											as "EGRMontoInteresTIF"
			, 0 											as "EGRMontoReajusteTIF"
			, 0 											as "EGRMontoInteresAcumTIF"
			, 0 											as "EGRMontoReajusteAcumTIF"
			, 0 											as "EGRMontoPresenteTIFMonedaInstrumento"
			, 0 											as "EGRMontoInteresTIFMonedaInstrumento"
			, 0 											as "EGRMontoReajusteTIFMonedaInstrumento"
			, 0 											as "EGRMontoInteresAcumTIFMonedaInstrumento"
			, 0 											as "EGRMontoReajusteAcumTIFMonedaInstrumento"
			, 0 											as "EGRMontoPresenteTIFMonedaAdicional"
			, 0 											as "EGRMontoInteresTIFMonedaAdicional"
			, 0 											as "EGRMontoReajusteTIFMonedaAdicional"
			, 0 											as "EGRMontoInteresAcumTIFMonedaAdicional"
			, 0 											as "EGRMontoReajusteAcumTIFMonedaAdicional"
			, 0 											as "EGRIdSpotAsociado"
			, 0 											as "EGRMontoMonedaPrincipal"
			, 0 											as "EGRMontoMonedaSecundaria"
			, 0 											as "EGRIdMonedaLiquidacion"
			, 0 											as "EGRMontoLiquidacion"
			, 0 											as "EGRMontoLiquidacionMonedaPrincipal"
			, 0 											as "EGRValorTipoCambioSpot"
			, 0 											as "EGRValorParidad"
			, 0 											as "EGRValorTipoCambioCotizador"
			, 0 											as "EGREsPuntos"
			, 0 											as "EGRTasaPuntosMonedaPrincipal"
			, 0 											as "EGRTasaPuntosMonedaSecundaria"
			, 0 											as "EGRTasaDescuentoAnticipo"
			, 0 											as "EGRTipoCambioAnticipo"
			, 0 											as "EGRMontoARecibirAnticipo"
			, 0 											as "EGRTipoCambioObservado"
			, GES_TBCTCIIF."RUT_CLI" 						as "RUT_CLI"
			, GES_TBCTCIIF."SEC_RUT_CLI" 					as "SEC_RUT_CLI"
			, GES_TBCTCIIF."SEC_CAR_INVER" 					as "SEC_CAR_INVER"
			, GES_TBCTCIIF."COD_PER" 						as "COD_PER"
			, GES_TBCTCIIF."FOLIO_COMP_ADJ" 				as "FOLIO_COMP_ADJ"
			, GES_TBCTCIIF."TIPO_COMP_ADJ" 					as "TIPO_COMP_ADJ"
			, 0 											as "LIN_COMP_ADJ"
			, 0 											as "LIN_ORDEN"
			, 0 											as "SEC_ORDEN"
			, GES_TBCTCIIF."TIPO_CARTERA" 					as "TIPO_CARTERA"
			, 0 											as "ID_ERROR"
			, 0 											as "SEC_MOVTO"
			, 0 											as "CAN_GARANTIA"
			, 0 											as "SEC_COMPROM"
  FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
  INNER JOIN 
       (SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
              , SUM(A."CAN_POSITIVO") As "CAN_POS"
              , SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
              , SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
              , SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
          FROM 
               (SELECT PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR", 
               			PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
     		      FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
			     WHERE PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      		       AND PO."CAN_ACTUAL" > 0
      		       
 	             UNION  ALL
       
                SELECT NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",   
                		0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
     	          FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
		         WHERE NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      	           AND NE."CAN_ACTUAL" < 0
               ) AS A	
         GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
         HAVING (SUM(A."CAN_POSITIVO") > 0 AND SUM(A."CAN_NEGATIVO") = 0)
         ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
        ) AS CARCLI
 		ON  CARCLI."COD_PER" 		= GES_TBCTCIIF."COD_PER"
		AND CARCLI."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 
		AND CARCLI."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI"
		AND TRIM(CARCLI."NEMO")		= TRIM(GES_TBCTCIIF."NEMO")
		AND TRIM(CARCLI."TIPO_REAJ")		= TRIM(GES_TBCTCIIF."TIPO_REAJ")
		AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
		AND CARCLI."FEC_VCTO" 	= GES_TBCTCIIF."FEC_VCTO"
		
 INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
 		ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
 		ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
 INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
 		ON 	Cliente."IdPersona" = Persona."IdPersona"
 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
 		ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
 			Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
 LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
 		ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
 LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
 		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
 LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
 		ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
 		Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
 		Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
 		Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
 LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
 		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
 LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
 		ON  TipoComprobante."TipoValor"  ='TipoComprobante'
 			AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
 LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
 		ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
 			AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
 LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
 		ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"			
 
 WHERE GES_TBCTCIIF."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
   AND GES_TBCTCIIF."COD_PER" NOT LIKE '%90%'
   AND GES_TBCTCIIF."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
   AND Cartera."RUT_CLI" IS NULL
   AND GES_TBCTCIIF."CAN_ACTUAL" > 0;

-----------------------------------------------------
--    CARTERAS IIF < 0   						-----
-----------------------------------------------------
INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito"
			(	
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
			,"RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR"
			,"SEC_MOVTO","CAN_GARANTIA"
			)

select
			 "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL AS "IdMovimientoCarteraEnTransito"
			,0 																AS "IdMovimientoCarteraEnTransitoRelacionado"
			,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaIngresoRegistro"
			,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaCertificacionRegistro"
			,'0001-01-01' 													AS "FechaAnulacionRegistro"
			, 0 															AS "IdUsuarioIngreso"
			, 0 															AS "IdUsuarioCertificacion"
			, 0 															AS "IdUsuarioAnulacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaOperacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaVencimientoOperacion"
			, GES_TBCTCIIF."PLAZO"  										AS "PlazoEnDiasOperacion"
			, 0 															AS "FolioCustodia"
			, coalesce(TipoComprobante."ValorDestino",'') 					AS "IdTipoComprobante"
			, GES_TBCTCIIF.FOLIO_COMP_ADJ 									as "FolioComprobante"
			, 'N' 															AS "EsRegistroGeneradoPorSistema"-- no lo se
			, 'S' 															as "MueveCustodia"
			, 'N' 															as "PermiteCertificacionPorUsuario"
			, 'CUS' 														AS "OrigenCustodia"
			, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdCustodiaOrigen"
			, 'RETTIT' 														AS "IdTipoOperacionOrigen"
			, 0 															AS "FolioCarteraGarantizada"
			, 0 															AS "IdInstrumentoCarteraGarantizada"
			, 'N' 															AS "EsEspecifica"
			, 0 															AS "IdContraparteGarantia" ---si la operacion fuera de garantia y estuviera en transito
			, '' 															AS "RazonSocialContraparteGarantia" -- si la operacion fuera de garantia
			, '' 															AS "CuentaContraparteGarantia" -- si la operacion fuera de garantia
			, 0 															AS "IdOrigenGarantia" 
			, 0 															AS "IdEspecieGarantia"
			, p_IdEmpresa 													AS "IdEmpresa"
			, 0 															AS "IdPais"-- DEBERIA SER CHILE O EL PAIS DEL INSTRUMENTO
			, 'N' 															AS "EsNominal" -- depende de la operacion no tengo acceso
			, 0 															AS "FolioExternoReferencia"
			, Cliente."IdCliente" 											AS "IdCliente"
			, Portafolio."IdPortafolio" 									AS "IdPortafolio"
			, 0 															as "IdCuentaCustodioPortafolio"
			, 'PEN' 														AS "IdEstado"
			, 'AGE' 														as "IdTipoCanalIngreso"
			, v_MonedaSistema 												AS "IdMonedaSistema"
			, ''  															AS "IdMonedaAdicional"
			, '0001-01-01' 													as "FechaFactorMonedaAdicional"
			, 0 															as "ValorFactorMonedaAdicional"
			, 0 															AS "CodigoIFRS" 
			, Concepto_Egre 												AS "IdConcepto"
			, 'RETTIT' 														AS "IdTipoOperacion"
			, 0 															as "IdMesa"
			, '' 															AS "IdBolsa" 		-- debo encontrar como sacarla"
			, coalesce(EmpleadoEjecutivo."IdSucursal",0) 					as "IdSucursal" 	-- lo estamos sacndo del ejecutivo del portafolio
			, Portafolio."IdEmpleadoEjecutivo" 								as "IdAgente"		-- lo sacamos del ejecutivo del portafolio
			, 'T0' 															AS "IdCondicionLiquidacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaLiquidacion"
			, 'N' 															as "EsIntraday"
			, 'N' 															AS "UsaGarantiaEspecifica"
			, '' 															AS "Glosa"
			, 0 															AS "IdUsuario"
			, p_IdModulo 													AS "IdModulo"
			, '' 															As "TipoCarteraCorredor"
			, '' 															AS "TipoCarteraNormativa"
			, '0001-01-01' 													AS "FechaCurse"
			, '' 															AS "TipoFactorAjuste"
			, 0 															AS "ValorFactorAjusteaFechaCurse"
			, 0 															AS "CostoEnFactorAjuste"
			, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
			, COALESCE(Instrumento."IdTipoInstrumento",'') 					as "IdTipoInstrumento"
			, COALESCE(Instrumento."IdInstrumento",0) 						as "IdInstrumento"
			, 0 															as "IdInstrumentoAnterior"
			, TRIM(GES_TBCTCIIF."NEMO")  									AS "Nemotecnico"
			, TRIM(GES_TBCTCIIF."EMISOR") 									as "IdEmisor"
			, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 					AS "NemoSVS" -- 
			, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
			,'' 															as "EsPagaderoEn"
			, GES_TBCTCIIF."FEC_EMI"  										as "FechaEmisionInstrumento"
			, GES_TBCTCIIF."FEC_VCTO"  										as "FechaVencimientoInstrumento"
			, GES_TBCTCIIF."IND_FUNGIBILIDAD"								AS "EsFungible"
			, Instrumento."ValorizaPor" 									AS "ValorizaPor"
			, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) AS "TipoValorizacion"
			,'N' 															as "TieneCorteDeCupon"
			, COALESCE(Instrumento."IdMoneda",'') 							as "IdMonedaInstrumento"
			, 0 															AS "Tamano"
			, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END AS "IdBoveda"
			, 0 															AS "CuentaBoveda"
			, COALESCE(Instrumento."ISIN",'0') 								as "ISIN"
			, '0001-01-01' 													as "FechaFactorMonedaInstrumento"
			, 0																as "ValorFactorMonedaInstrumento"
			, ABS(GES_TBCTCIIF."CAN_ACTUAL") 								AS "Cantidad" 
			, 0 															AS "CorteMinimo"
			, GES_TBCTCIIF."VAL_RESC" 										AS "ValorRescate"
			, 0 															AS "PrecioCompra"
			, GES_TBCTCIIF."TASA_PROM_COMP" 								as "TasaCompra"
			, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompra"
			, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompraMonedaInstrumento"
			, 0 															as "MontoCompraMonedaAdicional"
			, p_IdCarga 													AS "IdCarga"
			, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdSecuenciaCarteraMigracion"
			, GES_TBCTCIIF."COD_PER" 										AS "CodigoPeriodo"
			, GES_TBCTCIIF."RUT_CLI" 										AS "RUT_CLI"
			, GES_TBCTCIIF."SEC_RUT_CLI" 									AS "SEC_RUT_CLI"
			, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "SEC_CAR_INVER"
			, GES_TBCTCIIF."COD_PER" 										AS "COD_PER"
			, GES_TBCTCIIF."FOLIO_COMP_ADJ" 								AS "FOLIO_COMP_ADJ"
			, GES_TBCTCIIF."TIPO_COMP_ADJ" 									AS "TIPO_COMP_ADJ"
			, 0 															AS "LIN_COMP_ADJ"
			, 0 															AS "LIN_ORDEN"
			, 0 															AS "SEC_ORDEN"
			, GES_TBCTCIIF."TIPO_CARTERA" 									as "TIPO_CARTERA"
			, 0 															AS "ID_ERROR"
			, 0 															AS "SEC_MOVTO"
			, 0 															AS "CAN_GARANTIA"
  FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
  INNER JOIN 
       (SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
              , SUM(A."CAN_POSITIVO") As "CAN_POS"
              , SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
              , SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
              , SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
          FROM 
               (SELECT PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR", 
               			PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
     		      FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
			     WHERE PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      		       AND PO."CAN_ACTUAL" > 0
      		       
 	             UNION  ALL
       
                SELECT NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",   
                		0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
     	          FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
		         WHERE NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      	           AND NE."CAN_ACTUAL" < 0
               ) AS A	
         GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
         HAVING (SUM(A."CAN_POSITIVO") = 0 AND SUM(A."CAN_NEGATIVO") < 0)
         ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR"
        ) AS CARCLI
 		ON  CARCLI."COD_PER" 		= GES_TBCTCIIF."COD_PER"
		AND CARCLI."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 
		AND CARCLI."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI"
		AND TRIM(CARCLI."NEMO")		= TRIM(GES_TBCTCIIF."NEMO")
		AND TRIM(CARCLI."TIPO_REAJ")		= TRIM(GES_TBCTCIIF."TIPO_REAJ")
		AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
		AND CARCLI."FEC_VCTO" 	= GES_TBCTCIIF."FEC_VCTO"
		
  INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
  		ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
  		ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
  		ON 	Cliente."IdPersona" = Persona."IdPersona"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
  		ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
  			Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
  LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
  		ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
  LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
  		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
  LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
  		ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
  		Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
  		Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
  		Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
  LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
  		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
  LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
  		ON  TipoComprobante."TipoValor"  ='TipoComprobante'
  			AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
  LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
  		ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
  			AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
  LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
  		ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"

  WHERE GES_TBCTCIIF."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
    AND GES_TBCTCIIF."COD_PER" NOT LIKE '%90%'
	AND GES_TBCTCIIF."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
	AND Cartera."RUT_CLI" IS NULL
	AND GES_TBCTCIIF."CAN_ACTUAL" < 0 ;

-----------------------------------------------------------
----- CARTERAS ACUMULADAS IIF > 0        ------------------
-----------------------------------------------------------
INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"(
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
			,"EGRMontoARecibirAnticipo","EGRTipoCambioObservado","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN"
			,"SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA","SEC_COMPROM")
SELECT		
			  "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL 		as "IdCartera"
			, "BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL 	as "FolioCartera"
			, 0 											as "FolioCarteraRelacionada"
			, 0 											as "FolioGarantiaOrigen"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) as "FechaIngresoRegistro"
			, '0001-01-01' 									as "FechaModificacionRegistro"
			, p_IdEmpresa 									as "IdEmpresa" 
			, GES_TBCTCIIF."FEC_INVER" 						as "FechaOperacion"
			, '0001-01-01' 									as "FechaVencimientoOperacion"
			, GES_TBCTCIIF."PLAZO" 							as "PlazoEnDiasOperacion"
			, Cliente."IdCliente" 							as "IdCliente"
			, Portafolio."IdPortafolio" 					as "IdPortafolio"
			, 0 											as "IdCuentaCustodioPortafolio"
			, 0 											as "IdClienteRelacionado"
			, 0 											as "IdPortafolioRelacionado"
			, 'VIG' 										as "IdEstado"
			, 'INIF' 										as "IdTipoProducto"
			, 'AGE' 										as "IdTipoCanalIngreso"
			, v_MonedaSistema 								as "IdMonedaSistema" 
			,'' 											as "IdMonedaAdicional"
			,'0001-01-01' 									as "FechaFactorMonedaAdicional"
			,'0' 											as "ValorFactorMonedaAdicional"
			, 0 											as "IdMovimientoCarteraEnTransito"
			, Concepto_Ingre 								as "IdConcepto"
			, 'INGTIT' 										as "IdTipoOperacion"
			, 0 											as "IdOperacion"
			, coalesce(TipoComprobante."ValorDestino",'') 	as "IdTipoComprobante"
			, coalesce(ComprobanteFacturacion."IdComprobante",0) 			as "FolioComprobante" 
			, coalesce(ComprobanteFacturacion."FEC_ING_COMP",'0001-01-01')	as "FechaComprobante"
			, 0 											as "FolioExternoReferencia" 
			, 0 											as "IdMesa" 
			, 'BCS' 										as "IdBolsa"
			, coalesce(EmpleadoEjecutivo."IdSucursal",0) 	as "IdSucursal"  
			, Portafolio."IdEmpleadoEjecutivo" 				as "IdAgente" 
			, 'T0' 											as "IdCondicionLiquidacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "FechaLiquidacion"
			, 'N' 											as "EsIntraday"
			, '' 											as "UsaGarantiaEspecifica"
			, '' 											as "CobraPrimaAlInicio"
			, '' 											as "IdTipoPrestamo"
			, '' 											as "Glosa"
			, 0  											as "IdUsuario" 
			, p_IdModulo 									as "IdModulo"  
			, '' 											as "TipoCarteraCorredor"
			, '' 											as "TipoCarteraNormativa"
			, '0001-01-01' 									as "FechaCurseInversion"
			, '' 											as "TipoFactorAjuste"
			, 0 											as "ValorFactorAjusteaFechaCurse"
			, 0 											as "CostoInversionEnFactorAjuste"
			, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
			, COALESCE(Instrumento."IdTipoInstrumento",'') 	as "IdTipoInstrumento"
			, COALESCE(Instrumento."IdInstrumento",0) 		as "IdInstrumento" 
			, 0 											as "IdInstrumentoAnterior"
			, TRIM(GES_TBCTCIIF."NEMO")  					as "Nemotecnico"
			, TRIM(GES_TBCTCIIF."EMISOR") 					as "IdEmisor"
			, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 	as "NemoSVS" -- 
			, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
			, '' 											as "EsPagaderoEn"
			, GES_TBCTCIIF."FEC_EMI"  						as "FechaEmisionInstrumento" -- debo calcularla en caso de IF
			, GES_TBCTCIIF."FEC_VCTO"  						as "FechaVencimientoInstrumento" 
			, GES_TBCTCIIF."IND_FUNGIBILIDAD" 				as "EsFungible"
			, Instrumento."ValorizaPor" 					as "ValorizaPor"
			, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) 	as "TipoValorizacion"						
			, 0 											as "CodigoIFRS" 
			, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END 	AS "IdBoveda" 
			, 0 											as "CuentaBoveda" 
			, COALESCE(Instrumento."IdMoneda",'') 			as "IdMonedaInstrumento"
			, 0 											as "Tamano" 
			, '' 											as "TIFAfectoBeneficio"
			, 0 											as "TIFTasa"
			, COALESCE(Instrumento."ISIN",'') 				as "ISIN"
			, '0001-01-01' 									as "FechaFactorMonedaInstrumento"
			, 0 											as "ValorFactorMonedaInstrumento"
			, CARCLI."CAN_RES"								as "Cantidad"
			, 0 											as "CorteMinimo" 
			, CARCLI."VAL_RES" 								as "ValorRescate"
			, 0 											as "PrecioCompra"
			, GES_TBCTCIIF."TASA_PROM_COMP" 				as "TasaCompra"
			, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompra"
			, GES_TBCTCIIF."MONTO_INI_AJUS" 				as "MontoCompraMonedaInstrumento"
			, 0 											as "MontoCompraMonedaAdicional"
			, 0 											as "TasaAgente"
			, 0 											as "PrecioAPlazo"
			, 0 											as "MontoAPlazo"
			, 0 											as "MontoAPlazoMonedaInstrumento"
			, 0 											as "MontoAPlazoMonedaAdicional"
			, GES_TBCTCIIF."FEC_INVER"  					as "FechaEfectiva" 
			, 0 											as "IdPlaza"
			, 0 											as "IdFixing"
			, '0001-01-01' 									as "FechaFixing"
			, '0:00:00' 									as "HoraFixing"
			, 0 											as "IdModalidadCumplimiento"
			, 0 											as "IdReferenciaMercado"
			, 0 											as "IdMonedaPrincipal"
			, 0 											as "IdMonedaSecundaria"
			, 0 											as "ValorTipoCambioCierre"
			, 0 											as "ValorParidadCierre"
			, 0 											as "ValorTipoCambioCosto"
			, 0 											as "ValorParidadCosto"
			, 0 											as "MontoMonedaPrincipal"
			, 0 											as "MontoMonedaSecundaria"
			, p_IdCarga 									as "IdCarga"     
			, GES_TBCTCIIF."SEC_CAR_INVER" 					as "IdSecuenciaCarteraMigracion"
			, GES_TBCTCIIF."COD_PER" 						as "CodigoPeriodo"
			, '' 											as "INGGlosa"
			, 0 											as "INGTasaAgente"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "INGFechaOperacion"
			, 'T0' 											as "INGIdCondicionLiquidacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	as "INGFechaLiquidacion"
			, CARCLI."CAN_RES" 								as "INGCantidad"
			, CARCLI."VAL_RES"								as "INGValorRescate"
			, 0 											as "INGPrecioPromedioCompra"
			, 0 											as "INGMontoPrecioPromedioCompra"
			, 0 											as "INGMontoPrecioPromedioMonedaInstrumento"
			, 0 											as "INGMontoPrecioPromedioMonedaAdicional"
			, 0 											as "INGPrecioAPlazo"
			, 0 											as "INGMontoAPlazo"
			, 0 											as "INGMontoAPlazoMonedaInstrumento"
			, 0 											as "INGMontoAPlazoMonedaAdicional"
			, 0 											as "INGEsPuntos"
			, 0 											as "INGValorTipoCambioSpot"
			, 0 											as "INGValorTipoCambioCotizador"
			, 0 											as "INGTasaPuntosMonedaPrincipal"
			, 0 											as "INGTasaPuntosMonedaSecundaria"
			, 0 											as "INGTicketValorizacion"
			, 'N' 											as "INGValorizacionConProblemas"
			, '0001-01-01' 									as "INGFechaValorizacionCartera"
			, '0001-01-01' 									as "INGFechaValorizacionAnterior"
			, '0001-01-01' 									as "INGFechaValorizacionActual"
			, '0001-01-01' 									as "INGFechaValorizacionProximo"
			, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoAnterior"
			, 0 											as "INGValorFactorMonedaInstrumentoAnterior"
			, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoActual"
			, 0 											as "INGValorFactorMonedaInstrumentoActual"
			, '0001-01-01' 									as "INGFechaFactorMonedaInstrumentoProximo"
			, 0 											as "INGValorFactorMonedaInstrumentoProximo"
			, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalAnterior"
			, 0 											as "INGValorFactorMonedaAdicionalAnterior"
			, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalActual"
			, 0 											as "INGValorFactorMonedaAdicionalActual"
			, '0001-01-01' 									as "INGFechaFactorMonedaAdicionalProximo"
			, 0 											as "INGValorFactorMonedaAdicionalProximo"
			, 0 											as "INGPrecioCompra"
			, GES_TBCTCIIF."TASA_PROM_COMP" 				as "INGTasaCompra"
			, 0 											as "INGMontoATasaCompraAnterior"
			, 0 											as "INGMontoInteresCompraAnterior"
			, 0 											as "INGMontoReajusteCompraAnterior"
			, 0 											as "INGMontoCompraAnteriorMonedaInstrumento"
			, 0 											as "INGMontoInteresCompraAnteriorMonedaInstrumento"
			, 0 											as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
			, 0 											as "INGMontoCompraAnteriorMonedaAdicional"
			, 0 											as "INGMontoInteresCompraAnteriorMonedaAdicional"
			, 0 											as "INGMontoReajusteCompraAnteriorMonedaAdicional"
			, 0 											as "INGDurationCompraAnterior"
			, 0 											as "INGConvexidadCompraAnterior"
			, 0 											as "INGMontoCompraAnteriorPibMas"
			, 0 											as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
			, 0 											as "INGMontoCompraAnteriorPibMasMonedaAdicional"
			, 0 											as "INGMontoCompraAnteriorPibMenos"
			, 0 											as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
			, 0 											as "INGMontoCompraAnteriorPibMenosMonedaAdicional"
			, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaCompraActual"
			, 0 											as "INGMontoCompraActualMonedaInstrumento"
			, 0 											as "INGMontoCompraActualMonedaAdicional"
			, 0 											as "INGDurationCompraActual"
			, 0 											as "INGConvexidadCompraActual"
			, 0 											as "INGMontoInteresCorridoCompraActual"
			, 0 											as "INGMontoReajusteCorridoCompraActual"
			, 0 											as "INGMontoInteresCorridoCompraActualMonedaInstrumento"
			, 0 											as "INGMontoReajusteCorridoCompraActualMonedaInstrumento"
			, 0 											as "INGMontoInteresCorridoCompraActualMonedaAdicional"
			, 0 											as "INGMontoReajusteCorridoCompraActualMonedaAdicional"
			, 0 											as "INGMontoCompraActualPibMas"
			, 0 											as "INGMontoCompraActualPibMasMonedaInstrumento"
			, 0 											as "INGMontoCompraActualPibMasMonedaAdicional"
			, 0 											as "INGMontoCompraActualPibMenos"
			, 0 											as "INGMontoCompraActualPibMenosMonedaInstrumento"
			, 0 											as "INGMontoCompraActualPibMenosMonedaAdicional"
			, 0 											as "INGMontoATasaCompraProximo"
			, 0 											as "INGMontoInteresCompraProximo"
			, 0 											as "INGMontoReajusteCompraProximo"
			, 0 											as "INGMontoCompraProximoMonedaInstrumento"
			, 0 											as "INGMontoInteresCompraProximoMonedaInstrumento"
			, 0 											as "INGMontoReajusteCompraProximoMonedaInstrumento"
			, 0 											as "INGMontoCompraProximoMonedaAdicional"
			, 0 											as "INGMontoInteresCompraProximoMonedaAdicional"
			, 0 											as "INGMontoReajusteCompraProximoMonedaAdicional"
			, 0 											as "INGDurationCompraProximo"
			, 0 											as "INGConvexidadCompraProximo"
			, '0001-01-01' 									as "INGFechaPrecioTasaMercadoAnterior"
			, 0 											as "INGFuenteTasaPrecioMercadoAnterior"
			, 0 											as "INGPrecioMercadoAnterior"
			, 0 											as "INGTasaMercadoAnterior"
			, 0 											as "INGMontoATasaMercadoAnterior"
			, 0 											as "INGMontoInteresMercadoAnterior"
			, 0 											as "INGMontoReajusteMercadoAnterior"
			, 0 											as "INGMontoMercadoAnteriorMonedaInstrumento"
			, 0 											as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
			, 0 											as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
			, 0 											as "INGMontoMercadoAnteriorMonedaAdicional"
			, 0 											as "INGMontoInteresMercadoAnteriorMonedaAdicional"
			, 0 											as "INGMontoReajusteMercadoAnteriorMonedaAdicional"
			, 0 											as "INGDurationMercadoAnterior"
			, 0 											as "INGConvexidadMercadoAnterior"
			, 0 											as "INGMontoMercadoAnteriorPibMas"
			, 0 											as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
			, 0 											as "INGMontoMercadoAnteriorPibMasMonedaAdicional"
			, 0 											as "INGMontoMercadoAnteriorPibMenos"
			, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
			, 0 											as "INGMontoMercadoAnteriorPibMenosMonedaAdicional"
			, '0001-01-01' 									as "INGFechaPrecioTasaMercadoActual"
			, '' 											as "INGFuenteTasaPrecioMercadoActual"
			, 0												as "INGPrecioMercadoActual"
			, GES_TBCTCIIF."TASA_MED_CIE" 					as "INGTasaMercadoActual"  
			, GES_TBCTCIIF."MONTO_INI_AJUS"					as "INGMontoATasaMercadoActual"
			, 0 											as "INGMontoMercadoActualMonedaInstrumento"
			, 0 											as "INGMontoMercadoActualMonedaAdicional"
			, 0 											as "INGDurationMercadoActual"
			, 0 											as "INGConvexidadMercadoActual"
			, 0 											as	"INGMontoInteresCorridoMercadoActual"
			, 0 											as "INGMontoReajusteCorridoMercadoActual"
			, 0 											as "INGMontoInteresCorridoMercadoActualMonedaInstrumento"
			, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaInstrumento"
			, 0 											as "INGMontoInteresCorridoMercadoActualMonedaAdicional"
			, 0 											as "INGMontoReajusteCorridoMercadoActualMonedaAdicional"
			, 0 											as "INGMontoMercadoActualPibMas"
			, 0 											as "INGMontoMercadoActualPibMasMonedaInstrumento"
			, 0 											as "INGMontoMercadoActualPibMasMonedaAdicional"
			, 0 											as "INGMontoMercadoActualPibMenos"
			, 0 											as "INGMontoMercadoActualPibMenosMonedaInstrumento"
			, 0 											as "INGMontoMercadoActualPibMenosMonedaAdicional"
			, '0001-01-01' 									as "INGFechaPrecioTasaMercadoProximo"
			, 0 											as "INGFuenteTasaPrecioMercadoProximo"
			, 0 											as "INGPrecioMercadoProximo"
			, 0 											as "INGTasaMercadoProximo"
			, 0 											as "INGMontoATasaMercadoProximo"
			, 0 											as "INGMontoInteresMercadoProximo"
			, 0 											as "INGMontoReajusteMercadoProximo"
			, 0 											as "INGMontoMercadoProximoMonedaInstrumento"
			, 0 											as "INGMontoInteresMercadoProximoMonedaInstrumento"
			, 0 											as "INGMontoReajusteMercadoProximoMonedaInstrumento"
			, 0 											as "INGMontoMercadoProximoMonedaAdicional"
			, 0 											as "INGMontoInteresMercadoProximoMonedaAdicional"
			, 0 											as "INGMontoReajusteMercadoProximoMonedaAdicional"
			, 0 											as "INGDurationMercadoProximo"
			, 0 											as "INGConvexidadMercadoProximo"
			, 0 											as "INGTasaCorredora"
			, '0001-01-01' 									as "INGFechaTasaCorredora"
			, 0 											as "INGMontoATasaCorredora"
			, 0 											as "INGMontoATasaCorredoraMonedaInstrumento"
			, 0 											as "INGMontoATasaCorredoraMonedaAdicional"
			, '0001-01-01' 									as "INGFechaCorteCupon"
			, 0 											as "INGNumeroCupon"
			, 0 											as "INGIdMonedaPagoCupon"
			, 0 											as "INGMontoCorteCupon"
			, '0001-01-01' 									as "INGFechaPagoCorteCupon"
			, 0 											as "INGMontoCapitalCorteCupon"
			, 0 											as "INGMontoInteresCorteCupon"
			, 0 											as "INGMontoReajusteCorteCupon"
			, 0 											as "INGMontoCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoCapitalCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoInteresCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoReajusteCorteCuponMonedaInstrumento"
			, 0 											as "INGMontoCorteCuponMonedaAdicional"
			, 0 											as "INGMontoCapitalCorteCuponMonedaAdicional"
			, 0 											as "INGMontoInteresCorteCuponMonedaAdicional"
			, 0 											as "INGMontoReajusteCorteCuponMonedaAdicional"
			, 0 											as "INGMontoPresenteTIF"
			, 0 											as "INGMontoInteresTIF"
			, 0 											as "INGMontoReajusteTIF"
			, 0 											as "INGMontoInteresAcumTIF"
			, 0 											as "INGMontoReajusteAcumTIF"
			, 0 											as "INGMontoPresenteTIFMonedaInstrumento"
			, 0 											as "INGMontoInteresTIFMonedaInstrumento"
			, 0 											as "INGMontoReajusteTIFMonedaInstrumento"
			, 0 											as "INGMontoInteresAcumTIFMonedaInstrumento"
			, 0 											as "INGMontoReajusteAcumTIFMonedaInstrumento"
			, 0 											as "INGMontoPresenteTIFMonedaAdicional"
			, 0 											as "INGMontoInteresTIFMonedaAdicional"
			, 0 											as "INGMontoReajusteTIFMonedaAdicional"
			, 0 											as "INGMontoInteresAcumTIFMonedaAdicional"
			, 0 											as "INGMontoReajusteAcumTIFMonedaAdicional"
			, 0 											as "INGMontoMonedaPrincipal"
			, 0 											as "INGMontoMonedaSecundaria"
			, 0 											as "INGMontoDiferido"
			, 0 											as "INGMontoUtilidadDiferida"
			, 0 											as "INGMontoPerdidaDiferida"
			, 0 											as "INGMontoMargen"
			, 0 											as "INGMontoValorRazonableActivo"
			, 0 											as "INGMontoValorRazonablePasivo"
			, 0 											as "INGMontoValorRazonableNeto"
			, 0 											as "INGTasaValorRazonableActivo"
			, 0 											as "INGTasaValorRazonablePasivo"
			, 0 											as "INGMontoDevengoTipoCambio"
			, 0 											as "INGMontoDevengoTipoCambioAnterior"
			, 0 											as "INGMontoDiferidoDiario"
			, 0 											as "INGMontoDiferidoAcumulado"
			, 0 											as "INGMontoMatriz"
			, 0 											as "EGRTicketValorizacion"
			, ''											as "EGRValorizacionConProblemas"
			, '0001-01-01' 									as "EGRFechaFactorMonedaInstrumento"
			, 0 											as "EGRValorFactorMonedaInstrumento"
			, '0001-01-01' 									as "EGRFechaFactorMonedaAdicional"
			, 0 											as "EGRValorFactorMonedaAdicional"
			, '' 											as "EGRGlosa"
			, '0001-01-01'									as "EGRFechaOperacion"
			, '' 											as "EGRIdTipoOperacion"
			, 0 											as "EGRIdOperacion"
			, 0 											as "EGRIdMovimientoCarteraEnTransito"
			, '' 											as "EGRIdTipoCanalIngreso"
			, ''  											as "EGRIdCondicionLiquidacion"
			, '0001-01-01'									as "EGRFechaLiquidacion"
			, '' 											as "EGRIdTipoComprobante"
			, 0  											as "EGRFolioComprobante"
			, '0001-01-01' 									as "EGRFechaComprobante"
			, 0 											as "EGRFolioExternoReferencia"
			, '' 											as "EGRIdModulo"
			, ''											as "EGRIdConcepto" 
			, 0 											as "EGRIdUsuario"
			, 0 											as "EGRIdSucursal" 
			, 0												as "EGRIdAgente"
			, '0001-01-01' 									as "EGRFechaCurseRetiro"
			, 0 											as "EGRCostoRetiroenFactorAjuste"
			, 0 											as "EGRValorFactorAjusteaFechaRetiro"
			, 0												as "EGRCantidad"
			, 0												as "EGRValorRescate" 
			, 0 											as "EGRPrecio" 
			, 0 											as "EGRTasa" 
			, 0 											as "EGRMonto" 
			, 0 											as "EGRMontoMonedaInstrumento" 
			, 0 											as "EGRMontoMonedaAdicional"
			, 0 											as "EGRSpread" 
			, '0001-01-01' 									as "EGRFechaPrecioTasaMercado"
			, ''											as "EGRFuenteTasaPrecioMercado"
			, 0 											as "EGRPrecioMercado"
			, 0 											as "EGRTasaMercado"
			, 0 											as "EGRMontoATasaMercado"
			, 0 											as "EGRMontoMercadoMonedaInstrumento"
			, 0 											as "EGRMontoMercadoMonedaAdicional"
			, 0 											as "EGRSpreadMercado"
			, 0 											as "EGRPrecioAPlazo"
			, 0 											as "EGRMontoAPlazo"
			, 0 											as "EGRMontoAPlazoMonedaInstrumento"
			, 0 											as "EGRMontoAPlazoMonedaAdicional"
			, 0 											as "EGRMontoPresenteTIF"
			, 0 											as "EGRMontoInteresTIF"
			, 0 											as "EGRMontoReajusteTIF"
			, 0 											as "EGRMontoInteresAcumTIF"
			, 0 											as "EGRMontoReajusteAcumTIF"
			, 0 											as "EGRMontoPresenteTIFMonedaInstrumento"
			, 0 											as "EGRMontoInteresTIFMonedaInstrumento"
			, 0 											as "EGRMontoReajusteTIFMonedaInstrumento"
			, 0 											as "EGRMontoInteresAcumTIFMonedaInstrumento"
			, 0 											as "EGRMontoReajusteAcumTIFMonedaInstrumento"
			, 0 											as "EGRMontoPresenteTIFMonedaAdicional"
			, 0 											as "EGRMontoInteresTIFMonedaAdicional"
			, 0 											as "EGRMontoReajusteTIFMonedaAdicional"
			, 0 											as "EGRMontoInteresAcumTIFMonedaAdicional"
			, 0 											as "EGRMontoReajusteAcumTIFMonedaAdicional"
			, 0 											as "EGRIdSpotAsociado"
			, 0 											as "EGRMontoMonedaPrincipal"
			, 0 											as "EGRMontoMonedaSecundaria"
			, 0 											as "EGRIdMonedaLiquidacion"
			, 0 											as "EGRMontoLiquidacion"
			, 0 											as "EGRMontoLiquidacionMonedaPrincipal"
			, 0 											as "EGRValorTipoCambioSpot"
			, 0 											as "EGRValorParidad"
			, 0 											as "EGRValorTipoCambioCotizador"
			, 0 											as "EGREsPuntos"
			, 0 											as "EGRTasaPuntosMonedaPrincipal"
			, 0 											as "EGRTasaPuntosMonedaSecundaria"
			, 0 											as "EGRTasaDescuentoAnticipo"
			, 0 											as "EGRTipoCambioAnticipo"
			, 0 											as "EGRMontoARecibirAnticipo"
			, 0 											as "EGRTipoCambioObservado"
			, GES_TBCTCIIF."RUT_CLI" 						as "RUT_CLI"
			, GES_TBCTCIIF."SEC_RUT_CLI" 					as "SEC_RUT_CLI"
			, GES_TBCTCIIF."SEC_CAR_INVER" 					as "SEC_CAR_INVER"
			, GES_TBCTCIIF."COD_PER" 						as "COD_PER"
			, GES_TBCTCIIF."FOLIO_COMP_ADJ" 				as "FOLIO_COMP_ADJ"
			, GES_TBCTCIIF."TIPO_COMP_ADJ" 					as "TIPO_COMP_ADJ"
			, 0 											as "LIN_COMP_ADJ"
			, 0 											as "LIN_ORDEN"
			, 0 											as "SEC_ORDEN"
			, GES_TBCTCIIF."TIPO_CARTERA" 					as "TIPO_CARTERA"
			, 0 											as "ID_ERROR"
			, 0 											as "SEC_MOVTO"
			, 0 											as "CAN_GARANTIA"
			, 0 											as "SEC_COMPROM"
  FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
  INNER JOIN 
       (SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR",MAX(A."SEC_CAR_INVER") AS "SEC"
              , SUM(A."CAN_POSITIVO") As "CAN_POS"
              , SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
              , SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
              , SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
              , SUM(A."CAN_POSITIVO" + A."CAN_NEGATIVO") AS "CAN_RES"
              , SUM(A."VALRES_POSITIVO" - A."VALRES_NEGATIVO") AS "VAL_RES"
          FROM 
               (SELECT PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR",PO."SEC_CAR_INVER",
               			PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
     		      FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
			     WHERE PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      		       AND PO."CAN_ACTUAL" > 0
      		       
 	             UNION  ALL
       
                SELECT NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",NE."SEC_CAR_INVER",
                		0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
     	          FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
		         WHERE NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      	           AND NE."CAN_ACTUAL" < 0
               ) AS A	
         GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
         HAVING SUM(A."CAN_POSITIVO") + SUM(A."CAN_NEGATIVO") >  0
            AND SUM(A."CAN_POSITIVO") > 0 
            AND SUM(A."CAN_NEGATIVO") < 0
          ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
        ) AS CARCLI
		ON  CARCLI."COD_PER" 		= GES_TBCTCIIF."COD_PER"
		AND CARCLI."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 
		AND CARCLI."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI"
		AND TRIM(CARCLI."NEMO")		= TRIM(GES_TBCTCIIF."NEMO")
		AND TRIM(CARCLI."TIPO_REAJ")		= TRIM(GES_TBCTCIIF."TIPO_REAJ")
		AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
		AND CARCLI."FEC_VCTO" 	= GES_TBCTCIIF."FEC_VCTO"
			
  INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
  		ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
  		ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
  		ON 	Cliente."IdPersona" = Persona."IdPersona"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
  		ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
  			Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
  LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
  		ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
  LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
  		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
  LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
  		ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
  		Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
  		Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
  		Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
  LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
  		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
  LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
  		ON  TipoComprobante."TipoValor"  ='TipoComprobante'
  			AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
  LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
  		ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
  			AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
  LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
  		ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"
											
  WHERE GES_TBCTCIIF."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
 	AND GES_TBCTCIIF."COD_PER" NOT LIKE '%90%'
	AND GES_TBCTCIIF."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
	AND Cartera."RUT_CLI" IS NULL
	AND GES_TBCTCIIF."SEC_CAR_INVER" = CARCLI."SEC";

-----------------------------------------------------------
----- CARTERAS ACUMULADAS IIF < 0        ------------------
-----------------------------------------------------------
INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito"
			(	
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
			,"RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR"
			,"SEC_MOVTO","CAN_GARANTIA"
			)

select
			 "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL AS "IdMovimientoCarteraEnTransito"
			,0 																AS "IdMovimientoCarteraEnTransitoRelacionado"
			,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaIngresoRegistro"
			,(TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaCertificacionRegistro"
			,'0001-01-01' 													AS "FechaAnulacionRegistro"
			, 0 															AS "IdUsuarioIngreso"
			, 0 															AS "IdUsuarioCertificacion"
			, 0 															AS "IdUsuarioAnulacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaOperacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaVencimientoOperacion"
			, GES_TBCTCIIF."PLAZO"  										AS "PlazoEnDiasOperacion"
			, 0 															AS "FolioCustodia"
			, coalesce(TipoComprobante."ValorDestino",'') 					AS "IdTipoComprobante"
			, GES_TBCTCIIF.FOLIO_COMP_ADJ 									as "FolioComprobante"
			, 'N' 															AS "EsRegistroGeneradoPorSistema"-- no lo se
			, 'S' 															as "MueveCustodia"
			, 'N' 															as "PermiteCertificacionPorUsuario"
			, 'CUS' 														AS "OrigenCustodia"
			, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdCustodiaOrigen"
			, 'RETTIT' 														AS "IdTipoOperacionOrigen"
			, 0 															AS "FolioCarteraGarantizada"
			, 0 															AS "IdInstrumentoCarteraGarantizada"
			, 'N' 															AS "EsEspecifica"
			, 0 															AS "IdContraparteGarantia" ---si la operacion fuera de garantia y estuviera en transito
			, '' 															AS "RazonSocialContraparteGarantia" -- si la operacion fuera de garantia
			, '' 															AS "CuentaContraparteGarantia" -- si la operacion fuera de garantia
			, 0 															AS "IdOrigenGarantia" 
			, 0 															AS "IdEspecieGarantia"
			, p_IdEmpresa 													AS "IdEmpresa"
			, 0 															AS "IdPais"-- DEBERIA SER CHILE O EL PAIS DEL INSTRUMENTO
			, 'N' 															AS "EsNominal" -- depende de la operacion no tengo acceso
			, 0 															AS "FolioExternoReferencia"
			, Cliente."IdCliente" 											AS "IdCliente"
			, Portafolio."IdPortafolio" 									AS "IdPortafolio"
			, 0 															as "IdCuentaCustodioPortafolio"
			, 'PEN' 														AS "IdEstado"
			, 'AGE' 														as "IdTipoCanalIngreso"
			, v_MonedaSistema 												AS "IdMonedaSistema"
			, ''  															AS "IdMonedaAdicional"
			, '0001-01-01' 													as "FechaFactorMonedaAdicional"
			, 0 															as "ValorFactorMonedaAdicional"
			, 0 															AS "CodigoIFRS" 
			, Concepto_Egre 												AS "IdConcepto"
			, 'RETTIT' 														AS "IdTipoOperacion"
			, 0 															as "IdMesa"
			, '' 															AS "IdBolsa" 		-- debo encontrar como sacarla"
			, coalesce(EmpleadoEjecutivo."IdSucursal",0) 					as "IdSucursal" 	-- lo estamos sacndo del ejecutivo del portafolio
			, Portafolio."IdEmpleadoEjecutivo" 								as "IdAgente"		-- lo sacamos del ejecutivo del portafolio
			, 'T0' 															AS "IdCondicionLiquidacion"
			, (TO_DATE(CONCAT(GES_TBCTCIIF.COD_PER,'-01'),'YYYY-MM-DD')) 	AS "FechaLiquidacion"
			, 'N' 															as "EsIntraday"
			, 'N' 															AS "UsaGarantiaEspecifica"
			, '' 															AS "Glosa"
			, 0 															AS "IdUsuario"
			, p_IdModulo 													AS "IdModulo"
			, '' 															As "TipoCarteraCorredor"
			, '' 															AS "TipoCarteraNormativa"
			, '0001-01-01' 													AS "FechaCurse"
			, '' 															AS "TipoFactorAjuste"
			, 0 															AS "ValorFactorAjusteaFechaCurse"
			, 0 															AS "CostoEnFactorAjuste"
			, COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento"
			, COALESCE(Instrumento."IdTipoInstrumento",'') 					as "IdTipoInstrumento"
			, COALESCE(Instrumento."IdInstrumento",0) 						as "IdInstrumento"
			, 0 															as "IdInstrumentoAnterior"
			, TRIM(GES_TBCTCIIF."NEMO")  									AS "Nemotecnico"
			, TRIM(GES_TBCTCIIF."EMISOR") 									as "IdEmisor"
			, TRIM(COALESCE(GES_TBCTCIIF."NEMO_SVS",'')) 					AS "NemoSVS"  
			, CASE WHEN GES_TBCTCIIF."TIPO_REAJ" ='NR' THEN 'CLP' WHEN GES_TBCTCIIF."TIPO_REAJ" ='US' THEN 'USD' WHEN GES_TBCTCIIF."TIPO_REAJ" ='UF' THEN 'UF'  WHEN GES_TBCTCIIF."TIPO_REAJ" ='EU' THEN 'EUR'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IC' THEN 'IC'WHEN GES_TBCTCIIF."TIPO_REAJ" ='IV' THEN 'IVP'WHEN GES_TBCTCIIF."TIPO_REAJ" NOT IN ('NR','US','UF','EU','IC','IV') THEN '' END as "IdTipoReajuste"
			,'' 															as "EsPagaderoEn"
			, GES_TBCTCIIF."FEC_EMI"  										as "FechaEmisionInstrumento"
			, GES_TBCTCIIF."FEC_VCTO"  										as "FechaVencimientoInstrumento"
			, GES_TBCTCIIF."IND_FUNGIBILIDAD"								AS "EsFungible"
			, Instrumento."ValorizaPor" 									AS "ValorizaPor"
			, COALESCE(TipoValorizacionPorTipoInstrumento."IdTipoValorizacionPorTipoInstrumento",0) AS "TipoValorizacion"
			,'N' 															as "TieneCorteDeCupon"
			, COALESCE(Instrumento."IdMoneda",'') 							as "IdMonedaInstrumento"
			, 0 															AS "Tamano"
			, CASE WHEN GES_TBCTCIIF."COD_BOVEDA"= 'CIA' THEN 'COM' ELSE GES_TBCTCIIF."COD_BOVEDA"END AS "IdBoveda"
			, 0 															AS "CuentaBoveda"
			, COALESCE(Instrumento."ISIN",'0') 								as "ISIN"
			, '0001-01-01' 													as "FechaFactorMonedaInstrumento"
			, 0																as "ValorFactorMonedaInstrumento"
			, ABS(CARCLI."CAN_RES") 										AS "Cantidad" 
			, 0 															AS "CorteMinimo"
			, ABS(CARCLI."VAL_RES")											AS "ValorRescate"
			, 0 															AS "PrecioCompra"
			, GES_TBCTCIIF."TASA_PROM_COMP" 								as "TasaCompra"
			, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompra"
			, ABS(GES_TBCTCIIF."MONTO_INI_AJUS") 							AS "MontoCompraMonedaInstrumento"
			, 0 															as "MontoCompraMonedaAdicional"
			, p_IdCarga 													AS "IdCarga"
			, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "IdSecuenciaCarteraMigracion"
			, GES_TBCTCIIF."COD_PER" 										AS "CodigoPeriodo"
			, GES_TBCTCIIF."RUT_CLI" 										AS "RUT_CLI"
			, GES_TBCTCIIF."SEC_RUT_CLI" 									AS "SEC_RUT_CLI"
			, GES_TBCTCIIF."SEC_CAR_INVER" 									AS "SEC_CAR_INVER"
			, GES_TBCTCIIF."COD_PER" 										AS "COD_PER"
			, GES_TBCTCIIF."FOLIO_COMP_ADJ" 								AS "FOLIO_COMP_ADJ"
			, GES_TBCTCIIF."TIPO_COMP_ADJ" 									AS "TIPO_COMP_ADJ"
			, 0 															AS "LIN_COMP_ADJ"
			, 0 															AS "LIN_ORDEN"
			, 0 															AS "SEC_ORDEN"
			, GES_TBCTCIIF."TIPO_CARTERA" 									as "TIPO_CARTERA"
			, 0 															AS "ID_ERROR"
			, 0 															AS "SEC_MOVTO"
			, 0 															AS "CAN_GARANTIA"
 FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS GES_TBCTCIIF
  INNER JOIN 
       (SELECT  A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR",MAX(A."SEC_CAR_INVER") AS "SEC"
              , SUM(A."CAN_POSITIVO") As "CAN_POS"
              , SUM(A."VALRES_POSITIVO") As "VALRES_POS" 
              , SUM(A."CAN_NEGATIVO") AS "CAN_NEG"
              , SUM(A."VALRES_NEGATIVO") AS "VALRES_NEG"
              , SUM(A."CAN_POSITIVO" + A."CAN_NEGATIVO") AS "CAN_RES"
              , SUM(A."VALRES_POSITIVO" - A."VALRES_NEGATIVO") AS "VAL_RES"
          FROM 
               (SELECT PO."COD_PER",PO."RUT_CLI",PO."SEC_RUT_CLI",PO."NEMO",PO."FEC_VCTO",PO."TIPO_REAJ",PO."EMISOR",PO."SEC_CAR_INVER",
               			PO."CAN_ACTUAL" AS "CAN_POSITIVO",PO."VAL_RESC" AS "VALRES_POSITIVO" , 0 AS "CAN_NEGATIVO" ,0 AS "VALRES_NEGATIVO" 
     		      FROM "BSCL_VIRTUAL"."GES_TBCTCIIF"  AS PO
			     WHERE PO."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      		       AND PO."CAN_ACTUAL" > 0
      		       
 	             UNION  ALL
       
                SELECT NE."COD_PER",NE."RUT_CLI",NE."SEC_RUT_CLI",NE."NEMO",NE."FEC_VCTO",NE."TIPO_REAJ",NE."EMISOR",NE."SEC_CAR_INVER",
                		0 AS "CAN_POSITIVO",0 AS "VALRES_POSITIVO" , NE."CAN_ACTUAL" AS "CAN_NEGATIVO" ,NE."VAL_RESC" AS "VALRES_NEGATIVO"  
     	          FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" AS NE
		         WHERE NE."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
      	           AND NE."CAN_ACTUAL" < 0
               ) AS A	
         GROUP BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
         HAVING SUM(A."CAN_POSITIVO") + SUM(A."CAN_NEGATIVO") <  0
            AND SUM(A."CAN_POSITIVO") > 0 
            AND SUM(A."CAN_NEGATIVO") < 0
          ORDER BY A."COD_PER",A."RUT_CLI",A."SEC_RUT_CLI",A."NEMO",A."FEC_VCTO",A."TIPO_REAJ",A."EMISOR" 
        ) AS CARCLI
		ON  CARCLI."COD_PER" 		= GES_TBCTCIIF."COD_PER"
		AND CARCLI."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 
		AND CARCLI."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI"
		AND TRIM(CARCLI."NEMO")		= TRIM(GES_TBCTCIIF."NEMO")
		AND TRIM(CARCLI."TIPO_REAJ")		= TRIM(GES_TBCTCIIF."TIPO_REAJ")
		AND TRIM(CARCLI."EMISOR")		= TRIM(GES_TBCTCIIF."EMISOR")
		AND CARCLI."FEC_VCTO" 	= GES_TBCTCIIF."FEC_VCTO"
	
  INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
  		ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
  		ON 	Persona."IdPersona" = DocumentoIdentidad."IdPersona"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
  		ON 	Cliente."IdPersona" = Persona."IdPersona"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
  		ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
  			Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
  LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
  		ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
  LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
  		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoInstrumento."IdTipoInstrumento"))
  LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
  		ON Cartera."COD_PER" = GES_TBCTCIIF."COD_PER" AND 
  		Cartera."RUT_CLI" = GES_TBCTCIIF."RUT_CLI" AND 
  		Cartera."SEC_RUT_CLI" = GES_TBCTCIIF."SEC_RUT_CLI" AND 
  		Cartera."SEC_CAR_INVER" = GES_TBCTCIIF."SEC_CAR_INVER" 
  LEFT  JOIN "BSCL"."BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
  		ON TRIM((Instrumento."IdTipoInstrumento")) = TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento"))
  LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TipoComprobante
  		ON  TipoComprobante."TipoValor"  ='TipoComprobante'
  			AND TipoComprobante."ValorOrigen" = GES_TBCTCIIF."TIPO_COMP_ADJ"
  LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS ComprobanteFacturacion
  		ON 	ComprobanteFacturacion."FOLIO_COMP_FAC" =  GES_TBCTCIIF."FOLIO_COMP_ADJ"
  			AND ComprobanteFacturacion."TIPO_COMP_FAC" = GES_TBCTCIIF."TIPO_COMP_ADJ"
  LEFT  JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
  		ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"

  WHERE GES_TBCTCIIF."COD_PER" = SUBSTRING(p_Periodo_Inicial,1,7)
    AND GES_TBCTCIIF."COD_PER" NOT LIKE '%90%'
	AND GES_TBCTCIIF."RUT_CLI" =  CASE WHEN p_RutCliente ='        0-0' then GES_TBCTCIIF."RUT_CLI" ELSE p_RutCliente END
	AND Cartera."RUT_CLI" IS NULL
	AND GES_TBCTCIIF."SEC_CAR_INVER" = CARCLI."SEC";

END IF;

---------------------------------------------------------------------------------
----------------ELIMINA CARTERA QUE SUMAN 0 EN CARTERA Y TRANSITO ---------------
---------------------------------------------------------------------------------
		CREATE LOCAL TEMPORARY TABLE #Temporal_borra_0 (
		            periodo NVARCHAR(5000), 
		            rut NVARCHAR(5000), 
		            cuenta NVARCHAR(5000),
		            nemo NVARCHAR(5000),
		            cant_optimus_positivo NVARCHAR(5000),
		            cant_optimus_negativo NVARCHAR(5000)
		);

		insert into #Temporal_borra_0 (
		periodo,rut,cuenta,nemo
		, cant_optimus_positivo
		, cant_optimus_negativo 
		)
	
		select 
			periodo
			,rut
			,cuenta
			,nemo
			,sum(cantidad_optimus_positivo) as cant_optimus_positivo
			,sum(cantidad_optimus_negativo) as cant_optimus_negativo 
		from (
				SELECT Cartera."COD_PER" as periodo ,
									Cartera."RUT_CLI" as rut ,
									Cartera."SEC_RUT_CLI" as cuenta ,
									TRIM(Cartera."Nemotecnico")as nemo,
																								
								(sum(Cartera."Cantidad")) as cantidad_optimus_positivo,
								0 as cantidad_optimus_negativo,
									'OPTIMUS' as  Sistema
						FROM "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" as Cartera
						GROUP BY TRIM(Cartera."Nemotecnico"),
										Cartera."SEC_RUT_CLI",
										Cartera."RUT_CLI",
										Cartera."COD_PER"
										
				UNION
							
				SELECT Cartera."COD_PER" as periodo ,
							Cartera."RUT_CLI" as rut ,
							Cartera."SEC_RUT_CLI" as cuenta ,
							TRIM(Cartera."Nemotecnico")as nemo,
						0 as cantidad_optimus_positivo,																
						sum(Cartera."Cantidad")*-1 as cantidad_optimus_negativo,
						 															 'OPTIMUS' as  Sistema
				FROM "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" as Cartera
				GROUP BY TRIM(Cartera."Nemotecnico"),
								 Cartera."SEC_RUT_CLI",
								 Cartera."RUT_CLI",
								 Cartera."COD_PER"
								 ) as data
						
		where periodo = SUBSTRING(p_Periodo_Inicial,1,7) 	
		group by periodo,rut,cuenta,nemo	
		HAVING 	sum(cantidad_optimus_positivo)+sum(cantidad_optimus_negativo)=0
		order by 1,2,3,4;
					
		DELETE from "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" 
		 WHERE "IdCartera" in 
				(
					SELECT "IdCartera"
					From "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" as cartera  
							inner join #Temporal_borra_0 as transito 
							ON 	trim(cartera."COD_PER") = trim(transito.periodo)	AND 
								trim(cartera."RUT_CLI") = trim(transito.rut)		AND 
								trim(cartera."SEC_RUT_CLI") = trim(transito.cuenta) and 
								trim(cartera."Nemotecnico") = trim(transito.nemo)
				);
		
		DELETE from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" 
		 where "IdMovimientoCarteraEnTransito" in
				(		
					SELECT "IdMovimientoCarteraEnTransito"
					  from "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" as  cartera
							inner join #Temporal_borra_0 as transito 
							ON 	trim(cartera."COD_PER") = trim(transito.periodo)	AND 
								trim(cartera."RUT_CLI") = trim(transito.rut)	AND 
								trim(cartera."SEC_RUT_CLI") = trim(transito.cuenta) and 
								trim(cartera."Nemotecnico") = trim(transito.nemo)
				);

		DROP TABLE #Temporal_borra_0;

--------------------------------------------------------------------------------------------------------------------				
--------------------------------------------------------------------------------------------------------------------
SIGNAL SALIDA_CONTROLADA;

END;
