SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFisico";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Consulta Clearing Fisico (Instrumentos a transferir al CCLV) 
--             Camara RV --> Solo CN (ACC, CFI) / PH y PM son bilaterales
--             Camara SM --> Solo Simultaneas PH
--             Camara PH --> Solo RF, IF que sean PH
--             Camara PM --> Solo RF, IF que sean PM
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 03/05/2017
-- 
"BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFisico"
  (
    IN p_IdEmpresa              BIGINT,
    IN p_IdUsuario              BIGINT,
    IN p_IdModulo               NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    IN p_FechaLiquidacion	    NVARCHAR(10),
    IN p_Camaras			    TABLE   ("Camara"   NVARCHAR(2)),  -- PM/RV/SM/PH
    
    
	OUT p_out_ClearingFisicoResumen  TABLE (
	                         "IdBolsa"    		        		NVARCHAR(3)
	                        ,"IdCamara"                  		NVARCHAR(2)
	                        ,"IdClasificacionTipoInstrumento"   NVARCHAR(3)	                        
							,"IdTipoInstrumento" 				NVARCHAR(3)
							,"NombreTipoInstrumento" 			NVARCHAR(100)
	                        ,"IdInstrumento"					BIGINT
	                        ,"Instrumento"						NVARCHAR(40)
							,"CantidadVenta"   					DECIMAL(26,8)
							,"CantidadCompra"  					DECIMAL(26,8)
							,"SaldoAcreedor"  					DECIMAL(26,8)
							,"SaldoDeudor"    	        		DECIMAL(26,8)
				),
 

	OUT p_out_ClearingFisicoDetalle table (
							 "IdTransaccion"                    BIGINT
							,"IdBolsa"    		        		NVARCHAR(3)							
							,"AccionTipoOperacion"      		NVARCHAR(40)
							,"CantidadVenta"     				DECIMAL(26,8)
							,"CantidadCompra"    				DECIMAL(26,8)
							,"IdClasificacionTipoInstrumento"   NVARCHAR(3)							
						    ,"IdTipoInstrumento"				NVARCHAR(3)
			  				,"NombreTipoInstrumento"			NVARCHAR(100)			  
				  			,"FechaSistemaCreacion" 			NVARCHAR(10)
							,"FolioTransaccion"   				NVARCHAR(40)
							,"IdCondicionDeLiquidacion" 		NVARCHAR(2)     -- T0/T1/..
							,"CodigoCondicionDeLiquidacion"     NVARCHAR(2)     -- PH/PM/CN
							,"IdComprobanteTesoreria"           BIGINT
	                        ,"IdInstrumento"					BIGINT
	                        ,"Instrumento"						NVARCHAR(40)
						    ,"CantidadTransaccion" 				DECIMAL(26,8)
						    ,"PrecioTransaccion"				DECIMAL(26,8)
						    ,"Tasa"								DECIMAL(26,8)
						    ,"MontoTransaccion" 				DECIMAL(26,8)
						    ,"IdCamara"  						NVARCHAR(2)
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
		
		DECLARE v_ParametroClearingIncluyeRFPM   NVARCHAR(1);
		DECLARE v_ParametroClearingIncluyeIFPM   NVARCHAR(1);
		DECLARE v_ParametroClearingIncluyeRFPH   NVARCHAR(1);
		DECLARE v_ParametroClearingIncluyeIFPH   NVARCHAR(1);	
		DECLARE v_IncluirCamaraRV                NVARCHAR(1);
		DECLARE v_IncluirCamaraPM                NVARCHAR(1);
		DECLARE v_IncluirCamaraPH                NVARCHAR(1);
		DECLARE v_IncluirCamaraSM                NVARCHAR(1);		


		-------------------------------------------------------
		-- Define Tabla para Transacciones(Palos) HANA
		-------------------------------------------------------
		DECLARE v_Temp_Final table
		(
			 "IdTransaccion"                    BIGINT
			,"IdClasificacionTipoInstrumento"   NVARCHAR(3)
			,"IdTipoInstrumento" 	    		NVARCHAR(3)			 	
			,"NombreTipoInstrumento"			NVARCHAR(100)
			,"IdBolsa"    		        		NVARCHAR(3)			
			,"CodigoOperadorContraparte"		NVARCHAR(40)
			,"IdCorredorContraparte"    		BIGINT
			,"AccionTipoOperacion"      		NVARCHAR(40)
			,"CantidadVenta"   					DECIMAL(26,8)
			,"CantidadCompra"  					DECIMAL(26,8)
			,"FolioTransaccion"   				NVARCHAR(40)
			,"IdCondicionDeLiquidacion" 		NVARCHAR(2)     -- T0/T1/T2
			,"CodigoCondicionDeLiquidacion"     NVARCHAR(2)     -- PH/PM/CN
			,"FechaSistemaCreacion" 			NVARCHAR(10)
			,"IdComprobanteTesoreria"           BIGINT		
			,"IdInstrumento"                    BIGINT      
			,"Instrumento"                      NVARCHAR(40)
		    ,"CantidadTransaccion" 				DECIMAL(26,8)
		    ,"PrecioTransaccion"				DECIMAL(26,8)
		    ,"Tasa"								DECIMAL(26,8)
		    ,"MontoTransaccion" 				DECIMAL(26,8)
		    ,"IdCamara"    		        		NVARCHAR(2) 
		);


		-- Asigna estructura a variable tipo tabla
		v_Temp_Final	= 
		SELECT 
			0  as "IdTransaccion",
			'' as "IdClasificacionTipoInstrumento",
			'' as "IdTipoInstrumento", 
			'' as "NombreTipoInstrumento",
			'' as "IdBolsa", 
			'' as "CodigoOperadorContraparte", 
			0  as "IdCorredorContraparte", 
			'' as "AccionTipoOperacion",
			0  as "CantidadVenta",  
			0  as "CantidadCompra", 
			'' as "FolioTransaccion",
			'' as "IdCondicionDeLiquidacion",  
			'' as "CodigoCondicionDeLiquidacion",
			'' as "FechaSistemaCreacion",			
			0  as "IdComprobanteTesoreria",
			0  as "IdInstrumento",
			'' as "Instrumento",
		    0  as "CantidadTransaccion",
		    0  as "PrecioTransaccion",
		    0  as "Tasa",
		    0  as "MontoTransaccion",
		    '' as "IdCamara"
		FROM dummy where 1=2;


		
		-- Consulta Parámetros de configuración		
		SELECT	
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYERFPM' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeRFPM",  -- indica si incluye "RF" PM
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYEIFPM' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeIFPM",  -- indica si incluye "IF" PM
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYERFPH' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeRFPH",  -- indica si incluye "RF" PH
			MAX(CASE WHEN "CodigoParametro" = 'CLEARINGINCLUYEIFPH' THEN "Valor" ELSE null END) AS "ParametroClearingIncluyeIFPH"   -- indica si incluye "IF" PH
        INTO v_ParametroClearingIncluyeRFPM, v_ParametroClearingIncluyeIFPM, v_ParametroClearingIncluyeRFPH, v_ParametroClearingIncluyeIFPH
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria"
		WHERE "CodigoParametro" IN ('CLEARINGINCLUYERFPM','CLEARINGINCLUYEIFPM','CLEARINGINCLUYERFPH','CLEARINGINCLUYEIFPH')
		AND   "IdEmpresa" = :p_IdEmpresa;		
		--SELECT 'Parametros:', :v_ParametroClearingIncluyeRFPM, :v_ParametroClearingIncluyeIFPM, :v_ParametroClearingIncluyeRFPH, :v_ParametroClearingIncluyeIFPH FROM DUMMY;  -- REVISAR


		-- Deglosa las cámaras a incluir en consulta
		SELECT 
			MAX(CASE WHEN "Camara" = 'RV' THEN 'S' ELSE 'N' END) AS "IncluirCamaraRV",
			MAX(CASE WHEN "Camara" = 'PM' THEN 'S' ELSE 'N' END) AS "IncluirCamaraPM",
			MAX(CASE WHEN "Camara" = 'PH' THEN 'S' ELSE 'N' END) AS "IncluirCamaraPH",
			MAX(CASE WHEN "Camara" = 'SM' THEN 'S' ELSE 'N' END) AS "IncluirCamaraSM"
		INTO v_IncluirCamaraRV, v_IncluirCamaraPM, v_IncluirCamaraPH, v_IncluirCamaraSM
		FROM :p_Camaras;
		--SELECT 'Camaras:', :v_IncluirCamaraRV, :v_IncluirCamaraPM, :v_IncluirCamaraPH, :v_IncluirCamaraSM  FROM DUMMY;   -- REVISAR
		
		
		-------------------------------------------------------------------------------------------------
		-- Obtiene las transacciones de todos los mercados (mas a delante los filtra)
		-------------------------------------------------------------------------------------------------
		v_Transacciones = 		
		SELECT      
			 TRA."IdTransaccion"
			,TRA."IdClasificacionTipoInstrumento"   -- RV, RF, IF, DV, FM
		    ,TRA."IdTipoInstrumento"                -- ACN /ACX /CFI / CFM / SIM    
			,TRA."NombreTipoInstrumento"
			,TRA."FechaSistemaCreacion"	          -- fecha de sistema del ingreso del palo(esta fecha es igual a la FechaOperacion pero sin la hora) 
			,TRA."FolioTransaccion" 			      
			,TRA."IndicadorOrdenDirecta"      					      -- S/N
		    ,TRA."IdBolsa"
			,TRA."MontoTransaccion"
		    ,TRA."IdTipoOperacion"        		  -- COMACN  / VENACX
		    ,TRA."AccionTipoOperacion"    		  -- COMPRA / VENTA 
		    ,TRA."IdCondicionDeLiquidacion"    	  -- T0, T1, T2, T3, TP
		    ,TRA."CodigoCondicionDeLiquidacion"
			,TRA."InstrumentoUI"   AS "Instrumento"
			,TRA."IdInstrumentoUI" AS "IdInstrumento"					    
			,TRA."IdCorredorContraparte"
			,CNT."DocumentoIdentidad"         AS "RutContraparte"
			,CNT."NombreCompleto"             AS "RazonSocialContraparte"
			,TRA."CodigoOperadorContraparte"
			,TRA."CodigoOperadorCorredor"	   AS "CodigoOperadorCorredora"			
			,TRA."FechaLiquidacion"
			,TRA."FechaVencimiento"  -- Para Simultaneas
			,TRA."EsAPlazo"
			,TRA."EsBilateral"     -- Se carga desde TBCLOPER con WS de actualizacion de transacciones
		    ,TRA."IdCamara"        -- Se carga desde TBCLOPER con WS de actualizacion de transacciones
			,TRA."IdMonedaTransaccion"
			,TRA."IdComprobanteTesoreria"    -- Si existe este valor, ya se procesó la liquidación
		    ,TRA."CantidadTransaccion"
		    ,TRA."PrecioTransaccion"
		    ,TRA."Tasa"
		FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS CNT ON (CNT."IdCorredora" = TRA."IdCorredorContraparte")
		WHERE TRA."IdEstadoTransaccion"  	<> 'ANU'      	 -- No considerar anulados
		AND   TRA."IndicadorOrdenDirecta" 	= ''         	 -- No considerar OD (orden directa)
		AND   TRA."EsBilateral" 			= 'N'            -- No considerar transacciones bilaterales
		AND   TRA."IdEmpresa" 				= :p_IdEmpresa		
		AND   TRA."BloqueoLiquidacion" 		= 'N'            -- No bloqueado para liquidación
		AND   TRA."IdMonedaTransaccion"     = 'CLP'          -- Solo se compensa CLP
		AND  (TRA."FechaLiquidacion"   		= :p_FechaLiquidacion OR TRA."FechaVencimiento" = :p_FechaLiquidacion)  -- FechaVencimiento para Simultaneas
		AND  (   TRA."IdCamara" = (CASE WHEN (:v_IncluirCamaraRV = 'S') THEN 'RV' ELSE 'N/A' END)	
			  OR TRA."IdCamara" = (CASE WHEN (:v_IncluirCamaraPM = 'S') THEN 'PM' ELSE 'N/A' END)
			  OR TRA."IdCamara" = (CASE WHEN (:v_IncluirCamaraPH = 'S') THEN 'PH' ELSE 'N/A' END)
			  OR TRA."IdCamara" = (CASE WHEN (:v_IncluirCamaraSM = 'S') THEN 'SM' ELSE 'N/A' END)
		     );

		--SELECT 'v_Transacciones', * FROM :v_Transacciones;  -- REVISAR

		
		-------------------------------------------------
		-- CARGA TRANSACCIONES CN A CAMARA RV
		-------------------------------------------------
		v_Temp_Final = 
		  SELECT * FROM :v_Temp_Final  UNION ALL
		  SELECT
		  			 "IdTransaccion"
				    ,"IdClasificacionTipoInstrumento"
				 	,"IdTipoInstrumento"				 	
				 	,"NombreTipoInstrumento"
					,"IdBolsa"
					,"CodigoOperadorContraparte"
					,"IdCorredorContraparte"
					,"AccionTipoOperacion"
					,(CASE WHEN "AccionTipoOperacion" = 'VENTA'  THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadVenta"
					,(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadCompra"
					,"FolioTransaccion"							
					,"IdCondicionDeLiquidacion"      -- T2: Contado Normal(CN)					
			        ,"CodigoCondicionDeLiquidacion"
					,"FechaSistemaCreacion"
					,"IdComprobanteTesoreria"						
					,"IdInstrumento"
					,"Instrumento"						
				    ,"CantidadTransaccion"
				    ,"PrecioTransaccion"
				    ,"Tasa"
				    ,"MontoTransaccion"
				    ,"IdCamara"
				FROM :v_Transacciones		
				WHERE "IdClasificacionTipoInstrumento" = 'RV'
				AND   "IdCamara" = 'RV'                   -- Camara del CCLV: RV
				AND   "FechaLiquidacion" = :p_FechaLiquidacion
				AND   "IdCondicionDeLiquidacion" = 'T2'  -- T2: Contado Normal(CN)			
				AND   "IdTipoInstrumento"  <> 'SIM';      -- No considera simultáneas
				
	

		-------------------------------------------------
		-- CARGA TRANSACCIONES DE SIMULTANEA (CAMARA SM)
		-------------------------------------------------
		v_Temp_Final = 
		    SELECT * FROM :v_Temp_Final    UNION ALL
			SELECT
					 "IdTransaccion"
					,'SIM' AS "IdClasificacionTipoInstrumento"
					,"IdTipoInstrumento"	
					,"NombreTipoInstrumento"					
					,"IdBolsa"
					,"CodigoOperadorContraparte"						
					,"IdCorredorContraparte"
					,"AccionTipoOperacion"
					,(CASE WHEN "AccionTipoOperacion" = 'VENTA'  THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadVenta"
					,(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadCompra"			
					,"FolioTransaccion"
					,"IdCondicionDeLiquidacion"      -- T0: Contado Normal(CN)
			        ,"CodigoCondicionDeLiquidacion"
					,"FechaSistemaCreacion"
					,"IdComprobanteTesoreria"
					,"IdInstrumento"
					,"Instrumento"
				    ,"CantidadTransaccion"
				    ,"PrecioTransaccion"
				    ,"Tasa"
				    ,"MontoTransaccion"		
				    ,"IdCamara"
				FROM  :v_Transacciones							
				WHERE "IdClasificacionTipoInstrumento" = 'RV'
				AND   "FechaVencimiento" = :p_FechaLiquidacion				
				AND   "IdCamara" = 'SM'  -- Camara CCLV 'SM'
				AND   "IdCondicionDeLiquidacion" = 'T0'  -- T0: Pago Hoy (PH)
				AND   "IdTipoInstrumento" = 'SIM'
				AND   "AccionTipoOperacion" IN ('COMPRA','VENTA');					
				

				
		----------------------------------------
		-- INCLUIR "RF" a CAMARA "PH" (parámetrico)
		----------------------------------------
		IF (v_ParametroClearingIncluyeRFPH = 'S') THEN
		
			v_Temp_Final = 
			    SELECT * FROM :v_Temp_Final    UNION ALL
				SELECT
						 "IdTransaccion"
						,"IdClasificacionTipoInstrumento"
						,"IdTipoInstrumento"	
						,"NombreTipoInstrumento"					
						,"IdBolsa"
						,"CodigoOperadorContraparte"
						,"IdCorredorContraparte"			
						,"AccionTipoOperacion"			
						,(CASE WHEN "AccionTipoOperacion" = 'VENTA'  THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadVenta"
						,(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadCompra"
						,"FolioTransaccion"			
						,"IdCondicionDeLiquidacion"
				        ,"CodigoCondicionDeLiquidacion"				
						,"FechaSistemaCreacion"
						,"IdComprobanteTesoreria"
						,"IdInstrumento"
						,"Instrumento"						
					    ,"CantidadTransaccion"
					    ,"PrecioTransaccion"
					    ,"Tasa"
					    ,"MontoTransaccion"
					    ,"IdCamara"
					FROM :v_Transacciones
					WHERE "IdClasificacionTipoInstrumento" = 'RF'
					AND   "IdCamara" = 'PH'  -- Camara CCLV PH
					AND   "FechaLiquidacion" = :p_FechaLiquidacion
					AND   "IdCondicionDeLiquidacion" = 'T0';  -- T0: Pago Hoy (PH)

		END IF;
		
		
		
		
		--------------------------------------------
		-- INCLUIR "IF" A CAMARFA PH (parámetrico)
		--------------------------------------------
		
		IF (v_ParametroClearingIncluyeIFPH = 'S') THEN

			v_Temp_Final = 
			    SELECT * FROM :v_Temp_Final    UNION ALL
				SELECT
						 "IdTransaccion"
						,"IdClasificacionTipoInstrumento"
						,"IdTipoInstrumento"		
						,"NombreTipoInstrumento"				
						,"IdBolsa"			
						,"CodigoOperadorContraparte"
						,"IdCorredorContraparte"		
						,"AccionTipoOperacion"
						,(CASE WHEN "AccionTipoOperacion" = 'VENTA'  THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadVenta"
						,(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadCompra"
						,"FolioTransaccion"			
						,"IdCondicionDeLiquidacion"	
				        ,"CodigoCondicionDeLiquidacion"													
						,"FechaSistemaCreacion"
						,"IdComprobanteTesoreria"
						,"IdInstrumento"
						,"Instrumento"						
					    ,"CantidadTransaccion"
					    ,"PrecioTransaccion"
					    ,"Tasa"
					    ,"MontoTransaccion"
					    ,"IdCamara"
					FROM :v_Transacciones					
					WHERE "IdClasificacionTipoInstrumento" = 'IF'
					AND   "IdCamara" = 'PH'   -- Camara CCLV PH
					AND   "FechaLiquidacion" = :p_FechaLiquidacion
					AND   "IdCondicionDeLiquidacion" = 'T0';  -- T0: Pago Hoy (PH)
				
		END IF;
		
		
		

		---------------------------------------------------
		-- INCLUIR "RF" A CAMARA PM (parámetrico)
		---------------------------------------------------

		IF (:v_ParametroClearingIncluyeRFPM = 'S') THEN
					
			v_Temp_Final = 
			    SELECT * FROM :v_Temp_Final    UNION ALL
				SELECT
						 "IdTransaccion"
						,"IdClasificacionTipoInstrumento"
						,"IdTipoInstrumento"
						,"NombreTipoInstrumento"
						,"IdBolsa"
						,"CodigoOperadorContraparte"
						,"IdCorredorContraparte"
						,"AccionTipoOperacion"
						,(CASE WHEN "AccionTipoOperacion" = 'VENTA'  THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadVenta"
						,(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadCompra"
						,"FolioTransaccion"
						,"IdCondicionDeLiquidacion"
				        ,"CodigoCondicionDeLiquidacion"								
						,"FechaSistemaCreacion"
						,"IdComprobanteTesoreria"
						,"IdInstrumento"
						,"Instrumento"						
					    ,"CantidadTransaccion"
					    ,"PrecioTransaccion"
					    ,"Tasa"
					    ,"MontoTransaccion"
					    ,"IdCamara"
					FROM :v_Transacciones							
					WHERE "IdClasificacionTipoInstrumento" = 'RF'
					AND   "IdCamara" = 'PM'  -- Camara CCLV PM
					AND   "FechaLiquidacion" = :p_FechaLiquidacion
					AND   "IdCondicionDeLiquidacion" = 'T1';  -- T1: Pago Mañana (PM)

		END IF;


				
		
		----------------------------------------------------
		-- INCLUIR "IF" A CAMARA PM  (parámetrico)
		----------------------------------------------------
		IF (v_ParametroClearingIncluyeIFPM = 'S') THEN
			
			v_Temp_Final = 
			    SELECT * FROM :v_Temp_Final    UNION ALL
				SELECT
						 "IdTransaccion"
						,"IdClasificacionTipoInstrumento"
						,"IdTipoInstrumento"	
						,"NombreTipoInstrumento"					
						,"IdBolsa"
						,"CodigoOperadorContraparte"
						,"IdCorredorContraparte"		
						,"AccionTipoOperacion"
						,(CASE WHEN "AccionTipoOperacion" = 'VENTA'  THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadVenta"
						,(CASE WHEN "AccionTipoOperacion" = 'COMPRA' THEN "CantidadTransaccion" ELSE 0 END) AS "CantidadCompra"
						,"FolioTransaccion"
						,"IdCondicionDeLiquidacion"
				        ,"CodigoCondicionDeLiquidacion"
						,"FechaSistemaCreacion"
						,"IdComprobanteTesoreria"
						,"IdInstrumento"
						,"Instrumento"						
					    ,"CantidadTransaccion"
					    ,"PrecioTransaccion"
					    ,"Tasa"
						,"MontoTransaccion"
						,"IdCamara"
					FROM :v_Transacciones			
					WHERE "IdClasificacionTipoInstrumento" = 'IF'
					AND   "IdCamara" = 'PM'    -- Camara CCLV PM
					AND   "FechaLiquidacion" = :p_FechaLiquidacion
					AND   "IdCondicionDeLiquidacion" = 'T1';  -- T1: Pago Mañana (PM)
		END IF;
	
		
		--SELECT 'Temp_Final', * FROM :v_Temp_Final;  -- REVISAR
		
		
		
	
		----------------------------------------------------
		-- GENERA RESUMEN CLEARING FISICO(Agrupado)
		----------------------------------------------------
	    p_out_ClearingFisicoResumen =
		SELECT
			 "IdBolsa"
		    ,"IdCamara"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"NombreTipoInstrumento"
			,"IdInstrumento"
	        ,"Instrumento"
			,SUM("CantidadVenta")  AS "CantidadVenta"
			,SUM("CantidadCompra") AS "CantidadCompra"					
			,(CASE WHEN (SUM("CantidadVenta") > SUM("CantidadCompra")) 
				THEN (SUM("CantidadVenta") - SUM("CantidadCompra"))
				ELSE 0				
			END) AS "SaldoAcreedor"			
			,(CASE WHEN (SUM("CantidadVenta") <= SUM("CantidadCompra")) 
				THEN (SUM("CantidadCompra") - SUM("CantidadVenta"))
				ELSE 0 
			END) AS "SaldoDeudor"
		FROM :v_Temp_Final
		GROUP BY "IdBolsa","IdCamara", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento",	"IdInstrumento", "Instrumento";
		


		-----------------------------------------------------------
		-- GENERA DETALLE CLEARING FISICO(por transacción)
		-----------------------------------------------------------

		p_out_ClearingFisicoDetalle = 
		 SELECT
		 	   "IdTransaccion"
			  ,"IdBolsa"
			  ,"AccionTipoOperacion"			  
			  ,"CantidadVenta"             
			  ,"CantidadCompra"
			  ,"IdClasificacionTipoInstrumento"			  
			  ,"IdTipoInstrumento"
			  ,"NombreTipoInstrumento"
			  ,"FechaSistemaCreacion"
			  ,"FolioTransaccion"
			  ,"IdCondicionDeLiquidacion"
			  ,"CodigoCondicionDeLiquidacion"
			  ,"IdComprobanteTesoreria"
			  ,"IdInstrumento"
			  ,"Instrumento"
			  ,"CantidadTransaccion"
			  ,"PrecioTransaccion"
			  ,"Tasa"
			  ,"MontoTransaccion"
			  ,"IdCamara"
		  FROM :v_Temp_Final
		  ORDER BY "IdBolsa", "FechaSistemaCreacion";

	
	END;


END;
