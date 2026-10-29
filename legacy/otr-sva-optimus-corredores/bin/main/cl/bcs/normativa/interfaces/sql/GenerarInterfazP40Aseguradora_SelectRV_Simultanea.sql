 	SELECT    
  	         TO_VARCHAR(CAR."FechaOperacion", 'YYYYMMDD') 		 		AS "FechaCompra"
			,RPAD(COALESCE(REPLACE(MovNormal."NemotecnicoSubyacente",'Á','A'),''),14,' ')			AS "Nemotecnico"
	        ,CAR."IdTipoInstrumento"
	        ,LPAD(COALESCE(CAR."TipoCarteraCorredor",'0'),1,'0')		AS "TipoCartera"      
			,COALESCE(CAR."INGCantidad", 0)								AS "NumeroInstrMant"
	        ,CASE WHEN CAR."IdMonedaInstrumento" = 'EUR' THEN '142'
	        	  WHEN CAR."IdMonedaInstrumento" IN ('USD', 'DO') THEN '994'	-- Dolar Observado
	        	  WHEN CAR."IdMonedaInstrumento" = 'IV'  THEN '997'
	        	  WHEN CAR."IdMonedaInstrumento" = 'UF'  THEN '998'
	        	  WHEN COALESCE(CAR."IdMonedaInstrumento",'CLP') IN ('CLP','NR')  THEN '999'
	         	  ELSE '880'
	         END 													AS "Moneda"			
			,COALESCE(CAR."INGPrecioAPlazo",0)							AS "PrecioCompra"	        
			,COALESCE(CAR."INGMontoATasaMercadoActual",0)				AS "ValorRazonable"
			,TO_VARCHAR(MovNormal."FechaLiquidacion"	, 'YYYYMMDD') 		AS "FechaInicioCondicion"
			,TO_VARCHAR(CAR."FechaVencimientoOperacion"	, 'YYYYMMDD') 		AS "FechaFinCondicion"
	FROM   "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
	 	MovNormal."IdEmpresa" = CAR."IdEmpresa" and 
	 	MovNormal."IdComprobante" = CAR."FolioComprobante"  
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	  AND CAR."IdEstado" 						= 'VIG'
	  AND CAR."IdClasificacionTipoInstrumento" 	= 'RV'
	  AND CAR."IdTipoInstrumento" 				= 'SIM'
	  AND CAR."IdCliente"						= :IdCliente
