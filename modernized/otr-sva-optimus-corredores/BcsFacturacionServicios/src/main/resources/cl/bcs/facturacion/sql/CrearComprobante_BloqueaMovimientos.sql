SELECT "IdMovimientoComprobante","IdEstadoFacturacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
WHERE MvtoCmbte."IdMovimientoComprobante" IN (:idsMovimientoComprobante) FOR UPDATE;               
	    