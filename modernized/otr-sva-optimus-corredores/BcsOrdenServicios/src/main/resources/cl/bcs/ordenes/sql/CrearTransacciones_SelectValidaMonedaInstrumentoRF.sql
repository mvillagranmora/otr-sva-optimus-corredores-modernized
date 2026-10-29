SELECT ins."IdInstrumento"
	, CASE WHEN ( (ins."IdMoneda" = 'CLP' AND ins."IdMonedaEmision" = 'CLP') 
				OR (ins."IdMoneda" = 'CLP' AND ins."IdMonedaEmision" = 'UF')
				OR (ins."IdMoneda" = 'CLP' AND ins."IdMonedaEmision" = 'USD')
				OR (ins."IdMoneda" = 'USD' AND ins."IdMonedaEmision" = 'USD')
				OR (ins."IdMoneda" = 'CLP' AND ins."IdMonedaEmision" = 'EUR')
				OR (ins."IdMoneda" = 'EUR' AND ins."IdMonedaEmision" = 'EUR')
				OR (ins."IdMoneda" = 'CLP' AND ins."IdMonedaEmision" = 'ICP' AND ins."IdTipoReajuste" = 'ICP') )
			THEN 'S'
		ELSE 'N'
		END AS "MonedaEmisionValida"
FROM "BSCL.Plataforma.BD::INS.Instrumento" ins
WHERE ins."IdInstrumento" IN (:p_IdInstrumento);