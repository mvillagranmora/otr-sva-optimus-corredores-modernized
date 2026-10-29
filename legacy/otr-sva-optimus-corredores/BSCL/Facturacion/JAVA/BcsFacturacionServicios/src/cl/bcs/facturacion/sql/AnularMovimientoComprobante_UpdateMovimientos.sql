UPDATE MvtoCmbte 
SET "IdEstadoFacturacion" = 'ANU'
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
WHERE "IdMovimientoComprobante" = :IdMovimientoComprobante;       