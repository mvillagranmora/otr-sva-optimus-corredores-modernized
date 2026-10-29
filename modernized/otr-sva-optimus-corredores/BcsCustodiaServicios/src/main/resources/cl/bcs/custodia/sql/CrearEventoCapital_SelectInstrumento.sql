SELECT I."IdInstrumento",I."Nemotecnico",TI."IdTipoInstrumento",CTI."IdClasificacionTipoInstrumento",I."IdEstado"
FROM "BSCL.Plataforma.BD::INS.Instrumento" I
    INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TI ON (TI."IdTipoInstrumento" = I."IdTipoInstrumento")
    INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CTI ON (CTI."IdClasificacionTipoInstrumento" = TI."IdClasificacionTipoInstrumento")
WHERE "IdInstrumento" IN (:p_IdInstrumentoNuevo)