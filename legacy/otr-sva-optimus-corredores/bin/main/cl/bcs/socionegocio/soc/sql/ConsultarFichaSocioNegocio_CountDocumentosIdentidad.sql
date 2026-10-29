SELECT D."IdPersona"                   AS "IdEntidad"
     , COUNT(D."IdDocumentoIdentidad") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS D
WHERE D."Borrado" = 'N'
  AND D."IdEstado" = 'ACT'
  AND D."IdPersona" IN (:IdsEntidades)
GROUP BY D."IdPersona";