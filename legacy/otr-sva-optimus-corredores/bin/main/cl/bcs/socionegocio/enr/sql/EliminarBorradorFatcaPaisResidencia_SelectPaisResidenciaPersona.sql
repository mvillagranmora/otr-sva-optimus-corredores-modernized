SELECT "IdEnrolamiento" 
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" 
WHERE "IdEnrolamiento" = :IdEnrolamiento
AND "IdPaisResidencia" = :IdPaisResidencia;