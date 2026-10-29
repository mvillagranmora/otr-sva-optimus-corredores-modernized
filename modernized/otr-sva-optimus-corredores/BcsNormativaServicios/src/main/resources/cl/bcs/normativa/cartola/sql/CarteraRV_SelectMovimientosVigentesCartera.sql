WITH 
	Cartera AS (
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."FolioCartera"
			,CAR."INGFechaOperacion" AS "FechaOperacion"
			,CAR."INGFechaLiquidacion" AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,CAR."Cantidad" AS "CantidadOriginal"
			,CAR."INGCantidad" AS "CantidadCartera"
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
	),
	GarantiaAsociadaACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(GAR."INGCantidad") AS "CantidadGarantia"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" GAR 
				ON GAR."FolioCarteraOrigen" = CAR."FolioCartera"	
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND GAR."IdEstado" = 'VIG'
		GROUP BY CAR."FolioCartera"
	),
	OperacionesAPlazoAsociadoACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadAPlazo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"	
		WHERE 
		  CAR."IdPortafolio" IN (:IdPortafolio)
		  AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	  AND CAR."IdEmpresa" = :IdEmpresa
	  	  AND CAR."IdEstado" = 'VIG'
	   	  AND CAR."FechaOperacion" <= :FechaHasta
	  	  AND CAR."EGRCantidad" = 0
		  AND SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('SIM','PAC')
		GROUP BY CAR."FolioCartera"
	),
	ReporteAsociadoACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadReporte"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"	
		WHERE
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	   	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdTipoInstrumento" IN ('RRV','RRF')
		GROUP BY CAR."FolioCartera"
	),
	PrestamoEmprestitoAsociadoACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadPrestamo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"	
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	   	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdTipoInstrumento" IN ('PRT','EMP')
		GROUP BY CAR."FolioCartera"
	)
	SELECT   C."IdCliente"
			,C."IdPortafolio"
			,C."IdClasificacionTipoInstrumento"
			,C."IdTipoInstrumento"
			,C."IdBoveda"
			,C."IdInstrumento"
			,C."Nemotecnico"
			,IFNULL(SUM(C."CantidadOriginal"),TO_DECIMAL(0,26,8)) AS "CantidadOriginal"
			,IFNULL(SUM(C."CantidadCartera"),TO_DECIMAL(0,26,8)) AS "CantidadCartera"
			,IFNULL(SUM(G."CantidadGarantia"),TO_DECIMAL(0,26,8)) AS "CantidadGarantia"
			,IFNULL(SUM(O."CantidadAPlazo"),TO_DECIMAL(0,26,8)) AS "CantidadAPlazo"
			,IFNULL(SUM(R."CantidadReporte"),TO_DECIMAL(0,26,8)) AS "CantidadReporte"
			,IFNULL(SUM(P."CantidadPrestamo"),TO_DECIMAL(0,26,8)) AS "CantidadPrestamo"
			,C."IdMonedaInstrumento"
			,C."IdEmisor"
			--------------------------------------------------------------------------------------------
			,C."PrecioPromedioCompra"
			,IFNULL(SUM(C."MontoPromedioCompraMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM(C."MontoPromedioCompraMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaAdicional"
			,IFNULL(SUM(C."MontoATasaCompra"),TO_DECIMAL(0,26,8)) AS "MontoATasaCompra"
	FROM Cartera AS C
	FULL OUTER JOIN GarantiaAsociadaACartera G ON G."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN OperacionesAPlazoAsociadoACartera O ON O."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN ReporteAsociadoACartera R ON R."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN PrestamoEmprestitoAsociadoACartera P ON P."FolioCartera" = C."FolioCartera"
	GROUP BY C."IdCliente",C."IdPortafolio",C."IdClasificacionTipoInstrumento",C."IdTipoInstrumento",C."IdBoveda",C."IdInstrumento",C."Nemotecnico"
			,C."IdMonedaInstrumento",C."PrecioPromedioCompra",C."IdEmisor";