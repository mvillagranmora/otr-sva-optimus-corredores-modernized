SELECT ASIG."IdAsignacion",TRAN."IdTransaccion"
FROM "BSCL.Orden.BD::ORD.Transaccion" TRAN
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG ON ASIG."IdTransaccion" = TRAN."IdTransaccion"