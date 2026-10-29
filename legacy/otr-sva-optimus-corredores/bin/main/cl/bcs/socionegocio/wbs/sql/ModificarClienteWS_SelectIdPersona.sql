SELECT
PERSONA."IdPersona" AS "Id", PERSONA."IdTipoCanalIngresoCorredor" AS "IdTipoCanalIngresoCorredor"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE
DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = :DocumentoIdentidad
AND DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad;