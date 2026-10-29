SELECT 
INS."IdInstrumento"
,INS."IdTipoInstrumento"
,TIP."IdClasificacionTipoInstrumento"
,INS."IdMoneda"
,INS."Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIP ON TIP."IdTipoInstrumento" = INS."IdTipoInstrumento"
WHERE INS."IdInstrumento" IN (:IdInstrumento)