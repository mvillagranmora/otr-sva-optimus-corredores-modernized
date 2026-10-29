UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteReferencia"
SET "IdComprobante" = :IdComprobante
WHERE "IdMovimientoComprobante" = :IdMovimientoComprobante
AND "TipoReferencia" = :TipoReferencia;