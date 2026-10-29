SELECT
	Instrumento."IdMoneda"
   ,Instrumento."IdTipoInstrumento"
   ,TipoInstrumento."IdClasificacionTipoInstrumento"
   ,Instrumento."IdTipoReajuste"
   ,Instrumento."IdEmisor"
   ,Instrumento."FechaEmision"
   ,Instrumento."FechaVencimiento"
   ,Instrumento."CorteMinimo"
   ,Instrumento."Nemotecnico"
   ,Instrumento."IdInstrumento"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
WHERE Instrumento."IdInstrumento" = :p_IdInstrumento;