UPDATE ORD
SET "IdEstadoInterno" = 'AUT'
FROM "BSCL.Orden.BD::ORD.Orden" ORD
INNER JOIN "BSCL.Orden.BD::ORD.AutorizacionOrdenes_Entrada" CAOE
ON (ORD."IdOrden" = CAOE."IdOrden" AND "IdEstadoInterno" = 'PDA');