SELECT "IdInstrumento","Fecha","FechaEncontrada","PrecioMedio"
FROM 
(
	SELECT 
	row_number() over (partition by ff."IdInstrumento",ff."Fecha" order by ff."IdInstrumento",ff."Fecha",PRE."Fecha" desc) AS row_num,
	
	ff."IdInstrumento",ff."Fecha",PRE."Fecha" AS "FechaEncontrada",PRE."PrecioMedio"
	FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_InstrumentoFecha" ff
	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" PRE ON PRE."PrecioMedio" > 0
	AND ff."IdInstrumento" = PRE."IdInstrumento" AND PRE."Fecha" <= ff."Fecha"
	WHERE ff."Correlativo" = :Correlativo AND PRE."IdInstrumento" IS NOT NULL
) 
WHERE row_num = 1
