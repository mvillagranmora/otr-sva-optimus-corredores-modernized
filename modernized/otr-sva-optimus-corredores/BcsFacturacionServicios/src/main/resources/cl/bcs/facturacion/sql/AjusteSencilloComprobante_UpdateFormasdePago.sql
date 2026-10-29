UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
SET "MontoLiquidacionFormaDePago" = :MontoLiquidacionAjustado
WHERE "IdMovimientoComprobante" = :IdMovimientoComprobante AND "IdEstado" = 'VIG'