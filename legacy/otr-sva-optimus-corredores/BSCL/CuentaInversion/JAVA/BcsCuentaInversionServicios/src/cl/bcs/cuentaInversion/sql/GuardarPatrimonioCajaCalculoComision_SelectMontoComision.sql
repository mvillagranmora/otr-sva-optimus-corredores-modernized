SELECT DISTINCT
 CCOC."PortafolioAPV"
, CCOC."IdClasificacionTipoInstrumento"
, CCOC."OpcionCobro"
, CCOC."PorcentajeCobro"
, TP."IdPortafolio"
, TO_NVARCHAR(TP."FechaPatrimonio") AS "FechaPatrimonio"
, TP."MontoPatrimonioAfectoComision"
, CASE WHEN TP."FechaPatrimonio" >= CCOC."FechaVigencia" THEN  (COALESCE(CCOC."PorcentajeCobro", 0) / 365) * TP."MontoPatrimonioAfectoComision" ELSE 0 END AS "MontoComision"
FROM #PatrimonioCaja_CobroComisionTablaPaso AS TP
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON TP."IdPortafolio"  = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" AS CIP ON CIP."IdPortafolio" = TP."IdPortafolio" AND CIP."CodigoTipoIndicador" = 'COBROCUST'
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" AS CCOC ON CCOC."IdClasificacionTipoInstrumento" = 'CI' AND CCOC."PortafolioAPV" = POR."TienePropositoAPV" AND TO_NVARCHAR(CCOC."OpcionCobro") = CIP."ValorIndicador" AND CCOC."IdEstado" = 'ACT';