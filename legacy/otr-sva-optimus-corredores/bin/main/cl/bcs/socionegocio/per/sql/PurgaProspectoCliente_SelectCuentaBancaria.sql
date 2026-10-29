SELECT "IdPersona", "IdCuentaBancaria", "NumeroCuentaBancaria" 
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
WHERE "IdPersona" IN (:IdsPersonas);