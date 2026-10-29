UPDATE "BSCL.Orden.BD::ORD.Asignacion"
SET "MontoAsignado"= :MontoAjustado
, "MontoAsignadoLocal"= :MontoAjustadoLocal
, "MontoAsignadoLiquidacion"= :MontoAjustado
WHERE "IdTransaccion" = :IdTransaccion AND "IdMovimientoComprobante" = :IdMovimientoComprobante