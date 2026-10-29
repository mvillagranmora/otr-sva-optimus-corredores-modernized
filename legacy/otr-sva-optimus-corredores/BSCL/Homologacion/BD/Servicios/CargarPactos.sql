SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarPactos";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 


"BSCL.Homologacion.BD.Servicios::CargarPactos"(

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

		INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"(
				"IdCartera","FolioCartera","FolioCarteraRelacionada","FechaIngresoRegistro","FechaModificacionRegistro","IdUsuarioReserva","FechaReserva"                         
				,"IdEmpresa","IdUsuario","IdModulo","IdCliente","IdPortafolio","IdEstado","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","CodigoIFRS"	
				,"IdMovimientoCarteraTransito","IdConcepto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion"
				,"IdComprobanteFactReferencia","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion"
				,"EsIntraday","EsNominal","UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdClasificacionTipoInstrumento","IdTipoInstrumento"
				,"IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible"	
				,"IdBoveda","IdMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraEnMonedaInstrumento"		
				,"TasaAgente","MontoAPlazo","PrecioAPlazo","INGGlosa","INGFechaOperacion","INGCantidad","INGValorRescate","INGTasaAgente","INGMontoAPlazo"
				,"INGPrecioAPlazo","INGFechaPrecioTasaCompraAnterior","INGPrecioCompraAnterior","INGTasaCompraAnterior","INGMontoATasaCompraAnterior"		
				,"INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior","INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGFechaPrecioTasaCompraActual"		
				,"INGPrecioCompraActual","INGTasaCompraActual","INGMontoATasaCompraActual","INGFechaPrecioTasaCompraProximo","INGPrecioCompraProximo","INGTasaCompraProximo"
				,"INGMontoATasaCompraProximo","INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo","INGDurationCompraProximo","INGConvexidadCompraProximo"		  
				,"INGFechaPrecioTasaMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior","INGIdOrigenTasaPrecioMercadoAnterior","INGMontoATasaMercadoAnterior"	
				,"INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior","INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGFechaPrecioTasaMercadoActual"		
				,"INGIdOrigenTasaPrecioMercadoActual","INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGFechaPrecioTasaMercadoProximo"		
				,"INGPrecioMercadoProximo","INGTasaMercadoProximo","INGIdOrigenTasaPrecioMercadoProximo","INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo"	
				,"INGMontoReajusteMercadoProximo","INGDurationMercadoProximo","INGConvexidadMercadoProximo","INGFechaPrecioTasaDescuentoAnterior","INGPrecioDescuentoAnterior"
				,"INGTasaDescuentoAnterior","INGMontoATasaDescuentoAnterior","INGMontoInteresDescuentoAnterior","INGMontoReajusteDescuentoAnterior"	
				,"INGDurationDescuentoAnterior","INGConvexidadDescuentoAnterior","INGFechaPrecioTasaDescuentoActual","INGPrecioDescuentoActual"
				,"INGTasaDescuentoActual","INGMontoATasaDescuentoActual","INGFechaPrecioTasaDescuentoProximo","INGPrecioDescuentoProximo","INGTasaDescuentoProximo"
				,"INGMontoATasaDescuentoProximo","INGMontoInteresDescuentoProximo","INGMontoReajusteDescuentoProximo","INGDurationDescuentoProximo","INGConvexidadDescuentoProximo"
				,"INGPrecioPactoAnterior","INGTasaPactoAnterior","INGMontoATasaPactoAnterior","INGMontoInteresPactoAnterior","INGMontoReajustePactoAnterior"		
				,"INGDurationPactoAnterior","INGConvexidadPactoAnterior","INGFechaPrecioTasaPactoActual","INGPrecioPactoActual","INGTasaPactoActual"
				,"INGMontoATasaPactoActual","INGPrecioPactoProximo","INGTasaPactoProximo","INGMontoATasaPactoProximo","INGMontoInteresPactoProximo"		
				,"INGMontoReajustePactoProximo","INGDurationPactoProximo","INGConvexidadPactoProximo","INGTasaCorredora","INGFechaTasaCorredora"
				,"INGMontoATasaCorredora","INGMontoCorteCupon","INGFechaCorteCupon","INGMontoCorteCuponProximo","EGRGlosa","EGRFechaOperacion"
				,"EGRIdTipoOperacion","EGRIdOperacion","EGRIdMovimientoCarteraTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion"
				,"EGRIdComprobanteFactReferencia","EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente"	
				,"EGRCantidad","EGRMonto","EGRMontoEnMonedaInstrumento","EGRValorRescate","EGRPrecio","EGRTasa","EGRSpreadCompra","EGRSpreadMercado"	
				,"EGRMontoAPlazo","EGRPrecioAPlazo","IdCarga","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER"                        
				,"COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ","LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO","CAN_GARANTIA","SEC_COMPROM")  
				
				select
		         "BSCL.Custodia.BD.Secuencias::Cartera".NEXTVAL											 as "IdCartera"
		        ,"BSCL.Custodia.BD.Secuencias::FolioCartera".NEXTVAL 									 as "FolioCartera"			
				, 0 																					 as "FolioCarteraRelacionada"
				, '0001-01-01' 																			 as "FechaIngresoRegistro"
				, '0001-01-01' 																			 as "FechaModificacionRegistro"			
				, 0 																					 as "IdUsuarioReserva"
				, '0001-01-01' 																			 as "FechaReserva"
				, p_IdEmpresa 																			 as "IdEmpresa" 			
				, p_IdUsuario																			 as "IdOperacion" 						
                , p_Modulo																			 	 as "IdModulo"
                , COALESCE(CLIENTE."IdCliente",0) 													 	 as "IdCliente"
                , COALESCE(PORTAFOLIO."IdPortafolio",0) 												 as "IdPortafolio"
                ,'VIG' 																					 as "IdEstado"
                ,'MERF'																					 as "IdTipoProducto"   						
				, '' 																					 as "INGIdTipoCanalIngreso"
				, v_MonedaSistema																		 as "IdMonedaSistema"
				, 0 																					 as "CodigoIFRS"
				, 0 																					 as "IdMovimientoCarteraTransito"
				, '' 																					 as "IdConcepto"
				, OPERACION_MESA."ValorDestino"															 as "IdTipoOperacion"
				, 0																						 as "IdOperacion"
				, TBCPMSPC."FEC_OPERAC"																	 as "FechaOperacion"
				, TBCPMSPC."FEC_VCTO_COM"																 as "FechaVencimientoOperacion"
				, TBCPMSPC."PLAZO"																    	 as "PlazoEnDiasOperacion" 
				, 0 																					 as "IdComprobanteFactReferencia"
				, 0																						 as "FolioExternoReferencia"	
				, 0																					     as "IdMesa" 
				, ''																					 as "IdBolsa"
				, COALESCE(SUCURSAL."IdSucursal",0) 													 as "IdSucursal" 	
				, COALESCE(EMPLEADO."IdEmpleado",0)														 as "IdAgente"
				, 'T0' 																					 as "IdCondicionLiquidacion"  
				, TBCPMSPC."FEC_LIQUID"  														  		 as "FechaLiquidacion" 	
				, '' 																					 as "Intraday" 	
				, ''													 								 as "Nominal" 
				, ''																					 as "UsaGarantiaEspecifica"
				, ''																					 as "CobraPrimaAlInicio"  
				, 0																						 as "IdTipoPrestamo"
				, ''																					 as "Glosa"
				,'RF'                                               									 as "IdClasificacionTipoInstrumento"  										 
				,TIPOINSTRUMENTO."IdTipoInstrumento" 													 as "IdTipoInstrumento"
				,INSTRUMENTO."IdInstrumento" 															 as "IdInstrumento" 		
				,INSTRUMENTO."Nemotecnico" 																 as "Nemotecnico" 	
				,''																						 as "IdEmisor" 	
				, '' 																					 as "NemoSVS"
				,COALESCE(TIPOREAJUSTE."ValorDestino",'') 												 as "IdTipoReajuste"
				,'0001-01-01'																			 as "FechaEmisionInstrumento"		 
				,'0001-01-01' 																			 as "FechaVencimientoInstrumento"	
				,'N'																					 as "EsFungible" 
				, ''																					 as "IdBoveda" 	
				,COALESCE(TIPOREAJUSTE."ValorDestino",'')												 as "IdMonedaInstrumento" 	
				,TBCPMSPC."CANTIDAD_CAPITAL"															 as "Cantidad" 					
				,0 																						 as "CorteMinimo"
				,0 																						 as "ValorRescate" 
				,0		 																	 			 as "PrecioCompra"
				,TBCPMSPC."TASA_COMPROM"																 as "TasaCompra" 	
				,TBCPMSPC."MONTO_CAPITAL"	 															 as "MontoCompra"
				,0																						 as "MontoCompraEnMonedaInstrumento"
				,0 																						 as "TasaAgente"
				,0 																						 as "MontoAPlazo"  	
				,0 																						 as "PrecioAPlazo"
				,''																						 as "INGGlosa"	
				,TBCPMSPC."FEC_OPERAC"																	 as "INGFechaOperacion" 
				,TBCPMSPC."CANTIDAD_COMPROM" 															 as "INGCantidad"
				,0 																						 as "INGValorRescate"
				,TBCPMSPC."TASA_AGE"		 															 as "INGTasaAgente"
				,0 																						 as "INGMontoAPlazo" 
				,0 																						 as "INGPrecioAPlazo" 				 	
				,'0001-01-01'																			 as "INGFechaPrecioTasaCompraAnterior"
				,0 																						 as "INGPrecioCompraAnterior"
				,0																						 as "INGTasaCompraAnterior" 				
				,0																						 as "INGMontoATasaCompraAnterior" 		
				,0 																						 as "INGMontoInteresCompraAnterior" 		
				,0 										  												 as "INGMontoReajusteCompraAnterior"		
				,0 																						 as "INGDurationCompraAnterior" 			
				,0																						 as "INGConvexidadCompraAnterior" 			
				,'0001-01-01'																			 as "INGFechaPrecioTasaCompraActual"		
				,0			 																			 as "INGPrecioCompraActual"			
				,TBCPMSPC."TASA_COMPROM"																 as "INGTasaCompraActual" 					
				,TBCPMSPC."VALOR_PTE"																	 as "INGMontoATasaCompraActual"
	 			,'0001-01-01' 																			 as "INGFechaPrecioTasaCompraProximo"
	 			,0 																						 as "INGPrecioCompraProximo"				
				,0 																						 as "INGTasaCompraProximo" 				
				,0																						 as "INGMontoATasaCompraProximo" 
				,0 																						 as "INGMontoInteresCompraProximo"
				,0 																						 as "INGMontoReajusteCompraProximo"
				,0 																						 as "INGDurationCompraProximo"
				,0 																						 as "INGConvexidadCompraProximo"
				,'0001-01-01'																			 as "INGFechaPrecioTasaMercadoAnterior"	
				,0 																						 as "INGPrecioMercadoAnterior"
				,0 																						 as "INGTasaMercadoAnterior"
				,0 																						 as "INGIdOrigenTasaPrecioMercadoAnterior"
				,0 																						 as "INGMontoATasaMercadoAnterior"
				,0                             															 as "INGMontoInteresMercadoAnterior"
				,0 																						 as "INGMontoReajusteMercadoAnterior"
				,0 																						 as "INGDurationMercadoAnterior"
				,0 																						 as "INGConvexidadMercadoAnterior"
				,'0001-01-01' 																			 as "INGFechaPrecioTasaMercadoActual"		
				,0																						 as "INGIdOrigenTasaPrecioMercadoActual" 
				,0																						 as "INGPrecioMercadoActual"
				,TBCPMSPC."TASA_COMPROM"																 as "INGTasaMercadoActual" 
				,TBCPMSPC."VALOR_PTE"																	 as "INGMontoATasaMercadoActual"
				,'0001-01-01'																			 as "INGFechaPrecioTasaMercadoProximo"
				,0 																						 as "INGPrecioMercadoProximo"
				,0 																						 as "INGTasaMercadoProximo"
				,0 																						 as "INGIdOrigenTasaPrecioMercadoProximo"
				,0 																						 as "INGMontoATasaMercadoProximo"
				,0																						 as "INGMontoInteresMercadoProximo"
				,0 																						 as "INGMontoReajusteMercadoProximo"
				,0 																						 as "INGDurationMercadoProximo"
				,0																						 as "INGConvexidadMercadoProximo"
				,'0001-01-01' 																			 as "INGFechaPrecioTasaDescuentoAnterior"	
				,0 																						 as "INGPrecioDescuentoAnterior"
				,0 																						 as "INGTasaDescuentoAnterior"
				,0 																						 as "INGMontoATasaDescuentoAnterior"
				,0 																						 as "INGMontoInteresDescuentoAnterior"
				,0 																						 as "INGMontoReajusteDescuentoAnterior"
				,0 																						 as "INGDurationDescuentoAnterior"
				,0 																						 as "INGConvexidadDescuentoAnterior"
				,'0001-01-01'																			 as "INGFechaPrecioTasaDescuentoActual"
				,0																						 as "INGPrecioDescuentoActual"
				,0																						 as "INGTasaDescuentoActual"
				,0 																						 as "INGMontoATasaDescuentoActual"
				,'0001-01-01' 																			 as "INGFechaPrecioTasaDescuentoProximo"
				,0 																						 as "INGPrecioDescuentoProximo"
				,0 																						 as "INGTasaDescuentoProximo"
				,0 																						 as "INGMontoATasaDescuentoProximo"
				,0 																						 as "INGMontoInteresDescuentoProximo"
				,0 																						 as "INGMontoReajusteDescuentoProximo"
				,0 																						 as "INGDurationDescuentoProximo"
				,0 																						 as "INGConvexidadDescuentoProximo"
				,0																						 as "INGPrecioPactoAnterior"
				,0																						 as "INGTasaPactoAnterior"
				,0																						 as "INGMontoATasaPactoAnterior"
				,0																						 as "INGMontoInteresPactoAnterior"
				,0																						 as "INGMontoReajustePactoAnterior"
				,0																						 as "INGDurationPactoAnterior"
				,0																						 as "INGConvexidadPactoAnterior"
				,'0001-01-01'																			 as "INGFechaPrecioTasaPactoActual"
				,0																						 as "INGPrecioPactoActual"
				,TBCPMSPC."TASA_COMPROM"																 as "INGTasaPactoActual"
				,TBCPMSPC."VALOR_PTE"																	 as "INGMontoATasaPactoActual"
				,0																						 as "INGPrecioPactoProximo"
				,0																						 as "INGTasaPactoProximo"
				,0																						 as "INGMontoATasaPactoProximo"
				,0																						 as "INGMontoInteresPactoProximo"
				,0																						 as "INGMontoReajustePactoProximo"
				,0																						 as "INGDurationPactoProximo"
				,0																						 as "INGConvexidadPactoProximo"
				,0																						 as "INGTasaCorredora"
				,'0001-01-01'																			 as "INGFechaTasaCorredora" 
				,0 																						 as "INGMontoATasaCorredora"
				,0 																						 as "INGMontoCorteCupon"
				,'0001-01-01' 																			 as "INGFechaCorteCupon"
				,0 																						 as "INGMontoCorteCuponProximo"
				,'' 																					 as "EGRGlosa"
				,'0001-01-01'																			 as "EGRFechaOperacion"
				,'' 																					 as "EGRIdTipoOperacion"
				,0																						 as "EGRIdOperacion"
				,0  																					 as "EGRIdMovimientoCarteraTransito"
				,'' 																					 as "EGRIdTipoCanalIngreso"
				,'' 																					 as "EGRIdCondicionLiquidacion"
				,'0001-01-01'																			 as "EGRFechaLiquidacion"
				,0 																						 as "EGRIdComprobanteFactReferencia"
				,0 																						 as "EGRFolioExternoReferencia"
				,''			 																			 as "EGRIdModulo"
				,''																						 as "EGRIdConcepto"
				,0 																						 as "EGRIdUsuario"
				,0 																						 as "EGRIdSucursal"
				,0 																						 as "EGRIdAgente"
				,0 																						 as "EGRCantidad"
				,0																						 as "EGRMonto"
				,0 																						 as "EGRMontoEnMonedaInstrumento"
				,0 																						 as "EGRValorRescate"
				,0			 																			 as "EGRPrecio"
				,0				 																		 as "EGRTasa"
				,0																						 as "EGRSpreadCompra"
				,0 																						 as "EGRSpreadMercado"
				,0 																						 as "EGRMontoAPlazo"
				,0 																						 as "EGRPrecioAPlazo"
				,p_IdCarga																				 as "IdCarga"                            
	 			,TBCPMSPC."RUT_CLI"							 											 as "RUT_CLI"
	 			,TBCPMSPC."SEC_RUT_CLI"																	 as "SEC_RUT_CLI"
	 			,0																						 as "SEC_CAR_INVER"
	 			,TBCPMSPC."COD_PER"																		 as "COD_PER"
	 			,0																						 as "FOLIO_COMP_ADJ"
	 			,''																						 as "TIPO_COMP_ADJ"
     			,0						 																 as "LIN_COMP_ADJ"
	 			,0																						 as "LIN_ORDEN"
	 			,0																						 as "SEC_ORDEN"
	 			,0				 																		 as "TIPO_CARTERA"
	 			,0			 																			 as "ID_ERROR"
	 			,TBCPMSPC."SEC_MOVTO_CTDO"																 as "SEC_MOVTO"	
	 			,0																						 as "CAN_GARANTIA"
	 			,TBCPMSPC."SEC_COMPROM"                                                                  as "SEC_COMPROM"
		        FROM "BSCL_VIRTUAL"."GES_TBCPMSPC" AS TBCPMSPC
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO
		        	ON DOCUMENTO."DocumentoIdentidad" = TRIM(TBCPMSPC."RUT_CLI")
		        INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
		        	ON PERSONA."IdPersona" = DOCUMENTO."IdPersona"
		        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
		        	ON CLIENTE."IdPersona" = PERSONA."IdPersona"
		        INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
		        	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
		        	AND PORTAFOLIO."CodigoPortafolio" = TO_VARCHAR(TBCPMSPC."SEC_RUT_CLI")
		        INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
		        	ON INSTRUMENTO."Nemotecnico" = case when TBCPMSPC."TIPO_COMPROM" = 'CP' THEN 'PACTO COMPRA' ELSE 'PACTO VENTA' END
		        INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO
		        	ON TIPOINSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
		        LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPOREAJUSTE
		        	ON TIPOREAJUSTE."TipoValor" = 'TipoReajuste'
		        	AND TIPOREAJUSTE."ValorOrigen" = TBCPMSPC."TIPO_REAJ_COMP"
		        LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as OPERACION_MESA
		        	ON OPERACION_MESA."TipoValor" ='OperacionMesaRF'
		        	AND OPERACION_MESA."ValorOrigen" = TBCPMSPC."TIPO_COMPROM"
		        LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado"  AS EMPLEADO
		        	ON  EMPLEADO."CodigoAgenteMigracion"  = TBCPMSPC."COD_AGENTE"	
		        LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCURSAL
		        	ON  SUCURSAL."CodigoSucursal"  = TBCPMSPC."COD_SUC"		
                LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS CARTERA
		       		ON CARTERA."COD_PER" = TBCPMSPC."COD_PER"
		        	AND CARTERA."RUT_CLI" = TBCPMSPC."RUT_CLI"
		        	AND CARTERA."SEC_RUT_CLI" = TBCPMSPC."SEC_RUT_CLI"
		        	AND CARTERA."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 
		        WHERE TBCPMSPC."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		        	AND TBCPMSPC."COD_PER" <=  SUBSTRING(v_periodofinal,1,7) 
		        	AND TBCPMSPC."RUT_CLI" =  CASE WHEN TRIM(p_RutCliente) ='0-0' then  TBCPMSPC."RUT_CLI" ELSE v_RutCli END
		        	AND TBCPMSPC."TIPO_COMPROM" IN ('CP','VP')
		        	AND TBCPMSPC."ESTADO" IN ('E','A') 
		        AND CARTERA."RUT_CLI" IS NULL;

          --- ver como funcionara la venta y comras terminos

	      	 select count(*) into v_total_mov_gestion from "BSCL_VIRTUAL"."GES_TBCPMSPC" AS TBCPMSPC
	      	 WHERE TBCPMSPC."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
		  	 	AND TBCPMSPC."COD_PER" <=  SUBSTRING(v_periodofinal,1,7) 
		  	 	AND TBCPMSPC."RUT_CLI" =  CASE WHEN v_RutCli ='        0-0' then  TBCPMSPC."RUT_CLI" ELSE v_RutCli END
		   		AND TBCPMSPC."TIPO_COMPROM" IN ('CP','VP');
	        

		  	 SELECT COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Homologacion.BD::HOM.Cartera"
		  	 WHERE "IdCarga" = p_IdCarga
		    	 AND "SEC_COMPROM" <> 0;
		    	 
		    	 
		    	 
		    	 
		    	 
		   
			
			-- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOFACTURACION' as "CodigoProceso" 
	   			  							,'FAC' as "Modulo" 
	   			 							,v_total_mov_gestion as "TotalRegistrosSebra"
	   			  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   							    	union all
	   							    	select * from :p_ResumenDatosProcesados;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
