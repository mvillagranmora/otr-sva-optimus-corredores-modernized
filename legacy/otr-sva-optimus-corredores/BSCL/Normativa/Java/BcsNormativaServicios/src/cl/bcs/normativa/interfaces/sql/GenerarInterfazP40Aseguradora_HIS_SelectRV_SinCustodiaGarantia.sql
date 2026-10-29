 	SELECT    
  	         TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD') 		 		AS "FechaCompra"
			,RPAD(COALESCE(REPLACE(G."Nemotecnico",'Á','A'),''),14,' ')			AS "Nemotecnico"
	        ,G."IdTipoInstrumento"
	        ,'1'														AS "TipoCartera"      
			,COALESCE(G."INGCantidad", 0)								AS "NumeroInstrMant"
			,CASE WHEN G."IdMonedaInstrumento" = 'EUR' THEN '142'
	        	  WHEN G."IdMonedaInstrumento" IN ('USD', 'DO') THEN '994'	-- Dolar Observado
	        	  WHEN G."IdMonedaInstrumento" = 'IV'  THEN '997'
	        	  WHEN G."IdMonedaInstrumento" = 'UF'  THEN '998'
	        	  WHEN COALESCE(G."IdMonedaInstrumento",'CLP') IN ('CLP','NR')  THEN '999'
	         	  ELSE '880'
	         END 														AS "Moneda"	
			,COALESCE(G."INGPrecioGarantia",0)							AS "PrecioCompra"	        
			,COALESCE(G."INGMontoATasaMercadoActual",0)					AS "ValorRazonable"
			,'3'														AS "CondicionInstrumento"
			,TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD') 		AS "FechaInicioCondicion"
			,TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD') 	AS "FechaFinCondicion"
	FROM    "BSCL.Custodia.BD::HIS.Garantia" G
	WHERE G."IdEmpresa"							= :IdEmpresa
	  AND G."IdEstado" 							= 'VIG'
	  AND G."IdClasificacionTipoInstrumento" 	= 'RV'
  	  AND (G."IdTipoInstrumento" 				= :Mercado OR :Mercado = '')
	  AND G."IdCliente"							= :IdCliente
	  AND G."FechaRespaldo"  					= :Fecha