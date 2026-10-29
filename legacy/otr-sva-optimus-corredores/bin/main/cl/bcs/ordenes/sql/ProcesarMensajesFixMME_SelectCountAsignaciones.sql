SELECT ORD."IdOrden",COUNT(ASIG."IdAsignacion") AS "NumeroAsignaciones"
FROM "BSCL.Orden.BD::ORD.Orden" ORD
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG  ON ORD."IdOrden" = ASIG."IdOrden" AND ASIG."IdEstadoAsignacion" IN ('EAF','NEF')
WHERE ORD."IdOrden" IN (:idsOrden) 
GROUP BY ORD."IdOrden";