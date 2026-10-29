SELECT 
    "IdPersona" AS "IdPersona",
    "IdDocumentoIdentidad" AS "IdDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdDocumentoIdentidad" IN (:IdDocumentoIdentidad)
