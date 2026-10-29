UPDATE "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
SET "IdEstadoAutorizacion" = 'RZD',
    "IdEstado" = COALESCE(:IdEstado, "IdEstado")
WHERE "IdCuentaBancaria" = :IdCuentaBancaria;