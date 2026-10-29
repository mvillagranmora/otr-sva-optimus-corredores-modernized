UPDATE Cmbte
SET "IdEstadoComprobante" = 'ANU'
FROM "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte
WHERE "IdComprobante" = (:IdComprobante);
        
