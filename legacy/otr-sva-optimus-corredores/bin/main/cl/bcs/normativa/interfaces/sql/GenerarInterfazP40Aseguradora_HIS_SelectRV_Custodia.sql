 	SELECT    
  	         TO_VARCHAR(CAR."FechaOperacion", 'YYYYMMDD') 		 		AS "FechaCompra"
	        ,RPAD(COALESCE(REPLACE(CAR."Nemotecnico",'Á','A'),''),14,' ')			AS "Nemotecnico"
	        ,CAR."IdTipoInstrumento"
	        ,'1'		AS "TipoCartera"      
			,COALESCE(CAR."INGCantidad", 0)								AS "NumeroInstrMant"
	        ,CASE WHEN CAR."IdMonedaInstrumento" = 'EUR' THEN '142'
	        	  WHEN CAR."IdMonedaInstrumento" IN ('USD', 'DO') THEN '994'	-- Dolar Observado
	        	  WHEN CAR."IdMonedaInstrumento" = 'IV'  THEN '997'
	        	  WHEN CAR."IdMonedaInstrumento" = 'UF'  THEN '998'
	        	  WHEN COALESCE(CAR."IdMonedaInstrumento",'CLP') IN ('CLP','NR')  THEN '999'
	         	  ELSE '880'
	         END 														AS "Moneda"			
			,COALESCE(CAR."INGPrecioCompra",0)							AS "PrecioCompra"	        
			,COALESCE(CAR."INGMontoATasaMercadoActual",0)				AS "ValorRazonable"
			,'1'														AS "CondicionInstrumento"
			,'00000000'												AS "FechaInicioCondicion"
			,'00000000'													AS "FechaFinCondicion"
	FROM   "BSCL.Custodia.BD::HIS.Cartera" AS CAR 
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	  AND CAR."IdEstado" 						= 'VIG'
	  AND CAR."IdClasificacionTipoInstrumento" 	= 'RV'	
	  AND CAR."IdTipoInstrumento" 				<> 'SIM'
	  AND (CAR."IdTipoInstrumento" 				= :Mercado OR :Mercado = '')
	  AND CAR."IdCliente"						= :IdCliente	
	  AND CAR."FechaRespaldo"  					= :Fecha