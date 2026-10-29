UPDATE "BSCL.SocioNegocio.BD::PER.Persona" 
SET "IdPersonaRepresentante" = :IdPersonaDestino
WHERE "IdPersona" = :IdPersonaOrigen;