SELECT ASIG."IdAsignacion",ASIG."IdEstadoAsignacion",ASIG."CantidadAsignada",ASIG."MontoAsignado",ASIG."IdOrden",ASIG."IdTransaccion",ORD."IdMonedaOrden" AS "IdMonedaAsignacion"
FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ORD."IdOrden" = ASIG."IdOrden"
WHERE ASIG."IdAsignacion" IN (:ids)