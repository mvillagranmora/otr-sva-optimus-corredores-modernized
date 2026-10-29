WITH a AS (
    SELECT 1 AS "IdSaldo", 'Saldo_0-5' AS "Saldo", 0 AS "Desde", 4 AS "Hasta" FROM dummy UNION ALL
    SELECT 2 AS "IdSaldo", 'Saldo_5-10' AS "Saldo", 5 AS "Desde", 9 AS "Hasta" FROM dummy UNION ALL
    SELECT 3 AS "IdSaldo", 'Saldo_10-20' AS "Saldo", 10 AS "Desde", 19 AS "Hasta" FROM dummy UNION ALL
    SELECT 4 AS "IdSaldo", 'Saldo_20-50' AS "Saldo", 20 AS "Desde", 49 AS "Hasta" FROM dummy UNION ALL
    SELECT 5 AS "IdSaldo", 'Saldo_50-100' AS "Saldo", 50 AS "Desde", 99 AS "Hasta" FROM dummy UNION ALL
    SELECT 6 AS "IdSaldo", 'Saldo_100-200' AS "Saldo", 100 AS "Desde", 199 AS "Hasta" FROM dummy UNION ALL
    SELECT 7 AS "IdSaldo", 'Saldo_200-500' AS "Saldo", 200 AS "Desde", 499 AS "Hasta" FROM dummy UNION ALL
    SELECT 8 AS "IdSaldo", 'Saldo_500-1000' AS "Saldo", 500 AS "Desde", 999 AS "Hasta" FROM dummy UNION ALL
    SELECT 9 AS "IdSaldo", 'Saldo_1000-2000' AS "Saldo", 1000 AS "Desde", 1999 AS "Hasta" FROM dummy UNION ALL
    SELECT 10 AS "IdSaldo", 'Saldo_2000-3000' AS "Saldo", 2000 AS "Desde", 2999 AS "Hasta" FROM dummy UNION ALL
    SELECT 11 AS "IdSaldo", 'Saldo_3000-4000' AS "Saldo", 3000 AS "Desde", 3999 AS "Hasta" FROM dummy UNION ALL
    SELECT 12 AS "IdSaldo", 'Saldo_4000-5000' AS "Saldo", 4000 AS "Desde", 4999 AS "Hasta" FROM dummy UNION ALL
    SELECT 13 AS "IdSaldo", 'Saldo_5000-7000' AS "Saldo", 5000 AS "Desde", 6999 AS "Hasta" FROM dummy UNION ALL
    SELECT 14 AS "IdSaldo", 'Saldo_7000-10000' AS "Saldo", 7000 AS "Desde", 9999 AS "Hasta" FROM dummy UNION ALL
    SELECT 15 AS "IdSaldo", 'Saldo_10000-15000' AS "Saldo", 10000 AS "Desde", 14999 AS "Hasta" FROM dummy UNION ALL
    SELECT 16 AS "IdSaldo", 'Saldo_15000-20000' AS "Saldo", 15000 AS "Desde", 19999 AS "Hasta" FROM dummy UNION ALL
    SELECT 17 AS "IdSaldo", 'Saldo_20000-30000' AS "Saldo", 20000 AS "Desde", 29999 AS "Hasta" FROM dummy UNION ALL
    SELECT 18 AS "IdSaldo", 'Saldo_30000-40000' AS "Saldo", 30000 AS "Desde", 39999 AS "Hasta" FROM dummy UNION ALL
    SELECT 19 AS "IdSaldo", 'Saldo_40000-50000' AS "Saldo", 40000 AS "Desde", 49999 AS "Hasta" FROM dummy UNION ALL
    SELECT 20 AS "IdSaldo", 'Saldo_50000-60000' AS "Saldo", 50000 AS "Desde", 59999 AS "Hasta" FROM dummy UNION ALL
    SELECT 21 AS "IdSaldo", 'Saldo_60000-80000' AS "Saldo", 60000 AS "Desde", 79999 AS "Hasta" FROM dummy UNION ALL
    SELECT 22 AS "IdSaldo", 'Saldo_80000-100000' AS "Saldo", 80000 AS "Desde", 99999 AS "Hasta" FROM dummy UNION ALL
    SELECT 23 AS "IdSaldo", 'Saldo_100000-mas' AS "Saldo", 100000 AS "Desde", 999999999 AS "Hasta" FROM dummy
)
SELECT
    a."IdSaldo",
    a."Saldo",
    to_bigint(SUM(CASE WHEN b."Tramo" = 1 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_20_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 1 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_20_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 2 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_20_25_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 2 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_20_25_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 3 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_25_30_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 3 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_25_30_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 4 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_30_35_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 4 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_30_35_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 5 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_35_40_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 5 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_35_40_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 6 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_40_45_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 6 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_40_45_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 7 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_45_50_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 7 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_45_50_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 8 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_50_55_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 8 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_50_55_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 9 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_55_60_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 9 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_55_60_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 10 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_60_65_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 10 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_60_65_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 11 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_65_70_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 11 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_65_70_Femenino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 12 AND b."Sexo" = 'M' THEN 1 ELSE 0 END)) AS "Saldo_Edad_70_Mas_Masculino",
    to_bigint(SUM(CASE WHEN b."Tramo" = 12 AND b."Sexo" = 'F' THEN 1 ELSE 0 END)) AS "Saldo_Edad_70_Mas_Femenino"
FROM a
LEFT JOIN (
SELECT 
       c."Tramo",c."Sexo",to_bigint(SUM(c."Monto") / 1000) AS "Monto", c."IdCliente"
    FROM (
        SELECT
            CASE WHEN b."Edad" > -1 AND b."Edad" <= 20 THEN 1
                 WHEN b."Edad" > 20 AND b."Edad" <= 25 THEN 2
                 WHEN b."Edad" > 25 AND b."Edad" <= 30 THEN 3
                 WHEN b."Edad" > 30 AND b."Edad" <= 35 THEN 4
                 WHEN b."Edad" > 35 AND b."Edad" <= 40 THEN 5
                 WHEN b."Edad" > 40 AND b."Edad" <= 45 THEN 6
                 WHEN b."Edad" > 45 AND b."Edad" <= 50 THEN 7
                 WHEN b."Edad" > 50 AND b."Edad" <= 55 THEN 8
                 WHEN b."Edad" > 55 AND b."Edad" <= 60 THEN 9
                 WHEN b."Edad" > 60 AND b."Edad" <= 65 THEN 10
                 WHEN b."Edad" > 65 AND b."Edad" <= 70 THEN 11
                 ELSE 12
            END AS "Tramo",
            b.*
        FROM (
            SELECT
			    "IdCliente",:Fecha AS "Fecha","FechaNacimiento",
			    YEARS_BETWEEN (TO_DATE ("FechaNacimiento", 'YYYY-MM-DD'), TO_DATE(:Fecha, 'YYYY-MM-DD')) AS "Edad",
			    COALESCE("Monto",0) AS "Monto","Sexo"
			FROM (
				SELECT SC."IdCliente",SC."FechaNacimiento",SC."Sexo",
					("SaldoCustodia"+"SaldoCuentaInversion") AS "Monto"
				FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS SC
				WHERE
					SC."CodigoPortafolio" IN ('APV-A','APV-AN','APV-B')
			)
        ) AS b
    ) AS c
    GROUP BY c."Tramo",c."Sexo",c."IdCliente"
    ) b ON (a."Desde" <=b."Monto" and a."Hasta" >=b."Monto")
GROUP BY a."IdSaldo",a."Saldo"
ORDER BY a."IdSaldo";
