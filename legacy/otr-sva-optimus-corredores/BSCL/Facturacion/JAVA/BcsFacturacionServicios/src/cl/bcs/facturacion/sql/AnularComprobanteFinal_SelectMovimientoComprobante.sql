SELECT "IdMovimientoComprobante"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MOV
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" det on det."IdComprobanteDetalle" = MOV."IdComprobanteDetalle"
WHERE "IdComprobante" IN (:idsComprobante)