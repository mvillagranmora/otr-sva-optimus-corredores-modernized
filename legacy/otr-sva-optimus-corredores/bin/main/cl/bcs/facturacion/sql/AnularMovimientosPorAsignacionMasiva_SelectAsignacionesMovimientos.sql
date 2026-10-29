SELECT 
	"IdAsignacion" 
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
WHERE "IdMovimientoComprobante" IN (:IdsMovimientoComprobante)