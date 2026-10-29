SELECT 
CAR."IdPortafolio"
,CAR."IdInstrumento"
,CAR."IdBoveda"
,CAR."IdTipoOperacion"
,CAR."IdCartera"
,CAR."FolioCartera"
,CAR."IdClasificacionTipoInstrumento"
,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END AS "Cantidad"
,CAR."PrecioCompra"
,COALESCE(CAR."INGMontoCompraActualMonedaInstrumento",0.0) AS "MontoCarteraPrecioCompra"
,CAR."ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE
CAR."FechaRespaldo" = :Fecha
AND CAR."IdEstado" = 'VIG'
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
AND CAR."EGRCantidad" = 0
AND CAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR CAR."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR CAR."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR."IdBoveda" IN (:IdBoveda))