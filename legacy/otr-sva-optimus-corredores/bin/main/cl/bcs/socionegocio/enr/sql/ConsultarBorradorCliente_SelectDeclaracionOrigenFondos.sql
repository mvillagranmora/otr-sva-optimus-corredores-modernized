SELECT DOF."IdDeclaracionOrigenFondos"
, DOF."IdEnrolamiento"
, DOF."IdTipoOperacionOrigenFondos"
, TO_DATE(DOF."FechaDeclaracion") AS "FechaDeclaracion"
, DOF."Comentario"
, DOF."IdUsuarioCreador"
, DOF."FechaIngreso"
, DOF."FechaCreacion"
, DOF."FechaModificacion"
, DOF."IdEmpresa"
, DOF."OperacionInscripcion"
FROM "BSCL.SocioNegocio.BD::ENR.DeclaracionOrigenFondos" DOF
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);