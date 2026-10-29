SELECT ASIG."IdAsignacion",MOV."IdMovimientoComprobante"
FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV ON ASIG."IdAsignacion" = MOV."IdAsignacion"