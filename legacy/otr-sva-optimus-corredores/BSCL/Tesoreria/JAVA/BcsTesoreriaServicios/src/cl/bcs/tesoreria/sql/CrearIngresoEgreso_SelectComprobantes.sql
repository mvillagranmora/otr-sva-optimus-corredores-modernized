SELECT C."IdComprobante", C."IdTipoMovimiento", CASE WHEN C."IdComprobante" = C."IdComprobante" THEN 'CLI' END AS "AfectaCuentaInversion" 
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" CDO
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD ON CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" C ON C."IdComprobante" = CD."IdComprobante"
WHERE CDO."IdIngresoEgreso" IN (:IdsIngresoEgreso)