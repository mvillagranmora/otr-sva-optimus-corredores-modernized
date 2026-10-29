UPDATE "BSCL.Facturacion.BD::FAC.Comprobante"
SET "IdEstadoModPrefFact" = 'PRO'
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdComprobante" IN (:IdComprobante);