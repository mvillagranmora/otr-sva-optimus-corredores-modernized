UPDATE mov
SET "IdEstadoFacturacion" = 'ING'
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" mov
WHERE "IdMovimientoComprobante" = (:IdMovimientoComprobante);
        
