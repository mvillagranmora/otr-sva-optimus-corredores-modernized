SELECT CASE WHEN PF."IdParametroFinanciero" = 'DO' THEN 'USD'
		ELSE PF."IdParametroFinanciero" END AS "IdParametroFinanciero",
		PF."Valor",DPF."IdMonedaValor"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DPF ON (PF."IdParametroFinanciero" = DPF."IdParametroFinanciero")
WHERE PF."IdParametroFinanciero" IN ('DO','EUR','UF') AND
PF."Fecha" = :Fecha;