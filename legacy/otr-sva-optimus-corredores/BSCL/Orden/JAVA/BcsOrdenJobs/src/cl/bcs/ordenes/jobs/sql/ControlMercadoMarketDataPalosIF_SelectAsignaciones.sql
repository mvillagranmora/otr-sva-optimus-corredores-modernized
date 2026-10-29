SELECT "IdAsignacion", "IdTransaccion"
FROM (
    SELECT 
        "IdAsignacion",
        "IdTransaccion",
        COUNT("IdTransaccion") OVER (PARTITION BY "IdTransaccion") AS contador
    FROM "BSCL.Orden.BD::ORD.Asignacion"
    WHERE "IdTransaccion" IN (:IdTransaccion)
      AND "IdEstadoAsignacion" IN ('NEF', 'EAF')
) t
WHERE contador = 1;
