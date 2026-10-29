SELECT COUNT(1) AS "Contador"
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS LIB
WHERE LIB."IdLibroOrdenes" = :p_IdLibroOrdenes
  AND LIB."IdEmpresa" = :p_IdEmpresa;
