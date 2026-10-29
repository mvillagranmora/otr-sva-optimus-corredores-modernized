SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinanciero";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Consulta transacciones para Clearing Financiero de acuerdo a las camaras, segun agrupacion CCLV
--             Existe un WS que actualiza las transacciones(camara, indicador bilateral) desde negociacion(cloper) previo al clearing
--             Camara RV --> Solo RV y SIM que sean CN  / PH y PM son bilaterales
--             Camara SM --> Solo Simultaneas PH
--             Camara PH --> Solo RF, IF que sean PH
--             Camara PM --> Solo RF, IF que sean PM
--  Autor: BCS
--  Fecha Creacion: 15/02/2016
-- 
"BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinanciero"
  (
    IN p_IdEmpresa              BIGINT,
    IN p_IdUsuario              BIGINT,
    IN p_IdModulo               NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    IN  p_FechaLiquidacion	    NVARCHAR(10),
    IN  p_Camaras			    TABLE   ("Camara"   NVARCHAR(2)),  -- PM/RV/SM/PH
		
	OUT p_out_ClearingFinancieroResumen  TABLE (  -- Salida para Informe de Clearing(agrupado)
	                         "IdBolsa"    		        		NVARCHAR(3)
	                        ,"CodigoGrupoCamara"           		INTEGER
							,"NombreGrupoCamara"  				NVARCHAR(40)
							,"IdClasificacionTipoInstrumento"   NVARCHAR(3)   -- Mercado
							,"IdCorredorContraparte"            BIGINT
							,"RutContraparte" 					NVARCHAR(40)							
                            ,"RazonSocialContraparte"   		NVARCHAR(250)
                            ,"IdMonedaTransaccion"				NVARCHAR(3)
							,"IdComprobanteTesoreria"           BIGINT										
							,"TotalVenta"     					DECIMAL(26,8)
							,"TotalCompra"    					DECIMAL(26,8)
							,"SaldoAcreedor"  					DECIMAL(26,8)
							,"SaldoDeudor"    	        		DECIMAL(26,8)
							,"MontoRetencion"  					DECIMAL(26,8)
				),
							
				
	OUT p_out_ClearingFinancieroDetalle table (  -- Salida para procesamiento del clearing(detalle)
							 "IdTransaccion"                    BIGINT
							,"IdBolsa"    		        		NVARCHAR(3)
							,"CodigoGrupoCamara"           		INTEGER
							,"NombreGrupoCamara"  				NVARCHAR(40)
				            ,"RutContraparte" 					NVARCHAR(40)							
							,"AccionTipoOperacion"      		NVARCHAR(40)
							,"TotalVenta"     					DECIMAL(26,8)
							,"TotalCompra"    					DECIMAL(26,8)
							,"RazonSocialContraparte"   		NVARCHAR(250)
							,"IdClasificacionTipoInstrumento"   NVARCHAR(3)     -- Mercado
				  			,"FechaSistemaCreacion" 			NVARCHAR(10)
							,"FolioTransaccion"   				NVARCHAR(40)							
							,"IdCondicionDeLiquidacion" 		NVARCHAR(2)     -- T0/T1/T2							
							,"CodigoCondicionDeLiquidacion"     NVARCHAR(2)     -- PH/PM/CN
							,"IdMonedaTransaccion"				NVARCHAR(3)
							,"IdComprobanteTesoreria"           BIGINT
							,"Instrumento"						NVARCHAR(40)
						    ,"CantidadTransaccion" 				DECIMAL(26,8)
						    ,"PrecioTransaccion"				DECIMAL(26,8)
						    ,"Tasa"								DECIMAL(26,8)
						    ,"MontoRetencion"    				DECIMAL(26,8)	 -- En CHILE no se utiliza
			 ),

	
	OUT p_out_ClearingFinancieroDividendos 	TABLE (
					 "CodigoGrupoCamara"           		INTEGER
					,"NombreGrupoCamara"  				NVARCHAR(40)
					,"IdClasificacionTipoInstrumento"   NVARCHAR(3)
					,"IdEventoCapitalCliente"			BIGINT
					,"IdEventoCapital"					BIGINT
					,"IdTipoEventoCapital" 				NVARCHAR(2)
					,"TipoEvento" 						NVARCHAR(100)
					,"NumeroDividendo"					BIGINT
					,"Secuencia"						BIGINT
					,"IdTipoInstrumento"				NVARCHAR(3)   
					,"Nemo"								NVARCHAR(40)
					,"IdMoneda"							NVARCHAR(3)					 
					,"CantidadDerecho"					BIGINT
					,"Total"							DECIMAL(26,8)
					,"TotalVenta"						DECIMAL(26,8)
					,"TotalCompra"						DECIMAL(26,8)					
					,"IdBoveda"							NVARCHAR(3)
					,"IdComprobanteTesoreria" 			BIGINT
					,"IdBolsa"                			NVARCHAR(3)
					,"RutContraparte"         			NVARCHAR(40)			
					,"RazonSocialContraparte"   		NVARCHAR(250)					
					,"IdCorredorContraparte" 			BIGINT					
					,"TipoOperacion"         			NVARCHAR(1) -- C/V
	),		
			 
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS	
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
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
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN

		-- Declaración de Variables
		DECLARE v_ParametroClearingIncluyeRFPM   NVARCHAR(1);
		DECLARE v_ParametroClearingIncluyeIFPM   NVARCHAR(1);
		DECLARE v_ParametroClearingIncluyeRFPH   NVARCHAR(1);
		DECLARE v_ParametroClearingIncluyeIFPH   NVARCHAR(1);			
		DECLARE v_ParametroCambiosCCLV           NVARCHAR(1);		
		DECLARE v_NombreGrupoCamara        		 NVARCHAR(40);		
		DECLARE v_CodigoGrupoCamara              INTEGER;
		DECLARE v_CodigoCorredor                 INTEGER;   -- código de este corredor 		
		DECLARE v_FechaProcesoRV 		         NVARCHAR(10);
		DECLARE v_FechaProcesoPM 		         NVARCHAR(10);		
		DECLARE v_FechaProcesoT3 		         NVARCHAR(10);		
		DECLARE v_IncluirCamaraRV                NVARCHAR(1);
		DECLARE v_IncluirCamaraPM                NVARCHAR(1);
		DECLARE v_IncluirCamaraPH                NVARCHAR(1);
		DECLARE v_IncluirCamaraSM                NVARCHAR(1);

		

		-------------------------------------------------------
		-- Define Tabla para Transacciones(Palos) HANA
		-------------------------------------------------------
		DECLARE v_Temp_Transacciones table
		(
			 "IdTransaccion"                    BIGINT
			,"IdClasificacionTipoInstrumento"   NVARCHAR(3)
			,"IdTipoInstrumento" 	    		NVARCHAR(3)			 	
			,"IdBolsa"    		        		NVARCHAR(3)
			,"CodigoGrupoCamara"          		INTEGER
			,"NombreGrupoCamara"  				NVARCHAR(40)
			,"RutContraparte" 					NVARCHAR(40)
			,"CodigoOperadorContraparte"		NVARCHAR(40)
			,"IdCorredorContraparte"    		BIGINT
			,"AccionTipoOperacion"      		NVARCHAR(40)
			,"TotalVenta"     					DECIMAL(26,8)
			,"TotalCompra"    					DECIMAL(26,8)
			,"RazonSocialContraparte"   		NVARCHAR(250)				
			,"FolioTransaccion"   				NVARCHAR(40)			
			,"IdCondicionDeLiquidacion" 		NVARCHAR(2)     -- T0/T1/T2
			,"CodigoCondicionDeLiquidacion"     NVARCHAR(2)     -- PH/PM/CN
			,"FechaSistemaCreacion" 			NVARCHAR(10)			
			,"IdComprobanteTesoreria"           BIGINT			
			,"Instrumento"                      NVARCHAR(40)			
		    ,"CantidadTransaccion" 				DECIMAL(26,8)
		    ,"PrecioTransaccion"				DECIMAL(26,8)
		    ,"Tasa"								DECIMAL(26,8)
		    ,"IdMonedaTransaccion"				NVARCHAR(3)
		    ,"MontoRetencion" 					DECIMAL(26,8)-- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
		);


		-- Asigna estructura a variable tipo tabla
		v_Temp_Transacciones	= 
		SELECT 
			0  as "IdTransaccion",
			'' as "IdClasificacionTipoInstrumento",
			'' as "IdTipoInstrumento", 
			'' as "IdBolsa", 
			0  as "CodigoGrupoCamara",
			'' as "NombreGrupoCamara", 
			'' as "RutContraparte",
			'' as "CodigoOperadorContraparte", 
			0  as "IdCorredorContraparte", 
			'' as "AccionTipoOperacion",
			0  as "TotalVenta", 
			0  as "TotalCompra", 
			'' as "RazonSocialContraparte", 
			'' as "FolioTransaccion",
			'' as "IdCondicionDeLiquidacion",
			'' as "CodigoCondicionDeLiquidacion",
			'' as "FechaSistemaCreacion",			
			0  as "IdComprobanteTesoreria",
			'' as "Instrumento",			
		    0  as "CantidadTransaccion",
		    0  as "PrecioTransaccion",
		    0  as "Tasa",
		    '' as "IdMonedaTransaccion",
		    0  as "MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
		FROM dummy where 1=2;

		

		-- Obtiene datos del Corredor
		v_DatosCorredor = 
		SELECT "CodigoContraparte"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
		WHERE "IdEmpresa" = :p_IdEmpresa;
		--SELECT 'DatosCorredor:', * FROM :v_DatosCorredor;   -- REVISAR

		IF IS_EMPTY(:v_DatosCorredor) THEN
			SELECT COALESCE("CodigoContraparte",0) INTO v_CodigoCorredor FROM :v_DatosCorredor;

			IF (length(:v_CodigoCorredor) = 0 or (:v_CodigoCorredor <= 0)) THEN
				  v_hash = select 'CodigoParametros' as "Codigo", '' as "Valor" FROM DUMMY;
				  CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CODIGO_CORREDOR_NO_EXISTE',:v_hash,:v_erroroutmsg);
	
				  p_out_ErroresDetalle =  SELECT 'CODIGO_CORREDOR_NO_EXISTE' As "Codigo",:v_erroroutmsg As "Descripcion" FROM DUMMY
										  UNION ALL select * from :p_out_ErroresDetalle;
	
				  SIGNAL SALIDA_CONTROLADA; 
			END IF;			
		END IF;
		

		
		-- Consulta Parametros de configuracion		
		SELECT	
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYERFPM' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeRFPM",  -- indica si incluye "RF" en camara "PM"
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYEIFPM' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeIFPM",  -- indica si incluye "IF" en camara "PM"
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYERFPH' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeRFPH",  -- indica si incluye "RF" en camara "PH"
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYEIFPH' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeIFPH",  -- indica si incluye "IF" en camara "PH"
			MAX(CASE WHEN "CodigoParametro" = 'CAMBIOSCCLV'         THEN "Valor" ELSE null END) AS "ParametroCambiosCCLV"         
        INTO v_ParametroClearingIncluyeRFPM, v_ParametroClearingIncluyeIFPM, v_ParametroClearingIncluyeRFPH, v_ParametroClearingIncluyeIFPH, v_ParametroCambiosCCLV
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria"
		WHERE "CodigoParametro" IN ('CLEARINGINCLUYERFPM','CLEARINGINCLUYEIFPM','CLEARINGINCLUYERFPH','CLEARINGINCLUYEIFPH','CAMBIOSCCLV')
		AND   "IdEmpresa" = :p_IdEmpresa;		
		--SELECT 'Parametros:', :v_ParametroClearingIncluyeRFPM, :v_ParametroClearingIncluyeIFPM, :v_ParametroClearingIncluyeRFPH, :v_ParametroClearingIncluyeIFPH FROM DUMMY;  -- REVISAR


		-- Deglosa las camaras a incluir en el informe
		SELECT 
			MAX(CASE WHEN "Camara" = 'RV' THEN 'S' ELSE 'N' END) AS "IncluirCamaraRV",
			MAX(CASE WHEN "Camara" = 'PM' THEN 'S' ELSE 'N' END) AS "IncluirCamaraPM",
			MAX(CASE WHEN "Camara" = 'PH' THEN 'S' ELSE 'N' END) AS "IncluirCamaraPH",
			MAX(CASE WHEN "Camara" = 'SM' THEN 'S' ELSE 'N' END) AS "IncluirCamaraSM"
		INTO v_IncluirCamaraRV, v_IncluirCamaraPM, v_IncluirCamaraPH, v_IncluirCamaraSM
		FROM :p_Camaras;
		--SELECT 'Camaras:', :v_IncluirCamaraRV, :v_IncluirCamaraPM, :v_IncluirCamaraPH, :v_IncluirCamaraSM  FROM DUMMY;   -- REVISAR


		-------------------------------------------------------------------------------------------------
		-- Obtiene transacciones de todos los mercados (mas a delante los filtra segun camara)
		-------------------------------------------------------------------------------------------------
		v_Transacciones = 		
		SELECT      
			 TRA."IdTransaccion"
			,TRA."IdClasificacionTipoInstrumento"   -- RV, RF, IF, DV, FM
			,TRA."FechaSistemaCreacion"	          -- fecha de sistema del ingreso del palo(esta fecha es igual a la FechaOperacion pero sin la hora) 
		    ,TRA."IdTipoInstrumento"                -- ACN /ACX /CFI / CFM / SIM    
			,TRA."FolioTransaccion" 			      
			,TRA."IndicadorOrdenDirecta" 		     -- S/N
		    ,TRA."IdBolsa"
			,TRA."MontoTransaccion"				  
		    ,TRA."IdTipoOperacion"        		  -- COMACN  / VENACX
		    ,TRA."AccionTipoOperacion"    		  -- COMPRA / VENTA 
		    ,TRA."IdCondicionDeLiquidacion"    	  -- T0/T1/T2
		    ,TRA."CodigoCondicionDeLiquidacion"     -- PH/PM/CN
			,TRA."InstrumentoUI"  AS "Instrumento"
			,TRA."IdCorredorContraparte"
			,CNT."DocumentoIdentidad"         AS "RutContraparte"
			,CNT."NombreCorto"             AS "RazonSocialContraparte"
			,CNT."CodigoInscripcionRegulador" AS "CodigoCorredorContraparte"			
			,TRA."CodigoOperadorContraparte"
			,TRA."CodigoOperadorCorredor"	   AS "CodigoOperadorCorredora"			
			,TRA."FechaLiquidacion"
			,TRA."FechaVencimiento"  -- Para Simultaneas
			,TRA."EsAPlazo"
			,TRA."EsBilateral"     -- Se carga desde TBCLOPER con WS de actualizacion de transacciones
		    ,TRA."IdCamara"        -- Se carga desde TBCLOPER con WS de actualizacion de transacciones
			,TRA."IdComprobanteTesoreria"    	-- Si existe este valor, ya se proceso la liquidacion
		    ,TRA."CantidadTransaccion"
		    ,TRA."PrecioTransaccion"
		    ,TRA."Tasa"
		    ,TRA."IdMonedaTransaccion"
			,TRA."MontoRetencion" -- PERU: Monto retencion cavali(informado por archivo IGK)
			
		FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA -- aca no importa que no esten facturados
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS "CNT" ON ("CNT"."IdCorredora" = "TRA"."IdCorredorContraparte")
		
		WHERE TRA."EsBilateral" 		= 'N'      -- No sea bilateral
		AND (TRA."FechaLiquidacion" 	= :p_FechaLiquidacion   OR  TRA."FechaVencimiento" = :p_FechaLiquidacion)  -- Vencimiento para reportes(simultáneas)	         
		AND TRA."IdEstadoTransaccion"  <> 'ANU'    -- No considerar anulados
		AND TRA."BloqueoLiquidacion" 	= 'N' 	   -- Que no este bloqueada para liquidacion
		AND TRA."IndicadorOrdenDirecta" = ''       -- Que no sea OD, OE(OD con operador externo)
		AND TRA."IdMonedaTransaccion"   = 'CLP'    -- Solo se compensa CLP					
		AND TRA."IdEmpresa" 			= :p_IdEmpresa
		AND TRA."IdCamara"              <> ''
		AND(    -- Esta logica debe ser la misma en:  CrearTransaccion.java / ModificarTransaccion.js / ConsultarClearingFinanciero.SQL / ModificarTransaccionMarketData.java
				 (TRA."IdClasificacionTipoInstrumento"='RV' AND TRA."IdCondicionDeLiquidacion"='T2')                                                -- Toda operacion RV, CN (Incluye simultaneas)) --> Camara RV
			OR 	((TRA."IdClasificacionTipoInstrumento"='RF' OR TRA."IdClasificacionTipoInstrumento"='IF') AND TRA."IdCondicionDeLiquidacion"='T1')  -- Toda operacion RF o IF  T1 --> Camara PM
			OR  ((TRA."IdClasificacionTipoInstrumento"='RF' OR TRA."IdClasificacionTipoInstrumento"='IF') AND TRA."IdCondicionDeLiquidacion"='T0')  -- Toda operacion RF o IF  T0 --> Camara PH
			OR  ((TRA."IdClasificacionTipoInstrumento"='RF' OR TRA."IdClasificacionTipoInstrumento"='IF') AND TRA."IdCondicionDeLiquidacion"='T2')  -- NUEVO: Toda operacion RF o IF  T2
			OR   (TRA."IdClasificacionTipoInstrumento"='RV' AND TRA."IdTipoInstrumento" = 'SIM' AND TRA."IdCondicionDeLiquidacion"='T0')            -- Toda operacion de SIMULTANEA, T0 --> Camara SM
		);
		--SELECT 'v_Transacciones', * FROM :v_Transacciones;  -- REVISAR


		-------------------------------------------------
		-- CARGA DATOS RV(CN) PARA CAMARA RV
		-------------------------------------------------

		IF (:v_IncluirCamaraRV = 'S') THEN

			IF (:v_ParametroCambiosCCLV = 'S') THEN
				v_NombreGrupoCamara := 'RV';
				v_CodigoGrupoCamara := 3;
			ELSE
				v_NombreGrupoCamara := 'RV + PM';
				v_CodigoGrupoCamara := 1;
			END IF;


			-- Carga Transacciones CN a Camara RV
			v_Temp_Transacciones = 
			  SELECT * FROM :v_Temp_Transacciones  UNION ALL
			  SELECT
			  			 TRA."IdTransaccion"
					    ,TRA."IdClasificacionTipoInstrumento"
					 	,TRA."IdTipoInstrumento"
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"
						,TRA."CodigoOperadorContraparte"
						,TRA."IdCorredorContraparte"
						,TRA."AccionTipoOperacion"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"							
						,TRA."IdCondicionDeLiquidacion"      -- T2: Contado Normal(CN)
   				        ,TRA."CodigoCondicionDeLiquidacion"
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retencion cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'RV'
					AND   TRA."IdCondicionDeLiquidacion" = 'T2';  	-- T2: Contado Normal(CN)
					
					

					
					-- Carga Dividendos en transito a Camara RV
					-- Los dividendos en transito de corredores contrapartes(solo CLP) afectan al clearing financiero (afectan el monto final a Pagar o Cobrar del clearing)
					v_Temp_Dividendos = 
						SELECT
							 :v_CodigoGrupoCamara AS "CodigoGrupoCamara"
							,:v_NombreGrupoCamara AS "NombreGrupoCamara"						
							,'RV' AS "IdClasificacionTipoInstrumento" -- dividendos deben quedar con camara y mercado RV
							,EVC."IdEventoCapitalCliente"
							,EVC."IdEventoCapital"
							,EVC."IdTipoEventoCapital"
							,EVC."TipoEvento"
							,EVC."NumeroDividendo"
							,EVC."Secuencia"
							,EVC."IdTipoInstrumento"   
							,EVC."Nemo"
							,EVC."IdMoneda"
							,COR."NombreCorto" AS "RazonSocialContraparte"
							,EVC."CantidadDerecho"
							,EVC."Total"

							
							
							,(CASE WHEN EVC."TipoCartera" = 'TRV' THEN ABS(EVC."Total") ELSE 0 END) AS "TotalVenta"    -- lo deja positivo ya que en tesorería siempre es positivo 
							,(CASE WHEN EVC."TipoCartera" = 'TRC' THEN ABS(EVC."Total") ELSE 0 END) AS "TotalCompra"
							
							
							,EVC."IdBoveda"							
							,EVC."IdComprobanteTesoreria"
							,EVC."IdBolsa"
							
							
							,COR."DocumentoIdentidad" AS "RutContraparte"
							
							
							,EVC."IdCorredorContraparte"
							,EVC."TipoOperacion"   -- C/V
							,EVC."MontoRetencion"
						FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente"  AS EVC
						
					
						LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS COR ON (COR."IdCorredora" = EVC."IdCorredorContraparte")
						
						
						WHERE  EVC."AfectoaClearing"   = 'S'
						AND    EVC."ProcesadoClearing" = 'N'
						AND    EVC."FechaPago"         = :p_FechaLiquidacion;

		END IF;	-- IF (:v_IncluirCamaraRV = 'S')



	


		-------------------------------------------------
		-- CARGA DATOS SIMULTANEA A CAMARA SM
		-------------------------------------------------
		IF (:v_IncluirCamaraSM = 'S') THEN
		
			v_NombreGrupoCamara := 'PH + SM';
			v_CodigoGrupoCamara := 2;
						
			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL
				SELECT
						 TRA."IdTransaccion"
						,'SIM' AS "IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"						
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,"TRA"."RutContraparte"						
						,TRA."CodigoOperadorContraparte"
						,TRA."IdCorredorContraparte"
						,TRA."AccionTipoOperacion"
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"			
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"
						,TRA."IdCondicionDeLiquidacion"      -- T0: Contado Normal(CN)		
						,TRA."CodigoCondicionDeLiquidacion"
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"		
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retencion cavali(informado por archivo IGK)
					FROM :v_Transacciones AS  TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'RV'					
					AND   TRA."IdCamara" = 'SM'
					AND   TRA."IdCondicionDeLiquidacion" = 'T0'  -- T0: Pago Hoy (PH)
					AND   TRA."IdTipoInstrumento" = 'SIM'
					AND   TRA."AccionTipoOperacion" IN ('COMPRA','VENTA');

					
		END IF;	
			



		---------------------------------------------------
		-- INCLUIR "RF" EN CAMARA "PM" (segun parametro)
		---------------------------------------------------
		IF (:v_ParametroClearingIncluyeRFPM = 'S' and :v_IncluirCamaraPM = 'S') THEN
		
			IF (:v_ParametroCambiosCCLV = 'S') THEN
				v_NombreGrupoCamara := 'PM';
				v_CodigoGrupoCamara := 1;
			ELSE
				v_NombreGrupoCamara := 'RV + PM';
				v_CodigoGrupoCamara := 1;
			END IF;

			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL
				SELECT
						 TRA."IdTransaccion"
						,TRA."IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"						
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"
						,TRA."CodigoOperadorContraparte"
						,TRA."IdCorredorContraparte"
						,TRA."AccionTipoOperacion"
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"
						,TRA."IdCondicionDeLiquidacion"
						,TRA."CodigoCondicionDeLiquidacion"															
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"			
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'RF'
					AND   TRA."FechaLiquidacion" = :p_FechaLiquidacion
					AND   TRA."IdCondicionDeLiquidacion" = 'T1';  -- T1: Pago Manana (PM)
					
		END IF;



		----------------------------------------------------
		-- INCLUIR "IF" EN CAMARA "PM"  (segun parametro)
		----------------------------------------------------
		IF (v_ParametroClearingIncluyeIFPM = 'S' and :v_IncluirCamaraPM = 'S') THEN

			IF (:v_ParametroCambiosCCLV = 'S') THEN
				v_NombreGrupoCamara := 'PM';
				v_CodigoGrupoCamara := 1;
			ELSE
				v_NombreGrupoCamara := 'RV + PM';
				v_CodigoGrupoCamara := 1;
			END IF;
			
			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL
				SELECT
						 TRA."IdTransaccion"
						,TRA."IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"
						,TRA."CodigoOperadorContraparte"							
						,TRA."IdCorredorContraparte"		
						,TRA."AccionTipoOperacion"
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"			
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"
						,TRA."IdCondicionDeLiquidacion"
						,TRA."CodigoCondicionDeLiquidacion"
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retencion cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'IF'					
					AND   TRA."IdCondicionDeLiquidacion" = 'T1';  -- T1: Pago Manana (PM)
					
					
		END IF;
		
		---------------------------------------------------
		-- INCLUIR "RF" EN CAMARA "PH" PARA OPERACIONES CN
		---------------------------------------------------
		IF (v_ParametroClearingIncluyeRFPH = 'S' and :v_IncluirCamaraPH = 'S') THEN -- @WX_INC_RFPH (en SP antiguo usa este parámetro para cargar "RF"y"IF" en camara "PH")

			v_NombreGrupoCamara := 'HS';
			v_CodigoGrupoCamara := 5;
						 				
			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL          -- Agrega registros a la tabla final
				SELECT
						 TRA."IdTransaccion"
						,TRA."IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"						
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"						
						,TRA."CodigoOperadorContraparte"
						,TRA."IdCorredorContraparte"			
						,TRA."AccionTipoOperacion"			
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"			
						,TRA."IdCondicionDeLiquidacion"
						,TRA."CodigoCondicionDeLiquidacion"						
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"			
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'RF'					
					AND   TRA."IdCondicionDeLiquidacion" = 'T2';  -- T2: Pago en dos días (CN)
		END IF;
		
		
		---------------------------------------------------
		-- INCLUIR "IF" EN CAMARA "PH" PARA OPERACIONES CN
		---------------------------------------------------
		IF (v_ParametroClearingIncluyeRFPH = 'S' and :v_IncluirCamaraPH = 'S') THEN -- @WX_INC_RFPH (en SP antiguo usa este parámetro para cargar "RF"y"IF" en camara "PH")

			v_NombreGrupoCamara := 'HS';
			v_CodigoGrupoCamara := 5;
						 				
			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL          -- Agrega registros a la tabla final
				SELECT
						 TRA."IdTransaccion"
						,TRA."IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"						
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"						
						,TRA."CodigoOperadorContraparte"
						,TRA."IdCorredorContraparte"			
						,TRA."AccionTipoOperacion"			
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"			
						,TRA."IdCondicionDeLiquidacion"
						,TRA."CodigoCondicionDeLiquidacion"						
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"			
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'IF'					
					AND   TRA."IdCondicionDeLiquidacion" = 'T2';  -- T2: Pago en dos días (CN)
		END IF;

		----------------------------------------
		-- INCLUIR "RF" EN CAMARA "PH"
		----------------------------------------
		IF (v_ParametroClearingIncluyeRFPH = 'S' and :v_IncluirCamaraPH = 'S') THEN -- @WX_INC_RFPH (en SP antiguo usa este parámetro para cargar "RF"y"IF" en camara "PH")

			v_NombreGrupoCamara := 'PH + SM';
			v_CodigoGrupoCamara := 2;
						 				
			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL          -- Agrega registros a la tabla final
				SELECT
						 TRA."IdTransaccion"
						,TRA."IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"						
						,TRA."IdBolsa"
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"						
						,TRA."CodigoOperadorContraparte"
						,TRA."IdCorredorContraparte"			
						,TRA."AccionTipoOperacion"			
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"			
						,TRA."IdCondicionDeLiquidacion"
						,TRA."CodigoCondicionDeLiquidacion"						
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"			
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'RF'					
					AND   TRA."IdCondicionDeLiquidacion" = 'T0';  -- T0: Pago Hoy (PH)
		END IF;
		
		
		----------------------------------------
		-- INCLUIR "IF" EN CAMARA "PH"
		----------------------------------------
		IF (v_ParametroClearingIncluyeIFPH = 'S' and :v_IncluirCamaraPH = 'S') THEN -- @WX_INC_RFPH (en SP antiguo usa este parámetro para cargar "RF"y"IF" en camara "PH")

			v_NombreGrupoCamara := 'PH + SM';
			v_CodigoGrupoCamara := 2;

			v_Temp_Transacciones = 
			    SELECT * FROM :v_Temp_Transacciones    UNION ALL
				SELECT
						 TRA."IdTransaccion"
						,TRA."IdClasificacionTipoInstrumento"
						,TRA."IdTipoInstrumento"						
						,TRA."IdBolsa"			
						,:v_CodigoGrupoCamara AS "CodigoGrupoCamara"
						,:v_NombreGrupoCamara AS "NombreGrupoCamara"
						,TRA."RutContraparte"
						,TRA."CodigoOperadorContraparte"
						--,"TRA"."CodigoCorredorContraparte"	
						,TRA."IdCorredorContraparte"		
						,TRA."AccionTipoOperacion"
						,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalVenta"
						,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "TotalCompra"
						,TRA."RazonSocialContraparte"
						,TRA."FolioTransaccion"			
						,TRA."IdCondicionDeLiquidacion"
						,TRA."CodigoCondicionDeLiquidacion"															
						,TRA."FechaSistemaCreacion"
						,TRA."IdComprobanteTesoreria"
						,TRA."Instrumento"						
					    ,TRA."CantidadTransaccion"
					    ,TRA."PrecioTransaccion"
					    ,TRA."Tasa"
					    ,TRA."IdMonedaTransaccion"
					    ,TRA."MontoRetencion" -- PERU: Monto retencion cavali(informado por archivo IGK)
					FROM :v_Transacciones AS TRA
					WHERE TRA."IdClasificacionTipoInstrumento" = 'IF'
					AND   TRA."IdCondicionDeLiquidacion" = 'T0';  -- T0: Pago Hoy (PH)
					
		END IF;		
		--SELECT 'Temp_Final', * FROM :v_Temp_Transacciones;  -- REVISAR


		
		----------------------------------------------------------
		-- GENERA SALIDA CLEARING FINANCIERO RESUMEN (Agrupado)
		----------------------------------------------------------
		
		v_AgrupacionCamaraContraparte =
		SELECT DISTINCT "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"	
				   ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria", "IdMonedaTransaccion"
		FROM
		(
			SELECT "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"	
				   ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria", "IdMonedaTransaccion","MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
			FROM :v_Temp_Transacciones
			
			UNION ALL
			
			SELECT "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"			
				  ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria", "IdMoneda" AS "IdMonedaTransaccion","MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)		 
			FROM :v_Temp_Dividendos
		);
		--SELECT 'v_AgrupacionCamaraContraparte', * FROM :v_AgrupacionCamaraContraparte;
		
		v_ResumenTransacciones =		
			SELECT  "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"	
				    ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria", "IdMonedaTransaccion" 
				   	,SUM("TotalVenta") AS "TotalVenta"
					,SUM("TotalCompra") AS "TotalCompra"					
					,(CASE 
						 WHEN (SUM("TotalVenta") > SUM("TotalCompra")) THEN (SUM("TotalVenta") - SUM("TotalCompra"))
						 ELSE 0				
					  END) AS "SaldoAcreedor"
					,(CASE 
						 WHEN (SUM("TotalVenta") <= SUM("TotalCompra")) THEN (SUM("TotalCompra") - SUM("TotalVenta"))
						 ELSE 0 
					  END) AS "SaldoDeudor"
					,SUM("MontoRetencion") AS "MontoRetencion"       -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
			FROM :v_Temp_Transacciones			
			GROUP BY "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"	
				   ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria","IdMonedaTransaccion";
		--SELECT 'v_ResumenTransacciones', * FROM :v_ResumenTransacciones;

	
		v_ResumenDividendos =	
			SELECT "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"			
				  ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria","IdMoneda"
				  					
					-- Para los dividendos se invierte el SaldoDeudor y SaldoAcreedor
					-- Las Compras que afectan a dividendos suman en el clearing y las ventas en tránsito restan en el clearing. 
					-- Al comprar se compra un derecho al dividendo que te lo paga el CCLV, Si vendes las accion se pierde el derecho y le debes pagar al CCLV
					
					--           Transaccion        |  Dividendo
					--------------------------------------------------------------------
					-- Compras > Deudor (Egreso)    |  Acreedor(Ingreso)
					-- Ventas  > Acreedor(Ingreso)	|  Deudor  (Egreso)		
					
				  	,SUM("TotalCompra") AS "TotalVenta"
					,SUM("TotalVenta") AS "TotalCompra"	
					
					,(CASE 
						 WHEN (SUM("TotalVenta") <= SUM("TotalCompra")) THEN (SUM("TotalCompra") - SUM("TotalVenta"))
						 ELSE 0 
					  END) AS "SaldoAcreedor"
					  
					,(CASE 
						 WHEN (SUM("TotalVenta") > SUM("TotalCompra")) THEN (SUM("TotalVenta") - SUM("TotalCompra"))
						 ELSE 0				
					  END) AS "SaldoDeudor"
					
					  
					,SUM("MontoRetencion") AS "MontoRetencion"       -- PERU: Monto retenciÃƒÂ³n cavali(informado por archivo IGK)
			FROM :v_Temp_Dividendos
			GROUP BY "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento"	
				   ,"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdComprobanteTesoreria","IdMoneda";
		--SELECT 'v_ResumenDividendos', * FROM :v_ResumenDividendos;	
			
		
				   
		
	    p_out_ClearingFinancieroResumen =
		SELECT
			 GRU."IdBolsa"
			,GRU."CodigoGrupoCamara"
			,GRU."NombreGrupoCamara"
			,GRU."IdClasificacionTipoInstrumento"
			,GRU."IdCorredorContraparte"
			,GRU."RutContraparte" 	
			,GRU."RazonSocialContraparte"
			,GRU."IdMonedaTransaccion"
			,GRU."IdComprobanteTesoreria"   -- Si Id existe, es porque fue procesado por clearing
			,(COALESCE(TRA."TotalVenta",0)    + COALESCE(DIV."TotalVenta",0))    AS "TotalVenta"
			,(COALESCE(TRA."TotalCompra",0)   + COALESCE(DIV."TotalCompra",0))   AS "TotalCompra"


			
			,(CASE 
				 WHEN ((COALESCE(TRA."SaldoAcreedor",0) + COALESCE(DIV."SaldoAcreedor",0))-(COALESCE(TRA."SaldoDeudor",0)   + COALESCE(DIV."SaldoDeudor",0))) > 0 THEN (COALESCE(TRA."SaldoAcreedor",0) + COALESCE(DIV."SaldoAcreedor",0))-(COALESCE(TRA."SaldoDeudor",0)   + COALESCE(DIV."SaldoDeudor",0))
				 ELSE 0 
			 END) AS "SaldoAcreedor"
					  
			 
			 ,(CASE 
				 WHEN ((COALESCE(TRA."SaldoDeudor",0)  + COALESCE(DIV."SaldoDeudor",0))-(COALESCE(TRA."SaldoAcreedor",0) + COALESCE(DIV."SaldoAcreedor",0))) > 0 THEN (COALESCE(TRA."SaldoDeudor",0)  + COALESCE(DIV."SaldoDeudor",0))-(COALESCE(TRA."SaldoAcreedor",0) + COALESCE(DIV."SaldoAcreedor",0))
				 ELSE 0				
			  END) AS "SaldoDeudor"
			 
			
			
			
			,(COALESCE(TRA."MontoRetencion",0) + COALESCE(DIV."MontoRetencion",0)) AS "MontoRetencion" -- PERU: Monto retenciÃ³n cavali(informado por archivo IGK)
		FROM :v_AgrupacionCamaraContraparte AS GRU
		LEFT JOIN :v_ResumenTransacciones AS TRA ON
				(
					    GRU."IdBolsa" = TRA."IdBolsa"		
					AND GRU."CodigoGrupoCamara" = TRA."CodigoGrupoCamara"
					AND GRU."NombreGrupoCamara" = TRA."NombreGrupoCamara" 
					AND GRU."IdClasificacionTipoInstrumento" = TRA."IdClasificacionTipoInstrumento"
					AND GRU."IdCorredorContraparte" = TRA."IdCorredorContraparte"
					AND GRU."IdComprobanteTesoreria" = TRA."IdComprobanteTesoreria"
					AND GRU."IdMonedaTransaccion" = TRA."IdMonedaTransaccion"
				)		
		LEFT JOIN :v_ResumenDividendos AS DIV ON 
				(
					    GRU."IdBolsa" = DIV."IdBolsa"		
					AND GRU."CodigoGrupoCamara" = DIV."CodigoGrupoCamara"					
					AND GRU."NombreGrupoCamara" = DIV."NombreGrupoCamara"
					AND GRU."IdClasificacionTipoInstrumento" = DIV."IdClasificacionTipoInstrumento"
					AND GRU."IdCorredorContraparte" = DIV."IdCorredorContraparte"
					AND GRU."IdComprobanteTesoreria" = DIV."IdComprobanteTesoreria"
					AND GRU."IdMonedaTransaccion" = DIV."IdMoneda"
				);
				


		-----------------------------------------------------------------
		-- GENERA SALIDA DETALLE TRANSACCIONES - CLEARING FINANCIERO 
		-----------------------------------------------------------------
		p_out_ClearingFinancieroDetalle =
		 SELECT
		 	   "IdTransaccion"
			  ,"IdBolsa"
			  ,"CodigoGrupoCamara" 
			  ,"NombreGrupoCamara"
			  ,"RutContraparte"
			  ,"AccionTipoOperacion"    
			  ,"TotalVenta"             
			  ,"TotalCompra"            
			  ,"RazonSocialContraparte"
			  ,"IdClasificacionTipoInstrumento"
			  ,"FechaSistemaCreacion"
			  ,"FolioTransaccion"
			  ,"IdCondicionDeLiquidacion"
			  ,"CodigoCondicionDeLiquidacion"
			  ,"IdMonedaTransaccion"
			  ,"IdComprobanteTesoreria"
			  ,"Instrumento"			  
			  ,"CantidadTransaccion"
			  ,"PrecioTransaccion"
			  ,"Tasa"	
			  ,0 AS "MontoRetencion"    	 -- En CHILE no se utiliza
		  FROM :v_Temp_Transacciones		  
		  ORDER BY
		  "IdBolsa"
		  ,"CodigoGrupoCamara"
		  ,"IdTipoInstrumento"		  
		  ,"RutContraparte"
		  ,"IdClasificacionTipoInstrumento"
		  ,"FechaSistemaCreacion"
		  ,"CodigoOperadorContraparte"
		  ,"AccionTipoOperacion";

		  
		
		-----------------------------------------------------------------
		-- GENERA SALIDA DETALLE DIVIDENDOS - CLEARING FINANCIERO 
		-----------------------------------------------------------------		
		p_out_ClearingFinancieroDividendos =
			SELECT
				 "CodigoGrupoCamara"
				,"NombreGrupoCamara"
				,"IdClasificacionTipoInstrumento"
				,"IdEventoCapitalCliente"
				,"IdEventoCapital"
				,"IdTipoEventoCapital"
				,"TipoEvento"
				,"NumeroDividendo"
				,"Secuencia"
				,"IdTipoInstrumento"   
				,"Nemo"
				,"IdMoneda"
				,"CantidadDerecho"
				,"Total"
				,"TotalVenta"
				,"TotalCompra"								
				,"IdBoveda"
				,"IdComprobanteTesoreria"
				,"IdBolsa"
				,"RutContraparte"
				,"RazonSocialContraparte"
				,"IdCorredorContraparte"
				,"TipoOperacion"   -- C/V									
			FROM :v_Temp_Dividendos;
		
	END;


END;
