SELECT 
DocumentoIdentidad."IdDocumentoIdentidad"
,DocumentoIdentidad."IdTipoDocumentoIdentidad" AS "TipoDocumento"
,DocumentoIdentidad."DocumentoIdentidad" AS "NumeroDocumento"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
WHERE "IdPersona" = :p_IdPersona