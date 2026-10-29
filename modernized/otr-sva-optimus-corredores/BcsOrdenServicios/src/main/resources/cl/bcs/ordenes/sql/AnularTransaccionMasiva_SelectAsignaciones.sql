SELECT "IdTransaccion", COUNT(1) AS "Count" FROM "BSCL.Orden.BD::ORD.Asignacion" 
WHERE "IdTransaccion" IN (:IdTransaccion)
AND "IdEstadoAsignacion" != 'ANU'
GROUP BY "IdTransaccion";