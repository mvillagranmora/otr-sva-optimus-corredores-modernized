UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET "IdNombreFantasiaPrincipal" = COALESCE(:IdNombreFantasia, 0)
WHERE "IdPersona" IN (:IdPersona);