SELECT "IdGrupoDireccion"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N'