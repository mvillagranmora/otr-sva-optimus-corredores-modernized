SELECT DF."IdPersona"                AS "IdEntidad"
     , COUNT(DF."IdDireccionFisica") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DF
WHERE DF."Borrado" = 'N'
  AND DF."IdEstado" = 'ACT'
  AND DF."IdPersona" IN (:IdsEntidades)
GROUP BY DF."IdPersona";
