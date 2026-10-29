UPDATE "BSCL.SocioNegocio.BD::PER.Persona" AS PER
SET 
	PER."IdPersonaConyuge" = ( SELECT DOC."IdPersona" FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC WHERE DOC."DocumentoIdentidad" = :NumeroDocumento )
WHERE PER."IdPersona" = :IdPersonaConyuge;
