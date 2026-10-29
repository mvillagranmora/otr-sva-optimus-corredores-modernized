SELECT "IdPatrimonioCaja", "IdCliente", "IdPortafolio", "FechaPatrimonio", "TipoFechaConsulta", "MontoPatrimonio", "SaldoDisponible", "SaldoEnGarantia", "SaldoRetiro",
"SaldoAPlazoSimultanea", "SaldoAPlazoVentaCorta", "SaldoAPlazoCompromiso", "IdMoneda", "IdMonedaSistema", "IdEstado", "MontoPatrimonioAfectoComision", "MontoComision", "IdEstadoComision", 
"PorcentajeCobroComision"
FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja"
WHERE CAST("FechaPatrimonio" AS DATE) BETWEEN :p_Desde AND :p_Hasta
ORDER BY "IdPatrimonioCaja", "FechaPatrimonio" DESC
LIMIT :p_Limit OFFSET :p_Offset;