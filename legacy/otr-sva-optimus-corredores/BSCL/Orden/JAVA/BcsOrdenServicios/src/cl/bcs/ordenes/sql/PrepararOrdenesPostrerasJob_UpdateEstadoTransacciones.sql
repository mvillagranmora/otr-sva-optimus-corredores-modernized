UPDATE TRA
SET TRA."IdEstado" = :IdEstado
FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
INNER JOIN "BSCL.Orden.BD::ORD.ProcesarOrdenesPostreras_Entrada_Ordenes" AS PASO ON TRA."IdTransaccion" = PASO."IdTransaccion"
WHERE PASO."IdTrabajo" = :IdTrabajo
AND TRA."IdTransaccion" = PASO."IdTransaccion";

