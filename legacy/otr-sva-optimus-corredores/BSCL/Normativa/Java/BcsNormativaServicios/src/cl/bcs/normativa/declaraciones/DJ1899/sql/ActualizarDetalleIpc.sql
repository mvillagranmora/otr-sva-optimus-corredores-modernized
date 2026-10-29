UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1899"
SET
    "EjecutadoComplementoIPC" = 'S',
    "RetiroEmpleadorActualizadoIPC" = :RetiroEmpleadorActualizadoIPC,
    "MontoRetencionActualizadoIPC" = :MontoRetencionActualizadoIPC,
    "MontoRetiroPorAhorrosIncisoPrimeroIPC" = :MontoRetiroPorAhorrosIncisoPrimeroIPC,
    "RetencionImpuestoSobreRetirosIPC" = :RetencionImpuestoSobreRetirosIPC
WHERE "ID" = :ID;