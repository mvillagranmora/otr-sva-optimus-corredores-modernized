SELECT COUNT(1) as "Contador"
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LIB"
WHERE "LIB"."IdLibroOrdenes" = :IdLibroOrdenes
AND "LIB"."IdEmpresa" = :IdEmpresa
