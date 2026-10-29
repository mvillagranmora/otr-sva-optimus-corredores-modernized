SELECT
"IdNombreFantasia"
, "DescripcionNombreFantasia" AS "NombreFantasia"
, "IdEnrolamiento"
, "FechaCreacion"
, "FechaModificacion"
, "FechaIngreso"
, "IdUsuarioCreador"
FROM "BSCL.SocioNegocio.BD::ENR.NombreFantasia"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);