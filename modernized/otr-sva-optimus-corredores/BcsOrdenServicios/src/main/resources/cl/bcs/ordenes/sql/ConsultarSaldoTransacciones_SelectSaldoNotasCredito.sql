
SELECT NC."IdComprobante" AS "IdComprobanteNotaCredito",NC."IdTipoComprobante",NC."IdEstadoComprobante",
CMBTE."IdComprobante",
MvtoCmbte."IdMovimientoComprobante",
Asig."IdAsignacion",Asig."IdEstadoAsignacion",
CASE Tran."EsFungible" WHEN 'S' THEN Asig."ValorRescateAsignado" WHEN 'N' THEN Asig."CantidadAsignada" ELSE 0 END AS "CantidadAsignada",
Tran."EsFungible",
Tran."IdTransaccion",
NC."IdTipoInstrumento"

FROM "BSCL.Facturacion.BD::FAC.Comprobante" NC
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON NC."IdComprobanteReferencia" = CMBTE."IdComprobante" AND CMBTE."IdEstadoComprobante" != 'ANU' AND NC."IdTipoComprobante" IN ('NCE','NEO','NCR')
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS CMBTEDET ON CMBTE."IdComprobante" = CMBTEDET."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MvtoCmbteDet ON CMBTEDET."IdComprobanteDetalle" = MvtoCmbteDet."IdComprobanteDetalle"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte ON MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteDet."IdMovimientoComprobante"
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" Asig ON Asig."IdAsignacion" = MvtoCmbte."IdAsignacion" AND Asig."IdEstadoAsignacion" IN ('EAF','REF')
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" Tran ON Tran."IdTransaccion" = Asig."IdTransaccion"

WHERE NC."IdEstadoComprobante" != 'ANU';