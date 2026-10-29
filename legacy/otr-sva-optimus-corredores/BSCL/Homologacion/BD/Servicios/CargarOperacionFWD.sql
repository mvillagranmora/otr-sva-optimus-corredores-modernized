SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionFWD";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS FWD
--  renta variable
--  Autor: - EAR 


"BSCL.Homologacion.BD.Servicios::CargarOperacionFWD"(
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

	       delete from "BSCL"."BSCL.Homologacion.BD::HOM.Error"
	       where "IdCarga" = p_IdCarga 
	         and "Entidad" ='MESAFWD';
	
	       if p_Modalidad ='R' then 
	           delete from "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" 
	           where "IdCarga" = p_IdCarga
	           and "IdTipoProducto" = 'MEFW';
	       end if;
	       
	      -- contamos movimientos sebra 
                select count(*) into v_total_mov_gestion
                FROM "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE
					ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' then TRIM(MOVTOFORWARD."RutCliente") else trim(p_RutCliente) end
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
                  AND HOM_OPERACION."FOLIOOPERACION" IS NULL;	

	       insert into "BSCL"."BSCL.Homologacion.BD::HOM.Operacion"
				( 	 "IdOperacion","FechaOperacion","FolioOperacion","IdTipoProducto","IdTipoOperacion","IdOperacionRelacionada","IdInstrumento","IdMonedaPrincipal"
					,"IdMonedaSecundaria","IdModalidadLiquidacion","IdReferenciaMercado","IdPlaza","Intermesa","IdMesaOrigen","IdMesaDestino","IdCliente","IdPortafolio"
					,"IdClienteCorredor","IdPortafolioCorredor","MontoOperacion","MontoOperacionInicial","MontoOperacionFinal","IdTipoReajuste","Tasa","MontoOperacionMoneda"
					,"MontoOperacionInicialMoneda","MontoOperacionFinalMoneda","IdMonedaLiquidacion","TCCierre","ParidadCierre","TCCosto","PuntosTasasForward","PuntosForward"
					,"TCForward","TasasMonedaPrincipal","TasasMonedaSecundaria","MontoDiferido","MontoMargen","IdFixing","FechaFixing","HoraFixing","IdSpotAsociado"
					,"IdClasificacionCartera","AfectaCustodia","PlazoEnDias","FechaVencimiento","FechaLiquidacion","IdCondicionLiquidacion","FechaCondicionLiquidacion"
					,"IdAgente","TasaAgente","IdEstado","FechaCreacion","IdUsuarioCreacion","IdTipoCanalIngreso","Glosa","SecuenciaCompromisoMigracion","IdCarga"
					,"RUT_CLI","SEC_RUT","RUT_CORR","SEC_RUT_CORR","SEC_COMPROM","TIPO_COMPROM","COD_PER","FOLIOTRANSACCION","FOLIOOPERACION" 
				)
			    select 
			      "BSCL.Custodia.BD.Secuencias::Operacion".NEXTVAL as "IdOperacion" 
				, MOVTOFORWARD."FechaOperacion" AS "FechaOperacion" 
				, MOVTOFORWARD."FolioOperacion" AS "FolioOperacion" 
				, 'MEFW' AS "IdTipoProducto" 
				, TIPOOPERACION."ValorDestino" AS "IdTipoOperacion" 
				, 0 AS "IdOperacionRelacionada"
				, INSTRUMENTO."IdInstrumento" AS "IdInstrumento" 
				, MONEDA_PRIMARIA."IdMoneda" AS "IdMonedaPrincipal" 
				, MONEDA_SECUNDARIA."IdMoneda" AS "IdMonedaSecundaria" 			
				, MODALIDAD_CUMPLIMIENTO."ValorDestino"AS "IdModalidadLiquidacion" 
				, 0 AS "IdReferenciaMercado" 
				, 0 AS "IdPlaza" -- hay que homologar las plazas
				,'' AS "Intermesa"
				, 1 AS "IdMesaOrigen"
				, 0 AS "IdMesaDestino"  
				, CLIENTE."IdCliente" AS "IdCliente" 
				, PORTAFOLIO."IdPortafolio" AS "IdPortafolio" 
				, 0 AS "IdClienteCorredor" 
				, 0 AS "IdPortafolioCorredor" 		
				, MOVTOFORWARD."MtoMonPrinc" AS "MontoOperacion" -- monto moneda principal 
				, 0 AS "MontoOperacionInicial" 
				, COALESCE(MOVTOFORWARD."MtoMonSecu",0) AS "MontoOperacionFinal" --monto en moneda secundaria
				, '' AS "IdTipoReajuste" 
				, 0 AS "Tasa"
				, 0 AS "MontoOperacionMoneda"
				, 0 AS "MontoOperacionInicialMoneda"
				, 0 AS "MontoOperacionFinalMoneda"
				, '' AS "IdMonedaLiquidacion"
				, MOVTOFORWARD."TCCierreForward" AS "TCCierre" 
				, 0 AS "ParidadCierre" 
				, MOVTOFORWARD."TCReferencia" AS "TCCosto" 
				, 0 AS "PuntosTasasForward" 			
				, 0 AS "PuntosForward" 
				, MOVTOFORWARD."TCReferencia" AS "TCForward" 
				, 0 AS "TasasMonedaPrincipal" 
				, 0 AS "TasasMonedaSecundaria" 
				, 0 AS "MontoDiferido"-- revisar 
				, 0 AS "MontoMargen" 
				, FIXING."ValorDestino" AS "IdFixing" -- homologar fixing
				, MOVTOFORWARD."FechaFixing" AS "FechaFixing" 			
				, '00:00' AS "HoraFixing" 
				, 0 AS "IdSpotAsociado" 
				, 0 AS "IdClasificacionCartera" 
				, '' AS "AfectaCustodia" 
				, MOVTOFORWARD."Plazo" AS "PlazoEnDias" 
				, MOVTOFORWARD."FechaVencimiento" AS "FechaVencimiento" 
				, '0001-01-01' AS "FechaLiquidacion" 
				, '' AS "IdCondicionLiquidacion" 		
				, MOVTOFORWARD."FecEfectiva" AS "FechaCondicionLiquidacion" 
				, 0 AS "IdAgente" 
				, 0 AS "TasaAgente" 
				, ESTADOFORWARD."ValorDestino" AS "IdEstado" 
				, MOVTOFORWARD."FechaCreacion" AS "FechaCreacion" 
				, 0 AS "IdUsuarioCreacion" 
				, '' AS "IdTipoCanalIngreso" --debo revisar el canal de ingreso de las operaciones 
				, '' AS "Glosa"--problema con el cruce con la tabla momvto			
				, MOVTOFORWARD."FolioOperacion" AS "SecuenciaCompromisoMigracion"
				, p_IdCarga AS "IdCarga"
				, MOVTOFORWARD."RutCliente" AS "RUT_CLI"
				, MOVTOFORWARD."CuentaCliente" AS "SEC_RUT"
				, 0 AS "RUT_CORR"
				, 0 AS "SEC_RUT_CORR"
				, 0 AS "SEC_COMPROM"
				, '' AS "TIPO_COMPROM"
				, '' AS "COD_PER"
				, MOVTOFORWARD."FolioTransaccion" AS "FOLIOTRANSACCION"
				, MOVTOFORWARD."FolioOperacion" AS "FOLIOOPERACION"
				FROM "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_SECUNDARIA
					ON MONEDA_SECUNDARIA."CodigoNumerico" = MOVTOFORWARD."CodMonSecu"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPOOPERACION
					ON TIPOOPERACION."TipoValor" =  'TipoOperacionFWD'
					AND TIPOOPERACION."ValorOrigen" = MOVTOFORWARD."CodTipoMovto"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS ESTADO_FORWARD
					ON ESTADO_FORWARD."TipoValor" = 'EstadoFWD'
					AND ESTADO_FORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MODALIDAD_CUMPLIMIENTO
					ON MODALIDAD_CUMPLIMIENTO."TipoValor" = 'TipoCumplimientoLiquidacionFWD'
					AND MODALIDAD_CUMPLIMIENTO."ValorOrigen" = MOVTOFORWARD."ModCumplimiento"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE
					ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' then TRIM(MOVTOFORWARD."RutCliente") else trim(p_RutCliente) end
                INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
                  	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TRIM(MOVTOFORWARD."CuentaCliente")||''
                LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
                  AND HOM_OPERACION."FOLIOOPERACION" IS NULL;
                  

	           -- contamos las operaciones cargadas
			   SELECT COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Homologacion.BD::HOM.Operacion"
			   WHERE "IdCarga" = p_IdCarga
			    and "IdTipoProducto" = 'MEFW';

		   	  ------------------------------
		   	  --cargamos el log de errores--
           	  ------------------------------
		   
		   	-- debe existir el registro de cartera 
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    ,'los movimientos FWD no existe en cartera FWD SEBRA' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioCartera' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioCartera" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				LEFT JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND CARTERAFORWARD."FolioCartera" IS NULL;
				  
			  -- el estado de la operacion forward debe estar homologado
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    ,'El estado sebra no esta homologado' as "Mensaje"
                    , 'Entidad' as "NombreCampoGenerico1"
                    , 'EstadoFWD' as "ValorCampoGenerico1" 
                    , 'Estado' as "NombreCampoGenerico2" 
                    , MOVTOFORWARD."Estado" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND ESTADOFORWARD."Entidad" IS NULL; 
				  
			  -- el fixing debe estar homologado
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    ,'el tipo fixing no esta homologado' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'TipoFixing' as "ValorCampoGenerico1" 
                    , 'CodFixing' as "NombreCampoGenerico2" 
                    ,MOVTOFORWARD."CodFixing" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND FIXING."TipoValor" IS NULL;   
				
				
				--  el nemo forward debe estar homologado
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    ,'el tipo fixing no esta homologado' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'NemoInstrumentoFWD' as "ValorCampoGenerico1" 
                    , 'CodProducto' as "NombreCampoGenerico2" 
                    , TO_VARCHAR(MOVTOFORWARD."CodProducto") as "ValorCampoGenerico2"
                    , 'CodSubProducto' as "NombreCampoGenerico3"
                    , TO_VARCHAR(MOVTOFORWARD."CodSubProducto") as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND "NEMO_INSTRUMENTO_FORWARD"."TipoValor" IS NULL;
				
			  --  el instrumento forward debe existir
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    ,'El idinstrumento asociado al nemotecnico no existe' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'NemoInstrumentoFWD' as "ValorCampoGenerico1" 
                    , 'Nemotecnico' as "NombreCampoGenerico2" 
                    , NEMO_INSTRUMENTO_FORWARD."ValorDestino" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND INSTRUMENTO."Nemotecnico" IS NULL;
			    
			    -- la moneda primaria debe existir
                 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    , 'moneda primaria no existe en tabla moneda en optimus' as "Mensaje"
                    , 'CodMonPrinc' as "NombreCampoGenerico1"
                    ,  TO_VARCHAR(MOVTOFORWARD."CodMonPrinc") as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND MONEDA_PRIMARIA."CodigoNumerico" IS NULL; 

              -- la moneda secundaria debe existir
                 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    , 'moneda secundaria no existe en tabla moneda en optimus' as "Mensaje"
                    , 'CodMonSecu' as "NombreCampoGenerico1"
                    ,  TO_VARCHAR(MOVTOFORWARD."CodMonSecu") as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_SECUNDARIA
					ON MONEDA_SECUNDARIA."CodigoNumerico" = MOVTOFORWARD."CodMonSecu"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND MONEDA_SECUNDARIA."CodigoNumerico" IS NULL; 	
				  
				--- EL TIPO OPERACIUON DEBE EXISTIR 
                 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    , 'el tipo operacion no homologado en optimus' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'TipoOperacionFWD' as "ValorCampoGenerico1" 
                    , 'CodTipoMovto' as "NombreCampoGenerico2" 
                    , TO_VARCHAR(MOVTOFORWARD."CodTipoMovto") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_SECUNDARIA
					ON MONEDA_SECUNDARIA."CodigoNumerico" = MOVTOFORWARD."CodMonSecu"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPOOPERACION
					ON TIPOOPERACION."TipoValor" =  'TipoOperacionFWD'
					AND TIPOOPERACION."ValorOrigen" = MOVTOFORWARD."CodTipoMovto"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND TIPOOPERACION."ValorOrigen" IS NULL; 		

				-- la modalidad de cumplimiento debe existir
                  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    , 'la modalidad cumplimiento forward no esta homologada' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'TipoCumplimientoLiquidacionFWD' as "ValorCampoGenerico1" 
                    , 'ModCumplimiento' as "NombreCampoGenerico2" 
                    , TO_VARCHAR(MOVTOFORWARD."ModCumplimiento") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_SECUNDARIA
					ON MONEDA_SECUNDARIA."CodigoNumerico" = MOVTOFORWARD."CodMonSecu"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPOOPERACION
					ON TIPOOPERACION."TipoValor" =  'TipoOperacionFWD'
					AND TIPOOPERACION."ValorOrigen" = MOVTOFORWARD."CodTipoMovto"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS ESTADO_FORWARD
					ON ESTADO_FORWARD."TipoValor" = 'EstadoFWD'
					AND ESTADO_FORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MODALIDAD_CUMPLIMIENTO
					ON MODALIDAD_CUMPLIMIENTO."TipoValor" = 'TipoCumplimientoLiquidacionFWD'
					AND MODALIDAD_CUMPLIMIENTO."ValorOrigen" = MOVTOFORWARD."ModCumplimiento"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND MODALIDAD_CUMPLIMIENTO."ValorOrigen" IS NULL;

			  -- el cliente debe existir
                  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    , 'el cliente no existe en optimus' as "Mensaje"
                    , MOVTOFORWARD."RutCliente" as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_SECUNDARIA
					ON MONEDA_SECUNDARIA."CodigoNumerico" = MOVTOFORWARD."CodMonSecu"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPOOPERACION
					ON TIPOOPERACION."TipoValor" =  'TipoOperacionFWD'
					AND TIPOOPERACION."ValorOrigen" = MOVTOFORWARD."CodTipoMovto"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS ESTADO_FORWARD
					ON ESTADO_FORWARD."TipoValor" = 'EstadoFWD'
					AND ESTADO_FORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MODALIDAD_CUMPLIMIENTO
					ON MODALIDAD_CUMPLIMIENTO."TipoValor" = 'TipoCumplimientoLiquidacionFWD'
					AND MODALIDAD_CUMPLIMIENTO."ValorOrigen" = MOVTOFORWARD."ModCumplimiento"
				LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE
					ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' then TRIM(MOVTOFORWARD."RutCliente") else trim(p_RutCliente) end
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND CLIENTE."DocumentoIdentidad" IS NULL;
				
				--el portafolio debe existir
                  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESAFWD' as "Entidad"
                    , 'el cliente no tiene portafolio optimus' as "Mensaje"
                    , 'RutCliente' as "NombreCampoGenerico1"
                    , MOVTOFORWARD."RutCliente" as "ValorCampoGenerico1" 
                    , 'IdCliente' as "NombreCampoGenerico2" 
                    , TO_VARCHAR(CLIENTE."IdCliente") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FolioOperacion' as "NombreCampoGenerico6"
                    , MOVTOFORWARD."FolioOperacion" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
                FROM  "BSCL_VIRTUAL"."GES_FMMOVTOSFORWARD" as MOVTOFORWARD 
				INNER JOIN "BSCL_VIRTUAL"."GES_FMCARTERAFORWARD" as CARTERAFORWARD
					ON CARTERAFORWARD."FolioCartera" = MOVTOFORWARD."FolioCartera"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as ESTADOFORWARD
					ON ESTADOFORWARD."Entidad" = 'EstadoFWD'	
					AND ESTADOFORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as FIXING
					ON FIXING."TipoValor" = 'TipoFixing'	
					AND FIXING."ValorOrigen" = MOVTOFORWARD."CodFixing"	
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS NEMO_INSTRUMENTO_FORWARD		
					ON  "NEMO_INSTRUMENTO_FORWARD"."TipoValor" = 'NemoInstrumentoFWD'
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen" = MOVTOFORWARD."CodProducto"
					AND "NEMO_INSTRUMENTO_FORWARD"."ValorOrigen2" = MOVTOFORWARD."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
					ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_FORWARD"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_PRIMARIA
					ON MONEDA_PRIMARIA."CodigoNumerico" = MOVTOFORWARD."CodMonPrinc"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Moneda"  AS MONEDA_SECUNDARIA
					ON MONEDA_SECUNDARIA."CodigoNumerico" = MOVTOFORWARD."CodMonSecu"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPOOPERACION
					ON TIPOOPERACION."TipoValor" =  'TipoOperacionFWD'
					AND TIPOOPERACION."ValorOrigen" = MOVTOFORWARD."CodTipoMovto"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS ESTADO_FORWARD
					ON ESTADO_FORWARD."TipoValor" = 'EstadoFWD'
					AND ESTADO_FORWARD."ValorOrigen" = MOVTOFORWARD."Estado"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MODALIDAD_CUMPLIMIENTO
					ON MODALIDAD_CUMPLIMIENTO."TipoValor" = 'TipoCumplimientoLiquidacionFWD'
					AND MODALIDAD_CUMPLIMIENTO."ValorOrigen" = MOVTOFORWARD."ModCumplimiento"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE
					ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' then TRIM(MOVTOFORWARD."RutCliente") else trim(p_RutCliente) end
				LEFT JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
                  	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					AND PORTAFOLIO."CodigoPortafolio" = TRIM(MOVTOFORWARD."CuentaCliente")||''
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
                	ON HOM_OPERACION."FOLIOTRANSACCION" = MOVTOFORWARD."FolioTransaccion"
                	AND HOM_OPERACION."FOLIOOPERACION" = MOVTOFORWARD."FolioOperacion"
                WHERE MOVTOFORWARD."FechaOperacion" >= p_FechaInicial
                  AND MOVTOFORWARD."FechaOperacion" <= p_FechaFinal
				  AND HOM_OPERACION."FOLIOOPERACION" IS NULL
				  AND PORTAFOLIO."CodigoPortafolio" IS NULL;
				

		   -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		   p_ResumenDatosProcesados = select 'CARGAOPERACIONFWD' as "CodigoProceso" 
	   									,'FAC' as "Modulo" 
	   		 							,v_total_mov_gestion as "TotalRegistrosSebra"
	   		  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   						    	  union all
	   						    	  select * from :p_ResumenDatosProcesados;
	   			  			    	
		------------------
		-- FIN SP    
		-------------------
	END;	
END;

 
