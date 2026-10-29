SELECT
INSTRUMENTO."Nemotecnico"
,TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento"
,INSTRUMENTO."IdTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO ON INSTRUMENTO."IdTipoInstrumento" = TIPO_INSTRUMENTO."IdTipoInstrumento"
LEFT JOIN "BSCL.Custodia.BD::CUS.HomologacionCuadraturaCircular" HOM 
	ON INSTRUMENTO."Nemotecnico" = HOM."Nemotecnico" 
	AND HOM."Archivo" = :Archivo
WHERE INSTRUMENTO."Nemotecnico" IN (:Nemotecnico)
AND (HOM."Mercado" = TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" OR (COALESCE(HOM."Nemotecnico", '') = '' AND TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" <> 'FM'));