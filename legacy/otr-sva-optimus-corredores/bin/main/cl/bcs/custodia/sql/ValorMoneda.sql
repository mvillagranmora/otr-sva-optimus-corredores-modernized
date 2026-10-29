WITH ValorMoneda AS (
  SELECT  "IdParametroFinanciero", "Valor",
  RANK() OVER (PARTITION by "IdParametroFinanciero" ORDER BY "Fecha" DESC) AS RANKS
  FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
  WHERE "IdParametroFinanciero" IN ('UF', 'DO','EUR') AND "Fecha" <= :Fecha
)

SELECT
    PCFA."IdParametroFinanciero",
    PCFA."Valor"
FROM
    ValorMoneda PCFA WHERE PCFA.RANKS = 1;