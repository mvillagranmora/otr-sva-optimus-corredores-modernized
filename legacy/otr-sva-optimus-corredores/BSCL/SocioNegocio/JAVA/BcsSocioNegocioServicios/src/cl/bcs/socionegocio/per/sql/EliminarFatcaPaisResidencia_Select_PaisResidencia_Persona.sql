SELECT "IdPersona" 
FROM "BSCL.SocioNegocio.BD::PER.Persona" 
WHERE "IdPersona" = :IdPersona
AND "IdPaisResidencia" = :IdPaisResidencia;