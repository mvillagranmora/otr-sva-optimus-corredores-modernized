SELECT
     ORD."IdPortafolioCliente" AS "IdPortafolio"
    ,ORD."IdOrden"
    ,COALESCE(ASI."IdTransaccion", 0) AS "IdTransaccion"
FROM "BSCL.Orden.BD::ORD.Orden" ORD
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASI
    ON ASI."IdOrden" = ORD."IdOrden"
WHERE ORD."IdPortafolioCliente" IN (:IdsPortafolio);