SELECT GD."IdPersona"               AS "IdEntidad"
     , COUNT(GD."IdGrupoDireccion") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS GD
WHERE GD."Borrado" = 'N'
  AND GD."IdEstado" = 'ACT'
  AND GD."IdPersona" IN (:IdsEntidades)
GROUP BY GD."IdPersona";