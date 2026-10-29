SELECT "IdGrupoDireccion" AS "IdGrupoDireccion"
     , "IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion"
WHERE "IdGrupoDireccion" IN (:IdGrupoDireccion)
