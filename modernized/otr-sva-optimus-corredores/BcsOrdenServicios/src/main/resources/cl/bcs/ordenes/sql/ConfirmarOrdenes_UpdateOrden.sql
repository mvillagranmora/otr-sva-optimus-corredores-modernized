UPDATE "BSCL.Orden.BD::ORD.Orden" 
SET "IdEstadoConfirmacion" = :IdEstadoConfirmacion
    ,"FechaConfirmacion" = :FechaConfirmacion 
WHERE "IdOrden" IN (:IdOrden);