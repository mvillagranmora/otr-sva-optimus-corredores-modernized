SELECT
	Instrumento."IdInstrumento" AS "IdInstrumento"
	, Instrumento."Nemotecnico" AS "Nemotecnico"
	, Instrumento."RazonSocial" AS "RazonSocial"
	, TipoInstrumento."IdTipoInstrumento" AS "IdTipoInstrumento"
	, TipoInstrumento."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.Instrumento" Instrumento
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TipoInstrumento ON (TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento")
WHERE TipoInstrumento."IdTipoInstrumento" = :IdTipoInstrumento;
