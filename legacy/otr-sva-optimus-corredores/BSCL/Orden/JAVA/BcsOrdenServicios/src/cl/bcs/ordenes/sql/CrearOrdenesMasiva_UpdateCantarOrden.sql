UPDATE "BSCL.Orden.BD::ORD.Orden"
SET "IdEstadoOrden" = 'OFE'
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "IdOrden" = :IdOrden;