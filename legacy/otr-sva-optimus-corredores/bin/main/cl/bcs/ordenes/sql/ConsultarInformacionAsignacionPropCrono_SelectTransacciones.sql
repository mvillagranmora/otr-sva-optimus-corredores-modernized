SELECT "IdTransaccion",
	"CantidadDisponibleTransaccion","ValorRescateDisponibleTransaccion","PrecioTransaccion",
	"FechaOperacionTransaccion","IdLibroOrdenes","IdClasificacionTipoInstrumento","IdCondicionDeLiquidacion",
	"IdTipoInstrumento","AccionOrden","IdBolsa","FolioTransaccion","IdMonedaInstrumento",
	"NombreLibroOrdenes","Instrumento","IdTipoReajuste","Reservado","IdUsuarioReserva", "IdInstrumento"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar"
WHERE "FechaOperacionTransaccion" >= :v_FechaOperacionDesde AND "FechaOperacionTransaccion" <= :v_FechaOperacionHasta
AND "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
AND "IdInstrumento" = COALESCE(:p_IdInstrumento, "IdInstrumento")
AND "IdMonedaInstrumento" = COALESCE(:p_IdMonedaNegociacion, "IdMonedaInstrumento")
AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")
AND "IdCondicionDeLiquidacion" = COALESCE(:p_IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion")
AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
AND "IdTipoInstrumento" NOT IN ('SIM','RRV','RRF')
AND "IdClasificacionTipoInstrumento" IN ('RV','RF','DV')
AND "IdEmpresa" = :p_IdEmpresa;

