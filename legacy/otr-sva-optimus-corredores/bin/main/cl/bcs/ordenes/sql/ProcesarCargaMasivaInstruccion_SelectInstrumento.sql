SELECT
CLASIFICACION_TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento"
,INSTRUMENTO."IdTipoInstrumento"
,INSTRUMENTO."Nemotecnico"
,INSTRUMENTO."IdInstrumento"
,INSTRUMENTO."IdMonedaEmision"
,INSTRUMENTO."CorteMinimo"
,INSTRUMENTO."IdEmisor"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO ON (INSTRUMENTO."IdTipoInstrumento" = TIPO_INSTRUMENTO."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLASIFICACION_TIPO_INSTRUMENTO ON (TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" = CLASIFICACION_TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento")
WHERE INSTRUMENTO."Nemotecnico" IN (:Nemotecnico) AND INSTRUMENTO."IdEstado" = 'ACT' AND TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" <> 'FM';