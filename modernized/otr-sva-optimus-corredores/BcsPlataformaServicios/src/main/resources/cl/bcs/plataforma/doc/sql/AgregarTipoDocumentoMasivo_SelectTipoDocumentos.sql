SELECT TD."IdTipoDocumento" AS "IdTipoDocumento"
     , TD."IdEmpresa"       AS "IdEmpresa"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TD
WHERE "IdTipoDocumento" IN (:IdsTipoDocumentos)
  AND "IdEmpresa" = :IdEmpresa;