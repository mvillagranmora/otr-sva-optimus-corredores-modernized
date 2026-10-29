SELECT DISTINCT MC."IdMovimientoComprobante", CASE WHEN COMP_COM."IdEstadoComprobante" = 'ANU' OR COMP_COM."IdComprobanteReferencia" > 0 THEN 'ANU' ELSE MC."IdEstadoFacturacion" END AS "IdEstadoFacturacion", CM."IdComprobante"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" CM
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP on COMP."IdComprobante" = CM."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC ON MC."IdMovimientoComprobante" = CM."IdMovimientoComprobanteComisionMensual" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP_COM ON COMP_COM."IdComprobante" = CM."IdComprobanteComisionMensual" 
WHERE CM."IdComprobante" IN (:idsComprobante)