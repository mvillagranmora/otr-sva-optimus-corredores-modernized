SELECT i."IdInstrumento", i."Nemotecnico", t."IdClasificacionTipoInstrumento",i."IdTipoInstrumento", i."CorteMinimo", i."IdEmisor"
FROM "BSCL.Plataforma.BD::INS.Instrumento" i 
    INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" t ON (i."IdTipoInstrumento" = t."IdTipoInstrumento")
WHERE t."IdClasificacionTipoInstrumento" = 'RF' AND i."IdInstrumento" IN (:p_IdInstrumento);