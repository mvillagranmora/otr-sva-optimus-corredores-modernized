SELECT
DEF."IdParametroFinanciero"
,DEF."IdMonedaParametro" AS "IdMonedaOrigen"
,DEF."IdMonedaValor" AS "IdMonedaDestino"
,PAR."Fecha"
,PAR."Valor"
,1/PAR."Valor" AS "FactorInverso"
,USO."IdUso"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEF
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON PAR."IdParametroFinanciero" = DEF."IdParametroFinanciero"
LEFT JOIN "BSCL.Plataforma.BD::MER.UsoParidadParametroFinanciero" USO ON PAR."IdParametroFinanciero" = USO."IdParametroFinanciero"
WHERE DEF."IdMonedaParametro" = :IdMonedaPivote AND USO."IdUso" IN (:IdUso) AND PAR."Fecha" BETWEEN :FechaMinima AND :FechaMaxima AND NOT PAR."Valor" = 0
ORDER BY "Fecha" DESC, "Prioridad" ASC