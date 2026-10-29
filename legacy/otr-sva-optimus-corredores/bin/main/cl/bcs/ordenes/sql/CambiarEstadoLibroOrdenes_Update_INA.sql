UPDATE LIB
SET LIB."IdEstado" = :p_IdEstado,
    LIB."IdTipoDesbloqueo" = :p_IdTipo,
    LIB."FechaDesbloqueo" = :p_Fecha,
    LIB."ComentarioDesbloqueo" = :p_Comentario,
    LIB."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS LIB
WHERE LIB."IdLibroOrdenes" = :p_IdLibroOrdenes
  AND LIB."IdEmpresa" = :p_IdEmpresa;