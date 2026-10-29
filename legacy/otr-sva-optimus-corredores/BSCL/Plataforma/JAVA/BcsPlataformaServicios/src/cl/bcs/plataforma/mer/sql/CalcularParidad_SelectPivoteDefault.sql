SELECT
DEF."IdParametroFinanciero"
,DEF."IdMonedaParametro" AS "IdMonedaOrigen"
,DEF."IdMonedaValor" AS "IdMonedaDestino"
,PAR."Fecha"
,PAR."Valor"
,CASE
	WHEN IFNULL(PAR."Valor",0) = 0 THEN 0 
	ELSE 1/PAR."Valor" 
	END AS "FactorInverso"
	
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEF
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON PAR."IdParametroFinanciero" = DEF."IdParametroFinanciero"
WHERE DEF."IdParametroFinanciero" = :IdParametroFinanciero AND PAR."Fecha" BETWEEN :FechaMinima AND :FechaMaxima AND NOT PAR."Valor" = 0
ORDER BY "Fecha" DESC
