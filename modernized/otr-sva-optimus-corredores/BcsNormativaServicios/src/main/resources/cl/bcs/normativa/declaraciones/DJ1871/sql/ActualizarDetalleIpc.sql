UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1871"
SET
    "EjecutadoComplementoIPC" = 'S',
    "AhorrosTrabajadorActualizado" = :AhorrosTrabajadorActualizado, 
    "AhorrosDepositoActualizado" = :AhorrosDepositoActualizado,
    "AhorrosCotizacionesActualizado" = :AhorrosCotizacionesActualizado,
    --Retiros
    "RetirosTrabajadorActualizado" = :RetirosTrabajadorActualizado,
    "RetirosDepositoActualizado" = :RetirosDepositoActualizado,
    "RetirosCotizacionesActualizado" = :RetirosCotizacionesActualizado
WHERE "ID" = :ID;