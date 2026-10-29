WITH
	CteTasa AS (
		SELECT
			RANK() OVER(
				PARTITION BY
					PrecioTasa."IdClasificacionTipoInstrumento"
					, PrecioTasa."IdInstrumento"
					, PrecioTasa."Nemotecnico"
					, PrecioTasa."IdMoneda"
				ORDER BY
					PrecioTasa."FechaOficializacion" DESC
			) AS "Ranking"
			, PrecioTasa."IdPrecioTasa" AS "IdPrecioTasa"
			, PrecioTasa."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, PrecioTasa."IdInstrumento" AS "IdInstrumento"
			, PrecioTasa."Nemotecnico" AS "Nemotecnico"
			, PrecioTasa."IdMoneda" AS "IdMoneda"
			, PrecioTasa."Tasa" AS "Tasa"
		FROM
			"BSCL.Plataforma.BD::TAS.PrecioTasa" PrecioTasa
		WHERE
				PrecioTasa."Borrado" = 'N'
			AND PrecioTasa."IdEstado" = 'PUB'
			AND PrecioTasa."IdTipoTasa" = 'TASA'
			AND PrecioTasa."TipoRegistro" = 'TASA'
			AND PrecioTasa."IdEmpresa" = :IdEmpresa
	)
	, CteInstrumento AS (
		SELECT
			Instrumento."IdInstrumento" AS "IdInstrumento"
			, Instrumento."IdTipoInstrumento" AS "IdTipoInstrumento"
			, TipoInstrumento."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, Instrumento."Nemotecnico" AS "Nemotecnico"
			, Instrumento."IdEmisor" AS "IdEmisor"
			, Instrumento."IdMoneda" AS "IdMonedaInstrumento"
			, Instrumento."Tasa" AS "Tasa"
			, Instrumento."FechaVencimiento" AS "FechaVencimiento"
		FROM
			"BSCL.Plataforma.BD::INS.Instrumento" Instrumento
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TipoInstrumento ON (TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento")		
	)
	, CteClasificacionRiesgo AS (
		SELECT
			ClasificacionRiesgoInstrumento."IdInstrumento" AS "IdInstrumento"
			, ClasificacionRiesgoInstrumento."IdEntidadClasificadoraRiesgo" AS "IdEntidadClasificadoraRiesgo"
			, ClasificacionRiesgoInstrumento."ValorClasificacionRiesgo" AS "ValorClasificacionRiesgo"
			, ClasificacionRiesgo."Nivel" AS "NivelClasificacionRiesgo"
		FROM
			"BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" ClasificacionRiesgoInstrumento
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Instrumento."IdInstrumento" = ClasificacionRiesgoInstrumento."IdInstrumento")
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" TipoInstrumentoClasificacionRiesgo ON (TipoInstrumentoClasificacionRiesgo."IdTipoInstrumento" = Instrumento."IdTipoInstrumento")
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ClasificacionRiesgo ON
				(
						ClasificacionRiesgo."IdTipoClasificacionRiesgo" = TipoInstrumentoClasificacionRiesgo."IdTipoClasificacionRiesgo"	
					AND ClasificacionRiesgo."IdEntidadClasificadoraRiesgo" = TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo"
					AND ClasificacionRiesgo."ClasificacionRiesgo" = ClasificacionRiesgoInstrumento."ValorClasificacionRiesgo"
				)
		WHERE
			TipoInstrumentoClasificacionRiesgo."EntidadClasificadoraRiesgoPrincipal" = 'S'
	)
SELECT
	Instrumento."IdInstrumento" AS "IdInstrumento"
	, Instrumento."IdTipoInstrumento" AS "IdTipoInstrumento"
	, Instrumento."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
	, Instrumento."Nemotecnico" AS "Nemotecnico"
	, Instrumento."IdEmisor" AS "IdEmisor"
	, Instrumento."IdMonedaInstrumento" AS "IdMonedaInstrumento"
	, COALESCE(Tasa."Tasa", Instrumento."Tasa") AS "Tasa"
	, Instrumento."FechaVencimiento" AS "FechaVencimiento"
	, ClasificacionRiesgo."IdEntidadClasificadoraRiesgo" AS "IdEntidadClasificadoraRiesgo"
	, ClasificacionRiesgo."ValorClasificacionRiesgo" AS "ValorClasificacionRiesgo"
	, ClasificacionRiesgo."NivelClasificacionRiesgo" AS "NivelClasificacionRiesgo"
FROM
	CteInstrumento Instrumento 
	LEFT JOIN CteTasa Tasa ON
		(
				Tasa."Ranking" = 1
			AND Tasa."IdInstrumento" = Instrumento."IdInstrumento"
			AND Tasa."Nemotecnico" = Instrumento."Nemotecnico"
			AND Tasa."IdMoneda" = Instrumento."IdMonedaInstrumento"
		)
	LEFT JOIN CteClasificacionRiesgo ClasificacionRiesgo ON (ClasificacionRiesgo."IdInstrumento" = Instrumento."IdInstrumento")
WHERE
	(
		(
				Instrumento."IdClasificacionTipoInstrumento" = 'IF'
			AND Instrumento."IdInstrumento" IN (:Nemotecnico)
		)
		OR
		(
				Instrumento."IdClasificacionTipoInstrumento" = 'RF'
			AND Instrumento."IdInstrumento" IN (:IdInstrumento)
		)
	);
