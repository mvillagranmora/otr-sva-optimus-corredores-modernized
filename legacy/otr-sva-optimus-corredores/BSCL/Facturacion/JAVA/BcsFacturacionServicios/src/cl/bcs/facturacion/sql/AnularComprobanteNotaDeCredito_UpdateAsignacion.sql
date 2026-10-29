UPDATE "BSCL.Orden.BD::ORD.Asignacion"
SET "IdEstadoAsignacion" = 'EAF'
WHERE "IdAsignacion" IN (:IdAsignacion);