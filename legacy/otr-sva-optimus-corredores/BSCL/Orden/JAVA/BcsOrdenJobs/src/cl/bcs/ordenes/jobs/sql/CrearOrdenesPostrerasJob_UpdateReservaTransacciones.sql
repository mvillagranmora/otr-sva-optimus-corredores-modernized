UPDATE "BSCL.Orden.BD::ORD.Transaccion" 
SET "IdUsuarioReserva" = :IdUsuarioReserva, "MotivoReserva" = :MotivoReserva
WHERE "IdTransaccion" = :IdTransaccion;