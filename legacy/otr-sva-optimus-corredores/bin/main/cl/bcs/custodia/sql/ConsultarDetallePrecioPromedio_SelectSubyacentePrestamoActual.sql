SELECT
CAR."IdPortafolio"
,SUB."IdInstrumento"
,CAR."IdBoveda"
,CAR."IdCartera"
,CAR."FolioCartera"
,SUB."IdCarteraRetirada" AS "IdCarteraOrigen"
,CAR."FolioCartera" AS "FolioCarteraOrigen"
,SUB."IdClasificacionTipoInstrumento"
,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END AS "Cantidad"
,CAR."PrecioCompra"
,CAR."PrecioAPlazo" AS "PrecioAPlazo"
,COALESCE(SUB."INGMontoCompraActualMonedaInstrumento",0.0) AS "MontoCarteraPrecioCompra"
,CAR."FechaVencimientoOperacion"
,CAR."TasaCompra"
,CAR."FechaLiquidacion"
,CAR."ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG'
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE 
CAR."IdEstado" = 'VIG'
AND CAR."IdTipoOperacion" = 'PRESTA'
AND CAR."EGRCantidad" = 0
AND CAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR SUB."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR SUB."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR."IdBoveda" IN (:IdBoveda))