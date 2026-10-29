SELECT COUNT(1) AS "Count"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdPersona" = :IdPersona AND "IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
