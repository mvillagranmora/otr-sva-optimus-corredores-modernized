WITH ASDF AS (
	SELECT
		ent."IdInterno",
		par."IdParametroFinanciero",CASE defpar."Periodo" WHEN 'DI' THEN par."Fecha" WHEN 'ME' THEN par."AgnoMes" || '-01' WHEN 'AN' THEN par."Agno" || '-01-01' END as "FechaParametroFinanciero",defpar."Nombre",par."Valor",defpar."IdMonedaValor" as "IdMoneda",
	ROW_NUMBER() OVER (partition by "IdInterno",par."IdParametroFinanciero" order by par."IdParametroFinanciero",par."Fecha" DESC) AS "Prioridad"
	FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada" ent
	INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" par ON ent."IdMonedaOrigen" = (CASE par."IdParametroFinanciero" WHEN 'DO' THEN 'USD' ELSE par."IdParametroFinanciero" END) AND par."Fecha" <= ent."Fecha"
	INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" defpar ON defpar."IdParametroFinanciero" = par."IdParametroFinanciero"
	WHERE "IdTrabajo" = :p_IdTrabajo
)
SELECT "IdInterno","IdParametroFinanciero","FechaParametroFinanciero","Nombre","Valor","IdMoneda"
FROM ASDF WHERE "Prioridad" = 1;
