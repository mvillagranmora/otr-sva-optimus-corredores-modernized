UPDATE "BSCL.Orden.BD::ORD.Asignacion"
SET "IdEstadoAsignacion" = 'ANU'
WHERE "IdAsignacion" IN (:IdAsignacion);