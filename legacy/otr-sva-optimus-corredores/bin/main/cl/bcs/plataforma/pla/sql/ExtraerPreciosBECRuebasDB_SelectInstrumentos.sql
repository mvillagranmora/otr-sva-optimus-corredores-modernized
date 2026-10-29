SELECT INS."IdInstrumento",
	   INS."Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento" AND "IdClasificacionTipoInstrumento" <> 'FM';