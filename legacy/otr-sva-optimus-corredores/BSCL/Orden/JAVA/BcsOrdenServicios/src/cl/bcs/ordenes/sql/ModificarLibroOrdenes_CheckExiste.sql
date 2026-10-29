SELECT COUNT(1) AS "Contador"
FROM "BSCL.Orden.BD::ORD.LibroOrdenes"
WHERE "IdLibroOrdenes" = :p_IdLibroOrdenes;
