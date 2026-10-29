SELECT "IdPortafolio", "IdComprobante" 
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdPortafolio" IN (:IdsPortafolio);