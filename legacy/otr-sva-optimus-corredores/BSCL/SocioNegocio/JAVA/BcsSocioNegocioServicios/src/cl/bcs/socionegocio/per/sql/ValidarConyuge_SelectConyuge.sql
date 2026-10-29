SELECT 
PERSONA."IdPersona"
,PERSONA."IdPersonaConyuge"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad" AS "DocumentoIdentidadPersona"
,DOCUMENTO_IDENTIDAD_CONYUGE."DocumentoIdentidad" AS "DocumentoIdentidadConyuge"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS CONYUGE ON PERSONA."IdPersonaConyuge" = CONYUGE."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD_CONYUGE ON CONYUGE."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD_CONYUGE."IdDocumentoIdentidad"
WHERE 
PERSONA."IdPersonaConyuge" != 0
AND PERSONA."IdPersonaConyuge" IS NOT NULL
AND PERSONA."IdPersonaConyuge" IN (:IdPersonaConyuge)
AND PERSONA."Borrado" = 'N'