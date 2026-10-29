SELECT "IdPersona" AS "IdPersona",
       "IdCuentaBancaria" AS "IdCuentaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
WHERE "IdCuentaBancaria" IN ({ID_LIST})
