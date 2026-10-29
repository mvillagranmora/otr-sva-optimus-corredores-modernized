UPDATE "BSCL.Orden.BD::HIS.Transaccion"
SET "IdEstadoRegistro" = 'PRO'
WHERE "IdTransaccion" = :IdTransaccion AND "IdEstadoRegistro" = 'PEN';