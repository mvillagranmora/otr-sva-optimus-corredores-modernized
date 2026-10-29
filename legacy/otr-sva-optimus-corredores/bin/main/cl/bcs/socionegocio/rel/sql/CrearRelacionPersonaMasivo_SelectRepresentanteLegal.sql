SELECT "IdPersonaOrigen"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"
WHERE "IdPersonaOrigen" IN (:IdPersonaOrigen) AND "IdTipoRelacion" = 'REP';