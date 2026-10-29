SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinancieroPE";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Consulta transacciones para Clearing Financiero Peru de acuerdo a las camaras de CAVALI:
-- 				  RV: Operaciones rueda con instrumentos RV, Sólo T2
-- 				  RF: Operaciones rueda con instrumentos de deuda, Sólo T0, T1 y T2
-- 				  RP: Operaciones de Reporte. Para RF son T0 y T1 y RV solo T2
-- 				  RM: Operaciones MIEM, Forma de Negociación
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 03/01/2018
-- 
"BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinancieroPE"
  (
    -- VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa              BIGINT,
    IN p_IdUsuario              BIGINT,
    IN p_IdModulo               NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    
    -- VARIABLES DE ENTRADA
    IN  p_FechaLiquidacion	    NVARCHAR(10),
    IN  p_Camaras			    TABLE   ("Camara"   NVARCHAR(2)),  -- RV/FR/RP/RM

    --VARIABLES DE SALIDA		
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
						    ,"MontoRetencion"    				DECIMAL(26,8)	 -- PERU: monto retenido por cavali (Informado por archivo IGK)					    
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
		
		DECLARE v_NombreGrupoCamara        		 NVARCHAR(40);		
		DECLARE v_CodigoGrupoCamara              INTEGER;
		DECLARE v_IncluirCamaraRV                NVARCHAR(1);
		DECLARE v_IncluirCamaraRF                NVARCHAR(1);
		DECLARE v_IncluirCamaraRP                NVARCHAR(1);
		DECLARE v_IncluirCamaraRM                NVARCHAR(1);
		
		
		DECLARE v_Temp_Transacciones TABLE
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
			,"IdMonedaTransaccion"				NVARCHAR(3)
			,"IdComprobanteTesoreria"           BIGINT			
			,"Instrumento"                      NVARCHAR(40)			
		    ,"CantidadTransaccion" 				DECIMAL(26,8)
		    ,"PrecioTransaccion"				DECIMAL(26,8)
		    ,"Tasa"								DECIMAL(26,8)
		    ,"MontoRetencion"				    DECIMAL(26,8)  	 -- PERU: monto retenido por cavali (Informado por archivo IGK)
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
			'' as "IdMonedaTransaccion",
			0  as "IdComprobanteTesoreria",
			'' as "Instrumento",			
		    0  as "CantidadTransaccion",
		    0  as "PrecioTransaccion",
		    0  as "Tasa",
		    0  AS "MontoRetencion"		    
		FROM dummy where 1=2;
		
		

		SELECT 
			MAX(CASE WHEN "Camara" = 'RV' THEN 'S' ELSE 'N' END) AS "IncluirCamaraRV",
			MAX(CASE WHEN "Camara" = 'RF' THEN 'S' ELSE 'N' END) AS "IncluirCamaraRF",
			MAX(CASE WHEN "Camara" = 'RP' THEN 'S' ELSE 'N' END) AS "IncluirCamaraRP",
			MAX(CASE WHEN "Camara" = 'RM' THEN 'S' ELSE 'N' END) AS "IncluirCamaraRM"
		INTO v_IncluirCamaraRV, v_IncluirCamaraRF, v_IncluirCamaraRP, v_IncluirCamaraRM
		FROM :p_Camaras;
		--SELECT 'Camaras:', :v_IncluirCamaraRV, :v_IncluirCamaraRF, :v_IncluirCamaraRP, :v_IncluirCamaraRM  FROM DUMMY;   -- REVISAR


		-------------------------------------------------------------------------------------------------
		-- OBTIENE TRANSACCIONES PARA LIQUIDACION MULTILATERAL(CLEARING) (todos los mercados, mas adelante se filtran)
		-------------------------------------------------------------------------------------------------
		v_Transacciones = 		
		SELECT      
			 TRA."IdTransaccion"
			,TRA."IdClasificacionTipoInstrumento"   -- RV, RF, IF, DV, FM
			,TRA."FechaSistemaCreacion"	          	-- fecha de sistema del ingreso del palo(esta fecha es igual a la FechaOperacion pero sin la hora) 
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
			,CNT."NombreCompleto"             AS "RazonSocialContraparte"
			,CNT."CodigoInscripcionRegulador" AS "CodigoCorredorContraparte"
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
		    ,TRA."MontoRetencion"   		 -- PERU monto retención cavali(se informa en archivo IGK)
		    
		FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS CNT ON (CNT."IdCorredora" = TRA."IdCorredorContraparte")	         
		WHERE TRA."EsBilateral" = 'N'     -- No sea bilateral
		AND (TRA."FechaLiquidacion" = :p_FechaLiquidacion   OR  TRA."FechaVencimiento" = :p_FechaLiquidacion)  -- Vencimiento para reportes(simultáneas)
		AND TRA."IdEstadoTransaccion"  <> 'ANU'    -- No considerar anulados
		AND TRA."BloqueoLiquidacion" = 'N' 		   -- Que no esté bloqueada para liquidación
		AND TRA."IndicadorOrdenDirecta" = ''       -- Que no sea OD, OE(OD con operador externo)
		AND TRA."IdBolsa" = 'BVL'                  -- Todas las transacciones BVL son liquidadas vía Clearing financiero -- No considerar MILA (BOLSA DE SANTIAGO, COLOMBIA Y MEXICO) ni FUERA DE RUEDA
		AND TRA."MarcaIGK" = 'S'                   -- Transaccion confirmada por cavali a través de archivo IGK
		AND TRA."IdEmpresa" = :p_IdEmpresa;
		-- SELECT 'v_Transacciones', * FROM :v_Transacciones;  -- REVISAR

		
		
		-------------------------------------------------
		-- 1. CARGA TRANSACCIONES RV(T2) EN CAMARA "RV"
		-------------------------------------------------
		IF (:v_IncluirCamaraRV = 'S') THEN
			v_Temp_Transacciones = 
			  SELECT * FROM :v_Temp_Transacciones  UNION ALL
			  SELECT
			  		 TRA."IdTransaccion"
					,TRA."IdClasificacionTipoInstrumento"
				 	,TRA."IdTipoInstrumento"
					,TRA."IdBolsa"
					,1  AS "CodigoGrupoCamara" -- codigo ficticio(se usa en CL)
					,'RV' AS "NombreGrupoCamara"
					,TRA."RutContraparte"
					,TRA."CodigoOperadorContraparte"
					,TRA."IdCorredorContraparte"
					,TRA."AccionTipoOperacion"
					,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN "MontoTransaccion" ELSE 0 END) AS "TotalVenta"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN "MontoTransaccion" ELSE 0 END) AS "TotalCompra"
					,TRA."RazonSocialContraparte"
					,TRA."FolioTransaccion"
					,TRA."IdCondicionDeLiquidacion"
				    ,TRA."CodigoCondicionDeLiquidacion"
					,TRA."FechaSistemaCreacion"
					,TRA."IdMonedaTransaccion"
					,TRA."IdComprobanteTesoreria"
					,TRA."Instrumento"
				    ,TRA."CantidadTransaccion"
				    ,TRA."PrecioTransaccion"
				    ,TRA."Tasa"
				    ,TRA."MontoRetencion"

				FROM :v_Transacciones AS TRA				
				WHERE 	TRA."IdClasificacionTipoInstrumento" = 'RV'
				AND   	TRA."IdCondicionDeLiquidacion" = 'T2';  -- T2: Contado Normal
		END IF;  -- IF (:v_IncluirCamaraRV = 'S')
		
		
		-------------------------------------------------
		-- 2. CARGA TRANSACCIONES RF(T0, T1 y T2) EN CAMARA "RF"
		-------------------------------------------------
		IF (:v_IncluirCamaraRF = 'S') THEN
			v_Temp_Transacciones = 
			  SELECT * FROM :v_Temp_Transacciones  UNION ALL
			  SELECT
			  		 TRA."IdTransaccion"
					,TRA."IdClasificacionTipoInstrumento"
				 	,TRA."IdTipoInstrumento"
					,TRA."IdBolsa"
					,2  AS "CodigoGrupoCamara"
					,'RF' AS "NombreGrupoCamara"
					,TRA."RutContraparte"
					,TRA."CodigoOperadorContraparte"
					,TRA."IdCorredorContraparte"
					,TRA."AccionTipoOperacion"
					,(CASE WHEN TRA."AccionTipoOperacion" ='VENTA'   THEN "MontoTransaccion" ELSE 0 END) AS "TotalVenta"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN "MontoTransaccion" ELSE 0 END) AS "TotalCompra"
					,TRA."RazonSocialContraparte"
					,TRA."FolioTransaccion"
					,TRA."IdCondicionDeLiquidacion"   
				    ,TRA."CodigoCondicionDeLiquidacion"
					,TRA."FechaSistemaCreacion"	
					,TRA."IdMonedaTransaccion"
					,TRA."IdComprobanteTesoreria"
					,TRA."Instrumento"
				    ,TRA."CantidadTransaccion"
				    ,TRA."PrecioTransaccion"
				    ,TRA."Tasa"
				    ,TRA."MontoRetencion"
				    
				FROM :v_Transacciones AS TRA				
				WHERE 	TRA."IdClasificacionTipoInstrumento" = 'RF'
				AND   	TRA."IdCondicionDeLiquidacion" IN ('PH','PM','T2');		
		END IF;  -- IF (:v_IncluirCamaraRF = 'S')

		

		-------------------------------------------------
		-- 3. CARGA TRANSACCIONES DE REPORTES RF(T0, T1) y REPORTES RV(T2)
		-------------------------------------------------
		IF (:v_IncluirCamaraRP = 'S') THEN
			v_Temp_Transacciones = 
			  SELECT * FROM :v_Temp_Transacciones  UNION ALL
			  SELECT
			  		 TRA."IdTransaccion"
					,TRA."IdClasificacionTipoInstrumento"
				 	,TRA."IdTipoInstrumento"
					,TRA."IdBolsa"
					,3  AS "CodigoGrupoCamara"
					,'RP' AS "NombreGrupoCamara"    -- RP: camara reportes
					,TRA."RutContraparte"
					,TRA."CodigoOperadorContraparte"
					,TRA."IdCorredorContraparte"
					,TRA."AccionTipoOperacion"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'   THEN "MontoTransaccion" ELSE 0 END) AS "TotalVenta"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN "MontoTransaccion" ELSE 0 END) AS "TotalCompra"
					,TRA."RazonSocialContraparte"
					,TRA."FolioTransaccion"
					,TRA."IdCondicionDeLiquidacion"   
				    ,TRA."CodigoCondicionDeLiquidacion"
					,TRA."FechaSistemaCreacion"	
					,TRA."IdMonedaTransaccion"
					,TRA."IdComprobanteTesoreria"
					,TRA."Instrumento"
				    ,TRA."CantidadTransaccion"
				    ,TRA."PrecioTransaccion"
				    ,TRA."Tasa"
				    ,TRA."MontoRetencion"
				    
				FROM :v_Transacciones AS TRA				
				WHERE 	TRA."IdClasificacionTipoInstrumento" = 'RE'  -- Reportes
				AND (  
				       (TRA."IdTipoInstrumento" = 'RRV' AND TRA."IdCondicionDeLiquidacion" = 'T2') -- Reportes RV Cond Liq: T2(CN)					
					OR (TRA."IdTipoInstrumento" = 'RRF' AND TRA."IdCondicionDeLiquidacion" IN ('PH','PM')) -- Reportes RF Cond Liq: PH o PM
				    );
				
		END IF;  -- IF (:v_IncluirCamaraRP = 'S')

		
		-------------------------------------------------
		-- 4. CARGA TRANSACCIONES DE MIEN para titulos de deuda(Forma de Negociacion en Orden y transaccion)
		-------------------------------------------------		
		IF (:v_IncluirCamaraRM = 'S') THEN
			-- La corredora no hace operaciones de estos títulos(confirmado por jimmy de PE)
		END IF;  -- IF (:v_IncluirCamaraRM = 'S') THEN
		
			

		
		----------------------------------------------------------
		-- GENERA SALIDA CLEARING FINANCIERO RESUMEN (Agrupado)
		----------------------------------------------------------
	    p_out_ClearingFinancieroResumen =
		SELECT  "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento",				
				"IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdMonedaTransaccion","IdComprobanteTesoreria"
				,SUM("TotalVenta") AS "TotalVenta"
				,SUM("TotalCompra") AS "TotalCompra"					
				,(CASE    -- PERU: a las ventas se le resta MontoRetencion(cavali). En chile como es cero da lo mismo restar
					 WHEN ((SUM("TotalVenta")-SUM("MontoRetencion")) > SUM("TotalCompra")) THEN ((SUM("TotalVenta")-SUM("MontoRetencion")) - SUM("TotalCompra"))
					 ELSE 0
				  END) AS "SaldoAcreedor"
				,(CASE
					 WHEN ((SUM("TotalVenta")-SUM("MontoRetencion")) <= SUM("TotalCompra")) THEN (SUM("TotalCompra") - (SUM("TotalVenta")-SUM("MontoRetencion")))
					 ELSE 0
				  END) AS "SaldoDeudor"
				,SUM("MontoRetencion") AS "MontoRetencion"       -- PERU: Monto retención cavali(informado por archivo IGK)
		FROM :v_Temp_Transacciones
		GROUP BY "IdBolsa","CodigoGrupoCamara","NombreGrupoCamara","IdClasificacionTipoInstrumento",	
				 "IdCorredorContraparte","RutContraparte","RazonSocialContraparte","IdMonedaTransaccion","IdComprobanteTesoreria";
				   

		-----------------------------------------------------------------
		-- GENERA SALIDA CLEARING FINANCIERO DETALLE TRANSACCIONES  
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
			  ,"MontoRetencion"       -- PERU: Monto retención cavali(informado por archivo IGK)
			  
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
		  
		
	END;


END;
