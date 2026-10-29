UPDATE "BSCL.Custodia.BD::CUS.PatrimonioCustodia"
	SET "IdEstadoComision" = 'PRO'
WHERE "IdPatrimonioCustodia" IN (
	SELECT PATCUS."IdPatrimonioCustodia"
	FROM "BSCL.Custodia.BD::CUS.PatrimonioCustodia" PATCUS
	LEFT JOIN "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones" PC ON PC."IdPeriodo" = :IdPeriodo
	WHERE (PATCUS."FechaPatrimonio" BETWEEN PC."FechaDesde" AND PC."FechaHasta") AND PATCUS."IdPortafolio" = :IdPortafolio--85937
	AND PATCUS."TipoFechaConsulta" = 'LIQ' AND PATCUS."IdEstadoComision" = 'VIG'
);