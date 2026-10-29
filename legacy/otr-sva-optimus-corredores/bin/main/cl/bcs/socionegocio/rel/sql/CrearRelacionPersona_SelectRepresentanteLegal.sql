SELECT "IdPersonaOrigen"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"
WHERE "IdPersonaOrigen"= :IdPersonaOrigen AND "IdTipoRelacion" = 'REP';