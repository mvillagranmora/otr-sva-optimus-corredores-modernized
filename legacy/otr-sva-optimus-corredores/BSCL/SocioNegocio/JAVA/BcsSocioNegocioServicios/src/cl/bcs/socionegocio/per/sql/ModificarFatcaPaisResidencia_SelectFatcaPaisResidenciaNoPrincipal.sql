SELECT 
"IdPersona",
"IdPaisResidencia"
FROM "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia"
WHERE "IdPersona"  = :IdPersona
AND "PaisResidenciaPrincipal" = 'N';