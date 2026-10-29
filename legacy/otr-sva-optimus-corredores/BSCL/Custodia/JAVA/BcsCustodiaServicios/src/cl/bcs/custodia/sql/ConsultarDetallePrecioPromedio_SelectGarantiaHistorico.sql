SELECT
GAR."IdPortafolio"
,GAR."IdInstrumento"
,CAR."IdBoveda"
,GAR."IdGarantia"
,GAR."FolioGarantia"
,GAR."IdCarteraOrigen" AS "IdCarteraOrigen"
,CAR."FolioCartera" AS "FolioCarteraOrigen"
,CAR."IdClasificacionTipoInstrumento"
,GAR."INGCantidad" AS "Cantidad"
,CAR."PrecioCompra"
,COALESCE(CAR."EGRMonto",0.0) AS "MontoCarteraPrecioCompra"
,CAR."ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE 
GAR."FechaRespaldo" = :Fecha
AND GAR."IdEstado" = 'VIG'
AND GAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR CAR."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR GAR."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR."IdBoveda" IN (:IdBoveda))