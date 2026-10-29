SELECT "IdAsignacion"
FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
WHERE ASIG."IdAsignacionGrupo" IN (:idsAsignacionGrupo);