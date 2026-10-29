SELECT INS."IdInstrumento",ins."IdTipoInstrumento", ins."Tamano", 
	   ins."IdMoneda",tip."IdClasificacionTipoInstrumento",ins."IdTipoReajuste", ins."FechaVencimiento",ins."FechaEmision"
FROM "BSCL.Plataforma.BD::INS.Instrumento" ins
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tip on tip."IdTipoInstrumento" = ins."IdTipoInstrumento"
WHERE ins."IdInstrumento" in (:idsInstrumento);
