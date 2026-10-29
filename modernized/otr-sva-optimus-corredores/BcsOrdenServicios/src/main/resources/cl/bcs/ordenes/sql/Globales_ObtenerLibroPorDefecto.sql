SELECT "IdLibroOrdenes" 
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" WHERE "LibroPorDefecto" = 'S' AND "IdEmpresa" = :idEmpresa;