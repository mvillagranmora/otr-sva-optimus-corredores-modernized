SELECT "IdAsignacion", "IdTransaccion", "IdMovimientoComprobante", "IdPortafolio", "IdCliente"
FROM (
    SELECT 
        "IdAsignacion",
        "IdTransaccion",
        "IdMovimientoComprobante",
        "IdPortafolio",
        "IdCliente",
        COUNT("IdTransaccion") OVER (PARTITION BY "IdTransaccion") AS contador
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
    WHERE "IdTransaccion" IN (:IdTransaccion)
      AND "IdEstadoFacturacion" = 'ING'
) 
WHERE contador = 1;