SELECT "IdCuentaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N'