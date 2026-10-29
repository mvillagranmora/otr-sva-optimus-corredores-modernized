SELECT mcdt."IdMovimientoComprobanteDetalle"--,mct."IdComprobanteReferencia"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	mct
	INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt 	ON (mcdt."IdMovimientoComprobante"=mct."IdMovimientoComprobante")
	INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" 					com_org ON (com_org."IdComprobante"=mct."IdComprobanteReferencia" AND "CargoGananciaCap"='C' ) 

WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle) and 
mct."IdComprobanteReferencia">0;