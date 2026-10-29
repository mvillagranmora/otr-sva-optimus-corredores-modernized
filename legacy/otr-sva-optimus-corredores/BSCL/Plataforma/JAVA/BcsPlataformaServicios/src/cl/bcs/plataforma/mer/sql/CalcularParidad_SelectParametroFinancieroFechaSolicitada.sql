SELECT
DEF."IdParametroFinanciero"
,DEF."IdMonedaParametro" AS "IdMonedaOrigen"
,DEF."IdMonedaValor" AS "IdMonedaDestino"
,PAR."Fecha"
,PAR."Valor"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEF
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON PAR."IdParametroFinanciero" = DEF."IdParametroFinanciero"
WHERE PAR."IdParametroFinanciero" IS NOT NULL AND DEF."IdMonedaParametro" IN (:IdMonedaParametro) AND PAR."Fecha" BETWEEN :FechaMinima AND :FechaMaxima AND NOT PAR."Valor" = 0
ORDER BY PAR."Fecha" DESC