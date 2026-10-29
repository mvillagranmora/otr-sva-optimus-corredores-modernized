UPDATE ASIG
SET ASIG."IdEstadoAsignacion" = :IdEstadoAsignacion
FROM "BSCL.Orden.BD::ORD.Asignacion" asig
WHERE "IdAsignacion" = :IdAsignacion;
