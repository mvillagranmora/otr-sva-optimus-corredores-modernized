SELECT ORD."IdOrden", ORD."Nemotecnico", ORD."IdBolsa" AS "IdBolsaDistribucion", ORD."IdTipoCanalDistribucion"
FROM "BSCL.Orden.BD::ORD.AutorizacionOrdenes_Entrada" CAOE
INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON (CAOE."IdOrden" = ORD."IdOrden")
WHERE ORD."IdEstadoLimite" = 'AUT';