SELECT 
CAR."IdPortafolio"
,CAR."IdInstrumento"
,CAR."IdBoveda"
,CAR."IdTipoOperacion"
,CAR."IdCarteraNegativa" AS "IdCartera"
,CAR."IdCarteraNegativa" AS "FolioCartera"
,INS."IdClasificacionTipoInstrumento"
,CASE WHEN INS."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."ValorRescate" ELSE - 1 * CAR."Cantidad" END AS "Cantidad"
,CAR."PrecioOperacion" AS "PrecioCompra"
,COALESCE(CAR."MontoOperacionMonedaInstrumento",0.0) AS "MontoCarteraPrecioCompra"
, CAST(1 AS DECIMAL) AS "ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON ins."IdInstrumento" = car."IdInstrumento"
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE 
CAR."FechaRespaldo" = :Fecha
AND CAR."IdEstado" = 'VIG'
AND INS."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF', 'PRF', 'ERF')
AND CAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR ins."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR CAR."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR."IdBoveda" IN (:IdBoveda))
 