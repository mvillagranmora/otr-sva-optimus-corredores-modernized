UPDATE mov
SET "IdEstadoFacturacion" = 'ANU'
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" mov
WHERE "IdMovimientoComprobante" = (:IdMovimientoComprobante);