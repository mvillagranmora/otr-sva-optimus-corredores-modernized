SELECT RP."IdPersonaOrigen"          AS "IdEntidad"
     , COUNT(RP."IdRelacionPersona") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RP
WHERE RP."IdEstado" = 'ACT'
  AND RP."IdPersonaOrigen" IN (:IdsEntidades)
GROUP BY RP."IdPersonaOrigen";