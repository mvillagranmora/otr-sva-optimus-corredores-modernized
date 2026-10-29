SELECT "IdOrden","FechaOperacionOrden","IdLibroOrdenes","IdClasificacionTipoInstrumento","IdCondicionDeLiquidacion","IdTipoInstrumento",
"AccionOrden","IdBolsa","TipoPrecio","CantidadDisponibleOrden","PrecioOrdenOrigen" AS "PrecioOrden","NombreLibroOrdenes","IdMonedaOrden","Instrumento",
"IdInstrumento"
FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenPorAsignar"
WHERE "FechaOperacionOrden" <= :v_FechaOperacionHasta
AND "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
AND "IdInstrumento" = COALESCE(:p_IdInstrumento,"IdInstrumento")
AND "IdMonedaOrden" = COALESCE(:p_IdMonedaNegociacion,"IdMonedaOrden")
AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")
AND "IdCondicionDeLiquidacion" = COALESCE(:p_IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion")
AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
AND "IdTipoInstrumento" NOT IN ('SIM','RRV','RRF')
AND "IdClasificacionTipoInstrumento" IN ('RV','RF','DV')
AND "EsOrdenEspecial" = 'N'
AND "IdEmpresa" = :p_IdEmpresa
AND "IdTipoCanalIngreso" = COALESCE(:p_IdTipoCanalIngreso,"IdTipoCanalIngreso");