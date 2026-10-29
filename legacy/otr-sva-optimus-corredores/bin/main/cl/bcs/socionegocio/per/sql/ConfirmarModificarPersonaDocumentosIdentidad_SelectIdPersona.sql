SELECT "IdDocumentoIdentidad" AS "IdDocumentoIdentidad", "IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdDocumentoIdentidad" IN (:IdDocumentoIdentidad);
