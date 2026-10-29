UPDATE LIB
SET LIB."Borrado" = 'S',
    LIB."IdEstado" = 'INA',
    LIB."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS LIB
WHERE LIB."IdLibroOrdenes" = :p_IdLibroOrdenes
  AND LIB."IdEmpresa" = :p_IdEmpresa;