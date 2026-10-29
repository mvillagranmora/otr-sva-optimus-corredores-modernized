SELECT COUNT(1) AS "Contador"
FROM "BSCL.Orden.BD::ORD.LibroOrdenes"
WHERE "IdEmpresa" = :p_IdEmpresa
  AND "NombreLibroOrdenes" = :p_NombreLibroOrdenes;
