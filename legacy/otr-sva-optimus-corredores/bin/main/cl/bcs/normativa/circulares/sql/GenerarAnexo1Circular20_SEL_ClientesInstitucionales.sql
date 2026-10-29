SELECT to_bigint(PER."IdPersona") "IdPersona" 
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON (PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad")
WHERE IDENT."DocumentoIdentidad" ='96549050-7';