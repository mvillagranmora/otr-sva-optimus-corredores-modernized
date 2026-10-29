SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarCarteraFWDOptimus";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de CARTERA FWD
--  renta variable
--  Autor: - EAR 

"BSCL.Homologacion.BD.Servicios::CargarCarteraFWDOptimus"(
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
        	delete from "BSCL"."BSCL.Custodia.BD::CUS.Cartera" 
        	where "IdCarga" = p_IdCarga        
          	and "IdTipoInstrumento" = 'FWD';
        end if; 
               
   		-----------------------------------------------------
		-- Contar cantidad registro -------------------------
        -----------------------------------------------------
        
        select count(1) into v_total_mov_gestion from "BSCL.Homologacion.BD::HOM.Cartera" as CARTERA
        where CARTERA."RUT_CLI" = case when p_RutCliente='        0-0' then "RUT_CLI" else p_RutCliente end
          and CARTERA."IdTipoInstrumento" = 'FWD'
          and CARTERA."IdCarga" =p_IdCarga;
          
		insert into "BSCL"."BSCL.Custodia.BD::CUS.Cartera" 
			            (	"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","FechaIngresoRegistro","FechaModificacionRegistro","IdEmpresa","FechaOperacion"
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
							,"INGValorRescate","INGPrecioAPlazo","INGMontoAPlazo","INGMontoAPlazoMonedaInstrumento","INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador"
							,"INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria","INGTicketValorizacion","INGValorizacionConProblemas","INGFechaValorizacionCartera","INGFechaValorizacionAnterior"
							,"INGFechaValorizacionActual","INGFechaValorizacionProximo","INGPrecioCompra","INGTasaCompra","INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior"
							,"INGMontoReajusteCompraAnterior","INGMontoCompraAnteriorMonedaInstrumento","INGMontoInteresCompraAnteriorMonedaInstrumento","INGMontoReajusteCompraAnteriorMonedaInstrumento"
							,"INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGMontoCompraAnteriorPibMas","INGMontoCompraAnteriorPibMasMonedaInstrumento","INGMontoCompraAnteriorPibMenos"
							,"INGMontoCompraAnteriorPibMenosMonedaInstrumento","INGMontoATasaCompraActual","INGMontoCompraActualMonedaInstrumento","INGDurationCompraActual","INGConvexidadCompraActual"
							,"INGMontoCompraActualPibMas","INGMontoCompraActualPibMasMonedaInstrumento","INGMontoCompraActualPibMenos","INGMontoCompraActualPibMenosMonedaInstrumento","INGMontoATasaCompraProximo"
							,"INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGMontoCompraProximoMonedaInstrumento","INGMontoInteresCompraProximoMonedaInstrumento","INGMontoReajusteCompraProximoMonedaInstrumento"
							,"INGDurationCompraProximo","INGConvexidadCompraProximo","INGFechaPrecioTasaMercadoAnterior","INGFuenteTasaPrecioMercadoAnterior"
							,"INGPrecioMercadoAnterior","INGTasaMercadoAnterior","INGMontoATasaMercadoAnterior","INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior","INGMontoMercadoAnteriorMonedaInstrumento"
							,"INGMontoInteresMercadoAnteriorMonedaInstrumento","INGMontoReajusteMercadoAnteriorMonedaInstrumento","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGMontoMercadoAnteriorPibMas"
							,"INGMontoMercadoAnteriorPibMasMonedaInstrumento","INGMontoMercadoAnteriorPibMenos","INGMontoMercadoAnteriorPibMenosMonedaInstrumento","INGFechaPrecioTasaMercadoActual","INGFuenteTasaPrecioMercadoActual"
							,"INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGMontoMercadoActualMonedaInstrumento","INGDurationMercadoActual"
							,"INGConvexidadMercadoActual","INGMontoMercadoActualPibMas","INGMontoMercadoActualPibMasMonedaInstrumento","INGMontoMercadoActualPibMenos","INGMontoMercadoActualPibMenosMonedaInstrumento"
							,"INGFechaPrecioTasaMercadoProximo","INGFuenteTasaPrecioMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo","INGMontoATasaMercadoProximo"
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
							,"EGRTasaDescuentoAnticipo","EGRTipoCambioAnticipo","EGRMontoARecibirAnticipo","EGRTipoCambioObservado"
                            )
			    select
							 CARTERA_HOM."IdCartera",CARTERA_HOM."FolioCartera",CARTERA_HOM."FolioCarteraRelacionada",CARTERA_HOM."FolioGarantiaOrigen",CARTERA_HOM."FechaIngresoRegistro",CARTERA_HOM."FechaModificacionRegistro",CARTERA_HOM."IdEmpresa",CARTERA_HOM."FechaOperacion"
							,CARTERA_HOM."FechaVencimientoOperacion",CARTERA_HOM."PlazoEnDiasOperacion",CARTERA_HOM."IdCliente",CARTERA_HOM."IdPortafolio",CARTERA_HOM."IdClienteRelacionado",CARTERA_HOM."IdPortafolioRelacionado",CARTERA_HOM."IdEstado",CARTERA_HOM."IdTipoProducto"
							,CARTERA_HOM."IdTipoCanalIngreso",CARTERA_HOM."IdMonedaSistema",CARTERA_HOM."IdMovimientoCarteraEnTransito",CARTERA_HOM."IdConcepto",CARTERA_HOM."IdTipoOperacion",CARTERA_HOM."IdOperacion",CARTERA_HOM."IdTipoComprobante",CARTERA_HOM."FolioComprobante"
							,CARTERA_HOM."FechaComprobante",CARTERA_HOM."FolioExternoReferencia",CARTERA_HOM."IdMesa",CARTERA_HOM."IdBolsa",CARTERA_HOM."IdSucursal",CARTERA_HOM."IdAgente",CARTERA_HOM."IdCondicionLiquidacion",CARTERA_HOM."FechaLiquidacion",CARTERA_HOM."EsIntraday",CARTERA_HOM."UsaGarantiaEspecifica"
							,CARTERA_HOM."CobraPrimaAlInicio",CARTERA_HOM."IdTipoPrestamo",CARTERA_HOM."Glosa",CARTERA_HOM."IdUsuario",CARTERA_HOM."IdModulo",CARTERA_HOM."TipoCarteraNormativa",CARTERA_HOM."FechaCurseInversion",CARTERA_HOM."TipoFactorAjuste",CARTERA_HOM."ValorFactorAjusteaFechaCurse"
							,CARTERA_HOM."CostoInversionEnFactorAjuste",CARTERA_HOM."IdClasificacionTipoInstrumento",CARTERA_HOM."IdTipoInstrumento",CARTERA_HOM."IdInstrumento",CARTERA_HOM."Nemotecnico",CARTERA_HOM."IdEmisor",CARTERA_HOM."NemoSVS",CARTERA_HOM."IdTipoReajuste",CARTERA_HOM."FechaEmisionInstrumento"
							,CARTERA_HOM."FechaVencimientoInstrumento",CARTERA_HOM."EsFungible",CARTERA_HOM."ValorizaPor",CARTERA_HOM."TipoValorizacion",CARTERA_HOM."TieneCorteDeCupon",CARTERA_HOM."CodigoIFRS",CARTERA_HOM."IdBoveda",CARTERA_HOM."CuentaBoveda",CARTERA_HOM."IdMonedaInstrumento"
							,CARTERA_HOM."Tamano",CARTERA_HOM."TIFAfectoBeneficio",CARTERA_HOM."TIFTasa",CARTERA_HOM."Cantidad",CARTERA_HOM."CorteMinimo",CARTERA_HOM."ValorRescate",CARTERA_HOM."PrecioCompra",CARTERA_HOM."TasaCompra",CARTERA_HOM."MontoCompra",CARTERA_HOM."MontoCompraMonedaInstrumento",CARTERA_HOM."TasaAgente"
							,CARTERA_HOM."PrecioAPlazo",CARTERA_HOM."MontoAPlazo",CARTERA_HOM."MontoAPlazoMonedaInstrumento",CARTERA_HOM."FechaEfectiva",CARTERA_HOM."IdPlaza",CARTERA_HOM."IdFixing",CARTERA_HOM."FechaFixing",CARTERA_HOM."HoraFixing",CARTERA_HOM."IdModalidadCumplimiento",CARTERA_HOM."IdReferenciaMercado"
							,CARTERA_HOM."IdMonedaPrincipal",CARTERA_HOM."IdMonedaSecundaria",CARTERA_HOM."ValorTipoCambioCierre",CARTERA_HOM."ValorParidadCierre",CARTERA_HOM."ValorTipoCambioCosto",CARTERA_HOM."ValorParidadCosto",CARTERA_HOM."MontoMonedaPrincipal",CARTERA_HOM."MontoMonedaSecundaria"
							,CARTERA_HOM."IdCarga",CARTERA_HOM."IdSecuenciaCarteraMigracion",CARTERA_HOM."CodigoPeriodo",CARTERA_HOM."INGGlosa",CARTERA_HOM."INGTasaAgente",CARTERA_HOM."INGFechaOperacion",CARTERA_HOM."INGIdCondicionLiquidacion",CARTERA_HOM."INGFechaLiquidacion",CARTERA_HOM."INGCantidad"
							,CARTERA_HOM."INGValorRescate",CARTERA_HOM."INGPrecioAPlazo",CARTERA_HOM."INGMontoAPlazo",CARTERA_HOM."INGMontoAPlazoMonedaInstrumento",CARTERA_HOM."INGEsPuntos",CARTERA_HOM."INGValorTipoCambioSpot",CARTERA_HOM."INGValorTipoCambioCotizador"
							,CARTERA_HOM."INGTasaPuntosMonedaPrincipal",CARTERA_HOM."INGTasaPuntosMonedaSecundaria",CARTERA_HOM."INGTicketValorizacion",CARTERA_HOM."INGValorizacionConProblemas",CARTERA_HOM."INGFechaValorizacionCartera",CARTERA_HOM."INGFechaValorizacionAnterior"
							,CARTERA_HOM."INGFechaValorizacionActual",CARTERA_HOM."INGFechaValorizacionProximo",CARTERA_HOM."INGPrecioCompra",CARTERA_HOM."INGTasaCompra",CARTERA_HOM."INGMontoATasaCompraAnterior",CARTERA_HOM."INGMontoInteresCompraAnterior"
							,CARTERA_HOM."INGMontoReajusteCompraAnterior",CARTERA_HOM."INGMontoCompraAnteriorMonedaInstrumento",CARTERA_HOM."INGMontoInteresCompraAnteriorMonedaInstrumento",CARTERA_HOM."INGMontoReajusteCompraAnteriorMonedaInstrumento"
							,CARTERA_HOM."INGDurationCompraAnterior",CARTERA_HOM."INGConvexidadCompraAnterior",CARTERA_HOM."INGMontoCompraAnteriorPibMas",CARTERA_HOM."INGMontoCompraAnteriorPibMasMonedaInstrumento",CARTERA_HOM."INGMontoCompraAnteriorPibMenos"
							,CARTERA_HOM."INGMontoCompraAnteriorPibMenosMonedaInstrumento",CARTERA_HOM."INGMontoATasaCompraActual",CARTERA_HOM."INGMontoCompraActualMonedaInstrumento",CARTERA_HOM."INGDurationCompraActual",CARTERA_HOM."INGConvexidadCompraActual"
							,CARTERA_HOM."INGMontoCompraActualPibMas",CARTERA_HOM."INGMontoCompraActualPibMasMonedaInstrumento",CARTERA_HOM."INGMontoCompraActualPibMenos",CARTERA_HOM."INGMontoCompraActualPibMenosMonedaInstrumento",CARTERA_HOM."INGMontoATasaCompraProximo"
							,CARTERA_HOM."INGMontoInteresCompraProximo",CARTERA_HOM."INGMontoReajusteCompraProximo",CARTERA_HOM."INGMontoCompraProximoMonedaInstrumento",CARTERA_HOM."INGMontoInteresCompraProximoMonedaInstrumento",CARTERA_HOM."INGMontoReajusteCompraProximoMonedaInstrumento"
							,CARTERA_HOM."INGDurationCompraProximo",CARTERA_HOM."INGConvexidadCompraProximo",CARTERA_HOM."INGFechaPrecioTasaMercadoAnterior",CARTERA_HOM."INGFuenteTasaPrecioMercadoAnterior",CARTERA_HOM."INGPrecioMercadoAnterior",CARTERA_HOM."INGTasaMercadoAnterior",CARTERA_HOM."INGMontoATasaMercadoAnterior",CARTERA_HOM."INGMontoInteresMercadoAnterior",CARTERA_HOM."INGMontoReajusteMercadoAnterior",CARTERA_HOM."INGMontoMercadoAnteriorMonedaInstrumento"
							,CARTERA_HOM."INGMontoInteresMercadoAnteriorMonedaInstrumento",CARTERA_HOM."INGMontoReajusteMercadoAnteriorMonedaInstrumento",CARTERA_HOM."INGDurationMercadoAnterior",CARTERA_HOM."INGConvexidadMercadoAnterior",CARTERA_HOM."INGMontoMercadoAnteriorPibMas"
							,CARTERA_HOM."INGMontoMercadoAnteriorPibMasMonedaInstrumento",CARTERA_HOM."INGMontoMercadoAnteriorPibMenos",CARTERA_HOM."INGMontoMercadoAnteriorPibMenosMonedaInstrumento",CARTERA_HOM."INGFechaPrecioTasaMercadoActual",CARTERA_HOM."INGFuenteTasaPrecioMercadoActual"
							,CARTERA_HOM."INGPrecioMercadoActual",CARTERA_HOM."INGTasaMercadoActual",CARTERA_HOM."INGMontoATasaMercadoActual",CARTERA_HOM."INGMontoMercadoActualMonedaInstrumento",CARTERA_HOM."INGDurationMercadoActual"
							,CARTERA_HOM."INGConvexidadMercadoActual",CARTERA_HOM."INGMontoMercadoActualPibMas",CARTERA_HOM."INGMontoMercadoActualPibMasMonedaInstrumento",CARTERA_HOM."INGMontoMercadoActualPibMenos",CARTERA_HOM."INGMontoMercadoActualPibMenosMonedaInstrumento"
							,CARTERA_HOM."INGFechaPrecioTasaMercadoProximo",CARTERA_HOM."INGFuenteTasaPrecioMercadoProximo",CARTERA_HOM."INGPrecioMercadoProximo",CARTERA_HOM."INGTasaMercadoProximo",CARTERA_HOM."INGMontoATasaMercadoProximo"
							,CARTERA_HOM."INGMontoInteresMercadoProximo",CARTERA_HOM."INGMontoReajusteMercadoProximo",CARTERA_HOM."INGMontoMercadoProximoMonedaInstrumento",CARTERA_HOM."INGMontoInteresMercadoProximoMonedaInstrumento",CARTERA_HOM."INGMontoReajusteMercadoProximoMonedaInstrumento"
							,CARTERA_HOM."INGDurationMercadoProximo",CARTERA_HOM."INGConvexidadMercadoProximo",CARTERA_HOM."INGTasaCorredora",CARTERA_HOM."INGFechaTasaCorredora",CARTERA_HOM."INGMontoATasaCorredora",CARTERA_HOM."INGMontoATasaCorredoraMonedaInstrumento",CARTERA_HOM."INGFechaCorteCupon"
							,CARTERA_HOM."INGNumeroCupon",CARTERA_HOM."INGIdMonedaPagoCupon",CARTERA_HOM."INGMontoCorteCupon",CARTERA_HOM."INGFechaPagoCorteCupon",CARTERA_HOM."INGMontoCapitalCorteCupon",CARTERA_HOM."INGMontoInteresCorteCupon",CARTERA_HOM."INGMontoReajusteCorteCupon",CARTERA_HOM."INGMontoCorteCuponMonedaInstrumento"
							,CARTERA_HOM."INGMontoCapitalCorteCuponMonedaInstrumento",CARTERA_HOM."INGMontoInteresCorteCuponMonedaInstrumento",CARTERA_HOM."INGMontoReajusteCorteCuponMonedaInstrumento",CARTERA_HOM."INGMontoPresenteTIF",CARTERA_HOM."INGMontoInteresTIF",CARTERA_HOM."INGMontoReajusteTIF"
							,CARTERA_HOM."INGMontoInteresAcumTIF",CARTERA_HOM."INGMontoReajusteAcumTIF",CARTERA_HOM."INGMontoPresenteTIFMonedaInstrumento",CARTERA_HOM."INGMontoInteresTIFMonedaInstrumento",CARTERA_HOM."INGMontoReajusteTIFMonedaInstrumento",CARTERA_HOM."INGMontoInteresAcumTIFMonedaInstrumento"
							,CARTERA_HOM."INGMontoReajusteAcumTIFMonedaInstrumento",CARTERA_HOM."INGMontoMonedaPrincipal",CARTERA_HOM."INGMontoMonedaSecundaria",CARTERA_HOM."INGMontoDiferido",CARTERA_HOM."INGMontoUtilidadDiferida",CARTERA_HOM."INGMontoPerdidaDiferida",CARTERA_HOM."INGMontoMargen"
							,CARTERA_HOM."INGMontoValorRazonableActivo",CARTERA_HOM."INGMontoValorRazonablePasivo",CARTERA_HOM."INGMontoValorRazonableNeto",CARTERA_HOM."INGTasaValorRazonableActivo",CARTERA_HOM."INGTasaValorRazonablePasivo",CARTERA_HOM."INGMontoDevengoTipoCambio"
							,CARTERA_HOM."INGMontoDevengoTipoCambioAnterior",CARTERA_HOM."INGMontoDiferidoDiario",CARTERA_HOM."INGMontoDiferidoAcumulado",CARTERA_HOM."INGMontoMatriz",CARTERA_HOM."EGRTicketValorizacion",CARTERA_HOM."EGRValorizacionConProblemas",CARTERA_HOM."EGRGlosa"
							,CARTERA_HOM."EGRFechaOperacion",CARTERA_HOM."EGRIdTipoOperacion",CARTERA_HOM."EGRIdOperacion",CARTERA_HOM."EGRIdMovimientoCarteraEnTransito",CARTERA_HOM."EGRIdTipoCanalIngreso",CARTERA_HOM."EGRIdCondicionLiquidacion",CARTERA_HOM."EGRFechaLiquidacion"
							,CARTERA_HOM."EGRIdTipoComprobante",CARTERA_HOM."EGRFolioComprobante",CARTERA_HOM."EGRFechaComprobante",CARTERA_HOM."EGRFolioExternoReferencia",CARTERA_HOM."EGRIdModulo",CARTERA_HOM."EGRIdConcepto",CARTERA_HOM."EGRIdUsuario",CARTERA_HOM."EGRIdSucursal",CARTERA_HOM."EGRIdAgente"
                            ,CARTERA_HOM."EGRFechaCurseRetiro",CARTERA_HOM."EGRCostoRetiroenFactorAjuste",CARTERA_HOM."EGRValorFactorAjusteaFechaRetiro",CARTERA_HOM."EGRCantidad",CARTERA_HOM."EGRValorRescate",CARTERA_HOM."EGRPrecio",CARTERA_HOM."EGRTasa",CARTERA_HOM."EGRMonto",CARTERA_HOM."EGRMontoMonedaInstrumento"
                            ,CARTERA_HOM."EGRSpread",CARTERA_HOM."EGRPrecioAPlazo",CARTERA_HOM."EGRMontoAPlazo",CARTERA_HOM."EGRMontoAPlazoMonedaInstrumento",CARTERA_HOM."EGRMontoPresenteTIF",CARTERA_HOM."EGRMontoInteresTIF",CARTERA_HOM."EGRMontoReajusteTIF",CARTERA_HOM."EGRMontoInteresAcumTIF"						
                            ,CARTERA_HOM."EGRMontoReajusteAcumTIF",CARTERA_HOM."EGRMontoPresenteTIFMonedaInstrumento",CARTERA_HOM."EGRMontoInteresTIFMonedaInstrumento",CARTERA_HOM."EGRMontoReajusteTIFMonedaInstrumento",CARTERA_HOM."EGRMontoInteresAcumTIFMonedaInstrumento"
						  	,CARTERA_HOM."EGRMontoReajusteAcumTIFMonedaInstrumento",CARTERA_HOM."EGRIdSpotAsociado",CARTERA_HOM."EGRMontoMonedaPrincipal",CARTERA_HOM."EGRMontoMonedaSecundaria",CARTERA_HOM."EGRIdMonedaLiquidacion",CARTERA_HOM."EGRMontoLiquidacion"
							,CARTERA_HOM."EGRMontoLiquidacionMonedaPrincipal",CARTERA_HOM."EGRValorTipoCambioSpot",CARTERA_HOM."EGRValorParidad",CARTERA_HOM."EGRValorTipoCambioCotizador",CARTERA_HOM."EGREsPuntos",CARTERA_HOM."EGRTasaPuntosMonedaPrincipal",CARTERA_HOM."EGRTasaPuntosMonedaSecundaria"					
							,CARTERA_HOM."EGRTasaDescuentoAnticipo",CARTERA_HOM."EGRTipoCambioAnticipo",CARTERA_HOM."EGRMontoARecibirAnticipo",CARTERA_HOM."EGRTipoCambioObservado"
                   from "BSCL.Homologacion.BD::HOM.Cartera" as CARTERA_HOM
                    left join "BSCL.Custodia.BD::CUS.Cartera" as  CARTERA_OPT
                    on CARTERA_OPT."IdCartera" = CARTERA_HOM."IdCartera" 
                   where CARTERA_HOM."IdCarga" = p_IdCarga
                     and CARTERA_HOM."IdTipoInstrumento" ='FWD' 
                     and CARTERA_OPT."IdCartera" IS NULL;

	          --------------------------------------------                
              -- contamos los movimientos cargados	   ---                       
              --------------------------------------------
              select count(1) into v_total_mov_cargadas
              from "BSCL"."BSCL.Custodia.BD::CUS.Cartera"
              where "IdTipoInstrumento" ='FWD'
                and "IdCarga" = p_IdCarga;
                 
		  	 -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		  	 p_ResumenDatosProcesados = select 'CARGACARTERAFWD' as "CodigoProceso" 
	   									,'MFW' as "Modulo" 
	   		 							,v_total_mov_gestion as "TotalRegistrosSebra"
	   		  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   						    	  union all
	   		 				    	  select * from :p_ResumenDatosProcesados;
	END;	
END;

 
