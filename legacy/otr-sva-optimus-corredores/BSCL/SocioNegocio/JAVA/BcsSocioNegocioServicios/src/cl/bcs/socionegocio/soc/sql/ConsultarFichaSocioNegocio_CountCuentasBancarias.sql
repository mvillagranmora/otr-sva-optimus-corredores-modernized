SELECT CB."IdPersona"               AS "IdEntidad"
     , COUNT(CB."IdCuentaBancaria") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CB
WHERE CB."Borrado" = 'N'
  AND CB."IdEstado" = 'ACT'
  AND CB."IdPersona" IN (:IdsEntidades)
GROUP BY CB."IdPersona";