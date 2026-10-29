UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1899"
SET
    "EjecutadoComplementoMontos" = 'S',
    "DepositoEmpleadorMonedaDJ" = :DepositoEmpleadorMonedaDJ,
    "DepositoEmpleadorSegundaMonedaDJ" = :DepositoEmpleadorSegundaMonedaDJ,
    "AporteEmpleadorMonedaDJ" = :AporteEmpleadorMonedaDJ,
    "TraspasoEmpleadorMonedaDJ" = :TraspasoEmpleadorMonedaDJ,
    "RetiroEmpleadorActualizadoMonedaDJ" = :RetiroEmpleadorActualizadoMonedaDJ,
    "MontoRetencionActualizadoMonedaDJ" = :MontoRetencionActualizadoMonedaDJ,
    "AhorrosModalidadIndirectaEmpleadorMonedaDJ" = :AhorrosModalidadIndirectaEmpleadorMonedaDJ,
    "AhorrosModalidadIndirectaEmpleadorSegundaMonedaDJ" = :AhorrosModalidadIndirectaEmpleadorSegundaMonedaDJ,
    "AhorrosModalidadDirectaTrabajadorMonedaDJ" = :AhorrosModalidadDirectaTrabajadorMonedaDJ,
    "AhorrosModalidadDirectaTrabajadorSegundaMonedaDJ" = :AhorrosModalidadDirectaTrabajadorSegundaMonedaDJ,
    "CotizacionesModalidadIndirectaEmpleadorMonedaDJ" = :CotizacionesModalidadIndirectaEmpleadorMonedaDJ,
    "CotizacionesModalidadDirectaTrabajadorMonedaDJ" = :CotizacionesModalidadDirectaTrabajadorMonedaDJ,
    "CotizacionesTrabajadorIndependienteMonedaDJ" = :CotizacionesTrabajadorIndependienteMonedaDJ,
    "CotizacionesTrabajadorIndependienteSegundaMonedaDJ" = :CotizacionesTrabajadorIndependienteSegundaMonedaDJ,
    "MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ" = :MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ,
    "RetencionImpuestoSobreRetirosMonedaDJ" = :RetencionImpuestoSobreRetirosMonedaDJ,
    "MontoRetiroPorAhorrosIncisoSegundoMonedaDJ" = :MontoRetiroPorAhorrosIncisoSegundoMonedaDJ
WHERE "ID" = :ID;