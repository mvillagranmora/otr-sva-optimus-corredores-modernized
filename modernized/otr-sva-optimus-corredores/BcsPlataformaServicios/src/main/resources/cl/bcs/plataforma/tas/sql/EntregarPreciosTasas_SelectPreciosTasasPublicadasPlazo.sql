SELECT *
FROM (
	SELECT
		--row_number() over (partition by ff."Correlativo",ff."CodigoFuente",ff."IdTipoTasa",ff."IdTipoPrecio",ff."IdInstrumento",ff."Nemotecnico",ff."FechaDesde",ff."FechaHasta" order by "FechaSolicitud" desc) AS row_num,
		ff."IdInstrumento",
		ff."Nemotecnico",
		ff."FechaDesde",
		ff."FechaHasta",
		ff."IdMoneda",
		ins."IdTipoInstrumento",
		tip."IdClasificacionTipoInstrumento",
		PTP."IdPrecioTasa",
		PTP."CodigoFuente",
		PTP."IdTipoTasa",
		PTP."Tasa",
		PTP."TasaEstimada",
		PTP."IdTipoPrecio",
		PTP."Precio",
		PTP."FechaSolicitud",
		PTP."Plazo"
	FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_TasasPublicadas" AS ff	
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on (ff."IdInstrumento" = ins."IdInstrumento")
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tip on ins."IdTipoInstrumento" = tip."IdTipoInstrumento"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" AS PTP
		ON ( tip."IdClasificacionTipoInstrumento" = PTP."IdClasificacionTipoInstrumento" 
			 AND  (
			 		( PTP."IdClasificacionTipoInstrumento" IN ('IF') AND PTP."IdTipoInstrumento" = '' AND ff."IdMoneda" = PTP."IdMoneda" AND PTP."TipoTasa" = 'CURVA' AND PTP."Nemotecnico" = '' ) OR
			 		( PTP."IdClasificacionTipoInstrumento" IN ('RF','IF') AND ins."IdTipoInstrumento" = PTP."IdTipoInstrumento" AND ff."Nemotecnico" = PTP."Nemotecnico" ) OR
			 		( PTP."IdClasificacionTipoInstrumento" NOT IN ('RF','IF') AND ff."IdMoneda" = PTP."IdMoneda" AND PTP."IdTipoTasa" IN ('CURVA','PUNTO') OR (ins."IdTipoInstrumento" = PTP."IdTipoInstrumento" AND ins."Nemotecnico" = PTP."Nemotecnico") ) 
			 	  )
			 	  
		     AND ((PTP."IdTipoTasa" = ff."IdTipoTasa" AND PTP."Tasa" IS NOT NULL) OR (PTP."IdTipoPrecio" = ff."IdTipoPrecio" AND PTP."Precio" IS NOT NULL))
			 AND PTP."IdEmpresa"	  = :IdEmpresa
			 AND PTP."CodigoFuente"	 = ff."CodigoFuente"
		   )
			 
	WHERE PTP."IdEstado"  = 'PUB'   -- Publicado
	AND   PTP."Borrado"   = 'N'
	AND	  ff."Correlativo" = :Correlativo
	AND   CAST(PTP."FechaSolicitud" AS DATE) BETWEEN ff."FechaPlazo"  AND ff."FechaActual"
)
--WHERE row_num = 1
;
