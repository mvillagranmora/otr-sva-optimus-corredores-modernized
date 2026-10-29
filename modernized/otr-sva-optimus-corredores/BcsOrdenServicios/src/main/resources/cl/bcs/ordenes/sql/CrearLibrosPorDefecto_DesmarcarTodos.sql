UPDATE LIB
   SET LIB."LibroPorDefecto" = 'N'
  FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS LIB
 WHERE LIB."IdEmpresa" = :p_IdEmpresa;
