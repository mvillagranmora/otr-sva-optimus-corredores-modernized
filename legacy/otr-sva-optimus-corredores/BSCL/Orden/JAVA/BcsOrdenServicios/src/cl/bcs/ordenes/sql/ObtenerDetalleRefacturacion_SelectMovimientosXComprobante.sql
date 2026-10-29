SELECT mov."IdMovimientoComprobante",cmbte."IdComprobante",mov."IdInstrumento",mov."IdPortafolio",mov."IdCliente",mov."IdInstrumentoSubyacente",mov."IdTipoInstrumento",
mov."IdClasificacionTipoInstrumento",mov."IdAsignacion",mov."FechaVencimiento",mov."IdEmisor",mov."IdTipoReajuste",mov."EsFungible",mov."IdTipoOperacion",mov."IdBolsa",
mov."IdCondicionLiquidacion" AS "IdCondicionDeLiquidacion",mov."IdEstadoFacturacion",mov."Cantidad",mov."ValorRescate",mov."FechaMovimientoComprobante",mov."IdConceptoMovimiento",
mov."FechaLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" cmbte
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" detcmbte ON cmbte."IdComprobante" = detcmbte."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" detmov ON detcmbte."IdComprobanteDetalle" = detmov."IdComprobanteDetalle"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" mov ON detmov."IdMovimientoComprobante" = mov."IdMovimientoComprobante"
WHERE mov."IdComprobante" IN (:idsComprobante)
ORDER BY mov."IdComprobante"
;