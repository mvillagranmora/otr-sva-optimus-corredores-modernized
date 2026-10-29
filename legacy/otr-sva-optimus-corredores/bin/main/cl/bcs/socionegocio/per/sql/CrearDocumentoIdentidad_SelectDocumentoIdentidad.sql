SELECT COUNT(1) AS "Count"
FROM   "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DocumentoIdentidad ON Persona."IdDocumentoIdentidad" = DocumentoIdentidad."IdDocumentoIdentidad"
WHERE DocumentoIdentidad."DocumentoIdentidad" = :DocumentoIdentidad AND DocumentoIdentidad."IdTipoDocumentoIdentidad" = :TipoDocumentoIdentidad