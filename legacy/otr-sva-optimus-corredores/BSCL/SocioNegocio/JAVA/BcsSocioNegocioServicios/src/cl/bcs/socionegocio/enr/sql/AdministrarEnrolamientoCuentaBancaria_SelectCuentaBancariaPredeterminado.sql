SELECT "IdCuentaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
WHERE "IdPersona" = (:IdPersona) AND "Predeterminado" = 'S'