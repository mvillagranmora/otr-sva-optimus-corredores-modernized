SELECT "GD"."IdGrupoDireccion"
FROM "BSCL.SocioNegocio.BD::ENR.GrupoDireccion" AS "GD"
WHERE "GD"."IdGrupoDireccion" IN (:IdsGruposDirecciones);