SELECT
ENROLAMIENTO."IdEnrolamiento"
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" AS ENROLAMIENTO
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad" = ENROLAMIENTO."IdDocumentoIdentidad"
WHERE DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = :DocumentoIdentidad AND ENROLAMIENTO."Tipo" = :IdTipoPersona;