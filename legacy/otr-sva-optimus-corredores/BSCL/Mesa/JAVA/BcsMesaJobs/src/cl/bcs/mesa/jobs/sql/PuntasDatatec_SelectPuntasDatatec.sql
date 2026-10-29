SELECT 
    MAX(CASE WHEN "rnCompra" = 1 THEN "PuntaCompra" END) AS "PuntaCompra",
    MAX(CASE WHEN "rnVenta" = 1 THEN "PuntaVenta" END) AS "PuntaVenta", 
    MAX("Fecha") AS "Fecha"
FROM (
    SELECT 
        "PuntaCompra",
        ROW_NUMBER() OVER (ORDER BY CASE WHEN "PuntaCompra" IS NOT NULL THEN "Fecha" END DESC) "rnCompra",
        "PuntaVenta",
        ROW_NUMBER() OVER (ORDER BY CASE WHEN "PuntaVenta" IS NOT NULL THEN "Fecha" END DESC) "rnVenta",
        "Fecha"
    FROM 
       "BSCL.Mesa.BD::SPT.PuntasDatatec" GROUP BY "Fecha", "PuntaVenta", "PuntaCompra"
) t
WHERE "rnCompra" = 1 OR "rnVenta" = 1;