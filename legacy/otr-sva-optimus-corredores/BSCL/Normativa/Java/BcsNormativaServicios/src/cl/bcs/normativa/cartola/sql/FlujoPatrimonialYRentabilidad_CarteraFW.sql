-- Forward

SELECT
    CART."IdCliente"
    ,CASE "IdMonedaSecundaria" WHEN 'CLP' THEN "INGMontoMonedaSecundaria"  ELSE "INGMontoMonedaPrincipal" END AS "Monto"
    ,"INGFechaOperacion" AS "FechaOperacion"
FROM  "BSCL.Custodia.BD::CUS.Cartera" AS CART
INNER JOIN #tmpTblClientesPortafolios AS TEMP ON (CART."IdCliente" = TEMP."IdCliente" AND CART."IdPortafolio" = TEMP."IdPortafolio")
WHERE "IdTipoOperacion" IN ('COMFWD','VENFWD') 
