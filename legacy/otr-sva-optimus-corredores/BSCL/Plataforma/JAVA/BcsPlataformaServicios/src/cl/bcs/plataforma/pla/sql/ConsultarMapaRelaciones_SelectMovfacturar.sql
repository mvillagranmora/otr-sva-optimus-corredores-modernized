SELECT MOV."IdMovimientoComprobante",MOV."Monto",MOV."IdEstadoFacturacion",MOV."IdMoneda",MOV."IdTipoComprobante",TC."Nombre" AS "NombreTipoComprobante",EST."Nombre" AS "NombreEstadoFacturacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV
INNER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" TC ON MOV."IdTipoComprobante" = TC."IdTipoComprobante"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = MOV."IdEstadoFacturacion"
WHERE "IdMovimientoComprobante" IN (:ids)