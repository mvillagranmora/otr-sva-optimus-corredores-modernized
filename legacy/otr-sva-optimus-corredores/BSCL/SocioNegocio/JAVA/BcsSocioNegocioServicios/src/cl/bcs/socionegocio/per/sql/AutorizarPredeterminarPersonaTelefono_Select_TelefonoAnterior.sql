SELECT COALESCE("IdTelefonoPrincipal", 0) AS "IdTelefonoPrincipal"
FROM "BSCL.SocioNegocio.BD::PER.Persona"
WHERE "IdPersona" = :IdPersona
