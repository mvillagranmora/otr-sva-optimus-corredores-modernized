SELECT tipoins."IdClasificacionTipoInstrumento",tipoins."IdTipoInstrumento",tipoins."Nombre" AS "NombreTipoInstrumento",ins."IdInstrumento",ins."Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.Instrumento" ins
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins on ins."IdTipoInstrumento" = tipoins."IdTipoInstrumento"
WHERE tipoins."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento;