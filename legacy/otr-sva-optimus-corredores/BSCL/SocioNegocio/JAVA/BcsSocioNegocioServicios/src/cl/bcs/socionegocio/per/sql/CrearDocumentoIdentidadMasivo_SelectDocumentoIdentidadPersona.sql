SELECT
"IdPersona"
,"IdTipoDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdPersona" IN (:IdPersona)