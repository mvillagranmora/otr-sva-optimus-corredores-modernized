UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1889"
SET
    "EjecutadoComplementoIPC" = 'S',
    "Factor" = :Factor,
    "RentabilidadActualizadaPositiva" = :RentabilidadActualizadaPositiva, 
    "RentabilidadActualizadaNegativa" = :RentabilidadActualizadaNegativa
WHERE "ID" = :ID;