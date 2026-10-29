SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraGTRF";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCarteraGTRF"
	(  
	 	IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	IN  p_IdModulo         			NVARCHAR(3),     -- modulo 
     	IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
		IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
		-- Parametros salida del servicio publico
		-- Salidas para mensajes de advertencias y errores
		--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalPortafoliosCuentaACargar" DECIMAL(26,8),"TotalPortafoliosConCuentaInversion" DECIMAL(26,8)),
		OUT p_out_ErroresContador int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(520); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			nvarchar(520);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	
	    DECLARE v_FechaSistema NVARCHAR(40);
	    DECLARE v_MaxPeriodo NVARCHAR(40);
	    DECLARE v_FechaInicioPeriodo NVARCHAR(10);
	    DECLARE v_periodo  DATE := CONCAT(SUBSTRING(p_FechaInicial,1,7),'-01');
        DECLARE v_periodofinal DATE := CONCAT(SUBSTRING(p_FechaFinal,1,7),'-01'); 
	    DECLARE v_RutCli NVARCHAR(11):='0-0';
	    DECLARE v_MonedaSistema NVARCHAR(10);
	        
		--------- CONTROL DE ERRORES
		--- Controlado
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :p_out_ErroresContador = 0 THEN	 
		 	    p_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt nvarchar(40);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into p_out_ErroresContador 
				from :p_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)
				p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		p_out_ErroresContador		:= 0;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        
				
				SELECT LPAD(p_RutCliente, 11,' ') into v_RutCli FROM DUMMY;
				
				SELECT COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
        		WHERE  "IdEmpresa" = p_IdEmpresa
        		AND  "CodigoParametro" = 'PLA.MonedaSistema';

            ----------------------
            --- garantias RF  ----
            ----------------------
			INSERT INTO "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" ("IdGarantia","FolioGarantia","FolioCarteraOrigen","FolioCarteraGarantizada","EsEspecifica","IdInstrumentoGarantizado","IdContraparteGarantia"
				,"IdOrigenGarantia","IdEspecieGarantia","FechaIngresoRegistro","FechaModificacionRegistro","IdEmpresa","IdUsuario","IdModulo","IdCliente"
				,"IdPortafolio","IdEstado","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","CodigoIFRS","IdConcepto","IdTipoOperacion","IdOperacion"
				,"FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdComprobanteFactReferencia","FolioExternoReferencia","IdMesa"
				,"IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsNominal","Glosa","IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento"
				,"EsFungible","IdBoveda","IdMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraEnMonedaInstrumento"
				,"TasaAgente","MontoAPlazo","PrecioAPlazo","INGGlosa","INGFechaOperacion","INGCantidad","INGValorRescate","INGTasaAgente","INGMontoAPlazo"
				,"INGPrecioAPlazo","INGFechaPrecioTasaCompraAnterior","INGPrecioCompraAnterior","INGTasaCompraAnterior","INGMontoATasaCompraAnterior"
				,"INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior","INGDurationCompraAnterior","INGConvexidadCompraAnterior"
				,"INGFechaPrecioTasaCompraActual","INGPrecioCompraActual","INGTasaCompraActual","INGMontoATasaCompraActual","INGFechaPrecioTasaCompraProximo"
				,"INGPrecioCompraProximo","INGTasaCompraProximo","INGMontoATasaCompraProximo","INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo"
				,"INGDurationCompraProximo","INGConvexidadCompraProximo","INGFechaPrecioTasaMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior"
				,"INGIdOrigenTasaPrecioMercadoAnterior","INGMontoATasaMercadoAnterior","INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior"
				,"INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGFechaPrecioTasaMercadoActual","INGIdOrigenTasaPrecioMercadoActual"
				,"INGPrecioMercadoActual","INGTasaMercadoActual","INGMontoATasaMercadoActual","INGFechaPrecioTasaMercadoProximo","INGPrecioMercadoProximo"
				,"INGTasaMercadoProximo","INGIdOrigenTasaPrecioMercadoProximo","INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo","INGMontoReajusteMercadoProximo"
				,"INGDurationMercadoProximo","INGConvexidadMercadoProximo","INGFechaPrecioTasaDescuentoAnterior","INGPrecioDescuentoAnterior","INGTasaDescuentoAnterior"
				,"INGMontoATasaDescuentoAnterior","INGMontoInteresDescuentoAnterior","INGMontoReajusteDescuentoAnterior","INGDurationDescuentoAnterior","INGConvexidadDescuentoAnterior"
				,"INGFechaPrecioTasaDescuentoActual","INGPrecioDescuentoActual","INGTasaDescuentoActual","INGMontoATasaDescuentoActual","INGFechaPrecioTasaDescuentoProximo"
				,"INGPrecioDescuentoProximo","INGTasaDescuentoProximo","INGMontoATasaDescuentoProximo","INGMontoInteresDescuentoProximo","INGMontoReajusteDescuentoProximo"
				,"INGDurationDescuentoProximo","INGConvexidadDescuentoProximo","INGPrecioPactoAnterior","INGTasaPactoAnterior","INGMontoATasaPactoAnterior"
				,"INGMontoInteresPactoAnterior","INGMontoReajustePactoAnterior","INGDurationPactoAnterior","INGConvexidadPactoAnterior","INGFechaPrecioTasaPactoActual"
				,"INGPrecioPactoActual","INGTasaPactoActual","INGMontoATasaPactoActual","INGPrecioPactoProximo","INGTasaPactoProximo","INGMontoATasaPactoProximo"
				,"INGMontoInteresPactoProximo","INGMontoReajustePactoProximo","INGDurationPactoProximo","INGConvexidadPactoProximo","INGTasaCorredora","INGFechaTasaCorredora"
				,"INGMontoATasaCorredora","INGMontoCorteCupon","INGFechaCorteCupon","INGMontoCorteCuponProximo","EGRGlosa","EGRFechaOperacion","EGRIdTipoOperacion"
				,"EGRIdOperacion","EGRIdMovimientoCarteraTransito","EGRIdTipoCanalIngreso","EGRIdCondicionLiquidacion","EGRFechaLiquidacion","EGRIdComprobanteFactReferencia"
				,"EGRFolioExternoReferencia","EGRIdModulo","EGRIdConcepto","EGRIdUsuario","EGRIdSucursal","EGRIdAgente","EGRCantidad","EGRMonto"
				,"EGRMontoEnMonedaInstrumento","EGRValorRescate","EGRPrecio","EGRTasa","EGRSpreadCompra","EGRSpreadMercado","EGRMontoAPlazo"
				,"EGRPrecioAPlazo","IdCarga","RUT_CLI","SEC_RUT_CLI","SEC_CAR_INVER","COD_PER","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","LIN_COMP_ADJ"
				,"LIN_ORDEN","SEC_ORDEN","TIPO_CARTERA","ID_ERROR","SEC_MOVTO")
                
                select 	"BSCL.Custodia.BD.Secuencias::GarantiaId".NEXTVAL 																	as "IdGarantia"
						,"BSCL.Custodia.BD.Secuencias::GarantiaFolio".NEXTVAL																as "FolioGarantia"
						,0 																													as "FolioCarteraOrigen"
						,0 				 																									as "FolioCarteraGarantizada"
						,'N'																												as "EsEspecifica"
						,0 																													as "IdInstrumentoGarantizado"
						,0 																													as "IdContraparteGarantia"
						,'CUS' 																												as "IdOrigenGarantia"
						,0 																													as "IdEspecieGarantia"
						,CONCAT(A1."COD_PER",'-01')																							as "FechaIngresoRegistro"
						,'0001-01-01'																										as "FechaModificacionRegistro"
						,p_IdEmpresa																										as "IdEmpresa"
						,p_IdUsuario																										as "IdUsuario"
						,p_IdModulo																											as "IdModulo"
						,COALESCE(A4."IdCliente",0)  																						as "IdCliente"
						,COALESCE(A5."IdPortafolio",0) 																						as "IdPortafolio"
						,'LIQ' 																												as "IdEstado"
						,'' 																												as "IdTipoProducto"
						,'' 																												as "IdTipoCanalIngreso"
						,v_MonedaSistema 																									as "IdMonedaSistema"
						,''																													as "CodigoIFRS"
						,''																													as "IdConcepto"
						,0 																													as "IdTipoOperacion"
						,0 																													as "IdOperacion"
						,'0001-01-01' 																										as "FechaOperacion"
						,'0001-01-01'																										as "FechaVencimientoOperacion"
						,0 																													as "PlazoEnDiasOperacion"
						,0 																													as "IdComprobanteFactReferencia"
						,0																													as "FolioExternoReferencia"
						,0 																													as "IdMesa"
						,'' 																												as "IdBolsa"
						,0 																													as "IdSucursal"
						,0 																													as "IdAgente"
						,'T0' 																												as "IdCondicionLiquidacion"
						,A1."FEC_INVER"																										as "FechaLiquidacion"
						,'N' 																												as "EsNominal"
						,''																													as "Glosa"
						,COALESCE(A7."IdClasificacionTipoInstrumento",'')																	as "IdClasificacionTipoInstrumento"
						,COALESCE(A6."IdTipoInstrumento",'')																				as "IdTipoInstrumento"
						,COALESCE(A6."IdInstrumento",0)																						as "IdInstrumento"
						,COALESCE(A6."Nemotecnico",'') 																						as "Nemotecnico"
						,COALESCE(A6."IdEmisor",'') 																						as "IdEmisor"
						,''																													as "NemoSVS"
						,COALESCE(A6."IdMoneda",'')																							as "IdTipoReajuste"
						,'0001-01-01'																										as "FechaEmisionInstrumento"
						,'0001-01-01'																										as "FechaVencimientoInstrumento"
						,'N'																												as "EsFungible"
						,CASE WHEN A1."COD_BOVEDA" = 'CIA' THEN 'COM' ELSE A1."COD_BOVEDA" END 												as "IdBoveda"
						,COALESCE(A6."IdMoneda",'')																							as "IdMonedaInstrumento"
						,A1."CAN_GARANTIA"																									as "Cantidad"
						,0 																													as "CorteMinimo"
						,0 																													as "ValorRescate"
						,A1."PRECIO_PROM_COMP"																								as "PrecioCompra"
						,0 																													as "TasaCompra"
						,A1."CAN_GARANTIA" * ( A1."MONTO_VAL_DISP" / (A1."CAN_ACTUAL" + A1."CAN_COM_PLAZO" + A1."CAN_GARANTIA"))			as "MontoCompra"
						,0 																													as "MontoCompraEnMonedaInstrumento"
						,0 																													as "TasaAgente"
						,0 																													as "MontoAPlazo"
						,0 																													as "PrecioAPlazo"
						,'' 																												as "INGGlosa"
						,LAST_DAY(TO_DATE(CONCAT(A1."COD_PER",'-01'),'YYYY-MM-DD'))															as "INGFechaOperacion"
						,A1."CAN_GARANTIA"																									as "INGCantidad"
						,0 																													as "INGValorRescate"
						,0 																													as "INGTasaAgente"
						,0 																													as "INGMontoAPlazo"
						,0 																													as "INGPrecioAPlazo"
						,'0001-01-01'																								 		as "INGFechaPrecioTasaCompraAnterior"
						,0																													as "INGPrecioCompraAnterior"
						,0 																													as "INGTasaCompraAnterior"
						,0 																													as "INGMontoATasaCompraAnterior"
						,0 																													as "INGMontoInteresCompraAnterior"
						,0 																													as "INGMontoReajusteCompraAnterior"
						,0 																													as "INGDurationCompraAnterior"
						,0 																													as "INGConvexidadCompraAnterior"
						,'0001-01-01'																										as "INGFechaPrecioTasaCompraActual"
						,A1."PRECIO_MED_CIE"																								as "INGPrecioCompraActual"
						,A1."TIR_PROM_COMP"																									as "INGTasaCompraActual"
						,A1."CAN_GARANTIA" * ( A1."MONTO_VAL_DISP" / (A1."CAN_ACTUAL" + A1."CAN_COM_PLAZO" + A1."CAN_GARANTIA"))			as "INGMontoATasaCompraActual"
						,'0001-01-01'																										as "INGFechaPrecioTasaCompraProximo"
						,0 																													as "INGPrecioCompraProximo"
						,0 																													as "INGTasaCompraProximo"
						,0 																													as "INGMontoATasaCompraProximo"
						,0 																													as "INGMontoInteresCompraProximo"
						,0 																													as "INGMontoReajusteCompraProximo"
						,0 																													as "INGDurationCompraProximo"
						,0 																													as "INGConvexidadCompraProximo"
						,'0001-01-01'																										as "INGFechaPrecioTasaMercadoAnterior"
						,0																													as "INGPrecioMercadoAnterior"
						,0 																													as "INGTasaMercadoAnterior"
						,0 																													as "INGIdOrigenTasaPrecioMercadoAnterior"
						,0 																													as "INGMontoATasaMercadoAnterior"
						,0 																													as "INGMontoInteresMercadoAnterior"
						,0 																													as "INGMontoReajusteMercadoAnterior"
						,0 																													as "INGDurationMercadoAnterior"
						,0 																													as "INGConvexidadMercadoAnterior"
						,'0001-01-01'																										as "INGFechaPrecioTasaMercadoActual"
						,0																													as "INGIdOrigenTasaPrecioMercadoActual"
						,A1."PRECIO_MED_CIE"																								as "INGPrecioMercadoActual"
						,A1."TIR_MED_CIE"																									as "INGTasaMercadoActual"
						,A1."CAN_GARANTIA" * ( A1."MONTO_VAL_DISP" / (A1."CAN_ACTUAL" + A1."CAN_COM_PLAZO" + A1."CAN_GARANTIA"))			as "INGMontoATasaMercadoActual"
						,'0001-01-01'																										as "INGFechaPrecioTasaMercadoProximo"
						,0																												    as "INGPrecioMercadoProximo"
						,0 																													as "INGTasaMercadoProximo"
						,0																													as "INGIdOrigenTasaPrecioMercadoProximo"
						,0 																													as "INGMontoATasaMercadoProximo"
						,0																													as "INGMontoInteresMercadoProximo"
						,0 																													as "INGMontoReajusteMercadoProximo"
						,0 																													as "INGDurationMercadoProximo"
						,0 																													as "INGConvexidadMercadoProximo"
						,'0001-01-01'																										as "INGFechaPrecioTasaDescuentoAnterior"
						,0 																													as "INGPrecioDescuentoAnterior"
						,0 																													as "INGTasaDescuentoAnterior"
						,0																													as "INGMontoATasaDescuentoAnterior"
						,0 																													as "INGMontoInteresDescuentoAnterior"
						,0 																													as "INGMontoReajusteDescuentoAnterior"
						,0 																													as "INGDurationDescuentoAnterior"
						,0 																													as "INGConvexidadDescuentoAnterior"
						,'0001-01-01'																										as "INGFechaPrecioTasaDescuentoActual"
						,0 																													as "INGPrecioDescuentoActual"
						,0 																													as "INGTasaDescuentoActual"
						,0 																													as "INGMontoATasaDescuentoActual"
						,'0001-01-01'																										as "INGFechaPrecioTasaDescuentoProximo"
						,0 																													as "INGPrecioDescuentoProximo"
						,0 																													as "INGTasaDescuentoProximo"
						,0 																													as "INGMontoATasaDescuentoProximo"
						,0 																													as "INGMontoInteresDescuentoProximo"
						,0 																													as "INGMontoReajusteDescuentoProximo"
						,0																													as "INGDurationDescuentoProximo"
						,0 																													as "INGConvexidadDescuentoProximo"
						,0 																													as "INGPrecioPactoAnterior"
						,0 																													as "INGTasaPactoAnterior"
						,0 																													as "INGMontoATasaPactoAnterior"
						,0 																													as "INGMontoInteresPactoAnterior"
						,0 																													as "INGMontoReajustePactoAnterior"
						,0 																													as "INGDurationPactoAnterior"
						,0 																													as "INGConvexidadPactoAnterior"
						,'0001-01-01'																										as "INGFechaPrecioTasaPactoActual"
						,0 																													as "INGPrecioPactoActual"
						,0 																													as "INGTasaPactoActual"
						,0 																													as "INGMontoATasaPactoActual"
						,0 																													as "INGPrecioPactoProximo"
						,0 																													as "INGTasaPactoProximo"
						,0 																													as "INGMontoATasaPactoProximo"
						,0 																													as "INGMontoInteresPactoProximo"
						,0 																													as "INGMontoReajustePactoProximo"
						,0 																													as "INGDurationPactoProximo"
						,0 																													as "INGConvexidadPactoProximo"
						,0 																													as "INGTasaCorredora"
						,'0001-01-01'																										as "INGFechaTasaCorredora"
						,0																													as "INGMontoATasaCorredora"
						,0 																													as "INGMontoCorteCupon"
						,'0001-01-01'																										as "INGFechaCorteCupon"
						,0 																													as "INGMontoCorteCuponProximo"
						,''																													as "EGRGlosa"
						,LAST_DAY(ADD_MONTHS(TO_DATE(CONCAT(A1."COD_PER",'-01'),'YYYY-MM-DD'),1))											as "EGRFechaOperacion"
						,'' 																												as "EGRIdTipoOperacion"
						,0 																													as "EGRIdOperacion"
						,0 																													as "EGRIdMovimientoCarteraTransito"
						,''																						 							as "EGRIdTipoCanalIngreso"
						,'T0' 																												as "EGRIdCondicionLiquidacion"
						,A1."FEC_INVER"																										as "EGRFechaLiquidacion"
						,0																													as "EGRIdComprobanteFactReferencia"
						,0 																													as "EGRFolioExternoReferencia"
						,p_IdModulo																											as "EGRIdModulo"
						,''																													as "EGRIdConcepto"
						,0 																													as "EGRIdUsuario"
						,0 																													as "EGRIdSucursal"
						,0 																													as "EGRIdAgente"
						,A1."CAN_GARANTIA"																									as "EGRCantidad"
						,A1."CAN_GARANTIA" * ( A1."MONTO_VAL_DISP" / (A1."CAN_ACTUAL" + A1."CAN_COM_PLAZO" + A1."CAN_GARANTIA"))			as "EGRMonto"
						,0 																													as "EGRMontoEnMonedaInstrumento"
						,0 																													as "EGRValorRescate"
						,A1."PRECIO_MED_CIE"																								as "EGRPrecio"
						,A1."TIR_MED_CIE"																									as "EGRTasa"
						,0																													as "EGRSpreadCompra"
						,0																													as "EGRSpreadMercado"
						,0 																													as "EGRMontoAPlazo"
						,0 																													as "EGRPrecioAPlazo"
						,p_IdCarga																											as "IdCarga"
						,A1."RUT_CLI"																											as "RUT_CLI"
						,A1."SEC_RUT_CLI"																										as "SEC_RUT_CLI"
						,A1."SEC_CAR_INVER"																									as "SEC_CAR_INVER"
						,A1."COD_PER"																											as "COD_PER"
						,A1."FOLIO_COMP_ADJ"																									as "FOLIO_COMP_ADJ"
						,A1."TIPO_COMP_ADJ"																									as "TIPO_COMP_ADJ"
						,A1."LIN_COMP_ADJ" 																									as "LIN_COMP_ADJ"
						,0 																													as "LIN_ORDEN"
						,0 																													as "SEC_ORDEN"
						,A1."TIPO_CARTERA" 																									as "TIPO_CARTERA"
						,0 																													as "ID_ERROR"
						,0 																													as "SEC_MOVTO"
		                FROM "BSCL_VIRTUAL"."GES_TBCTCIRF" AS A1
		                LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS A2
				        ON A2."DocumentoIdentidad" = TRIM(A1."RUT_CLI")
				        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS A3
				        ON A3."IdPersona" = A2."IdPersona"
				        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS A4
				        ON A4."IdPersona" = A3."IdPersona"
				        LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS A5
				        ON A5."IdCliente" = A4."IdCliente"
				        AND A5."CodigoPortafolio" = TO_VARCHAR(A1."SEC_RUT_CLI")
				        LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS A6
				        ON A6."Nemotecnico" = A1."NEMO"
				        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS A7
				        ON A7."IdTipoInstrumento" = A6."IdTipoInstrumento" 
				        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS A9
				        ON 	A9."COD_PER" 	 	 = A1."COD_PER"
				        AND A9."RUT_CLI"	 	 = A1."RUT_CLI"
				        AND A9."SEC_RUT_CLI" 	 = A1."SEC_RUT_CLI"
				        AND A9."SEC_CAR_INVER"   = A1."SEC_CAR_INVER"      
				        WHERE A1."COD_PER" >= SUBSTRING(p_FechaInicial,1,7)
				        AND A1."COD_PER" <=  SUBSTRING(v_periodofinal,1,7)
				        AND A1."CAN_GARANTIA" > 0
				        AND A1."RUT_CLI" =  CASE WHEN v_RutCli ='        0-0' then  A1."RUT_CLI" ELSE v_RutCli END
				        AND A9."RUT_CLI" IS NULL;
				
		               						SIGNAL SALIDA_CONTROLADA;         
	END;
