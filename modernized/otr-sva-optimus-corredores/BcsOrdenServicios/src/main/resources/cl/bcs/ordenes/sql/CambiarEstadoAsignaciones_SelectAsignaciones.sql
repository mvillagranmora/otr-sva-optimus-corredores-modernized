SELECT "IdAsignacion","IdEstadoAsignacion"
FROM "BSCL.Orden.BD::ORD.Asignacion"
WHERE "IdAsignacion" IN (:idsAsignacion)