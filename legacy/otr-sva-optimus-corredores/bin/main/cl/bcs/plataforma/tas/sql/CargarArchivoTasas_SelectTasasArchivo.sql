SELECT "IdTipoTasa"
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
WHERE "IdTipoTasa"		= :IdTipoTasa
	AND "IdFuente"		= :IdFuente
	AND ("FechaSolicitud"	BETWEEN :FechaDesde AND :FechaHasta)
	AND "IdEmpresa"		= :IdEmpresa
	AND "Borrado"			= 'N'
	AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento;
			