SELECT 
CAR."IdPortafolio"
,CAR."IdInstrumento"
,CAR."IdBoveda"
,CAR."IdTipoOperacion"
,CAR."IdCarteraNegativa" AS "IdCartera"
,CAR."IdCarteraNegativa" AS "FolioCartera"
,TINS."IdClasificacionTipoInstrumento"
,CASE WHEN TINS."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."ValorRescate" ELSE - 1 * CAR."Cantidad" END AS "Cantidad"
,CAR."PrecioOperacion" AS "PrecioCompra"
,COALESCE(CAR."MontoOperacionMonedaInstrumento",0.0) AS "MontoCarteraPrecioCompra"
, CAST(1 AS DECIMAL) AS "ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" CAR
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = CAR."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE 
CAR."IdEstado" = 'VIG'
AND INS."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF', 'PRF', 'ERF')
AND CAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR TINS."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR CAR."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR."IdBoveda" IN (:IdBoveda))
 