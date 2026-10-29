SELECT 
INSTRUMENTO."Nemotecnico"
,TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento"
,INSTRUMENTO."CorteMinimo"
,INSTRUMENTO."FechaVencimiento"
,INSTRUMENTO."IdMoneda"
,INSTRUMENTO."IdTipoReajuste"
,INSTRUMENTO."IdMonedaEmision"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
WHERE INSTRUMENTO."Nemotecnico" IN (:Nemotecnico) AND "IdClasificacionTipoInstrumento" IN ('IF', 'RF');