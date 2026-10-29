SELECT MOV."IdMovimientoComprobante"
FROM "BSCL.Orden.BD::ORD.Asignacion" AS ASI
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOV ON MOV."IdTransaccion" = ASI."IdTransaccion" 
WHERE
    ASI."IdAsignacionMasiva" IN (:IdAsignacionMasiva)
    AND MOV."IdClasificacionTipoInstrumento" = 'RV'
    AND MOV."IdEstadoFacturacion" = 'ING';