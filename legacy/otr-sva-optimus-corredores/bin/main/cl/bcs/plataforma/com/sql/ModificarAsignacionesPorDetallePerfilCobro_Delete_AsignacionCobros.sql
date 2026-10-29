DELETE FROM "BSCL.Orden.BD::ORD.CobroAsignacion"
WHERE "IdAsignacion" = :IdAsignacion
AND "IdCobro" = :IdCobro
AND "IdCobroAsignacion" = :IdCobroAsignacion
;