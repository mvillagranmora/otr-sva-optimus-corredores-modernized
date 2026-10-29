UPDATE "BSCL.Orden.BD::ORD.Transaccion" 
SET "IdEstado" = 'ING'
WHERE "IdTransaccion" IN (:IdTransaccion);