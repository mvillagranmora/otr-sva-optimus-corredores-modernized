SELECT "MontoLiquidacionFormaDePago"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" 
WHERE "IdMovimientoComprobante" = :IdMovimientoComprobante AND "IdEstado" = 'VIG'