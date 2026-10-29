SELECT
"IdPersona" AS "IdPersona"
,COALESCE("IdDocumentoIdentidad",0) AS "IdDocumentoIdentidad"
,COALESCE("IdTipoDocumentoIdentidad",'') AS "TipoDocumento"
,COALESCE("DocumentoIdentidad",'') AS "NumeroDocumento"
,COALESCE("NumeroSerie",'') AS "NumeroSerie"
,CAST("FechaEmision" AS VARCHAR(10)) AS "FechaEmision"
,CAST("FechaVencimiento" AS VARCHAR(10)) AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdPersona" IN (:p_IdPersonas)