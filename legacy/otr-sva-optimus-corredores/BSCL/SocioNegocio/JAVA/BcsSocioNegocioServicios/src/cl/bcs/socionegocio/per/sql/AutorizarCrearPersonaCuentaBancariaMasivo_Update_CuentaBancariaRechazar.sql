UPDATE "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = :IdEstado
WHERE "IdCuentaBancaria" = :IdCuentaBancaria
