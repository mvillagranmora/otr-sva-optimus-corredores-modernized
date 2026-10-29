SELECT SPT."IdOperacionSpot"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTE ON (MVTOCMBTE."IdComprobante"= :idComprobante AND SPT."IdOperacionSpot" = MVTOCMBTE."IdTransaccion")
WHERE SPT."Estado" = 'ANU';