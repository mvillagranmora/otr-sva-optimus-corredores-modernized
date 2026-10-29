UPDATE "BSCL.Orden.BD::HIS.Transaccion"
SET "IdEstadoRegistro" = 'PRO'
WHERE ("IdTransaccion" = :IdTransaccion OR "IdTransaccion" = :IdTransaccionPlazo) AND "IdEstadoRegistro" = 'PEN';