SELECT
CAR."IdPortafolio"
,SUB."IdInstrumento"
,CAR_SUB."IdBoveda"
,CAR."IdCartera"
,CAR."FolioCartera"
,SUB."IdCarteraRetirada" AS "IdCarteraOrigen"
,CAR_SUB."FolioCartera" AS "FolioCarteraOrigen"
,SUB."INGCantidad" AS "Cantidad"
,CAR_SUB."PrecioCompra"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG' AND CAR."FechaRespaldo" = SUB."FechaRespaldo"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR_SUB ON SUB."IdCarteraRetirada" = CAR_SUB."IdCartera"
WHERE
CAR."FechaRespaldo" = :Fecha
AND CAR."IdEstado" = 'VIG'
AND CAR."IdTipoOperacion" = 'VENSIM'
AND CAR."EGRCantidad" = 0
AND CAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR SUB."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR CAR."IdPortafolio" IN (:IdPortafolio))
AND (:BuscarPorInstrumento = 'N' OR CAR_SUB."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR_SUB."IdBoveda" IN (:IdBoveda))