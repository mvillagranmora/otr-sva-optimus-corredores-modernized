UPDATE ORD
SET "FechaAceptacion" = :FechaAceptacion, "FechaAutorizacion" = :FechaAutorizacion
FROM "BSCL.Orden.BD::ORD.Orden" ORD
WHERE ORD."IdOrden" = :IdOrden;