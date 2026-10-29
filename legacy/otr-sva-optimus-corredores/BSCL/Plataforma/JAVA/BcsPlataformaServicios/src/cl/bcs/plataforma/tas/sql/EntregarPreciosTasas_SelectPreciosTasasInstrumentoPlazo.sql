SELECT "IdTipoInstrumento", "IdTipoReajuste", "Plazo", "FechaModificacion" AS "FechaEncontrada", "Tasa", "Precio"
FROM 
(
	SELECT	ROW_NUMBER() OVER (	PARTITION BY FF."IdTipoInstrumento", FF."IdTipoReajuste", FF."Plazo"
								ORDER BY  FF."IdTipoInstrumento", FF."IdTipoReajuste", PT."PlazoDesde" DESC) AS ROW_NUM,
			"Correlativo", FF."IdTipoInstrumento", FF."IdTipoReajuste", FF."Plazo", PT."Tasa", PT."Precio", PT."FechaModificacion"	
	FROM	"BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_InstrumentoPlazo" FF
			LEFT JOIN 	"BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento" PT 
				 	ON 	FF."IdTipoInstrumento" = PT."IdTipoInstrumento" 
				 		AND FF."IdTipoReajuste" = PT."IdTipoReajuste"
				 		AND PT."IdEstado" = 'ACT'
	WHERE 	"Correlativo" = :Correlativo
	AND 	PT."PlazoDesde" <= FF."Plazo" AND PT."PlazoHasta" >= ff."Plazo"
	AND 	(PT."Precio" > 0 OR PT."Tasa" > 0)
)
WHERE ROW_NUM = 1
