UPDATE ENR
SET ENR."IdPaisResidencia" = :IdPaisResidencia
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" AS ENR
WHERE ENR."IdEnrolamiento" = :IdEnrolamiento;