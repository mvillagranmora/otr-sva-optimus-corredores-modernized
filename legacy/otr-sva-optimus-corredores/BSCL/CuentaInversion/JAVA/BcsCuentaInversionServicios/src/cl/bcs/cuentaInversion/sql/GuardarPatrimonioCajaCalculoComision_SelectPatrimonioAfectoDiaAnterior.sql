SELECT DISTINCT
	TP."IdPortafolio"
	, COALESCE(PC."MontoPatrimonioAfectoComision", 0) AS "MontoPatrimonioAfectoComision"
FROM #PatrimonioCaja_CobroComisionTablaPaso AS TP
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" AS PC ON PC."IdPortafolio" = TP."IdPortafolio"
WHERE PC."FechaPatrimonio" = TO_DATE(:p_FechaConsulta) AND PC."TipoFechaConsulta" = 'ADQ';