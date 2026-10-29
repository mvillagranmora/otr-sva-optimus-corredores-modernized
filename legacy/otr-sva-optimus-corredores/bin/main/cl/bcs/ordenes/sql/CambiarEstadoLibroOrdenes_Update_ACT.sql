UPDATE LIB
SET LIB."IdEstado" = :p_IdEstado,
    LIB."IdTipoBloqueo" = :p_IdTipo,
    LIB."FechaBloqueo" = :p_Fecha,
    LIB."ComentarioBloqueo" = :p_Comentario,
    LIB."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS LIB
WHERE LIB."IdLibroOrdenes" = :p_IdLibroOrdenes
  AND LIB."IdEmpresa" = :p_IdEmpresa;