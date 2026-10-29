UPDATE "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja"
	SET "IdEstadoComision" = 'PRO'
WHERE "IdPatrimonioCaja" IN (
	SELECT PATCAJ."IdPatrimonioCaja"
	FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" AS PATCAJ
	LEFT JOIN "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones" PC ON PC."IdPeriodo" = :IdPeriodo
	WHERE (PATCAJ."FechaPatrimonio" BETWEEN PC."FechaDesde" AND PC."FechaHasta") AND PATCAJ."IdPortafolio" = :IdPortafolio --85937
		AND PATCAJ."TipoFechaConsulta" = 'LIQ' AND PATCAJ."IdEstadoComision" = 'VIG'
);