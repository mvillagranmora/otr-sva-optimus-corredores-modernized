SELECT 
"IdEnrolamiento",
"IdPaisResidencia"
FROM "BSCL.SocioNegocio.BD::ENR.FatcaPaisResidencia"
WHERE "IdEnrolamiento"  = :IdEnrolamiento
AND "PaisResidenciaPrincipal" = 'N';