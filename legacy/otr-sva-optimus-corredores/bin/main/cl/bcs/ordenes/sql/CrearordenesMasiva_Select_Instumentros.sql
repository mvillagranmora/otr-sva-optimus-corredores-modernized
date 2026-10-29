SELECT	INS."IdInstrumento",
		INS."Nemotecnico",
		INS."IdEmisor",
		INS."IdMoneda",
		INS."IdTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON (TINS."IdTipoInstrumento" = INS."IdTipoInstrumento" AND "IdClasificacionTipoInstrumento" <> 'FM')
WHERE INS."Nemotecnico"  IN (:Nemotecnicos);