SELECT "IdPortafolioTitularPago" AS "IdPortafolio","IdComprobante" 
FROM "BSCL.Tesoreria.BD::TES.Comprobante" 
WHERE "IdPortafolioTitularPago" IN (:IdsPortafolio);