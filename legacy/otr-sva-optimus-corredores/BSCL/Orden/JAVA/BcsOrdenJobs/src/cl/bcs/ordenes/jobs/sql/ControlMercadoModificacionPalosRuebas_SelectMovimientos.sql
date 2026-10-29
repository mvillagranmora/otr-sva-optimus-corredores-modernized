SELECT "IdAsignacion", "IdTransaccion", "IdMovimientoComprobante", "IdPortafolio", "IdCliente", "IdEstadoFacturacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
WHERE "IdTransaccion" IN (:IdTransaccion) AND "IdEstadoFacturacion" <> 'ANU';