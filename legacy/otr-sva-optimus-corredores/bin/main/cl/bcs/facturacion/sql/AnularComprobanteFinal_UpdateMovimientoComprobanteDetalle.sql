UPDATE MvtoCmbteDet
SET "IdEstadoMovimientoComprobanteDetalle" = 'ANU'
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" As MvtoCmbteDet
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS CmbteDet ON MvtoCmbteDet."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle"
WHERE "IdComprobante" = (:IdComprobante);
