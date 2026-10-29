SELECT "IdAsignacion", "IdTransaccion","IdEstadoAsignacion"
FROM "BSCL.Orden.BD::ORD.Asignacion" 
WHERE "IdTransaccion" IN (:IdTransaccion) AND "IdEstadoAsignacion" IN ('NEF', 'EAF');