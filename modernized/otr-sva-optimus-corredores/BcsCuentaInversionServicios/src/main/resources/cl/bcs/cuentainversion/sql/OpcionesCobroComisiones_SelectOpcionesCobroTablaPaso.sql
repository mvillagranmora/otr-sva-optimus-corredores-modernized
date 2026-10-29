SELECT
     COALESCE(CCOC."IdOpcionCobro",0) AS "IdOpcionCobro"
    ,TP."OpcionCobro"
    ,TP."PortafolioAPV"
    ,TP."Mercado"
    ,TP."PorcentajeCobro"
    ,TP."Estado"
    ,TP."FechaIngreso"
    ,TP."FechaVigencia"
FROM #OpcionesCobro_TablaPaso AS TP
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" AS CCOC ON CCOC."IdOpcionCobro" = TP."IdOpcionCobro" OR (CCOC."OpcionCobro" = TP."OpcionCobro" AND CCOC."PortafolioAPV" = TP."PortafolioAPV" AND CCOC."IdClasificacionTipoInstrumento" = TP. "Mercado");