SELECT "IdPortafolio", "IdMovimientoComprobante" 
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
WHERE "IdPortafolio" IN (:IdsPortafolio);