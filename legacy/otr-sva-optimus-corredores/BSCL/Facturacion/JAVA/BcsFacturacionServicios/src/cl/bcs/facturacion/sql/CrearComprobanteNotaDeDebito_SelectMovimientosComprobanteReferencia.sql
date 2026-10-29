SELECT DISTINCT "IdComprobanteReferencia" AS "IdComprobante"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
WHERE "IdComprobanteReferencia" IN (:idsComprobanteReferencia)
   AND "IdTipoComprobante" in ('NCE','NCO','NCR')
   AND "IdEstadoFacturacion" <> 'ANU'; 