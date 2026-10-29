INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenes"(
    "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes","IdTipoLibroOrden","IdEstado","AsignacionAutomatica",
    "IdTipoAsignacion","Prioridad","LibroPorDefecto","FechaCreacion","FechaModificacion","IdInstrumento",
    "IdClienteOfertante","Cantidad","IdPlantillaCobros","FechaApertura","FechaCierre","Precio","IdBolsa","TipoSubasta")
VALUES(
    :p_out_RefId,:p_IdEmpresa,:p_NombreLibroOrdenes,:p_IdTipoLibroOrden,:p_IdEstado,:p_AsignacionAutomatica,:p_IdTipoAsignacion,
    :p_Prioridad,'N',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,:p_IdInstrumento,:p_IdClienteOfertante,:p_Cantidad,:p_IdPlantillaCobros,
    COALESCE(:p_FechaApertura,'0001-01-01'),COALESCE(:p_FechaCierre,'0001-01-01'),:p_Precio,:p_Bolsa,:p_TipoSubasta);
