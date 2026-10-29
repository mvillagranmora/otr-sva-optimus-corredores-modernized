WITH "InformesOrdenados" AS (
    SELECT 
        "IdInforme",
        LEFT("IdInforme", LENGTH("IdInforme") - 19) AS "Key",
        RIGHT("IdInforme", 19) AS "FechaHora",
        ROW_NUMBER() OVER (PARTITION BY LEFT("IdInforme", LENGTH("IdInforme") - 19) 
        ORDER BY RIGHT("IdInforme", 19) DESC) AS "Rank"
    FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF"
)
SELECT 
    "IdInforme",
    "Key",
    "FechaHora"
FROM "InformesOrdenados"
WHERE "Rank" = 1
AND "Key" = :p_Key;