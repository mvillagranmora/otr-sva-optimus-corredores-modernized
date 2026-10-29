SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarDetallePactos";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 


"BSCL.Homologacion.BD.Servicios::CargarDetallePactos"(

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
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
	-- declaracion variables
	   DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_mov_cargadas DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_cargadas DECIMAL(26,8):=0;
	   DECLARE v_periodofinal DATE := CONCAT(SUBSTRING(p_FechaFinal,1,7),'-01'); 	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
	   DECLARE v_MonedaSistema NVARCHAR(40):='';
	   DECLARE v_RutCli NVARCHAR(11):='0-0';
	   DECLARE v_ARR_MOV BIGINT ARRAY;
	   DECLARE v_j BIGINT;
	   DECLARE v_IdSecuenciaCartera BIGINT;
    
	-- creo tabla temporal para cargar los datos

		--------------------
		--Negocio Aqui
		--------------------

		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;
		
		SELECT LPAD(p_RutCliente, 11,' ') into v_RutCli FROM DUMMY;
		---
		SELECT COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
        		WHERE  "IdEmpresa" = p_IdEmpresa
        		AND  "CodigoParametro" = 'PLA.MonedaSistema';

		 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Subyacente"
				("IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","FechaIngresoRegistro" ,"FechaModificacionRegistro","IdEmpresa"
				,"IdUsuario","IdModulo","IdCliente","IdPortafolio","IdEstado","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdConcepto"
				,"IdTipoOperacion","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdBolsa","IdMesa","CodigoIFRS","EsNominal"
				,"Glosa","IdCondicionLiquidacion" ,"FechaLiquidacion","IdClasificacionTipoInstrumento" ,"IdTipoInstrumento","IdInstrumento","Nemotecnico"
				,"IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdBoveda","IdMonedaInstrumento"
				,"Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraEnMonedaInstrumento" ,"TasaAgente","MontoAPlazo"
				,"PrecioAPlazo","INGGlosa","INGFechaOperacion","INGCantidad","INGValorRescate","INGTasaAgente","INGMontoAPlazo","INGPrecioAPlazo"
				,"INGFechaPrecioTasaCompraAnterior","INGPrecioCompraAnterior","INGTasaCompraAnterior" ,"INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior" 
				,"INGMontoReajusteCompraAnterior","INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGFechaPrecioTasaCompraActual","INGPrecioCompraActual"
				,"INGTasaCompraActual","INGMontoATasaCompraActual","INGFechaPrecioTasaCompraProximo","INGPrecioCompraProximo","INGTasaCompraProximo","INGMontoATasaCompraProximo"
				,"INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGDurationCompraProximo","INGConvexidadCompraProximo","INGFechaPrecioTasaMercadoAnterior"
				,"INGPrecioMercadoAnterior","INGTasaMercadoAnterior" ,"INGIdOrigenTasaPrecioMercadoAnterior" ,"INGMontoATasaMercadoAnterior" ,"INGMontoInteresMercadoAnterior" 
				,"INGMontoReajusteMercadoAnterior","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior" ,"INGFechaPrecioTasaMercadoActual","INGIdOrigenTasaPrecioMercadoActual"
				,"INGPrecioMercadoActual","INGTasaMercadoActual" ,"INGMontoATasaMercadoActual","INGFechaPrecioTasaMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo" 
				,"INGIdOrigenTasaPrecioMercadoProximo","INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo" ,"INGMontoReajusteMercadoProximo","INGDurationMercadoProximo"
				,"INGConvexidadMercadoProximo","INGFechaPrecioTasaDescuentoAnterior","INGPrecioDescuentoAnterior","INGTasaDescuentoAnterior","INGMontoATasaDescuentoAnterior" 
				,"INGMontoInteresDescuentoAnterior","INGMontoReajusteDescuentoAnterior","INGDurationDescuentoAnterior" ,"INGConvexidadDescuentoAnterior" 
				,"INGFechaPrecioTasaDescuentoActual","INGPrecioDescuentoActual","INGTasaDescuentoActual" ,"INGMontoATasaDescuentoActual" ,"INGFechaPrecioTasaDescuentoProximo"
				,"INGPrecioDescuentoProximo","INGTasaDescuentoProximo" ,"INGMontoATasaDescuentoProximo" ,"INGMontoInteresDescuentoProximo" ,"INGMontoReajusteDescuentoProximo"
				,"INGDurationDescuentoProximo","INGConvexidadDescuentoProximo" ,"INGPrecioPactoAnterior","INGTasaPactoAnterior" ,"INGMontoATasaPactoAnterior"
				,"INGMontoInteresPactoAnterior","INGMontoReajustePactoAnterior","INGDurationPactoAnterior","INGConvexidadPactoAnterior","INGFechaPrecioTasaPactoActual"
				,"INGPrecioPactoActual","INGTasaPactoActual","INGMontoATasaPactoActual","INGPrecioPactoProximo","INGTasaPactoProximo","INGMontoATasaPactoProximo"
				,"INGMontoInteresPactoProximo","INGMontoReajustePactoProximo","INGDurationPactoProximo","INGConvexidadPactoProximo","INGTasaCorredora","INGFechaTasaCorredora" 
				,"INGMontoATasaCorredora","INGMontoCorteCupon","INGFechaCorteCupon","INGMontoCorteCuponProximo","EGRGlosa","EGRFechaOperacion","EGRIdTipoOperacion"
				,"EGRIdOperacion","EGRIdMovimientoCarteraTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion","EGRIdComprobanteFactReferencia"
				,"EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente","EGRCantidad","EGRMonto","EGRMontoEnMonedaInstrumento"
				,"EGRValorRescate","EGRPrecio","EGRTasa","EGRSpreadCompra","EGRSpreadMercado","EGRMontoAPlazo","EGRPrecioAPlazo","IdCarga","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER"                        
				,"COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","SEC_COMPROM","LIN_COMPROM","SEC_CAR_CORR"
				,"SEC_CAR_CLI")
                 select
                		 "BSCL.Custodia.BD.Secuencias::Subyacente".NEXTVAL														as "IdSubyacente"                  
					    ,"BSCL.Custodia.BD.Secuencias::FolioSubyacente".NEXTVAL 												as "FolioSubyacente"	
						,COALESCE(CARTERA_PACTO."FolioCartera",0)																		as "FolioCarteraRelacionada"  
						,COALESCE(CARTERA_PACTO."FechaOperacion",'0001-01-01')													as "FechaIngresoRegistro"
						,'0001-01-01'																							as "FechaModificacionRegistro"
						,p_IdEmpresa																							as "IdEmpresa"
						,p_IdUsuario																							as "IdUsuario"
						,p_Modulo																								as "IdModulo"
						,COALESCE(CARTERA_PACTO."IdCliente",0)																	as "IdCliente"
						,COALESCE(CARTERA_PACTO."IdPortafolio",0)																as "IdPortafolio"
						,COALESCE(CARTERA_PACTO."IdEstado",'') 																	as "IdEstado"
						,'MERF'																									as "IdTipoProducto"
						,''																										as "IdTipoCanalIngreso"
						,v_MonedaSistema																						as "IdMonedaSistema"
						,''																										as "IdConcepto"
						,COALESCE(CARTERA_PACTO."IdTipoOperacion",0)															as "IdTipoOperacion"
						,COALESCE(CARTERA_PACTO."FechaOperacion",'0001-01-01')													as "FechaOperacion"
						,COALESCE(CARTERA_PACTO."FechaVencimientoOperacion",'0001-01-01')										as "FechaVencimientoOperacion"
						,COALESCE(CARTERA_PACTO."PlazoEnDiasOperacion",0)  														as "PlazoEnDiasOperacion"
						,'' 																									as "IdBolsa"
						,0  																									as "IdMesa"
						,''																										as "CodigoIFRS"
						,''														 												as "EsNominal"
						,'' 																									as "Glosa"
						,'T0' 																									as "IdCondicionLiquidacion"
						,COALESCE(CARTERA_PACTO."FechaLiquidacion",'0001-01-01')															as "FechaLiquidacion"
						,COALESCE(TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento",'')		 									as "IdClasificacionTipoInstrumento"
						,COALESCE(TIPO_INSTRUMENTO."IdTipoInstrumento",'')														as "IdTipoInstrumento"
						,COALESCE(INSTRUMENTO."IdInstrumento",0)																as "IdInstrumento"
						,COALESCE(INSTRUMENTO."Nemotecnico",'')																	as "Nemotecnico"
						,COALESCE(TBCPDTPC."EMISOR",'')																			as "IdEmisor"
						,COALESCE(CARTERA."NemoSVS",'')																			as "NemoSVS"--REVISAR
						,COALESCE(TIPOREAJUSTE."ValorDestino",'') 																as "IdTipoReajuste"
						,COALESCE(CARTERA."FechaEmisionInstrumento",'0001-01-01')												as "FechaEmisionInstrumento"
						,COALESCE(CARTERA."FechaVencimientoInstrumento",'0001-01-01')											as "FechaVencimientoInstrumento"
						,'N'																									as "EsFungible"
						,COALESCE(CARTERA."IdBoveda",'')																		as "IdBoveda"
						,CASE WHEN TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" ='IF' THEN COALESCE(TIPOREAJUSTE."ValorDestino",'') 
						     ELSE COALESCE(INSTRUMENTO."IdMoneda",'')
						 END																									as "IdMonedaInstrumento"
						,TBCPDTPC."CANT_PC"																						as "Cantidad"
						,0																										as "CorteMinimo"
						,TBCPDTPC."VAL_RESC"																					as "ValorRescate"
						,TBCPDTPC."PRECIO"																						as "PrecioCompra"
						,TBCPDTPC."TIR"																							as "TasaCompra"
						,TBCPDTPC."MONTO_INI_CAP"																				as "MontoCompra"
						,0																										as "MontoCompraEnMonedaInstrumento"
						,0																										as "TasaAgente"
						,0																										as "MontoAPlazo"
						,0																										as "PrecioAPlazo"
						,''																										as "INGGlosa"
						,COALESCE(CARTERA_PACTO."FechaOperacion",'0001-01-01')	    											as "INGFechaOperacion"
						,TBCPDTPC."CANT_PC"																					as "INGCantidad"
						,TBCPDTPC."VAL_RESC"																					as "INGValorRescate"
						,0																										as "INGTasaAgente"
						,0																										as "INGMontoAPlazo"
						,0																										as "INGPrecioAPlazo"
						,'0001-01-01'																							as "INGFechaPrecioTasaCompraAnterior"
						,0																										as "INGPrecioCompraAnterior"
						,0																										as "INGTasaCompraAnterior"
						,0 																										as "INGMontoATasaCompraAnterior"
						,0																										as "INGMontoInteresCompraAnterior"
						,0																										as "INGMontoReajusteCompraAnterior"
						,0																										as "INGDurationCompraAnterior"
						,0																										as "INGConvexidadCompraAnterior"
						,'0001-01-01'																							as "INGFechaPrecioTasaCompraActual"
						,TBCPDTPC."PRECIO"																						as "INGPrecioCompraActual"
						,TBCPDTPC."TIR"																							as "INGTasaCompraActual"
						,TBCPDTPC."MONTO_INI_CAP" 																				as "INGMontoATasaCompraActual"
						,'0001-01-01'																							as "INGFechaPrecioTasaCompraProximo"
						,0																										as "INGPrecioCompraProximo"
						,0																										as "INGTasaCompraProximo"
						,0																										as "INGMontoATasaCompraProximo"
						,0																										as "INGMontoInteresCompraProximo"
						,0																										as "INGMontoReajusteCompraProximo"
						,0																										as "INGDurationCompraProximo"
						,0																										as "INGConvexidadCompraProximo"
						,'0001-01-01'																							as "INGFechaPrecioTasaMercadoAnterior"
						,0																										as "INGPrecioMercadoAnterior"
						,0																										as "INGTasaMercadoAnterior"
						,0																										as "INGIdOrigenTasaPrecioMercadoAnterior" 
						,0																										as "INGMontoATasaMercadoAnterior"
						,0																										as "INGMontoInteresMercadoAnterior"
						,0																										as "INGMontoReajusteMercadoAnterior"
						,0																										as "INGDurationMercadoAnterior"
						,0																										as "INGConvexidadMercadoAnterior"
						,'0001-01-01'																							as "INGFechaPrecioTasaMercadoActual"
						,0																										as "INGIdOrigenTasaPrecioMercadoActual"
						,TBCPDTPC."PRECIO"																						as "INGPrecioMercadoActual"
						,TBCPDTPC."TIR"																							as "INGTasaMercadoActual"
						,TBCPDTPC."MONTO_INI_CAP" 																				as "INGMontoATasaMercadoActual"
						,'0001-01-01'																							as "INGFechaPrecioTasaMercadoProximo"
						,0																										as "INGPrecioMercadoProximo"
						,0																										as "INGTasaMercadoProximo"
						,0																										as "INGIdOrigenTasaPrecioMercadoProximo"
						,0																										as "INGMontoATasaMercadoProximo"
						,0																										as "INGMontoInteresMercadoProximo"
						,0																										as "INGMontoReajusteMercadoProximo"
						,0																										as "INGDurationMercadoProximo"
						,0																										as "INGConvexidadMercadoProximo"
						,'0001-01-01'																							as "INGFechaPrecioTasaDescuentoAnterior"
						,0																										as "INGPrecioDescuentoAnterior"
						,0																										as "INGTasaDescuentoAnterior"
						,0																										as "INGMontoATasaDescuentoAnterior"
						,0																										as "INGMontoInteresDescuentoAnterior"
						,0																										as "INGMontoReajusteDescuentoAnterior"
						,0																										as "INGDurationDescuentoAnterior"
						,0																										as "INGConvexidadDescuentoAnterior"
						,'0001-01-01'																							as "INGFechaPrecioTasaDescuentoActual"
						,0																										as "INGPrecioDescuentoActual"
						,0																										as "INGTasaDescuentoActual"
						,0																										as "INGMontoATasaDescuentoActual"
						,'0001-01-01'																							as "INGFechaPrecioTasaDescuentoProximo"
						,0																										as "INGPrecioDescuentoProximo"
						,0																										as "INGTasaDescuentoProximo"
						,0																										as "INGMontoATasaDescuentoProximo"
						,0																										as "INGMontoInteresDescuentoProximo"
						,0																										as "INGMontoReajusteDescuentoProximo"
						,0																										as "INGDurationDescuentoProximo"
						,0																										as "INGConvexidadDescuentoProximo"
						,0																										as "INGPrecioPactoAnterior"
						,0																										as "INGTasaPactoAnterior"
						,0																										as "INGMontoATasaPactoAnterior"
						,0																										as "INGMontoInteresPactoAnterior"
						,0																										as "INGMontoReajustePactoAnterior"
						,0																										as "INGDurationPactoAnterior"
						,0																										as "INGConvexidadPactoAnterior"
						,'0001-01-01'																							as "INGFechaPrecioTasaPactoActual"
						,0																										as "INGPrecioPactoActual"
						,0																										as "INGTasaPactoActual"
						,0																										as "INGMontoATasaPactoActual"
						,0																										as "INGPrecioPactoProximo"
						,0																										as "INGTasaPactoProximo"
						,0																										as "INGMontoATasaPactoProximo"
						,0																										as "INGMontoInteresPactoProximo"
						,0																										as "INGMontoReajustePactoProximo"
						,0																										as "INGDurationPactoProximo"
						,0																										as "INGConvexidadPactoProximo"
						,0																										as "INGTasaCorredora"
						,'0001-01-01'																							as "INGFechaTasaCorredora"
						,0																										as "INGMontoATasaCorredora"
						,0																										as "INGMontoCorteCupon"
						,'0001-01-01'																							as "INGFechaCorteCupon"
						,0																										as "INGMontoCorteCuponProximo" 
						,0																										as "EGRGlosa"
						,'0001-01-01'																							as "EGRFechaOperacion"
						,''																										as "EGRIdTipoOperacion"
						,0																										as "EGRIdOperacion"
						,0																										as "EGRIdMovimientoCarteraTransito"
						,''																										as "EGRIdTipoCanalIngreso"
						,''																										as "EGRIdCondicionLiquidacion"
						,'0001-01-01'																							as "EGRFechaLiquidacion"
						,0																										as "EGRIdComprobanteFactReferencia"
						,0																										as "EGRFolioExternoReferencia"
						,''																										as "EGRIdModulo"
						,''																										as "EGRIdConcepto"
						,''																										as "EGRIdUsuario"
						,0																										as "EGRIdSucursal"
						,0																										as "EGRIdAgente"
						,0																										as "EGRCantidad"
						,0																										as "EGRMonto"
						,0																										as "EGRMontoEnMonedaInstrumento"
						,0																										as "EGRValorRescate"
						,0																										as "EGRPrecio"
						,0																										as "EGRTasa"
						,0																										as "EGRSpreadCompra"
						,0																										as "EGRSpreadMercado"
						,0																										as "EGRMontoAPlazo"
						,0																										as "EGRPrecioAPlazo"
						,p_IdCarga 																								as "IdCarga" 					  		
						,TBCPDTPC."RUT_CLI"                                                                                     as "RUT_CLI"	
						,TBCPDTPC."SEC_RUT_CLI"																					as "SEC_RUT_CLI"
						,0   																									as "SEC_CAR_INVER"                       
						,TBCPDTPC."COD_PER" 																					as "COD_PER" 								
						,0  																									as "FOLIO_COMP_ADJ"                     
						,''																										as "TIPO_COMP_ADJ"						
					    ,0 																										as "LIN_COMP_ADJ"                        
						,0 																										as "LIN_ORDEN"							
						,0 																										as "SEC_ORDEN"							
						,''																										as "TIPO_CARTERA"							
						,0 																										as "ID_ERROR"								
						,0					 																					as "SEC_MOVTO"
						,TBCPDTPC."SEC_COMPROM"																					as "SEC_COMPROM"
						,TBCPDTPC."LIN_COMPROM"																					as "LIN_COMPROM" 						        
			            ,TBCPDTPC."SEC_CAR_CORR"                                                                                as "SEC_CAR_CORR"
			            ,TBCPDTPC."SEC_CAR_CLI"                                                                                 as "SEC_CAR_CLI"
			        FROM "BSCL_VIRTUAL"."GES_TBCPDTPC" AS TBCPDTPC
			        INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS CARTERA_PACTO
			          ON CARTERA_PACTO."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM" 
			          AND CARTERA_PACTO."COD_PER" = TBCPDTPC."COD_PER"
                    INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
                      ON INSTRUMENTO."Nemotecnico" =  TBCPDTPC."NEMO"
                    INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO
                      ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
                       LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPOREAJUSTE
                        ON TIPOREAJUSTE."TipoValor"    = 'TipoReajuste'
                       AND TIPOREAJUSTE."ValorOrigen"  = TBCPDTPC."TIPO_REAJ" 
                    LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS CARTERA
                      ON CARTERA_PACTO."SEC_CAR_INVER" = TBCPDTPC."SEC_CAR_CLI" 
			         AND CARTERA_PACTO."COD_PER" = SUBSTRING(CARTERA_PACTO."FechaOperacion",1,7)
			         AND CARTERA."IdClasificacionTipoInstrumento" = TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento"
                    LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS CARTERA_HOM
                      ON CARTERA_HOM."COD_PER" =  TBCPDTPC."COD_PER"
                      AND CARTERA_HOM."SEC_COMPROM" =  TBCPDTPC."SEC_COMPROM"
			        WHERE TBCPDTPC."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
			       	 AND TBCPDTPC."COD_PER" <=  SUBSTRING(v_periodofinal,1,7) 
			         AND TBCPDTPC."RUT_CLI" =  CASE WHEN v_RutCli ='        0-0' then TBCPDTPC."RUT_CLI" ELSE v_RutCli END
		           	 AND CARTERA_HOM."RUT_CLI" IS NULL;
  	 
          --- ver como funcionara la venta y comras terminos
	      	 select count(*) into v_total_mov_gestion from "BSCL_VIRTUAL"."GES_TBCPDTPC" AS TBCPDTPC
	      	 WHERE TBCPDTPC."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
			    AND TBCPDTPC."COD_PER" <=  SUBSTRING(v_periodofinal,1,7) 
			    AND TBCPDTPC."RUT_CLI" =  CASE WHEN v_RutCli ='        0-0' then  TBCPDTPC."RUT_CLI" ELSE v_RutCli END;

		  	 select COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Homologacion.BD::HOM.Subyacente"
		  	 WHERE "IdCarga" = p_IdCarga
		    	 AND "SEC_COMPROM" <> 0;
		   
			
			-- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOFACTURACION' as "CodigoProceso" 
	   			  							  , p_Modulo as "Modulo" 
	   			 							  ,v_total_mov_gestion as "TotalRegistrosSebra"
	   			  							  ,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   							    	union all
	   							    	select * from :p_ResumenDatosProcesados;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
