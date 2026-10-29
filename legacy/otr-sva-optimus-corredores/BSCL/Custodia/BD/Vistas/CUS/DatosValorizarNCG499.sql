SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.DatosValorizarNCG499";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.DatosValorizarNCG499" AS

	SELECT
	"FuenteDatos",
	"IdValorizacion",
	"OrigenProcesamiento",
	"FechaValorizacion",
	
	"FechaOperacion",				
	"FechaVencimientoOperacion",
	"PlazoEnDiasOperacion",							
	
	"IdClasificacionTipoInstrumento", 		
	"IdTipoInstrumento",					
	"IdInstrumento",
	"Nemotecnico",
	"InsFechaVencimiento",
	"InsFechaEmision", 
	"IdMonedaInstrumento",
	"IdMonedaEmisionInstrumento",
	"IdTipoReajusteInstrumento",
	"Emisor",								
	"IdCondicionDeLiquidacion",				
	"FechaDeLiquidacion",
	"Cantidad",
	"TasaCompra",
	"PrecioCompra",
	--"ValorRescate",
	
	"PrecioSubyacente",  
	"TasaSubyacente",  	 
	"PrecioGarantia",  	
	"TasaGarantia",  	
	"TasaTIF",  		
	"IdPortafolio",		
	"TablaOrigen",				
	"IdTipoOperacion",
	"Situacion",
    "IndicadorValorizado",
	"IdEstado"
	
	FROM (
		SELECT
			DISTINCT
			NCG."IdOrigen" AS "FuenteDatos",
			NCG."IdCircularCartera" AS "IdValorizacion",
			'MESA_UI' AS "OrigenProcesamiento",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaValorizacion",
			-----------------------------------------------------------------------------------------Plazo
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaOperacion", -- Solo para operaciones a plazo
			NULL AS "FechaVencimientoOperacion", -- Solo para operaciones a plazo
			0  AS "PlazoEnDiasOperacion", -- Solo para operaciones a plazo		
			-----------------------------------------------------------------------------------------Categoria
			NCG."IdClasificacionTipoInstrumento", -- RF/IF
			NCG."IdTipoInstrumento", -- PAC/AUT/DEP
			CAR."IdInstrumento" AS "IdInstrumento",
			-----------------------------------------------------------------------------------------Instrumento
			NCG."Nemotecnico" AS "Nemotecnico",
			TO_VARCHAR(TO_DATE(NCG."FechaVencimiento")) AS"InsFechaVencimiento",
			TO_VARCHAR(TO_DATE(COALESCE(INS."FechaEmision", '0001-01-01'))) 	AS"InsFechaEmision",
			NCG."IdMonedaPrincipal" AS "IdMonedaInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdMonedaEmisionInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdTipoReajusteInstrumento",
			NCG."IdEmisor" AS "Emisor",								-- valorizacionCL
		   	-----------------------------------------------------------------------------------------Operacion
			'T0' AS"IdCondicionDeLiquidacion", -- valorizacionCL
			TO_VARCHAR(TO_DATE(NCG."Fecha")) AS "FechaDeLiquidacion",
			NCG."CantidadCustodia" AS "Cantidad",
			NCG."TasaMercadoCustodia" AS "TasaCompra",
			NCG."PrecioMercadoCustodia" AS "PrecioCompra",
			-----------------------------------------------------------------------------------------Garantias
			NULL AS "PrecioSubyacente",  		 		-- PUEDE SER NULL 
			NULL AS "TasaSubyacente",  		 			-- PUEDE SER NULL 
			NULL AS "PrecioGarantia",  		 			-- PUEDE SER NULL
			NULL AS "TasaGarantia",  		 			-- PUEDE SER NULL
			NULL AS "TasaTIF",  						-- PUEDE SER NULL
			NCG."IdPortafolio" AS "IdPortafolio",		-- PUEDE SER 0L
			'CARTERA' AS "TablaOrigen",					-- CARTERA/TRANSITO;		
			'INGTIT' AS "IdTipoOperacion", 				-- COMPAC/VENPAC
			'CUSTODIA' AS "Situacion",
            NCG."IndicadorValorizado" AS "IndicadorValorizado",
			NCG."IdEstado"
		FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF"		AS NCG
		RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" 			AS CAR ON (CAR."NemoSVS" = NCG."Nemotecnico" AND CAR."IdClasificacionTipoInstrumento" = NCG."IdClasificacionTipoInstrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS INS ON (INS."IdInstrumento" = NCG."IdInstrumento")
		UNION -- Garantias
		SELECT
			DISTINCT
			NCG."IdOrigen" AS "FuenteDatos",
			NCG."IdCircularCartera" AS "IdValorizacion",
			'MESA_UI' AS "OrigenProcesamiento",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaValorizacion",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaOperacion",
			NULL AS "FechaVencimientoOperacion",
			0  AS "PlazoEnDiasOperacion",	
			NCG."IdClasificacionTipoInstrumento",
			NCG."IdTipoInstrumento",
			CAR."IdInstrumento" AS "IdInstrumento",
			NCG."Nemotecnico" AS "Nemotecnico",
			TO_VARCHAR(TO_DATE(NCG."FechaVencimiento")) AS"InsFechaVencimiento",
			TO_VARCHAR(TO_DATE(COALESCE(INS."FechaEmision", '0001-01-01'))) 	AS"InsFechaEmision",
			NCG."IdMonedaPrincipal" AS "IdMonedaInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdMonedaEmisionInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdTipoReajusteInstrumento",
			NCG."IdEmisor" AS "Emisor",								
			'T0' AS"IdCondicionDeLiquidacion", 
			TO_VARCHAR(TO_DATE(NCG."Fecha")) AS "FechaDeLiquidacion",
			NCG."CantidadGarantia" AS "Cantidad",
			NCG."TasaMercadoCustodia" AS "TasaCompra",
			NCG."PrecioMercadoCustodia" AS "PrecioCompra",
			NULL AS "PrecioSubyacente",  		 		
			NULL AS "TasaSubyacente",  		 		
			NULL AS "PrecioGarantia",  		 			
			NULL AS "TasaGarantia",  		 			
			NULL AS "TasaTIF",  						
			NCG."IdPortafolio" AS "IdPortafolio",		
			'CARTERA' AS "TablaOrigen",							
			'INGTIT' AS "IdTipoOperacion", 				
			'GARANTIA' AS "Situacion",
            NCG."IndicadorValorizado" AS "IndicadorValorizado",
			NCG."IdEstado"
		FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF"		AS NCG
		RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" 			AS CAR ON (CAR."NemoSVS" = NCG."Nemotecnico" AND CAR."IdClasificacionTipoInstrumento" = NCG."IdClasificacionTipoInstrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS INS ON (INS."IdInstrumento" = NCG."IdInstrumento")
		UNION -- Prestamos
		SELECT
			DISTINCT
			NCG."IdOrigen" AS "FuenteDatos",
			NCG."IdCircularCartera" AS "IdValorizacion",
			'MESA_UI' AS "OrigenProcesamiento",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaValorizacion",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaOperacion",
			NULL AS "FechaVencimientoOperacion",
			0  AS "PlazoEnDiasOperacion",	
			NCG."IdClasificacionTipoInstrumento",
			NCG."IdTipoInstrumento",
			CAR."IdInstrumento" AS "IdInstrumento",
			NCG."Nemotecnico" AS "Nemotecnico",
			TO_VARCHAR(TO_DATE(NCG."FechaVencimiento")) AS"InsFechaVencimiento",
			TO_VARCHAR(TO_DATE(COALESCE(INS."FechaEmision", '0001-01-01'))) 	AS"InsFechaEmision",
			NCG."IdMonedaPrincipal" AS "IdMonedaInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdMonedaEmisionInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdTipoReajusteInstrumento",
			NCG."IdEmisor" AS "Emisor",								
			'T0' AS"IdCondicionDeLiquidacion", 
			TO_VARCHAR(TO_DATE(NCG."Fecha")) AS "FechaDeLiquidacion",
			NCG."CantidadPrestamo" AS "Cantidad",
			NCG."TasaMercadoCustodia" AS "TasaCompra",
			NCG."PrecioMercadoCustodia" AS "PrecioCompra",
			NULL AS "PrecioSubyacente",  		 		
			NULL AS "TasaSubyacente",  		 		
			NULL AS "PrecioGarantia",  		 			
			NULL AS "TasaGarantia",  		 			
			NULL AS "TasaTIF",  						
			NCG."IdPortafolio" AS "IdPortafolio",		
			'CARTERA' AS "TablaOrigen",							
			'INGTIT' AS "IdTipoOperacion", 				
			'PRESTAMO' AS "Situacion",
            NCG."IndicadorValorizado" AS "IndicadorValorizado",
			NCG."IdEstado"
		FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF"		AS NCG
		RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" 			AS CAR ON (CAR."NemoSVS" = NCG."Nemotecnico" AND CAR."IdClasificacionTipoInstrumento" = NCG."IdClasificacionTipoInstrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS INS ON (INS."IdInstrumento" = NCG."IdInstrumento")
		UNION -- Plazo Compra
		SELECT
			DISTINCT
			NCG."IdOrigen" AS "FuenteDatos",
			NCG."IdCircularCartera" AS "IdValorizacion",
			'MESA_UI' AS "OrigenProcesamiento",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaValorizacion",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaOperacion",
			NULL AS "FechaVencimientoOperacion",
			0  AS "PlazoEnDiasOperacion",	
			NCG."IdClasificacionTipoInstrumento",
			NCG."IdTipoInstrumento",
			CAR."IdInstrumento" AS "IdInstrumento",
			NCG."Nemotecnico" AS "Nemotecnico",
			TO_VARCHAR(TO_DATE(NCG."FechaVencimiento")) AS"InsFechaVencimiento",
			TO_VARCHAR(TO_DATE(COALESCE(INS."FechaEmision", '0001-01-01'))) 	AS"InsFechaEmision",
			NCG."IdMonedaPrincipal" AS "IdMonedaInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdMonedaEmisionInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdTipoReajusteInstrumento",
			NCG."IdEmisor" AS "Emisor",								
			'T0' AS"IdCondicionDeLiquidacion", 
			TO_VARCHAR(TO_DATE(NCG."Fecha")) AS "FechaDeLiquidacion",
			NCG."CantidadPlazoCompra" AS "Cantidad",
			NCG."TasaMercadoCustodia" AS "TasaCompra",
			NCG."PrecioMercadoCustodia" AS "PrecioCompra",
			NULL AS "PrecioSubyacente",  		 		
			NULL AS "TasaSubyacente",  		 		
			NULL AS "PrecioGarantia",  		 			
			NULL AS "TasaGarantia",  		 			
			NULL AS "TasaTIF",  						
			NCG."IdPortafolio" AS "IdPortafolio",		
			'CARTERA' AS "TablaOrigen",							
			'INGTIT' AS "IdTipoOperacion", 				
			'PLAZOCOM' AS "Situacion",
            NCG."IndicadorValorizado" AS "IndicadorValorizado",
			NCG."IdEstado"
		FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF"		AS NCG
		RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" 			AS CAR ON (CAR."NemoSVS" = NCG."Nemotecnico" AND CAR."IdClasificacionTipoInstrumento" = NCG."IdClasificacionTipoInstrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS INS ON (INS."IdInstrumento" = NCG."IdInstrumento")
		UNION -- Plazo Venta
		SELECT
			DISTINCT
			NCG."IdOrigen" AS "FuenteDatos",
			NCG."IdCircularCartera" AS "IdValorizacion",
			'MESA_UI' AS "OrigenProcesamiento",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaValorizacion",
			TO_VARCHAR(TO_DATE(NCG."Fecha"))  AS "FechaOperacion",
			NULL AS "FechaVencimientoOperacion",
			0  AS "PlazoEnDiasOperacion",	
			NCG."IdClasificacionTipoInstrumento",
			NCG."IdTipoInstrumento",
			CAR."IdInstrumento" AS "IdInstrumento",
			NCG."Nemotecnico" AS "Nemotecnico",
			TO_VARCHAR(TO_DATE(NCG."FechaVencimiento")) AS"InsFechaVencimiento",
			TO_VARCHAR(TO_DATE(COALESCE(INS."FechaEmision", '0001-01-01'))) 	AS"InsFechaEmision",
			NCG."IdMonedaPrincipal" AS "IdMonedaInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdMonedaEmisionInstrumento",
			NCG."IdMonedaInstrReajuste" AS "IdTipoReajusteInstrumento",
			NCG."IdEmisor" AS "Emisor",								
			'T0' AS"IdCondicionDeLiquidacion", 
			TO_VARCHAR(TO_DATE(NCG."Fecha")) AS "FechaDeLiquidacion",
			NCG."CantidadPlazoVenta" AS "Cantidad",
			NCG."TasaMercadoCustodia" AS "TasaCompra",
			NCG."PrecioMercadoCustodia" AS "PrecioCompra",
			NULL AS "PrecioSubyacente",  		 		
			NULL AS "TasaSubyacente",  		 		
			NULL AS "PrecioGarantia",  		 			
			NULL AS "TasaGarantia",  		 			
			NULL AS "TasaTIF",  						
			NCG."IdPortafolio" AS "IdPortafolio",		
			'CARTERA' AS "TablaOrigen",							
			'INGTIT' AS "IdTipoOperacion", 				
			'PLAZOVEN' AS "Situacion",
            NCG."IndicadorValorizado" AS "IndicadorValorizado",
			NCG."IdEstado"
		FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF"		AS NCG
		RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" 			AS CAR ON (CAR."NemoSVS" = NCG."Nemotecnico" AND CAR."IdClasificacionTipoInstrumento" = NCG."IdClasificacionTipoInstrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS INS ON (INS."IdInstrumento" = NCG."IdInstrumento")
	) GROUP BY
	"FuenteDatos",
	"IdValorizacion",
	"OrigenProcesamiento",
	"FechaValorizacion",
	"FechaOperacion",				
	"FechaVencimientoOperacion",
	"PlazoEnDiasOperacion",							
	"IdClasificacionTipoInstrumento", 		
	"IdTipoInstrumento",					
	"IdInstrumento",
	"Nemotecnico",
	"InsFechaVencimiento",
	"InsFechaEmision", 
	"IdMonedaInstrumento",
	"IdMonedaEmisionInstrumento",
	"IdTipoReajusteInstrumento",
	"Emisor",								
	"IdCondicionDeLiquidacion",				
	"FechaDeLiquidacion",
	"Cantidad",
	"TasaCompra",
	"PrecioCompra",
	"PrecioSubyacente",  
	"TasaSubyacente",  	 
	"PrecioGarantia",  	
	"TasaGarantia",  	
	"TasaTIF",  		
	"IdPortafolio",		
	"TablaOrigen",				
	"IdTipoOperacion",
	"Situacion",
    "IndicadorValorizado",
	"IdEstado"
	HAVING SUM("Cantidad") != 0
	ORDER BY "Nemotecnico"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.DatosValorizarNCG499" TO bcs_desarrollador WITH GRANT OPTION;
