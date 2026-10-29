SELECT ASIG."IdAsignacion",ORD."IdOrden"
FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ASIG."IdOrden" = ORD."IdOrden"