WITH TransaccionesValidas AS (
    SELECT "IdTransaccion"
    FROM "BSCL.Orden.BD::ORD.Asignacion"
    WHERE "IdTransaccion" IN (:IdTransaccion)
      AND "IdEstadoAsignacion" NOT IN ('ANU')
    GROUP BY "IdTransaccion"
    HAVING COUNT("IdAsignacion") = 1
)
SELECT 
    A."IdTransaccion",
    A."IdAsignacion", 
    M."IdMovimientoComprobante",
    M."IdEstadoFacturacion"
FROM "BSCL.Orden.BD::ORD.Asignacion" A 
JOIN TransaccionesValidas T
    ON A."IdTransaccion" = T."IdTransaccion"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" M 
    ON A."IdAsignacion" = M."IdAsignacion"
   AND M."IdEstadoFacturacion" <> 'ANU'
WHERE A."IdEstadoAsignacion" NOT IN ('ANU');