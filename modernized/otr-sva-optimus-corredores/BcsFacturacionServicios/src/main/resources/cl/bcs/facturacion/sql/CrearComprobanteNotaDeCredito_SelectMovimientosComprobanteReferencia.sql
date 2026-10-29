SELECT DISTINCT "IdComprobanteReferencia" AS "IdComprobante"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
WHERE "IdComprobanteReferencia" IN (:idsComprobanteReferencia)
  AND "IdEstadoFacturacion" <> 'ANU';