SELECT TO_BIGINT(COALESCE("DiferenciaMontoAsignacion",0)) AS "ValorPivot"
FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot"
WHERE "IdEmpresa" = :IdEmpresa