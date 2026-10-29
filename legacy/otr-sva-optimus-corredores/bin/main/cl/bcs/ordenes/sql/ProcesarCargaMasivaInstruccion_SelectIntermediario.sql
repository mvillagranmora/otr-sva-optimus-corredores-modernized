SELECT
CORREDORA."IdCorredora" AS "IdIntermediario"
,CAST(CODIGO_CORREDORA."CodigoCorredora" AS BIGINT) AS "Intermediario"
FROM "BSCL.Plataforma.BD::INS.Corredora" AS CORREDORA
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CODIGO_CORREDORA ON CORREDORA."IdCorredora" = CODIGO_CORREDORA."IdCorredora"
WHERE CAST(CODIGO_CORREDORA."CodigoCorredora" AS BIGINT) IN (:Intermediario)