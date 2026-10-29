UPDATE LIB
SET
    LIB."NombreLibroOrdenes" = :p_NombreLibroOrdenes,
    LIB."IdTipoLibroOrden" = :p_IdTipoLibroOrden,
    LIB."IdEstado" = :p_IdEstado,
    LIB."AsignacionAutomatica" = :p_AsignacionAutomatica,
    LIB."IdTipoAsignacion" = CASE WHEN :p_AsignacionAutomatica = 'S' THEN :p_IdTipoAsignacion ELSE NULL END,
    LIB."Prioridad" = :p_Prioridad,
    LIB."FechaModificacion" = CURRENT_UTCTIMESTAMP,
    LIB."IdInstrumento" = :p_IdInstrumento,
    LIB."IdClienteOfertante" = :p_IdClienteOfertante,
    LIB."Cantidad" = :p_Cantidad,
    LIB."IdPlantillaCobros" = :p_IdPlantillaCobros,
    LIB."FechaApertura" = COALESCE(:p_FechaApertura,'0001-01-01'),
    LIB."FechaCierre" = COALESCE(:p_FechaCierre,'0001-01-01'),
    LIB."Precio" = :p_Precio,
    LIB."IdBolsa" = :p_Bolsa,
    LIB."TipoSubasta" = :p_TipoSubasta
FROM "BSCL.Orden.BD::ORD.LibroOrdenes" LIB
WHERE LIB."IdLibroOrdenes" = :p_IdLibroOrdenes;
