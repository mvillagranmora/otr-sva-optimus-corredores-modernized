UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
SET "Monto" = :MontoAjustado
, "MontoMovimientoComprobante" = :MontoAjustadoLocal
, "MontoLiquidacion" = :MontoAjustado
WHERE "IdTransaccion" = :IdTransaccion AND "IdMovimientoComprobante" = :IdMovimientoComprobante
