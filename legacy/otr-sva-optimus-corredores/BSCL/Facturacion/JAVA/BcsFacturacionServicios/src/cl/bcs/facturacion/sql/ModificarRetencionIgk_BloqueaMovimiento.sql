SELECT "IdComprobante","IdEstadoComprobante"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" As MvtoCmbte
WHERE MvtoCmbte."IdComprobante" IN (:idsComprobante) FOR UPDATE;
	    