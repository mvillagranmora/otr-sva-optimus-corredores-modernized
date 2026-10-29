SELECT
    CD."IdEstado",
    CD."Monto",
    CD."IdTipoOperacion",
    CD."IdMoneda",
    CD."IdComprobanteOrigen",
    CD."IdComprobante",
    CDO."IdIngresoEgreso"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" CDO ON CDO."IdComprobanteDetalle" = CD."IdComprobanteDetalle"     
WHERE CD."IdEstado" = 'LIQ'
AND CD."IdComprobanteOrigen" IN (:idsComprobanteOrigen);