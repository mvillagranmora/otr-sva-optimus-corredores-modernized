SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateralPE";

--  Proposito: Consultar y filtrar transacciones bilaterales para liquidacion de contrapartes para PERU
-- 		RV : No existen bilaterales en RV para PERU
-- 		RF c/marca bilateral: El monto a liquidar es monto transacción + intereses    
--      RF s/marca bilateral: El monto a liquidar son solo los intereses
-- 		MILA: Todo MILA(Mercado integreso Latinoamericano) es bilateral. Toma monto liquidación corresponsal y moneda liquidación corresponsal extranjero
-- 		BOLSAS EXTRANJERAS(OUT): Todas son bilateral. Toma monto liquidación corresponsal y moneda liquidación corresponsal extranjero
--  Autor: JVILLAGRAN - BCS 
--  Fecha Creacion: 21/12/2017
-- 
GO
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateralPE"
  (
  
    IN p_IdEmpresa             				BIGINT,
    IN p_IdUsuario              			BIGINT,
    IN p_IdModulo               			NVARCHAR(3),
    
    IN p_FechaLiquidacion			    	NVARCHAR(10),
    IN p_ClasificacionTipoInstrumento  		NVARCHAR(3)  DEFAULT '', -- RV,RF,IF
    IN p_TipoInstrumento               		NVARCHAR(3)  DEFAULT '', -- ACN
    IN p_CondicionDeLiquidacion        		NVARCHAR(2)  DEFAULT '', -- T0,T1,T2,T3    
	IN p_IdAgrupacion						BIGINT       DEFAULT 0,  -- Para consultar un detalle(grupo) especifico

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
												,"MontoLiquidacion"    						DECIMAL(26,8) -- RF: Monto+Intereses ó Intereses     MILA/OUT: Monto liquidacion Corresponsal												
												,"AccionTipoOperacion"      				NVARCHAR(40)												
												,"IdMonedaTransaccion"						NVARCHAR(3)												
												,"IdMonedaLiquidacion"						NVARCHAR(3)   -- RF: MonedaTransaccion     MILA/OUT: IdMonedaLiquidacionCorresponsal												
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
												,"IdMonedaLiquidacion"						NVARCHAR(3) -- RF: MonedaTransaccion     MILA/OUT: IdMonedaLiquidacionCorresponsal
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
												,"Intereses"								DECIMAL(26,8)
												,"MontoLiquidacion"    						DECIMAL(26,8)-- RF: Monto+Intereses ó Intereses     MILA/OUT: Monto liquidacion Corresponsal
												,"AccionTipoOperacion"      				NVARCHAR(40)												
												,"IdMonedaTransaccion"						NVARCHAR(3)
												,"IdMonedaLiquidacion" 						NVARCHAR(3) -- RF: MonedaTransaccion     MILA/OUT: IdMonedaLiquidacionCorresponsal
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
		   '' as "CodigoCondicionDeLiquidacion", 0 as "MontoCompra", 0 as "MontoVenta", 0 as "MontoTransaccion", 0 as "Intereses" ,0 AS "MontoLiquidacion",
		   '' as "AccionTipoOperacion", '' as "IdMonedaTransaccion",'' AS "IdMonedaLiquidacion", 0 as "IdComprobanteTesoreria", '' as "NombreTipoOperacion"
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

		

		------------------------------------------------------------------------------------
		-- OBTIENE TRANSACCIONES BILATERALES (todos los mercados, mas adelante se filtran)
		------------------------------------------------------------------------------------
		 
		v_TransaccionesBilaterales = 		
		SELECT
		     TRA."IdTransaccion"
			,TRA."IdClasificacionTipoInstrumento"    -- RV, RF, IF, DV, FM
			,TRA."FechaSistemaCreacion"	             -- Fecha de ingreso del palo(esta fecha es igual a FechaOperacion pero sin hora)
			,TRA."FolioTransaccion" 			      
		    ,TRA."IdBolsa"   					     -- BCS, BEC, BCV
			,TRA."CantidadTransaccion"
			,TRA."PrecioTransaccion"
			,TRA."MontoTransaccion"				    
		    ,TRA."AccionTipoOperacion"    		     -- COMPRA / VENTA 
		    ,TRA."IdCondicionDeLiquidacion"    	     -- T0, T1, T2, T3
		    ,TRA."CodigoCondicionDeLiquidacion"      -- PH, PM, CN, T3
			,TRA."InstrumentoUI"  	AS "Nemotecnico" -- Para simultanea viene el nombre del instrumento subyacente 
			,TRA."IdCorredorContraparte"			                                    
			,TRA."IdTipoInstrumento"     -- ACN /ACX /CFI / CFM / SIM    			
			,TRA."NombreTipoInstrumento"			
			,TRA."IdMonedaTransaccion" 
			,COALESCE(CNT."DocumentoIdentidad",'') 			AS "DocumentoIdentidadCorredoraContraparte"
			,COALESCE(CNT."CodigoInscripcionRegulador",'') 	AS "CodigoInscripcionCorredoraContraparte"
			,COALESCE(CNT."NombreCorto",'') 				AS "NombreCortoCorredoraContraparte"
			,COALESCE(CNT."NombreCompleto",'') 				AS "NombreCompletoCorredoraContraparte"
			,COALESCE(CNT."IdPersona",0) 					AS "IdPersonaCorredoraContraparte"			
			,TRA."IdComprobanteTesoreria"
			,TRA."NombreTipoOperacion"
			,TRA."MontoLiquidacionCorresponsal"        -- Para transacciones extranjeras(OUT) y MILA			
			,TRA."IdMonedaLiquidacionCorresponsal"     -- Para transacciones extranjeras(OUT) y MILA			
			,TRA."Intereses"
			,TRA."EsBilateral"
			
			-- Datos corredor corresponsal (Para transacción MILA o de Bolsa extranjera)
			,COALESCE(COR."IdPersona",0) 					AS "IdPersonaCorredorCorresponsal"
			,COALESCE(COR."NombreCompleto",'') 				AS "NombreCompletoCorredorCorresponsal"
			,COALESCE(COR."DocumentoIdentidad",'') 			AS "DocumentoIdentidadCorredorCorresponsal"			
			,COALESCE(COR."CodigoCorredor",'') 				AS "NombreCortoCorredorCorresponsal"
		
		FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS CNT ON (CNT."IdCorredora" = TRA."IdCorredorContraparte")   -- Datos corredor contraparte
		LEFT OUTER JOIN "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal" AS COR ON (COR."IdCorredorCorresponsal" = TRA."IdCorredorCorresponsal")  -- Para transacción MILA o de Bolsa extranjera 
		
		WHERE (TRA."EsBilateral" = 'S' OR (TRA."EsBilateral" = 'N' AND TRA."Intereses" <> 0))  -- OPTIMUSPE-837: Incluir transacciones sin marca bilateral que tengan intereses
		AND  (TRA."FechaLiquidacion" = :p_FechaLiquidacion OR TRA."FechaVencimiento" = :p_FechaLiquidacion)  -- Vencimiento para simultáneas 
		AND   TRA."IdEstadoTransaccion"  <> 'ANU' 	-- Que no esté anulada
		AND   TRA."BloqueoLiquidacion" = 'N' 		-- Que no esté bloqueada		
		--AND   TRA."IndicadorOrdenDirecta" = ''    -- Que no sea OD, OE(OD con operador externo) -- OPTIMUSPE-846 solicitó que las OD deben ir en liquidacion bilateral, si tiene marca bilateral		
		AND   TRA."IdClasificacionTipoInstrumento" = (CASE WHEN COALESCE(:p_ClasificacionTipoInstrumento,'') = '' THEN TRA."IdClasificacionTipoInstrumento" ELSE :p_ClasificacionTipoInstrumento END)
		AND   TRA."IdTipoInstrumento" = (CASE WHEN COALESCE(:p_TipoInstrumento,'') = '' THEN TRA."IdTipoInstrumento" ELSE :p_TipoInstrumento END)
		AND   TRA."IdCondicionDeLiquidacion" = (CASE WHEN COALESCE(:p_CondicionDeLiquidacion,'') = '' THEN TRA."IdCondicionDeLiquidacion" ELSE :p_CondicionDeLiquidacion END)
		AND   TRA."IdEmpresa" = :p_IdEmpresa;
    
    	-- SELECT 'TransaccionesBilaterales', * FROM :v_TransaccionesBilaterales; -- REVISION


		
		-----------------------------------------------
		-- RV --> No existen bilaterales en RV Peru
		-----------------------------------------------

		
		-------------------------------------------------------------
		-- RF --> Si transacción tiene marca bilateral   --> El monto a liquidar es monto transacción + intereses    
		--    --> Si transacción NO tiene marca bilateral--> El monto a liquidar son solo los intereses
		-------------------------------------------------------------
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
    			--> RF c/marca bilateral:  intereses + monto transacción --> RF s/marca bilateral:  intereses   -- OPTIMUSPE-837: Incluir transacciones sin marca bilateral que tengan intereses
				,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN (CASE WHEN (TRA."EsBilateral" = 'S') THEN TRA."MontoTransaccion" + TRA."Intereses" WHEN (TRA."EsBilateral" = 'N' AND TRA."Intereses" <> 0) THEN TRA."Intereses" END) ELSE 0 END) AS "MontoCompra"  -- RF Monto a Liquidar = intereses + monto transacción
				,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN (CASE WHEN (TRA."EsBilateral" = 'S') THEN TRA."MontoTransaccion" + TRA."Intereses" WHEN (TRA."EsBilateral" = 'N' AND TRA."Intereses" <> 0) THEN TRA."Intereses" END) ELSE 0 END) AS "MontoVenta"   -- RF Monto a Liquidar = intereses + monto transacción
				,TRA."MontoTransaccion"
				,TRA."Intereses"
				
				,CASE 
					WHEN (TRA."EsBilateral" = 'S') THEN TRA."MontoTransaccion" + TRA."Intereses"   --> RF c/marca bilateral:  Monto a Liquidar = intereses + monto transacción
					WHEN (TRA."EsBilateral" = 'N' AND TRA."Intereses" <> 0) THEN TRA."Intereses"   --> RF s/marca bilateral:  Monto a Liquidar = intereses   -- OPTIMUSPE-837: Incluir transacciones sin marca bilateral que tengan intereses
					ELSE 0  -- este caso no debe darse nunca
				END AS "MontoLiquidacion"
				
				,TRA."AccionTipoOperacion"     -- COMPRA / VENTA
				,TRA."IdMonedaTransaccion"
				,TRA."IdMonedaTransaccion" AS "IdMonedaLiquidacion"  -- Para RF la moneda de liquidación es la moneda del instrumento
				,TRA."IdComprobanteTesoreria"	
				,TRA."NombreTipoOperacion"												
			FROM :v_TransaccionesBilaterales AS TRA
			WHERE "IdClasificacionTipoInstrumento" = 'RF'
			AND   "IdBolsa" = 'BVL';  -- BOLSA DE VALORES DE LIMA

			
			
			

		---------------------------------------------------------------------------
		-- MILA --> Todo MILA(Mercado integreso Latinoamericano) es bilateral
		--      --> Toma el monto liquidación corresponsal extranjero y moneda liquidación corresponsal extranjero
		---------------------------------------------------------------------------

		  v_LiquidacionBilateralDetalle =
		  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
		  SELECT
		         0 AS "IdAgrupacion"   -- Se asigna al final
		  		,TRA."IdTransaccion"
		  		
			    -- MILA usa como contraparte al corredor corresponsal	
				,"DocumentoIdentidadCorredorCorresponsal" AS "DocumentoIdentidadCorredoraContraparte"
				,"NombreCortoCorredorCorresponsal" AS "NombreCortoCorredoraContraparte"
				,"NombreCompletoCorredorCorresponsal" AS "NombreCompletoCorredoraContraparte"
				,'0' AS "CodigoInscripcionCorredoraContraparte"
				,"IdPersonaCorredorCorresponsal" AS "IdPersonaCorredoraContraparte"
			
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
				,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoCompra"  -- Para MILA se liquida monto liquidacion corresponsal
				,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoVenta"   -- Para MILA se liquida monto liquidacion corresponsal
				,TRA."MontoTransaccion"
				,TRA."Intereses"
				,TRA."MontoLiquidacionCorresponsal" AS "MontoLiquidacion" --> Para MILA se liquida monto liquidacion corresponsal
				,TRA."AccionTipoOperacion"     -- COMPRA / VENTA							
				,TRA."IdMonedaTransaccion"
				,TRA."IdMonedaLiquidacionCorresponsal" AS "IdMonedaLiquidacion"    -- Para MILA la moneda de liquidación es MonedaLiquidacionCorresponsal
				,TRA."IdComprobanteTesoreria"	
				,TRA."NombreTipoOperacion"												
			FROM :v_TransaccionesBilaterales AS TRA
			WHERE   "IdBolsa" IN ('BCS','BVC','BMV'); -- BOLSA DE COMERCIO DE SANTIAGO, COLOMBIA Y MEXICO
			

			



		
		---------------------------------------------------------------------------
		-- BOLSAS EXTRANJERAS --> Todas son bilateral
		--                    --> Toma el monto liquidación corresponsal extranjero y moneda liquidación corresponsal extranjero
		---------------------------------------------------------------------------

		  v_LiquidacionBilateralDetalle =
		  SELECT * FROM :v_LiquidacionBilateralDetalle  UNION ALL
		  SELECT
		         0 AS "IdAgrupacion"   -- Se asigna al final
		  		,TRA."IdTransaccion"
		  		
			    -- MILA usa como contraparte al corredor corresponsal	
				,"DocumentoIdentidadCorredorCorresponsal" AS "DocumentoIdentidadCorredoraContraparte"
				,"NombreCortoCorredorCorresponsal" AS "NombreCortoCorredoraContraparte"
				,"NombreCompletoCorredorCorresponsal" AS "NombreCompletoCorredoraContraparte"
				,'0' AS "CodigoInscripcionCorredoraContraparte"
				,"IdPersonaCorredorCorresponsal" AS "IdPersonaCorredoraContraparte"
				
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
				,(CASE WHEN TRA."AccionTipoOperacion" = 'COMPRA' THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoCompra"  -- Para BOLSAS EXTRANJERAS(OUT) se liquida monto liquidacion corresponsal
				,(CASE WHEN TRA."AccionTipoOperacion" = 'VENTA'  THEN TRA."MontoLiquidacionCorresponsal" ELSE 0 END) AS "MontoVenta"   -- Para BOLSAS EXTRANJERAS(OUT) se liquida monto liquidacion corresponsal
				,TRA."MontoTransaccion"
				,TRA."Intereses"
				,TRA."MontoLiquidacionCorresponsal" AS "MontoLiquidacion" --> Para BOLSAS EXTRANJERAS(OUT) se liquida monto liquidacion corresponsal
				,TRA."AccionTipoOperacion"     -- COMPRA / VENTA
				,TRA."IdMonedaTransaccion"
				,TRA."IdMonedaLiquidacionCorresponsal" AS "IdMonedaLiquidacion"    -- Para BOLSAS EXTRANJERAS(OUT) la moneda de liquidación es MonedaLiquidacionCorresponsal
				,TRA."IdComprobanteTesoreria"
				,TRA."NombreTipoOperacion"
			FROM :v_TransaccionesBilaterales AS TRA
			WHERE   "IdBolsa" = 'OUT'; -- OUT BOLSAS EXTRANJERAS







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
				AND LBR."IdPersonaCorredoraContraparte"  = LBD."IdPersonaCorredoraContraparte"
				AND LBR."IdMonedaLiquidacion" 			 = LBD."IdMonedaLiquidacion"
				AND LBR."IdCondicionDeLiquidacion" 		 = LBD."IdCondicionDeLiquidacion"
				AND LBR."IdClasificacionTipoInstrumento" = LBD."IdClasificacionTipoInstrumento"
				AND LBR."IdTipoInstrumento" 			 = LBD."IdTipoInstrumento"
				AND LBR."AccionTipoOperacion" 			 = LBD."AccionTipoOperacion"
				AND LBR."IdComprobanteTesoreria"         = LBD."IdComprobanteTesoreria"
		)
		WHERE (LBR."IdAgrupacion" = :p_IdAgrupacion OR :p_IdAgrupacion = 0);
		
	
	END;


END;
GO