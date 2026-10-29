SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateral";

--  Proposito: Consultar transaccionales bilaterales(no compensadas que no se liquidan por CCLV) para el pago a corredores
--  Autor: BCS 
--  Fecha Creacion: 25/08/2016
-- 
GO
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateral"
  (
  
    IN p_IdEmpresa             				BIGINT,
    IN p_IdUsuario              			BIGINT,
    IN p_IdModulo               			NVARCHAR(3),
    
    -- VARIABLES DE ENTRADA
    IN p_FechaLiquidacion			    	NVARCHAR(10),
    IN p_ClasificacionTipoInstrumento  		NVARCHAR(3)  	DEFAULT '',   -- RV,RF,IF (Ex Mercado)
    IN p_TipoInstrumento               		NVARCHAR(3) 	DEFAULT '',   -- ACN
    IN p_CondicionDeLiquidacion        		NVARCHAR(2)  	DEFAULT '',   -- T0,T1,T2,T3

    
    
	IN p_IdAgrupacion						BIGINT  DEFAULT 0,     -- Para consultar un detalle(grupo) especifico(desde subgrilla UI)

	OUT p_out_LiquidacionBilateralDetalle  	TABLE  (
												 "IdAgrupacion"								BIGINT
												,"IdTransaccion"                    		BIGINT
												,"DocumentoIdentidadCorredoraContraparte" 	NVARCHAR(40)
												,"NombreCortoCorredoraContraparte"		 	NVARCHAR(40)
												,"NombreCompletoCorredoraContraparte"	 	NVARCHAR(250)
												,"CodigoInscripcionCorredoraContraparte"   	NVARCHAR(40)
												,"IdPersonaCorredoraContraparte"			BIGINT
												,"IdBolsa"									NVARCHAR(3)
												,"IdClasificacionTipoInstrumento" 			NVARCHAR(3)
												,"IdTipoInstrumento"						NVARCHAR(3) 
									  			,"FechaSistemaCreacion" 					DATE
												,"Nemotecnico"  							NVARCHAR(40)
												,"FolioTransaccion"  						NVARCHAR(40)
												,"CantidadTransaccion"     					DECIMAL(26,8)
												,"PrecioTransaccion"    					DECIMAL(26,8)
												,"IdCondicionDeLiquidacion"         		NVARCHAR(2)   -- T0/T1/T2/T3
												,"CodigoCondicionDeLiquidacion"     		NVARCHAR(40)  -- PH/PM/CN/T3
												,"MontoCompra"     							DECIMAL(26,8)
												,"MontoVenta"    							DECIMAL(26,8)								
												,"MontoTransaccion"    						DECIMAL(26,8)
												,"Intereses"								DECIMAL(26,8)
												,"MontoLiquidacion"    						DECIMAL(26,8) -- Monto a liquidar (Para Mila --> Monto liquidacion corresponsal)
												,"AccionTipoOperacion"      				NVARCHAR(40)												
												,"IdMonedaTransaccion"						NVARCHAR(3)
												,"IdMonedaLiquidacion"						NVARCHAR(3)   -- MILA/OUT: IdMonedaLiquidacionCorresponsal    Resto: MonedaTransaccion
												,"IdComprobanteTesoreria"           		BIGINT
												,"NombreTipoOperacion"              		NVARCHAR(100)),
												
	OUT p_out_LiquidacionBilateralResumen  	TABLE  (
												 "IdAgrupacion"								BIGINT
												,"IdBolsa"									NVARCHAR(3)
												,"IdPersonaCorredoraContraparte"			BIGINT
												,"DocumentoIdentidadCorredoraContraparte" 	NVARCHAR(40)
												,"NombreCortoCorredoraContraparte"			NVARCHAR(40)
												,"NombreCompletoCorredoraContraparte"		NVARCHAR(250)																								
												,"CodigoInscripcionCorredoraContraparte"    NVARCHAR(40)
												,"IdMonedaLiquidacion"						NVARCHAR(3)
												,"IdCondicionDeLiquidacion"         		NVARCHAR(2)
												,"CodigoCondicionDeLiquidacion"     		NVARCHAR(40)
												,"IdClasificacionTipoInstrumento" 			NVARCHAR(3)
												,"IdTipoInstrumento"						NVARCHAR(3)
												,"AccionTipoOperacion"      				NVARCHAR(40)
												,"IdComprobanteTesoreria"           		BIGINT
												,"TotalMontoCompra" 						DECIMAL(26,8)
												,"TotalMontoVenta"  						DECIMAL(26,8)
												),

	-- Salida errores
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL	
		
AS	
BEGIN

	DECLARE SALIDA_CONTROLADA_NO_DATA 			CONDITION;
	DECLARE v_errorExt	 						NVARCHAR(40);
	DECLARE v_IncluirRV 			 			NVARCHAR(1);
	DECLARE v_IncluirRF 			 			NVARCHAR(1);
	DECLARE v_IncluirIF 			 			NVARCHAR(1);
	
	DECLARE v_LiquidacionBilateralDetalle   	TABLE(
												 "IdAgrupacion"								BIGINT
												,"IdTransaccion"                    		BIGINT
												,"DocumentoIdentidadCorredoraContraparte" 	NVARCHAR(40)
												,"NombreCortoCorredoraContraparte"		 	NVARCHAR(40)
												,"NombreCompletoCorredoraContraparte"	 	NVARCHAR(250)
												,"CodigoInscripcionCorredoraContraparte"   	NVARCHAR(40)
												,"IdPersonaCorredoraContraparte"			BIGINT
												,"IdBolsa"									NVARCHAR(3)
												,"IdClasificacionTipoInstrumento" 			NVARCHAR(3)
												,"IdTipoInstrumento"						NVARCHAR(3) 
									  			,"FechaSistemaCreacion" 					DATE
												,"Nemotecnico"  							NVARCHAR(40)
												,"FolioTransaccion"  						NVARCHAR(40)
												,"CantidadTransaccion"     					DECIMAL(26,8)
												,"PrecioTransaccion"    					DECIMAL(26,8)
												,"IdCondicionDeLiquidacion"         		NVARCHAR(2)   -- T0/T1/T2/T3
												,"CodigoCondicionDeLiquidacion"     		NVARCHAR(40)  -- PH/PM/CN/T3
												,"MontoCompra"     							DECIMAL(26,8)
												,"MontoVenta"    							DECIMAL(26,8)												
												,"MontoTransaccion"    						DECIMAL(26,8)
												,"Intereses"	    						DECIMAL(26,8)
												,"MontoLiquidacion"    						DECIMAL(26,8) -- Monto a liquidar (Para Mila --> Monto liquidacion corresponsal)
												,"AccionTipoOperacion"      				NVARCHAR(40)												
												,"IdMonedaTransaccion"						NVARCHAR(3)
												,"IdMonedaLiquidacion"						NVARCHAR(3)
												,"IdComprobanteTesoreria"           		BIGINT
												,"NombreTipoOperacion"              		NVARCHAR(100));
												
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;		
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;	
	

	v_LiquidacionBilateralDetalle	= 
	SELECT 0 as "IdAgrupacion", 0 as "IdTransaccion", '' as "DocumentoIdentidadCorredoraContraparte", '' as "NombreCortoCorredoraContraparte",'' as "NombreCompletoCorredoraContraparte", '' as "CodigoInscripcionCorredoraContraparte", 0 as "IdPersonaCorredoraContraparte", 
		   '' as "IdBolsa", '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento",
		   '' as "FechaSistemaCreacion", '' as "Nemotecnico", '' as "FolioTransaccion",
		   0 as "CantidadTransaccion", 0 as "PrecioTransaccion", '' as "IdCondicionDeLiquidacion",
		   '' as "CodigoCondicionDeLiquidacion", 0 as "MontoCompra", 0 as "MontoVenta", 0 as "MontoTransaccion",0 as "Intereses",
		   0 AS "MontoLiquidacion", '' as "AccionTipoOperacion", '' as "IdMonedaTransaccion",'' as "IdMonedaLiquidacion",0 as "IdComprobanteTesoreria", '' as "NombreTipoOperacion"
	FROM dummy WHERE 1=2;
													
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy WHERE 1=2;
	

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;	
		
		---------------------------------------------------------------------------------------
		-- NOTA: ACTUALIZA LAS TRANSACCIONES CON DATOS DEL CCLV  (SCL Sistema de compensacion y liquidacion de operaciones)
		-- El SCL es quien indica si la operacion el bilateral o multilateral
		-- El servicio [20038 - PLUGIN_TES.jar@cl.bcs.tesoreria.cloper.ActualizarTransaccion]  Actualiza la camara y la marca bilateral de las transacciones
		-- Este servicio es invocado desde el botón MARCAR de la UI
		---------------------------------------------------------------------------------------
		

		------------------------------------------------------------------------------------
		-- OBTIENE TRANSACCIONES BILATERALES (todos los mercados, mas adelante se filtran)
		------------------------------------------------------------------------------------
		v_TransaccionesBilaterales = 		
		SELECT
		     TRA."IdTransaccion"
			,TRA."IdClasificacionTipoInstrumento"   -- RV, RF, IF, DV, FM
			,TRA."FechaSistemaCreacion"	            -- Fecha de ingreso del palo(esta fecha es igual a FechaOperacion pero sin hora)
			,TRA."FolioTransaccion" 			      
		    ,TRA."IdBolsa"   					    -- BCS, BEC, BCV
			,TRA."CantidadTransaccion"
			,TRA."PrecioTransaccion"
			,TRA."MontoTransaccion"				    
		    ,TRA."AccionTipoOperacion"    		    -- COMPRA / VENTA 
		    ,TRA."IdCondicionDeLiquidacion"    	    -- T0, T1, T2, T3
		    ,TRA."CodigoCondicionDeLiquidacion"     -- PH, PM, CN, T3
			,TRA."InstrumentoUI"         	AS "Nemotecnico"    -- Para simultanea viene el nombre del instrumento subyacente 
			,TRA."IdCorredorContraparte"			                                    
			,TRA."IdTipoInstrumento"     -- ACN /ACX /CFI / CFM / SIM    			
			,TRA."NombreTipoInstrumento"			
			,TRA."IdMonedaTransaccion" 
			,COALESCE(COR."DocumentoIdentidad",'') 			AS "DocumentoIdentidadCorredoraContraparte"
			,COALESCE(COR."CodigoInscripcionRegulador",'') 	AS "CodigoInscripcionCorredoraContraparte"
			,COALESCE(COR."NombreCorto",'') 				AS "NombreCortoCorredoraContraparte"
			,COALESCE(COR."NombreCompleto",'') 				AS "NombreCompletoCorredoraContraparte"
			,COALESCE(COR."IdPersona",0) 					AS "IdPersonaCorredoraContraparte"
			,TRA."Intereses"
			
			,TRA."IdComprobanteTesoreria"
			,TRA."NombreTipoOperacion"
			,TRA."MontoLiquidacionCorresponsal"        -- Para transacciones extranjeras(OUT) y MILA
			,TRA."IdMonedaLiquidacionCorresponsal"     -- Para transacciones extranjeras(OUT) y MILA
			
		FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS COR ON (COR."IdCorredora" = "TRA"."IdCorredorContraparte") -- Corredora contraparte
		WHERE TRA."EsBilateral" = 'S'     -- Solo transacciones bilaterales
		AND  (TRA."FechaLiquidacion" = :p_FechaLiquidacion)  -- Vencimiento para simultáneas
		AND  TRA."IdEstadoTransaccion"  <> 'ANU'  	-- Que no esté anulada
		AND   TRA."BloqueoLiquidacion" = 'N' 		-- Que no esté bloqueada
		AND   TRA."IndicadorOrdenDirecta" = ''    	-- Que no sea OD, OE(OD con operador externo)
		AND   TRA."IdClasificacionTipoInstrumento" 	= (CASE WHEN COALESCE(:p_ClasificacionTipoInstrumento,'') = '' THEN TRA."IdClasificacionTipoInstrumento" ELSE :p_ClasificacionTipoInstrumento END)
		AND   TRA."IdTipoInstrumento" 				= (CASE WHEN COALESCE(:p_TipoInstrumento,'') = '' THEN TRA."IdTipoInstrumento" ELSE :p_TipoInstrumento END)
		AND   TRA."IdCondicionDeLiquidacion" 		= (CASE WHEN COALESCE(:p_CondicionDeLiquidacion,'') = '' THEN TRA."IdCondicionDeLiquidacion" ELSE :p_CondicionDeLiquidacion END)
		AND   TRA."IdEmpresa" = :p_IdEmpresa;
		-- SELECT 'TransaccionesBilaterales', * FROM :v_TransaccionesBilaterales; -- REVISION


		
		
		-------------------------------------------------
		-- FILTRAR PALOS RV (Normales y Simultaneas)
		-------------------------------------------------
		v_LiquidacionBilateralDetalle =
			  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
			  SELECT
			         0 AS "IdAgrupacion"   -- Se asigna al final
			  		,TRA."IdTransaccion"
					,TRA."DocumentoIdentidadCorredoraContraparte"
					,TRA."NombreCortoCorredoraContraparte"			
					,TRA."NombreCompletoCorredoraContraparte"			
					,TRA."CodigoInscripcionCorredoraContraparte"		
					,TRA."IdPersonaCorredoraContraparte"
					,TRA."IdBolsa"
					,TRA."IdClasificacionTipoInstrumento"  -- RV, RF, CN, T3
					,TRA."IdTipoInstrumento"
					,TRA."FechaSistemaCreacion"
					,TRA."Nemotecnico"
					,TRA."FolioTransaccion"
					,TRA."CantidadTransaccion"
					,TRA."PrecioTransaccion"
					,TRA."IdCondicionDeLiquidacion"    	 -- T0, T1, T2, T3
	    			,TRA."CodigoCondicionDeLiquidacion"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoCompra"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoVenta"
					,TRA."MontoTransaccion"
					,TRA."Intereses"
					,TRA."MontoTransaccion" AS "MontoLiquidacion" --> Para RV se liquida el monto transaccion
					,TRA."AccionTipoOperacion"     -- COMPRA / VENTA
					,TRA."IdMonedaTransaccion"
					,TRA."IdMonedaTransaccion" AS "IdMonedaLiquidacion" 
					,TRA."IdComprobanteTesoreria"	
					,TRA."NombreTipoOperacion"												
				FROM :v_TransaccionesBilaterales AS TRA
				WHERE TRA."IdClasificacionTipoInstrumento" = 'RV'
				AND   TRA."IdCondicionDeLiquidacion" IN ('T0', 'T1')  -- RV (CN) se liquidan por Compensacion(clearing financiero) 
				AND   TRA."IdBolsa" = 'BCS';  -- No incluye transacciones Extranjeras(OUT) ni MILA
				

		-------------------------------------------------
		-- FILTRAR PALOS RF
		-------------------------------------------------		
		v_LiquidacionBilateralDetalle =
			  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
			  SELECT
			         0 AS "IdAgrupacion"   -- Se asigna al final
			  		,TRA."IdTransaccion"
					,TRA."DocumentoIdentidadCorredoraContraparte"
					,TRA."NombreCortoCorredoraContraparte"			
					,TRA."NombreCompletoCorredoraContraparte"
					,TRA."CodigoInscripcionCorredoraContraparte"		
					,TRA."IdPersonaCorredoraContraparte"
					,TRA."IdBolsa"
					,TRA."IdClasificacionTipoInstrumento"  -- RV, RF, CN, T3
					,TRA."IdTipoInstrumento"
					,TRA."FechaSistemaCreacion"
					,TRA."Nemotecnico"
					,TRA."FolioTransaccion"
					,TRA."CantidadTransaccion"
					,TRA."PrecioTransaccion"
					,TRA."IdCondicionDeLiquidacion"    	 -- T0, T1, T2, T3
	    			,TRA."CodigoCondicionDeLiquidacion"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoCompra"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoVenta"
					,TRA."MontoTransaccion"
					,TRA."Intereses"
					,TRA."MontoTransaccion" AS "MontoLiquidacion" --> Para RF se liquida el monto transaccion
					,TRA."AccionTipoOperacion"     -- COMPRA / VENTA					
					,TRA."IdMonedaTransaccion"
					,TRA."IdMonedaTransaccion" AS "IdMonedaLiquidacion"					
					,TRA."IdComprobanteTesoreria"	
					,TRA."NombreTipoOperacion"						
				FROM :v_TransaccionesBilaterales AS TRA
				WHERE   ((TRA."IdClasificacionTipoInstrumento" = 'RF' AND TRA."IdCondicionDeLiquidacion" = 'T2' AND TRA."IdMonedaTransaccion" = 'CLP')
				      OR (TRA."IdClasificacionTipoInstrumento" = 'RF' AND TRA."IdMonedaTransaccion" = 'USD'))
				AND   TRA."IdBolsa" = 'BCS'; -- No incluye transacciones Extranjeras(OUT) ni MILA
		
				
		-------------------------------------------------
		-- FILTRAR PALOS IF
		-------------------------------------------------
		v_LiquidacionBilateralDetalle =
			  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
			  SELECT
			         0 AS "IdAgrupacion"   -- Se asigna al final
			  		,TRA."IdTransaccion"
					,TRA."DocumentoIdentidadCorredoraContraparte"
					,TRA."NombreCortoCorredoraContraparte"			
					,TRA."NombreCompletoCorredoraContraparte"
					,TRA."CodigoInscripcionCorredoraContraparte"		
					,TRA."IdPersonaCorredoraContraparte"
					,TRA."IdBolsa"
					,TRA."IdClasificacionTipoInstrumento"  -- RV, RF, CN, T3
					,TRA."IdTipoInstrumento"
					,TRA."FechaSistemaCreacion"
					,TRA."Nemotecnico"
					,TRA."FolioTransaccion"
					,TRA."CantidadTransaccion"
					,TRA."PrecioTransaccion"
					,TRA."IdCondicionDeLiquidacion"    	 -- T0, T1, T2, T3
	    			,TRA."CodigoCondicionDeLiquidacion"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoCompra"
					,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoVenta"
					,TRA."MontoTransaccion"
					,TRA."Intereses"
					,TRA."MontoTransaccion" AS "MontoLiquidacion" --> Para IF se liquida el monto transaccion
					,TRA."AccionTipoOperacion"     -- COMPRA / VENTA
					,TRA."IdMonedaTransaccion"
					,TRA."IdMonedaTransaccion" AS "IdMonedaLiquidacion"
					,TRA."IdComprobanteTesoreria"	
					,TRA."NombreTipoOperacion"						
				FROM :v_TransaccionesBilaterales AS TRA
				WHERE ((TRA."IdClasificacionTipoInstrumento" = 'IF' AND TRA."IdCondicionDeLiquidacion" = 'T2' AND TRA."IdMonedaTransaccion" = 'CLP')				
				   OR (TRA."IdClasificacionTipoInstrumento" = 'IF' AND TRA."IdMonedaTransaccion" = 'USD'))
				AND   TRA."IdBolsa" = 'BCS'; -- No incluye transacciones Extranjeras(OUT) ni MILA



		---------------------------------------------------------------------------
		-- MILA --> Todo MILA(Mercado integreso Latinoamericano) es bilateral
		--      --> Toma el monto liquidación corresponsal extranjero
		---------------------------------------------------------------------------

		  v_LiquidacionBilateralDetalle =
		  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
		  SELECT
		         0 AS "IdAgrupacion"   -- Se asigna al final
		  		,TRA."IdTransaccion"
				,TRA."DocumentoIdentidadCorredoraContraparte"
				,TRA."NombreCortoCorredoraContraparte"			
				,TRA."NombreCompletoCorredoraContraparte"			
				,TRA."CodigoInscripcionCorredoraContraparte"		
				,TRA."IdPersonaCorredoraContraparte"
				,TRA."IdBolsa"
				,TRA."IdClasificacionTipoInstrumento"  -- RV, RF, CN, T3
				,TRA."IdTipoInstrumento"
				,TRA."FechaSistemaCreacion"
				,TRA."Nemotecnico"
				,TRA."FolioTransaccion"
				,TRA."CantidadTransaccion"
				,TRA."PrecioTransaccion"
				,TRA."IdCondicionDeLiquidacion"    	 -- T0, T1, T2, T3
    			,TRA."CodigoCondicionDeLiquidacion"
				,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoCompra"
				,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoVenta"
				,TRA."MontoTransaccion"
				,TRA."Intereses"
				,TRA."MontoLiquidacionCorresponsal" AS "MontoLiquidacion" --> Para MILA se liquida monto liquidacion corresponsal
				,TRA."AccionTipoOperacion"     -- COMPRA / VENTA				
				,TRA."IdMonedaTransaccion"
				,TRA."IdMonedaLiquidacionCorresponsal" AS "IdMonedaLiquidacion"    -- Para MILA se usa MonedaLiquidacionCorresponsal
				,TRA."IdComprobanteTesoreria"	
				,TRA."NombreTipoOperacion"												
			FROM :v_TransaccionesBilaterales AS TRA
			WHERE   "IdBolsa" IN ('BVL','BVC','BMV'); -- BOLSA DE LIMA, COLOMBIA Y MEXICO
			
			
			
		---------------------------------------------------------------------------
		-- BOLSAS EXTRANJERAS --> Todas son bilateral
		--                    --> Toma el monto liquidación corresponsal extranjero
		---------------------------------------------------------------------------

		  v_LiquidacionBilateralDetalle =
		  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
		  SELECT
		         0 AS "IdAgrupacion"   -- Se asigna al final
		  		,TRA."IdTransaccion"
				,TRA."DocumentoIdentidadCorredoraContraparte"
				,TRA."NombreCortoCorredoraContraparte"			
				,TRA."NombreCompletoCorredoraContraparte"			
				,TRA."CodigoInscripcionCorredoraContraparte"		
				,TRA."IdPersonaCorredoraContraparte"
				,TRA."IdBolsa"
				,TRA."IdClasificacionTipoInstrumento"  -- RV, RF, CN, T3
				,TRA."IdTipoInstrumento"
				,TRA."FechaSistemaCreacion"
				,TRA."Nemotecnico"
				,TRA."FolioTransaccion"
				,TRA."CantidadTransaccion"
				,TRA."PrecioTransaccion"
				,TRA."IdCondicionDeLiquidacion"    	 -- T0, T1, T2, T3
    			,TRA."CodigoCondicionDeLiquidacion"
				,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoCompra"
				,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoVenta"
				,TRA."MontoTransaccion"
				,TRA."Intereses"
				,TRA."MontoLiquidacionCorresponsal" AS "MontoLiquidacion" --> Para BOLSAS EXTRANJERAS se liquida monto liquidacion corresponsal
				,TRA."AccionTipoOperacion"     -- COMPRA / VENTA
				,TRA."IdMonedaTransaccion"
				,TRA."IdMonedaLiquidacionCorresponsal" AS "IdMonedaLiquidacion"    -- Para MILA se usa MonedaLiquidacionCorresponsal
				,TRA."IdComprobanteTesoreria"	
				,TRA."NombreTipoOperacion"												
			FROM :v_TransaccionesBilaterales AS TRA
			WHERE   "IdBolsa" = 'OUT'; -- OUT BOLSAS EXTRANJERAS			
			
			
		------------------------------------------------------------------------
		-- AGREGA TRANSACCIONES CON MARCA BILATERAL NO CONSIDERAS EN FILTROS ANTERIORES
		-- La marca bilateral es mandane sobre las demás condiciones(Ej. si la transaccion cumple todas las condiciones para ser compensada, pero tiene la marca bilateral, se liquida como bilateral
		------------------------------------------------------------------------
	
		  v_LiquidacionBilateralDetalle =
		  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL		  
		  SELECT
		         0 AS "IdAgrupacion"   -- Se asigna al final
		  		,TRA."IdTransaccion"
				,TRA."DocumentoIdentidadCorredoraContraparte"
				,TRA."NombreCortoCorredoraContraparte"			
				,TRA."NombreCompletoCorredoraContraparte"			
				,TRA."CodigoInscripcionCorredoraContraparte"		
				,TRA."IdPersonaCorredoraContraparte"
				,TRA."IdBolsa"
				,TRA."IdClasificacionTipoInstrumento"  -- RV, RF, CN, T3
				,TRA."IdTipoInstrumento"
				,TRA."FechaSistemaCreacion"
				,TRA."Nemotecnico"
				,TRA."FolioTransaccion"
				,TRA."CantidadTransaccion"
				,TRA."PrecioTransaccion"
				,TRA."IdCondicionDeLiquidacion"    	 -- T0, T1, T2, T3
    			,TRA."CodigoCondicionDeLiquidacion"
				,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoCompra"
				,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoTransaccion" ELSE 0 END) AS "MontoVenta"
				,TRA."MontoTransaccion"
				,TRA."Intereses"
				,TRA."MontoTransaccion" AS "MontoLiquidacion" --> Para RV se liquida el monto transaccion
				,TRA."AccionTipoOperacion"     -- COMPRA / VENTA
				,TRA."IdMonedaTransaccion"
				,TRA."IdMonedaTransaccion" AS "IdMonedaLiquidacion" 
				,TRA."IdComprobanteTesoreria"
				,TRA."NombreTipoOperacion"
				
			FROM :v_TransaccionesBilaterales AS TRA
			WHERE  TRA."IdTransaccion" NOT IN (SELECT "IdTransaccion" FROM :v_LiquidacionBilateralDetalle);	

		
			
		-------------------------------------------------------------
		-- OBTIENE SALIDAS PARA DETALLE Y RESUMEN LIQUIDACION BILATERAL
		-------------------------------------------------------------

		-- Obtiene salida para Resumen Liquidación Bilateral
		p_out_LiquidacionBilateralResumen =
		SELECT
		        ROW_NUMBER() over (Order by    -- IdAgrupacion, se usa para generación de comprobantes y marca de transacciones con folios comprobantes(en procesar)  
								 "IdBolsa"
								,"IdPersonaCorredoraContraparte"				
								,"IdClasificacionTipoInstrumento"
								,"IdTipoInstrumento"
								,"IdMonedaLiquidacion"
								,"IdCondicionDeLiquidacion"
								,"AccionTipoOperacion"
								,"IdComprobanteTesoreria") as "IdAgrupacion"

		 		,"IdBolsa"
				,"IdPersonaCorredoraContraparte"
				,MAX("DocumentoIdentidadCorredoraContraparte") AS "DocumentoIdentidadCorredoraContraparte"
				,MAX("NombreCortoCorredoraContraparte") AS "NombreCortoCorredoraContraparte"
				,MAX("NombreCompletoCorredoraContraparte") AS "NombreCompletoCorredoraContraparte"
				,MAX("CodigoInscripcionCorredoraContraparte") AS "CodigoInscripcionCorredoraContraparte"
				,"IdMonedaLiquidacion"
				,"IdCondicionDeLiquidacion"
				,MAX("CodigoCondicionDeLiquidacion") AS "CodigoCondicionDeLiquidacion"  -- Está agrupado por IdCondicionDeLiquidacion
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento"				
				,"AccionTipoOperacion"
				,"IdComprobanteTesoreria"
				,SUM("MontoCompra") AS "TotalMontoCompra"
				,SUM("MontoVenta")  AS "TotalMontoVenta"
		FROM :v_LiquidacionBilateralDetalle
		GROUP BY
				 "IdBolsa"
				,"IdPersonaCorredoraContraparte"				
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento"
				,"IdMonedaLiquidacion"
				,"IdCondicionDeLiquidacion"
				,"AccionTipoOperacion"
				,"IdComprobanteTesoreria";


		-- Obtiene salida para Detalle Liquidación Bilateral (Ligada al resumen por IdAgrupacion)
		p_out_LiquidacionBilateralDetalle =
		SELECT
			 LBR."IdAgrupacion"
		    ,LBD."IdTransaccion"
			,LBD."DocumentoIdentidadCorredoraContraparte"
			,LBD."NombreCortoCorredoraContraparte"
			,LBD."NombreCompletoCorredoraContraparte"
			,LBD."CodigoInscripcionCorredoraContraparte"
			,LBD."IdPersonaCorredoraContraparte"						
			,LBD."IdBolsa"
			,LBD."IdClasificacionTipoInstrumento"
			,LBD."IdTipoInstrumento"		     
			,LBD."FechaSistemaCreacion"
			,LBD."Nemotecnico"					
			,LBD."FolioTransaccion"					
			,LBD."CantidadTransaccion"					
			,LBD."PrecioTransaccion"					
			,LBD."IdCondicionDeLiquidacion"    	 
			,LBD."CodigoCondicionDeLiquidacion"	    			
			,LBD."MontoCompra"
			,LBD."MontoVenta"	
			,LBD."MontoTransaccion"
			,LBD."Intereses"
			,LBD."MontoLiquidacion"
			,LBD."AccionTipoOperacion"           
			,LBD."IdMonedaTransaccion"
			,LBD."IdMonedaLiquidacion"
			,LBD."IdComprobanteTesoreria"
			,LBD."NombreTipoOperacion"
		FROM :v_LiquidacionBilateralDetalle AS LBD
		INNER JOIN :p_out_LiquidacionBilateralResumen AS LBR ON (      -- Para obtener Id de agrupacion
				    LBR."IdBolsa" = LBR."IdBolsa"
				AND LBR."IdPersonaCorredoraContraparte" = LBD."IdPersonaCorredoraContraparte"
				AND LBR."IdMonedaLiquidacion" 			= LBD."IdMonedaLiquidacion"
				AND LBR."IdCondicionDeLiquidacion" 		= LBD."IdCondicionDeLiquidacion"
				AND LBR."IdClasificacionTipoInstrumento" = LBD."IdClasificacionTipoInstrumento"
				AND LBR."IdTipoInstrumento" 			= LBD."IdTipoInstrumento"
				AND LBR."AccionTipoOperacion" 			= LBD."AccionTipoOperacion"
				AND LBR."IdComprobanteTesoreria"        = LBD."IdComprobanteTesoreria"
		)
		WHERE (LBR."IdAgrupacion" = :p_IdAgrupacion OR :p_IdAgrupacion = 0);
		
	
	END;


END;
GO