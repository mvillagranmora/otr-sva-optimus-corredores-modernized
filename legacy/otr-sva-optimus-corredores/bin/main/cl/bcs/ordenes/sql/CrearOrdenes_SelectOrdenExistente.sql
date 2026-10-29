SELECT "IdPortafolioCliente","IdTipoOperacion","IdInstrumento",COUNT(1)
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "IdPortafolioCliente" || "IdTipoOperacion" || "IdInstrumento" IN (:llaves) AND "OrdenVigente" = 'S' AND "FechaSistemaCreacion" = :fechaSistema
GROUP BY "IdPortafolioCliente","IdTipoOperacion","IdInstrumento"
HAVING COUNT(1) > 0;