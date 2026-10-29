SELECT 
INS."Nemotecnico", 
TI."IdClasificacionTipoInstrumento"  
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TI ON INS."IdTipoInstrumento" = TI."IdTipoInstrumento"
WHERE INS."Nemotecnico" IN (:Instrumentos)
AND TI."IdClasificacionTipoInstrumento" = 'RV'