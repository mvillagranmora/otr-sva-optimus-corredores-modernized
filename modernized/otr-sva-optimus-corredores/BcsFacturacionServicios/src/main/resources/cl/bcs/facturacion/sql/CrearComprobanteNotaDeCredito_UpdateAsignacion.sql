UPDATE "BSCL.Orden.BD::ORD.Asignacion"
SET "IdEstadoAsignacion" = 'NEF'
WHERE "IdAsignacion" IN (:IdAsignacion);