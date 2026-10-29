SELECT 
DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
,DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE DOCUMENTO_IDENTIDAD."DocumentoIdentidad" IN (:DocumentoIdentidad)