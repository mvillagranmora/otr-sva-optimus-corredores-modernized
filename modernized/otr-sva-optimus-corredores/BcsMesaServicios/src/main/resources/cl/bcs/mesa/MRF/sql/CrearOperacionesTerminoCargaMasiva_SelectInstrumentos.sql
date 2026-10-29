SELECT
INSTRUMENTO."IdTipoInstrumento"
,INSTRUMENTO."IdInstrumento"
,INSTRUMENTO."FechaVencimiento"
,INSTRUMENTO."FechaEmision"
,INSTRUMENTO."IdMoneda"
,INSTRUMENTO."IdTipoReajuste"
,INSTRUMENTO."Nemotecnico"
,INSTRUMENTO."IdMonedaEmision"
,INSTRUMENTO."IdEmisor"
,TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento"
,INSTRUMENTO."CorteMinimo"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
WHERE INSTRUMENTO."Nemotecnico" IN (:Nemotecnico) AND "IdClasificacionTipoInstrumento" IN ('IF', 'RF');