SELECT "IdPaisResidencia"
FROM "BSCL.SocioNegocio.BD::ENR.FatcaPaisResidencia"
WHERE "IdEnrolamiento" = :IdEnrolamiento AND "IdPaisResidencia" = :IdPaisResidencia;