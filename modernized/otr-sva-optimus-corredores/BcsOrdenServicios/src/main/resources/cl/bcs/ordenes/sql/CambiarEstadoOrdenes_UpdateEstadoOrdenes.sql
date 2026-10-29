UPDATE ORD
SET ORD."IdEstadoOrden" = COALESCE(:IdEstadoOrden,ORD."IdEstadoOrden"),
ORD."OrdenVigente" = COALESCE(:OrdenVigente,ORD."OrdenVigente")
FROM "BSCL.Orden.BD::ORD.Orden" ORD
WHERE "IdOrden" = :IdOrden;
