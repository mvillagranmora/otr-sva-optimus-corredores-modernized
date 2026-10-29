SELECT "IdComprobante","IdEstadoComprobante","IdComprobanteReferencia"
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdEstadoComprobante" != 'ANU'
AND "IdTipoComprobante" IN (:tiposComprobanteNotaCredito)
AND "IdComprobanteReferencia" IN (:idsComprobante)
;