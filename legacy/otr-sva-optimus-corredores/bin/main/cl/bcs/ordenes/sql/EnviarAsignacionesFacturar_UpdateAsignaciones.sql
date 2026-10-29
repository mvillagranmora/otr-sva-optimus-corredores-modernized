UPDATE ASIG
SET ASIG."IdMovimientoComprobante" = :IdMovimientoComprobante, ASIG."IdOperacionSpot" = :IdOperacionSpot
FROM "BSCL.Orden.BD::ORD.Asignacion" asig
WHERE "IdAsignacion" = :IdAsignacion;