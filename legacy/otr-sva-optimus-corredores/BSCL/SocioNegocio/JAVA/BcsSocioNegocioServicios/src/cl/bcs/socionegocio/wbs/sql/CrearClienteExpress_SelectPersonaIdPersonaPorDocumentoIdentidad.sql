SELECT "IdPersona" as "Id"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
WHERE "IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad AND "DocumentoIdentidad"=:DocumentoIdentidad;