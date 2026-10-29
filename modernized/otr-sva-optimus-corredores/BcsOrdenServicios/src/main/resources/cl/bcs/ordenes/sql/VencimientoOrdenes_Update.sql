UPDATE "BSCL.Orden.BD::ORD.Orden"
SET "OrdenVigente" = 'N', "IdEstadoOrden" = :IdEstado
WHERE "IdOrden" = :IdOrden;