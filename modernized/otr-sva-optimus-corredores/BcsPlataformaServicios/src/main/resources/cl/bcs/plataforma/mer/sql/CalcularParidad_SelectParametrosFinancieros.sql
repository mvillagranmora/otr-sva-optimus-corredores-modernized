SELECT
DEF."IdParametroFinanciero"
,DEF."IdMonedaParametro" AS "IdMonedaOrigen"
,DEF."IdMonedaValor" AS "IdMonedaDestino"
,PAR."Fecha"
,PAR."Valor"
,1/PAR."Valor" AS "FactorInverso"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEF
INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON PAR."IdParametroFinanciero" = DEF."IdParametroFinanciero"
WHERE DEF."IdParametroFinanciero" IN (:IdParametroFinanciero) AND PAR."Fecha" BETWEEN :FechaMinima AND :FechaMaxima AND NOT PAR."Valor" = 0
ORDER BY PAR."Fecha" DESC