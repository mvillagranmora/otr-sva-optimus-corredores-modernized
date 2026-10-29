SELECT "IdInstrumento","Fecha","FechaEncontrada",
CASE 
    WHEN :Fuente = 'VPG' THEN "ValorPrecioBase"
    WHEN :Fuente = 'VPG100' THEN "ValorPrecioAlgorithmics100"
    WHEN :Fuente = 'VPG98' THEN "ValorPrecioAlgorithmics98"
    WHEN :Fuente = 'VPG95' THEN "ValorPrecioAlgorithmics95"
END AS "Precio"
FROM 
(
	SELECT 
    	row_number() over (partition by ff."IdInstrumento",ff."Fecha" order by ff."IdInstrumento",ff."Fecha",PRE."FechaVector" desc) AS row_num,
    	ff."IdInstrumento",ff."Fecha",PRE."FechaVector" AS "FechaEncontrada",
    	PRE."ValorPrecioBase",PRE."ValorPrecioAlgorithmics100",PRE."ValorPrecioAlgorithmics98",PRE."ValorPrecioAlgorithmics95"
	FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_InstrumentoFecha" ff
	LEFT JOIN "BSCL.Plataforma.BD::INS.VectorPrecios" PRE ON PRE."ValorPrecioAlgorithmics100" > 0
	AND ff."IdInstrumento" = PRE."IdInstrumento" AND LEFT(PRE."FechaVector",10) <= ff."Fecha"
	WHERE ff."Correlativo" = :Correlativo AND PRE."IdInstrumento" IS NOT NULL
) 
WHERE row_num = 1
