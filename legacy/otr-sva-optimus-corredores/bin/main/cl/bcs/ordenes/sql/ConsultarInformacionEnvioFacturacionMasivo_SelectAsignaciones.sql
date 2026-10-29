SELECT "IdTransaccion","IdOrden","IdAsignacion","FolioTransaccion","CantidadAsignada","ValorRescateAsignado",
		"IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","AccionOrden","IdBolsa","IdMonedaInstrumento","IdMonedaOperacion","NombreLibroOrdenes","Instrumento","EsAPlazo",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsAPlazo") as "EsAPlazoUI", "IdAsignacionMasiva"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ASIG
WHERE "FechaAsignacion" >= :v_FechaAsignacionDesde AND "FechaAsignacion" <= :v_FechaAsignacionHasta
AND "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")			
AND "IdCondicionDeLiquidacion" = COALESCE(:p_IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion")
AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
AND "IdTipoInstrumento" NOT IN ('RRV','RRF')
AND "IdClasificacionTipoInstrumento" IN ('RV','RF','DV','IF')
AND "IdInstrumento" = COALESCE(:p_IdInstrumento,"IdInstrumento")
AND "IdAsignacion" = COALESCE(:p_IdAsignacion,"IdAsignacion")
AND "IdOrden" = COALESCE(:p_IdOrden,"IdOrden")
AND "FolioTransaccion" = COALESCE(:p_FolioTransaccion,"FolioTransaccion")
AND "IdInstrumento" = COALESCE(:p_IdInstrumento,"IdInstrumento")
AND COALESCE("MarcaAsignacion", '') = COALESCE(:p_MarcaAsignacion,"MarcaAsignacion",'')
AND "IdTipoCanalIngreso" = COALESCE(:p_IdTipoCanalIngreso,"IdTipoCanalIngreso")
AND "IdEstadoAsignacion" = 'NEF';