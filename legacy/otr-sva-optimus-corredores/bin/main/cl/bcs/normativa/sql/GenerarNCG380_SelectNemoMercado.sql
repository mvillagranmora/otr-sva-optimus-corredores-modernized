SELECT TOP 1 'S' AS "Existe" FROM "BSCL.Plataforma.BD::INS.Instrumento" i
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" ti ON i."IdTipoInstrumento" = ti."IdTipoInstrumento"
WHERE i."Nemotecnico" = :Nemotecnico AND ti."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento;