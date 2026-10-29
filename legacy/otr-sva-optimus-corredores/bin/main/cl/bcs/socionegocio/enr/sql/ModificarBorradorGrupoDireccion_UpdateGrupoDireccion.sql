UPDATE "BSCL.SocioNegocio.BD::ENR.GrupoDireccion" AS "GD"
SET "GD"."Grupo"      = :Grupo
  , "GD"."Comentario" = :Comentario
WHERE "GD"."IdGrupoDireccion" = :IdGrupoDireccion