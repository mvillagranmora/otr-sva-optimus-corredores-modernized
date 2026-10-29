SELECT "IdInstrumento","Fecha","FechaDesde" AS "FechaEncontrada","Tasa","Precio"
FROM 
(
	SELECT
		row_number() over (partition by ff."IdInstrumento",ff."Fecha" order by ff."IdInstrumento",ff."Fecha",CAST(ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoDesde",0)) AS DATE) desc) AS row_num,
		"Correlativo",
		ff."IdInstrumento",ff."Fecha",PTTI."Tasa",PTTI."Precio",
		CAST(ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoDesde",0)) AS DATE) AS "FechaDesde",
		CAST(ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoHasta",0)) AS DATE) AS "FechaHasta"
		
	FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_InstrumentoFecha" ff
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON ff."IdInstrumento" = ins."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento" PTTI ON (ins."IdTipoInstrumento" = PTTI."IdTipoInstrumento" AND PTTI."IdEstado" = 'ACT')
	WHERE "Correlativo" = :Correlativo
	AND CAST(ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoDesde",0)) AS DATE) <= ff."Fecha"
	AND   CAST(ADD_DAYS(PTTI."FechaModificacion", COALESCE(PTTI."PlazoHasta",0)) AS DATE) >= ff."Fecha"
	AND (PTTI."Precio" > 0 OR PTTI."Tasa" > 0)
)
WHERE row_num = 1