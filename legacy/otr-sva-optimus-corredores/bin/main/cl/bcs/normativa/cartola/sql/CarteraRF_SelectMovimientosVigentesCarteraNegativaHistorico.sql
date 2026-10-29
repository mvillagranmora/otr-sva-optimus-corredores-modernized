WITH CarteraNegativa AS (
	SELECT 	 
		 CAR."IdCliente"
		,CAR."IdPortafolio"
		,CAR."FolioCustodia" AS "FolioCartera" --,CAR."FolioCartera"
		,CAR."FechaOperacion"
		,CAR."FechaLiquidacion"
		,CAR."IdBoveda"
		,TipoInstrumento."IdTipoInstrumento"
		,CAR."IdInstrumento"
		,CAR."Nemotecnico"	
		,CAR."Cantidad" * -1 AS "CantidadOriginal"
		,CAR."Cantidad" * -1 AS "CantidadCartera"
		,CASE WHEN IFNULL(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento" ELSE IFNULL(CAR."IdTipoReajuste",'') END AS "IdMonedaInstrumento"
		--------------------------------------------------------------------------------------------
		,CAR."TasaOperacion" AS "TasaCompra"
		,COALESCE (CAR."PrecioOperacion" ,0) AS "PrecioCompra"
		,CAR."MontoOperacionMonedaInstrumento" AS "MontoCompraMonedaSistema" --,"INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
		,COALESCE (CAR."TasaMercado" ,0) AS "TasaMercado"
		,COALESCE (CAR."PrecioMercado" ,0) AS "PrecioMercado"
		,CAR."MontoMercadoMonedaInstrumento" * -1 AS "MontoMercadoMonedaSistema" --,"INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
		,CAR."MontoMercadoMonedaInstrumento" * -1 AS "MontoMercadoMonedaAdicional" --,"INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		---------------------------------------------------------------------------------------------
		,TO_DECIMAL(0,26,8) AS "MontoInteresCorridoMercadoActualMonedaSistema" --,"INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
		,TO_DECIMAL(0,26,8) AS "MontoInteresCorridoMercadoActualMonedaAdicional" --,"INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
		---------------------------------------------------------------------------------------------
		,'N' AS "TieneCorteDeCupon"
		,TO_DATE('0001-01-01') AS "FechaPagoCorteCupon" 			
		,TO_DECIMAL(0,26,8) AS "CuponesPagadosMonedaSistema"
		,ClasInst."IdClasificacionTipoInstrumento"
	FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" CAR
	INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS	ON INS."IdInstrumento" = CAR."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INS."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"	
	WHERE
		    ClasInst."IdClasificacionTipoInstrumento" = 'RF'
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."FechaOperacion" <= :FechaHasta
		AND TipoInstrumento."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
)
SELECT   
		 C."IdCliente"
		,C."IdPortafolio"
		,C."FolioCartera"
		,C."FechaOperacion"
		,C."FechaLiquidacion"
		,C."IdTipoInstrumento"
		,C."IdBoveda"
		,C."IdInstrumento"
		,C."Nemotecnico"
		,IFNULL(C."CantidadOriginal",TO_DECIMAL(0,26,8)) AS "CantidadOriginal"
		,IFNULL(C."CantidadCartera",TO_DECIMAL(0,26,8)) AS "CantidadCartera"
		,TO_DECIMAL(0,26,8) AS "CantidadGarantia"
		,TO_DECIMAL(0,26,8) AS "CantidadAPlazo"
		,TO_DECIMAL(0,26,8) AS "CantidadReporte"
		,C."IdMonedaInstrumento"
		--------------------------------------------------------------------------------------------
		,C."TasaCompra"
		,C."PrecioCompra"
		,C."MontoCompraMonedaSistema"
		,C."TasaMercado"
		,C."PrecioMercado"
		,C."MontoMercadoMonedaSistema"
		,C."MontoMercadoMonedaAdicional"
		---------------------------------------------------------------------------------------------
		,C."MontoInteresCorridoMercadoActualMonedaSistema"
		,C."MontoInteresCorridoMercadoActualMonedaAdicional"
		---------------------------------------------------------------------------------------------
		,C."TieneCorteDeCupon"
		,C."FechaPagoCorteCupon"			
		,C."CuponesPagadosMonedaSistema"
		,C."IdClasificacionTipoInstrumento"
FROM CarteraNegativa AS C;