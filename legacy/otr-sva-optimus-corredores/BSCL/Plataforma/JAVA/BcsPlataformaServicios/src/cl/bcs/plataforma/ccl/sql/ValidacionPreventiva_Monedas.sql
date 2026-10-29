SELECT "IdParametroFinanciero" AS "Valor1", CAST("Valor" AS NVARCHAR(100)) AS "Valor2", CAST("Fecha" AS NVARCHAR(10))  AS "Valor3"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
WHERE "Fecha" = :p_FechaSistema
AND "IdParametroFinanciero" IN ('DO','EUR');