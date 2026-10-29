UPDATE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = CASE WHEN :MarcarInactivo = 'S' THEN 'INA' ELSE "IdEstado" END
WHERE "IdCuentaCustodioPortafolio" = :IdCuentaCustodioPortafolio
