SELECT COUNT(1) AS "Contador"
FROM "BSCL.Orden.BD::ORD.LibroOrdenes"
WHERE "IdEmpresa" = :p_IdEmpresa
  AND "IdLibroOrdenes" = :p_IdLibroOrdenes
  AND "LibroPorDefecto" = 'S';
