SELECT "IdRelacionPersona"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"
WHERE "IdPersonaOrigen" IN (:IdPersona) AND "IdEstado" != 'BLO'