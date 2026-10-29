SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarCarteraFWD";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraFWD"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD

	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
	----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	-- declaracion variables
	   DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_mov_cargadas DECIMAL(26,8):=0;
	   DECLARE v_count_ope BIGINT :=0;	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
	   DECLARE v_MonedaSistema NVARCHAR(40):='';
	   DECLARE v_RutCli NVARCHAR(11):='0-0';
    				
        ----------------------------------------------------
        -- ELIMINO ERRORES DE ANTERIORES CARGAS DE ERRORES--
        ----------------------------------------------------
        
        if p_Modalidad = 'R' then
        	delete from "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" 
        	where "IdCarga" = p_IdCarga        
          	and "IdTipoInstrumento" ='FWD';
        end if; 
               
   		-----------------------------------------------------
		-- Contar cantidad registro -------------------------
        -----------------------------------------------------

        select count(1) into v_total_mov_gestion from "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERA
        where CARTERA."FechaCreacion" >= p_FechaInicial     
	      AND CARTERA."FechaCreacion" <= p_FechaFinal
          AND CARTERA."RutCliente" = case when p_RutCliente='        0-0' then "RutCliente" else p_RutCliente end 
          AND CARTERA."Estado" in ('V','L');
          
		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" 
							(
							"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","FechaIngresoRegistro","FechaModificacionRegistro","IdEmpresa","FechaOperacion"
							,"FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdClienteRelacionado","IdPortafolioRelacionado","IdEstado","IdTipoProducto"
							,"IdTipoCanalIngreso","IdMonedaSistema","IdMovimientoCarteraEnTransito","IdConcepto","IdTipoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante"
							,"FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica"
							,"CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraNormativa","FechaCurseInversion","TipoFactorAjuste","ValorFactorAjusteaFechaCurse"
							,"CostoInversionEnFactorAjuste","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento"
							,"FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento"
							,"Tamano","TIFAfectoBeneficio","TIFTasa","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","TasaAgente"
							,"PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento","FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdModalidadCumplimiento","IdReferenciaMercado"
							,"IdMonedaPrincipal","IdMonedaSecundaria","ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto","MontoMonedaPrincipal","MontoMonedaSecundaria"
							,"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo","INGGlosa","INGTasaAgente","INGFechaOperacion","INGIdCondicionLiquidacion","INGFechaLiquidacion","INGCantidad"
							,"INGValorRescate","INGPrecioPromedioTasaCompra","INGPrecioAPlazo","INGMontoAPlazo","INGMontoAPlazoMonedaInstrumento","INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador"
							,"INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria","INGTicketValorizacion","INGValorizacionConProblemas","INGFechaValorizacionCartera","INGFechaValorizacionAnterior"
							,"INGFechaValorizacionActual","INGFechaValorizacionProximo","INGPrecioCompra","INGTasaCompra","INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior"
							,"INGMontoReajusteCompraAnterior","INGMontoCompraAnteriorMonedaInstrumento","INGMontoInteresCompraAnteriorMonedaInstrumento","INGMontoReajusteCompraAnteriorMonedaInstrumento"
							,"INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGMontoCompraAnteriorPibMas","INGMontoCompraAnteriorPibMasMonedaInstrumento","INGMontoCompraAnteriorPibMenos"
							,"INGMontoCompraAnteriorPibMenosMonedaInstrumento","INGMontoATasaCompraActual","INGMontoCompraActualMonedaInstrumento","INGDurationCompraActual","INGConvexidadCompraActual"
							,"INGMontoCompraActualPibMas","INGMontoCompraActualPibMasMonedaInstrumento","INGMontoCompraActualPibMenos","INGMontoCompraActualPibMenosMonedaInstrumento","INGMontoATasaCompraProximo"
							,"INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGMontoCompraProximoMonedaInstrumento","INGMontoInteresCompraProximoMonedaInstrumento","INGMontoReajusteCompraProximoMonedaInstrumento"
							,"INGDurationCompraProximo","INGConvexidadCompraProximo","INGFechaPrecioTasaMercadoAnterior","INGFuenteTasaPrecioMercadoAnterior","INGTablaFuenteTasaPrecioMercadoAnterior"
							,"INGPrecioMercadoAnterior","INGTasaMercadoAnterior","INGMontoATasaMercadoAnterior","INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior","INGMontoMercadoAnteriorMonedaInstrumento"
							,"INGMontoInteresMercadoAnteriorMonedaInstrumento","INGMontoReajusteMercadoAnteriorMonedaInstrumento","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGMontoMercadoAnteriorPibMas"
							,"INGMontoMercadoAnteriorPibMasMonedaInstrumento","INGMontoMercadoAnteriorPibMenos","INGMontoMercadoAnteriorPibMenosMonedaInstrumento","INGFechaPrecioTasaMercadoActual","INGFuenteTasaPrecioMercadoActual"
							,"INGTablaFuenteTasaPrecioMercadoActual","INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGMontoMercadoActualMonedaInstrumento","INGDurationMercadoActual"
							,"INGConvexidadMercadoActual","INGMontoMercadoActualPibMas","INGMontoMercadoActualPibMasMonedaInstrumento","INGMontoMercadoActualPibMenos","INGMontoMercadoActualPibMenosMonedaInstrumento"
							,"INGFechaPrecioTasaMercadoProximo","INGFuenteTasaPrecioMercadoProximo","INGTablaFuenteTasaPrecioMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo","INGMontoATasaMercadoProximo"
							,"INGMontoInteresMercadoProximo","INGMontoReajusteMercadoProximo","INGMontoMercadoProximoMonedaInstrumento","INGMontoInteresMercadoProximoMonedaInstrumento","INGMontoReajusteMercadoProximoMonedaInstrumento"
							,"INGDurationMercadoProximo","INGConvexidadMercadoProximo","INGTasaCorredora","INGFechaTasaCorredora","INGMontoATasaCorredora","INGMontoATasaCorredoraMonedaInstrumento","INGFechaCorteCupon"
							,"INGNumeroCupon","INGIdMonedaPagoCupon","INGMontoCorteCupon","INGFechaPagoCorteCupon","INGMontoCapitalCorteCupon","INGMontoInteresCorteCupon","INGMontoReajusteCorteCupon","INGMontoCorteCuponMonedaInstrumento"
							,"INGMontoCapitalCorteCuponMonedaInstrumento","INGMontoInteresCorteCuponMonedaInstrumento","INGMontoReajusteCorteCuponMonedaInstrumento","INGMontoPresenteTIF","INGMontoInteresTIF","INGMontoReajusteTIF"
							,"INGMontoInteresAcumTIF","INGMontoReajusteAcumTIF","INGMontoPresenteTIFMonedaInstrumento","INGMontoInteresTIFMonedaInstrumento","INGMontoReajusteTIFMonedaInstrumento","INGMontoInteresAcumTIFMonedaInstrumento"
							,"INGMontoReajusteAcumTIFMonedaInstrumento","INGMontoMonedaPrincipal","INGMontoMonedaSecundaria","INGMontoDiferido","INGMontoUtilidadDiferida","INGMontoPerdidaDiferida","INGMontoMargen"
							,"INGMontoValorRazonableActivo","INGMontoValorRazonablePasivo","INGMontoValorRazonableNeto","INGTasaValorRazonableActivo","INGTasaValorRazonablePasivo","INGMontoDevengoTipoCambio"
							,"INGMontoDevengoTipoCambioAnterior","INGMontoDiferidoDiario","INGMontoDiferidoAcumulado","INGMontoMatriz","EGRTicketValorizacion","EGRValorizacionConProblemas","EGRGlosa"
							,"EGRFechaOperacion","EGRIdTipoOperacion","EGRIdOperacion","EGRIdMovimientoCarteraEnTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion"
							,"EGRIdTipoComprobante","EGRFolioComprobante","EGRFechaComprobante","EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente"
                            ,"EGRFechaCurseRetiro","EGRCostoRetiroenFactorAjuste","EGRValorFactorAjusteaFechaRetiro","EGRCantidad","EGRValorRescate","EGRPrecio","EGRTasa","EGRMonto","EGRMontoMonedaInstrumento"
                            ,"EGRSpread","EGRPrecioAPlazo","EGRMontoAPlazo","EGRMontoAPlazoMonedaInstrumento","EGRMontoPresenteTIF","EGRMontoInteresTIF","EGRMontoReajusteTIF","EGRMontoInteresAcumTIF"						
                            ,"EGRMontoReajusteAcumTIF","EGRMontoPresenteTIFMonedaInstrumento","EGRMontoInteresTIFMonedaInstrumento","EGRMontoReajusteTIFMonedaInstrumento","EGRMontoInteresAcumTIFMonedaInstrumento"
						  	,"EGRMontoReajusteAcumTIFMonedaInstrumento","EGRIdSpotAsociado","EGRMontoMonedaPrincipal","EGRMontoMonedaSecundaria","EGRIdMonedaLiquidacion","EGRMontoLiquidacion"
							,"EGRMontoLiquidacionMonedaPrincipal","EGRValorTipoCambioSpot","EGRValorParidad","EGRValorTipoCambioCotizador","EGREsPuntos","EGRTasaPuntosMonedaPrincipal","EGRTasaPuntosMonedaSecundaria"					
							,"EGRTasaDescuentoAnticipo","EGRTipoCambioAnticipo","EGRMontoARecibirAnticipo","EGRTipoCambioObservado","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER"
							,"FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA","SEC_COMPROM"
			    			,"FolioCarteraFWD","FolioTransaccionFWD","FolioOperacionFWD"
                            )
			    
			    select
			    
					                 "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL					as "IdCartera"
				        			,"BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL			as "FolioCartera"
									, 0 as "FolioCarteraRelacionada"
									, 0 as "FolioGarantiaOrigen"
									, MOVIMIENTO."FechaCreacion" as "FechaIngresoRegistro"
									, '0001-01-01'as "FechaModificacionRegistro" -- puedo saber si es modificado no puedo saber cuando 
									, p_IdEmpresa as "IdEmpresa"
									, SUBSTRING(MOVIMIENTO."FechaOperacion",1,10) as "FechaOperacion"
									, SUBSTRING(MOVIMIENTO."FechaVencimiento",1,10) as "FechaVencimientoOperacion"
									, MOVIMIENTO."PlazoEnDias" as "PlazoEnDiasOperacion"
									, MOVIMIENTO."IdCliente" as "IdCliente"
									, MOVIMIENTO."IdPortafolio" as "IdPortafolio"
									, 0 as "IdClienteRelacionado"
									, 0 as "IdPortafolioRelacionado"
									, CASE WHEN CARTERA."Estado" = 'L' then 'LIQ' else 'VIG' end as "IdEstado"-- revisar los estados
									, MOVIMIENTO."IdTipoProducto" as "IdTipoProducto"
									, 'AGE' as "IdTipoCanalIngreso"-- revisar si todas entran por canal agente
									, 'CLP' as "IdMonedaSistema" -- CLP
									, 0 as "IdMovimientoCarteraEnTransito"
									, MOVIMIENTO."IdTipoOperacion" as "IdConcepto"-- REVISAR
									, MOVIMIENTO."IdTipoOperacion" as "IdTipoOperacion"-- REVISAR
									, MOVIMIENTO."IdOperacion" as "IdOperacion"
									, 'CUS' as "IdTipoComprobante"-- si esto viene por facturacion
									, 0 as "FolioComprobante"--DEBO VER COMO HACER ESTO conectarlo a facturacion sebra
									, '0001-01-01' as "FechaComprobante" --DEBO VER COMO HACER ESTO conectarlo a facturacion sebra
									, 0 as "FolioExternoReferencia"
									, 1 as "IdMesa"-- deben decirme a que mesa le pego
									, 'FRD' as "IdBolsa" -- las operaciones de forward y spot son fuera de rueda
									, 0 as "IdSucursal"-- debe ser la del agente 
									, 0  as "IdAgente"--  debemos colocar el agente de la operacion revisar
									, 'T0' as "IdCondicionLiquidacion"-- tienen condicion de liquidacion--debo revisar ya que son operacion a plazo 
									, SUBSTRING(CARTERA."FechaVencimiento",1,10) as "FechaLiquidacion"
									, 'N' as "EsIntraday"
									, 'N' as "UsaGarantiaEspecifica"
									, 'N' as "CobraPrimaAlInicio"
									, ''  as "IdTipoPrestamo"
									, ''  as "Glosa"
									, 0 as "IdUsuario" --REVISAR
									, 'MFW' as "IdModulo"
									, '' as "TipoCarteraNormativa"
									, '0001-01-01' as "FechaCurseInversion"
									, '' as "TipoFactorAjuste"
									, 0  as "ValorFactorAjusteaFechaCurse"
									, 0  as "CostoInversionEnFactorAjuste"
									, 'DV' as "IdClasificacionTipoInstrumento"
									, 'FWD' as "IdTipoInstrumento"
									, MOVIMIENTO."IdInstrumento" as "IdInstrumento"
									, INSTRUMENTO."Nemotecnico" as "Nemotecnico"
									, '' as "IdEmisor"
									, '' as "NemoSVS"
									, '' as "IdTipoReajuste"
									, '0001-01-01' as "FechaEmisionInstrumento"
									, '0001-01-01' as "FechaVencimientoInstrumento"
									, 'N' as "EsFungible"
									, INSTRUMENTO."ValorizaPor" as "ValorizaPor"
									, '-1' as "TipoValorizacion"
									, 'N' as "TieneCorteDeCupon"
									, '' as "CodigoIFRS"
									, '' as "IdBoveda"
									, '' as "CuentaBoveda"
									, '' as "IdMonedaInstrumento"
									, 1 as "Tamano"
									, 'N' as "TIFAfectoBeneficio"
									, 0 as "TIFTasa"
									, CARTERA."MtoMonPrinc" as "Cantidad" 
									, 0 as "CorteMinimo"
									, 0 as "ValorRescate"
									, 0 as "PrecioCompra"
									, CARTERA."TasaCierreForward" as "TasaCompra"-- REVISAR SI EL FORWARD FUE POR TASA O PUNTA
									, CARTERA."MtoMonPrinc" as "MontoCompra"
									, 0 as "MontoCompraMonedaInstrumento"
									, 0 as "TasaAgente"
									, 0 as "PrecioAPlazo"
									, 0 as "MontoAPlazo"
									, 0 as "MontoAPlazoMonedaInstrumento"
									, substring(CARTERA."FechaVencimiento",1,10) as "FechaEfectiva"-- Fecha sistema 
									, 0 as "IdPlaza"
									, MOVIMIENTO."IdFixing" as "IdFixing"
									, MOVIMIENTO."FechaFixing" as "FechaFixing"
									, MOVIMIENTO."HoraFixing" as "HoraFixing"
									, MOVIMIENTO."IdModalidadLiquidacion" as "IdModalidadCumplimiento"-- recuperar 
									, 0 as "IdReferenciaMercado"
									, MOVIMIENTO."IdMonedaPrincipal" as "IdMonedaPrincipal"
									, MOVIMIENTO."IdMonedaSecundaria" as "IdMonedaSecundaria"
									, CARTERA."TCCierreForward" as "ValorTipoCambioCierre"
									, CARTERA."ParidadCierreMonPrinc"  as "ValorParidadCierre"
									, CARTERA."TCReferencia"  as "ValorTipoCambioCosto"
									, 0 as "ValorParidadCosto"
									, CARTERA."MtoMonPrinc" as "MontoMonedaPrincipal"
									, CARTERA."MtoMonSecu" as "MontoMonedaSecundaria"
									, p_IdCarga as "IdCarga"
									, CARTERA."FolioCartera" as "IdSecuenciaCarteraMigracion"
									, '0001-01' as "CodigoPeriodo"
									, '' as "INGGlosa"
									, 0 as "INGTasaAgente"
									, SUBSTRING(CARTERA."FechaCreacion",1,10) as "INGFechaOperacion"
									, 'T0' as "INGIdCondicionLiquidacion"
									, SUBSTRING(MOVIMIENTO."FechaVencimiento",1,10) as "INGFechaLiquidacion"
									, CARTERA."MtoMonPrinc" as "INGCantidad"-- QUE ES CANTIDAD 
									, 0 as "INGValorRescate"
									, 0 as "INGPrecioPromedioTasaCompra"
									, 0 as "INGPrecioAPlazo"
									, 0 as "INGMontoAPlazo"
									, 0 as "INGMontoAPlazoMonedaInstrumento"
									, CASE WHEN (CARTERA."Estado" = 'L' and CARTERA."CotPuntoForward" = 0) then 'S' else 'N' end  as "INGEsPuntos"-- debo averiguar si es por puntos o tasa
									, 0 as "INGValorTipoCambioSpot"
									, 0 as "INGValorTipoCambioCotizador"
									, 0 as "INGTasaPuntosMonedaPrincipal" --si es por punto debo saber de donde recuperar este datos
									, 0 as "INGTasaPuntosMonedaSecundaria"
									, 0 as "INGTicketValorizacion"
									, 'N' as "INGValorizacionConProblemas"
									, '0001-01-01' as "INGFechaValorizacionCartera"
									, '0001-01-01' as "INGFechaValorizacionAnterior"
									, '0001-01-01' as "INGFechaValorizacionActual"
									, '0001-01-01' as "INGFechaValorizacionProximo"
									, 0 as "INGPrecioCompra" -- va el precio mercado 
									, 0 as "INGTasaCompra" -- esta colocando la tasa y el tipo cambio de la operacion aqui
									, 0 as "INGMontoATasaCompraAnterior"
									, 0 as "INGMontoInteresCompraAnterior"
									, 0 as "INGMontoReajusteCompraAnterior"
									, 0 as "INGMontoCompraAnteriorMonedaInstrumento"
									, 0 as "INGMontoInteresCompraAnteriorMonedaInstrumento"
									, 0 as "INGMontoReajusteCompraAnteriorMonedaInstrumento"
									, 0 as "INGDurationCompraAnterior"
									, 0 as "INGConvexidadCompraAnterior"
									, 0 as "INGMontoCompraAnteriorPibMas"
									, 0 as "INGMontoCompraAnteriorPibMasMonedaInstrumento"
									, 0 as "INGMontoCompraAnteriorPibMenos"
									, 0 as "INGMontoCompraAnteriorPibMenosMonedaInstrumento"
									, MOVIMIENTO."MontoOperacion" as "INGMontoATasaCompraActual" --monto enmoneda principal
									, 0 as "INGMontoCompraActualMonedaInstrumento"
									, 0 as "INGDurationCompraActual"
									, 0 as "INGConvexidadCompraActual"
									, 0 as "INGMontoCompraActualPibMas"
									, 0 as "INGMontoCompraActualPibMasMonedaInstrumento"
									, 0 as "INGMontoCompraActualPibMenos"
									, 0 as "INGMontoCompraActualPibMenosMonedaInstrumento"
									, 0 as "INGMontoATasaCompraProximo"
									, 0 as "INGMontoInteresCompraProximo"
									, 0 as "INGMontoReajusteCompraProximo"
									, 0 as "INGMontoCompraProximoMonedaInstrumento"
									, 0 as "INGMontoInteresCompraProximoMonedaInstrumento"
									, 0 as "INGMontoReajusteCompraProximoMonedaInstrumento"
									, 0 as "INGDurationCompraProximo"
									, 0 as "INGConvexidadCompraProximo"
									, '0001-01-01' as "INGFechaPrecioTasaMercadoAnterior"
									, '' as "INGFuenteTasaPrecioMercadoAnterior"
									, 0 as "INGTablaFuenteTasaPrecioMercadoAnterior"
									, 0 as "INGPrecioMercadoAnterior"
									, 0 as "INGTasaMercadoAnterior"
									, 0 as "INGMontoATasaMercadoAnterior"
									, 0 as "INGMontoInteresMercadoAnterior"
									, 0 as "INGMontoReajusteMercadoAnterior"
									, 0 as "INGMontoMercadoAnteriorMonedaInstrumento"
									, 0 as "INGMontoInteresMercadoAnteriorMonedaInstrumento"
									, 0 as "INGMontoReajusteMercadoAnteriorMonedaInstrumento"
									, 0 as "INGDurationMercadoAnterior"
									, 0 as "INGConvexidadMercadoAnterior"
									, 0 as "INGMontoMercadoAnteriorPibMas"
									, 0 as "INGMontoMercadoAnteriorPibMasMonedaInstrumento"
									, 0 as "INGMontoMercadoAnteriorPibMenos"
									, 0 as "INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
									, '0001-01-01' as "INGFechaPrecioTasaMercadoActual"
									, '' as "INGFuenteTasaPrecioMercadoActual"
									, '' as "INGTablaFuenteTasaPrecioMercadoActual"
									, 0 as "INGPrecioMercadoActual"
									, 0 as "INGTasaMercadoActual"
									, 0 as "INGMontoATasaMercadoActual"
									, 0 as "INGMontoMercadoActualMonedaInstrumento"
									, 0 as "INGDurationMercadoActual"
									, 0 as "INGConvexidadMercadoActual"
									, 0 as "INGMontoMercadoActualPibMas"
									, 0 as "INGMontoMercadoActualPibMasMonedaInstrumento"
									, 0 as "INGMontoMercadoActualPibMenos"
									, 0 as "INGMontoMercadoActualPibMenosMonedaInstrumento"
									, '0001-01-01' as "INGFechaPrecioTasaMercadoProximo"
									, '' as "INGFuenteTasaPrecioMercadoProximo"
									, '' as "INGTablaFuenteTasaPrecioMercadoProximo"
									, 0 as "INGPrecioMercadoProximo"
									, 0 as "INGTasaMercadoProximo"
									, 0 as "INGMontoATasaMercadoProximo"
									, 0 as "INGMontoInteresMercadoProximo"
									, 0 as "INGMontoReajusteMercadoProximo"
									, 0 as "INGMontoMercadoProximoMonedaInstrumento"
									, 0 as "INGMontoInteresMercadoProximoMonedaInstrumento"
									, 0 as "INGMontoReajusteMercadoProximoMonedaInstrumento"
									, 0 as "INGDurationMercadoProximo"
									, 0 as "INGConvexidadMercadoProximo"
									, 0 as "INGTasaCorredora"
									, '0001-01-01' as "INGFechaTasaCorredora"
									, 0 as "INGMontoATasaCorredora"
									, 0 as "INGMontoATasaCorredoraMonedaInstrumento"   
									, '0001-01-01' as "INGFechaCorteCupon"
									, 0 as "INGNumeroCupon"
									, 0 as "INGIdMonedaPagoCupon"
									, 0 as "INGMontoCorteCupon"
									, '0001-01-01' as "INGFechaPagoCorteCupon"
									, 0 as "INGMontoCapitalCorteCupon"
									, 0 as "INGMontoInteresCorteCupon"
									, 0 as "INGMontoReajusteCorteCupon"
									, 0 as "INGMontoCorteCuponMonedaInstrumento"
									, 0 as "INGMontoCapitalCorteCuponMonedaInstrumento"
									, 0 as "INGMontoInteresCorteCuponMonedaInstrumento"
									, 0 as "INGMontoReajusteCorteCuponMonedaInstrumento"
									, 0 as "INGMontoPresenteTIF"
									, 0 as "INGMontoInteresTIF"
									, 0 as "INGMontoReajusteTIF"
									, 0 as "INGMontoInteresAcumTIF"
									, 0 as "INGMontoReajusteAcumTIF"
									, 0 as "INGMontoPresenteTIFMonedaInstrumento"
									, 0 as "INGMontoInteresTIFMonedaInstrumento"
									, 0 as "INGMontoReajusteTIFMonedaInstrumento"
									, 0 as "INGMontoInteresAcumTIFMonedaInstrumento"
									, 0 as "INGMontoReajusteAcumTIFMonedaInstrumento"
									, CARTERA."MtoMonPrinc" as "INGMontoMonedaPrincipal"
									, CARTERA."MtoMonSecuIni" as "INGMontoMonedaSecundaria"
									, MOVIMIENTO."MontoDiferido" as "INGMontoDiferido"
									, CARTERA."MtoUtilidadDiferida" as "INGMontoUtilidadDiferida"
									, CARTERA."MtoPerdidaDiferida" as "INGMontoPerdidaDiferida"
									, CARTERA."Margen" as "INGMontoMargen"
									, CARTERA."MtoVRActivo" as "INGMontoValorRazonableActivo"
									, CARTERA."MtoVRPasivo" as "INGMontoValorRazonablePasivo"
									, CARTERA."MtoVRNeto" as "INGMontoValorRazonableNeto"
									, CARTERA."TasaVRActivo" as "INGTasaValorRazonableActivo"
									, CARTERA."TasaVRPasivo" as "INGTasaValorRazonablePasivo"
									, CARTERA."MtoDevTC" as "INGMontoDevengoTipoCambio"
									, CARTERA."MtoDevTCAnt" as "INGMontoDevengoTipoCambioAnterior"
									, CARTERA."MtoDifDiario" as "INGMontoDiferidoDiario"
									, CARTERA."MtoDifAcum" as "INGMontoDiferidoAcumulado"
									, 0 as "INGMontoMatriz"
									, 0 as "EGRTicketValorizacion"
									, 'N' as "EGRValorizacionConProblemas"
									, '' as "EGRGlosa"
									, '0001-01-01' as "EGRFechaOperacion" -- ver si la operaciones estan vencidad o fueron anticipadas
									, CASE WHEN CARTERA."FechaVctoAnt"!='1900-01-01 00:00:00.0000000' then 'ANTFWD' 
									                                                   else case when CARTERA."Estado" = 'L' then 'VCDFWD'
									                                                    else '' end 
									  end as "EGRIdTipoOperacion" -- revisar esto
									, 0  as "EGRIdOperacion" -- revisar esto
									, 0  as "EGRIdMovimientoCarteraEnTransito"
									, '' as "EGRIdTipoCanalIngreso"
									, case when CARTERA."Estado" = 'L' then 'T0' else '' end as "EGRIdCondicionLiquidacion"
									, case when CARTERA."Estado" = 'L' then  SUBSTRING(CARTERA."FechaVencimiento",1,10) else '0001-01-01' end as "EGRFechaLiquidacion"
									, case when CARTERA."Estado" = 'L' then 'CUS' else '' end as "EGRIdTipoComprobante"
									, 0 as "EGRFolioComprobante"
									, '0001-01-01' as "EGRFechaComprobante"
									, 0 as "EGRFolioExternoReferencia"
									, CASE WHEN CARTERA."Estado" = 'L' then 'MFW' else '' end as "EGRIdModulo"
									, CASE WHEN CARTERA."FechaVctoAnt"!='1900-01-01 00:00:00.0000000' then 'ANTFWD' 
									                                                   else case when CARTERA."Estado" = 'L' then 'VCDFWD'
									                                                    else '' end 
									  end as "EGRIdConcepto"
									, 0 as "EGRIdUsuario" -- como recuperar los usuarios 
									, 0 as "EGRIdSucursal" -- como recuperar la sucursal 
									, 0 as "EGRIdAgente" -- agente 
									, '0001-01-01' as "EGRFechaCurseRetiro"
									, 0 as "EGRCostoRetiroenFactorAjuste"
									, 0 as "EGRValorFactorAjusteaFechaRetiro"
									, CASE WHEN CARTERA."Estado" = 'L' then CARTERA."MtoMonPrinc" else 0 end as "EGRCantidad"
									, 0 as "EGRValorRescate"
									, 0 as "EGRPrecio"
									, 0 as "EGRTasa"
									, CASE WHEN CARTERA."Estado" = 'L' then CARTERA."MtoMonPrinc" else 0 end as "EGRMonto"
									, 0 as "EGRMontoMonedaInstrumento"
                                    , CASE WHEN CARTERA."Estado" = 'L' then CARTERA."Margen" else 0 end as "EGRSpread"
									, 0 as "EGRPrecioAPlazo"
									, 0 as "EGRMontoAPlazo"
									, 0 as "EGRMontoAPlazoMonedaInstrumento"
									, 0 as "EGRMontoPresenteTIF"
									, 0 as "EGRMontoInteresTIF"
									, 0 as "EGRMontoReajusteTIF"
									, 0 as "EGRMontoInteresAcumTIF"
                                    , 0 as "EGRMontoReajusteAcumTIF"
                                    , 0 as "EGRMontoPresenteTIFMonedaInstrumento"
									, 0 as "EGRMontoInteresTIFMonedaInstrumento"
									, 0 as "EGRMontoReajusteTIFMonedaInstrumento"
									, 0 as "EGRMontoInteresAcumTIFMonedaInstrumento"
									, 0 as "EGRMontoReajusteAcumTIFMonedaInstrumento"
									, 0 as "EGRIdSpotAsociado" -- buscar como relacionar la operacion de spot  a un registro  en caso de entrega fisica
									, CASE WHEN CARTERA."Estado" = 'L' then CARTERA."MtoMonPrinc" else 0 end as "EGRMontoMonedaPrincipal"
									, CASE WHEN CARTERA."Estado" = 'L' then CARTERA."MtoMonSecu" else 0 end as "EGRMontoMonedaSecundaria"
									, '' as "EGRIdMonedaLiquidacion"
									, 0 as "EGRMontoLiquidacion"
                                    , 0 as "EGRMontoLiquidacionMonedaPrincipal"
									, 0 as "EGRValorTipoCambioSpot"
									, CASE WHEN CARTERA."Estado" = 'L' THEN CARTERA."ParidadCierreMonPrinc" ELSE 0 end as "EGRValorParidad"
									, 0 as "EGRValorTipoCambioCotizador"
									, CASE WHEN (CARTERA."Estado" = 'L' and CARTERA."CotPuntoForward" = 0) then 'S' else 'N' end as "EGREsPuntos"
									, CASE WHEN CARTERA."Estado" = 'L' then CARTERA."CotTasaMonPrinc" else 0 end  as "EGRTasaPuntosMonedaPrincipal"
									, CASE WHEN CARTERA."Estado" = 'L' then CARTERA."CotTasaMonSecu" else 0 end as "EGRTasaPuntosMonedaSecundaria"
                                    , 0 as "EGRTasaDescuentoAnticipo"-- falta como identificar los anticipos
									, 0 as "EGRTipoCambioAnticipo"-- falta como identificar los anticipos
									, 0 as "EGRMontoARecibirAnticipo"
									, 0 as "EGRTipoCambioObservado"
									, CARTERA."RutCliente" as "RUT_CLI"
									, CARTERA."CuentaCliente" as "SEC_RUT_CLI"
									, 0 as "SEC_CAR_INVER"
									, 0 as "COD_PER"
                                    , 0 as "FOLIO_COMP_ADJ"
									, '' as "TIPO_COMP_ADJ"
									, 0 as "LIN_COMP_ADJ"
									, 0 as "LIN_ORDEN"
									, 0 as "SEC_ORDEN"
									, '' as "TIPO_CARTERA"
									, 0 as "ID_ERROR"
									, 0 as "SEC_MOVTO"
									, 0 as "CAN_GARANTIA"
									, 0 as "SEC_COMPROM"
					                , CARTERA."FolioCartera" as "FolioCarteraFWD"
					                , CARTERA."FolioTransaccion" as "FolioTransaccionFWD"
					                , CARTERA."FolioOperacion" as "FolioOperacionFWD"
							FROM "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" AS CARTERA
							INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" AS MOVIMIENTO
							  ON  CARTERA."FolioOperacion" = MOVIMIENTO."FOLIOOPERACION"
							  AND CARTERA."FolioTransaccion" = MOVIMIENTO."FOLIOTRANSACCION"
						    INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
						      ON INSTRUMENTO."IdInstrumento" = MOVIMIENTO."IdInstrumento"
						    INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE
					          ON CLIENTE."IdCliente" = MOVIMIENTO."IdCliente"
					          AND CLIENTE."DocumentoIdentidad"  = case when p_RutCliente = '        0-0'  then  CLIENTE."DocumentoIdentidad" else trim(p_RutCliente) end
						    LEFT JOIN "BSCL.Homologacion.BD::HOM.Cartera"  as CARTERA_HOM
						      ON  CARTERA_HOM."FolioCarteraFWD"  = CARTERA."FolioCartera"
						    WHERE CARTERA."FechaCreacion" >= p_FechaInicial     
	                          AND CARTERA."FechaCreacion" <= p_FechaFinal
	                          AND CARTERA."Estado" in ('V','L') -- SOLO VIGENTES Y LIQUIDADOS
	                          AND CARTERA_HOM."IdCartera" IS NULL;
	                          
	          --------------------------------------------                
              -- contamos los movimientos cargados	   ---                       
              --------------------------------------------
              select count(1) into v_total_mov_cargadas
              from "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"
              where "IdTipoInstrumento" ='FWD'
                and "IdCarga" = p_IdCarga;

		   	  ------------------------------
		   	  --cargamos el log de errores--
           	  ------------------------------
               
			  -- debe existir la operacion que genera la cartera forward
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'CARTERAFWD' as "Entidad"
                    , 'cartera fwd sin operacion cargada en homologacion' as "Mensaje"
                    , 'FolioOperacion' as "NombreCampoGenerico1"
                    , TO_VARCHAR(CARTERA."FolioOperacion") as "ValorCampoGenerico1" 
                    , 'FolioTransaccion'  as "NombreCampoGenerico2" 
                    , TO_VARCHAR(CARTERA."FolioTransaccion") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
           		FROM "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" AS CARTERA
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" AS MOVIMIENTO
						  ON  CARTERA."FolioOperacion" = MOVIMIENTO."FOLIOOPERACION"
						  AND CARTERA."FolioTransaccion" = MOVIMIENTO."FOLIOTRANSACCION"
					INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE
					      ON CLIENTE."IdCliente" = MOVIMIENTO."IdCliente"
					      AND CLIENTE."DocumentoIdentidad"  = case when p_RutCliente = '        0-0'  then  CLIENTE."DocumentoIdentidad" else trim(p_RutCliente) end
					LEFT JOIN "BSCL.Homologacion.BD::HOM.Cartera" AS CARTERA_HOM
						  ON CARTERA_HOM."FolioCarteraFWD"  = CARTERA."FolioCartera"
				WHERE CARTERA."FechaCreacion" >= p_FechaInicial     
	                   AND CARTERA."FechaCreacion" <= p_FechaFinal
	                   AND CARTERA."Estado" in ('V','L') -- SOLO VIGENTES Y LIQUIDADOS
                       AND CARTERA_HOM."IdCartera" IS NULL
                       AND MOVIMIENTO."IdOperacion" IS NULL; 

		   -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		   p_ResumenDatosProcesados = select 'CARGAOPERACIONFWD' as "CodigoProceso" 
	   									,'FAC' as "Modulo" 
	   		 							,v_total_mov_gestion as "TotalRegistrosSebra"
	   		  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   						    	  union all
	   						    	  select * from :p_ResumenDatosProcesados;

	END;	
END;

 
