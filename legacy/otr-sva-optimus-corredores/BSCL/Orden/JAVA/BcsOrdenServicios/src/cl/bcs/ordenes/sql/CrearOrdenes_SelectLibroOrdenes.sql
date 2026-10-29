SELECT "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes","IdTipoLibroOrden","IdEstado","IdTipoAsignacion","Prioridad",
"FechaCreacion","FechaModificacion","UltimaFechaDeEjecucion","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo",
"IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","Borrado","LibroPorDefecto",
"AsignacionAutomatica","IdInstrumento","IdClienteOfertante","Cantidad","IdPlantillaCobros","FechaApertura","FechaCierre"
FROM "BSCL.Orden.BD::ORD.LibroOrdenes"
WHERE "IdLibroOrdenes" IN (:idsLibroOrdenes);