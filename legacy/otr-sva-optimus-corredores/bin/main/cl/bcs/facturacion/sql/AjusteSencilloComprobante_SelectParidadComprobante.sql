SELECT "FechaAsignacion"
FROM "BSCL.Orden.BD::ORD.Asignacion"
WHERE "IdTransaccion" = :IdTransaccion AND  "IdMovimientoComprobante" = :IdMovimientoComprobante