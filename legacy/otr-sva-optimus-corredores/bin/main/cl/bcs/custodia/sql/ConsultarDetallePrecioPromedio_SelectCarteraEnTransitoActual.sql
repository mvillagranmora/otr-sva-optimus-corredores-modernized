SELECT 
CAR."IdPortafolio"
,CAR."IdInstrumento"
,CAR."IdBoveda"
,CAR."IdMovimientoCarteraEnTransito" AS "IdCartera"
,CAR."IdMovimientoCarteraEnTransito" AS "FolioCartera"
,CAR."IdClasificacionTipoInstrumento"
,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."ValorRescate" ELSE CAR."Cantidad" END AS "Cantidad"
,CAR."PrecioCompra"
,COALESCE(CAR."MontoCompraMonedaInstrumento",0.0) AS "MontoCarteraPrecioCompra"
,CAR."ValorFactorMonedaInstrumento"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS CAR
LEFT JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio" AND PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo)
WHERE
CAR."IdEstado" = 'PEN'
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
AND CAR."IdEmpresa" = :IdEmpresa
AND (:BuscarPorClasificacionTipoInstrumento = 'N' OR CAR."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:BuscarPorPortafolio = 'N' OR PORTAFOLIO_PASO."IdPortafolio" IS NOT NULL)
AND (:BuscarPorInstrumento = 'N' OR CAR."IdInstrumento" IN (:IdInstrumento))
AND (:BuscarPorBoveda = 'N' OR CAR."IdBoveda" IN (:IdBoveda))
AND CAR."EsVentaDescubierta" = 'S'