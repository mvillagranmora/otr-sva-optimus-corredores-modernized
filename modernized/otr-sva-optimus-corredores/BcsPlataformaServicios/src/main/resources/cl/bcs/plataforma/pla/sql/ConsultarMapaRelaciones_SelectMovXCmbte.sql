SELECT CMBTE."IdComprobante",MOVDET."IdMovimientoComprobante"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CMBTEDET ON CMBTE."IdComprobante" = CMBTEDET."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MOVDET ON MOVDET."IdComprobanteDetalle" = CMBTEDET."IdComprobanteDetalle"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV ON MOV."IdMovimientoComprobante" = MOVDET."IdMovimientoComprobante"
