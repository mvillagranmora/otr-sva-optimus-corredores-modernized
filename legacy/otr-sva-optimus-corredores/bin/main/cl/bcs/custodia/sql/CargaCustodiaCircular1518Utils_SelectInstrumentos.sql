SELECT
INSTRUMENTO."Nemotecnico"
,TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento"
,INSTRUMENTO."IdTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO ON INSTRUMENTO."IdTipoInstrumento" = TIPO_INSTRUMENTO."IdTipoInstrumento"
WHERE 	INSTRUMENTO."Nemotecnico" IN (:Nemotecnico)
		AND (
			:FiltroIdClasificacionTipoInstrumento = 0
			OR TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento)
		)