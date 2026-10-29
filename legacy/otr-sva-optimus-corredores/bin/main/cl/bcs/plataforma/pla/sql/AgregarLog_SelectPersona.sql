WITH PersonasConDocPriorizado AS (
  SELECT 
    PER."IdPersona",
    PER."NombreCompleto",
    DOC."DocumentoIdentidad",
    DOC."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
    ROW_NUMBER() OVER (
      PARTITION BY PER."IdPersona"
      ORDER BY
        CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN 0 ELSE 1 END,
        DOC."DocumentoIdentidad"
    ) AS "ROW_NUMBER"
  FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PER
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
    ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad" AND DOC."Borrado" = 'N'
  WHERE PER."IdPersona" IN (:IdPersona)
)
SELECT
  "IdPersona",
  "NombreCompleto",
  "DocumentoIdentidad",
  "TipoDocumentoIdentidad"
FROM PersonasConDocPriorizado
WHERE "ROW_NUMBER" = 1;