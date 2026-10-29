SELECT
CAR."IdPortafolio"
,SUB."IdInstrumento"
,CAR_SUB."IdBoveda"
,CAR."IdCartera"
,CAR."FolioCartera"
,SUB."IdCarteraRetirada" AS "IdCarteraOrigen"
,CAR_SUB."FolioCartera" AS "FolioCarteraOrigen"
,CAR_SUB."IdClasificacionTipoInstrumento"
,CASE WHEN CAR_SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END AS "Cantidad"
,CAR_SUB."PrecioCompra"
,CAR."PrecioAPlazo"
,COALESCE(SUB."INGMontoCompraActualMonedaInstrumento",0.0) AS "MontoCarteraPrecioCompra"
,CAR."FechaVencimientoOperacion"
,CAR."TasaCompra"
,CAR."FechaLiquidacion"
,CAR_SUB."ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG'
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR_SUB ON SUB."IdCarteraRetirada" = CAR_SUB."IdCartera"
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE 
CAR."IdEstado" = 'VIG'
AND CAR."IdTipoOperacion" = 'VENSIM'
AND CAR."EGRCantidad" = 0
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdTipoInstrumento" = 'SIM'
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR SUB."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR CAR_SUB."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR_SUB."IdBoveda" IN (:IdBoveda))