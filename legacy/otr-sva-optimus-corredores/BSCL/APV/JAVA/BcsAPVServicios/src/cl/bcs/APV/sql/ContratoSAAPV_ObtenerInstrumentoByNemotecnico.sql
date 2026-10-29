SELECT INS."IdInstrumento", INS."Nemotecnico" 
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON INS."IdTipoInstrumento" = TINS."IdTipoInstrumento"
WHERE INS."Nemotecnico" IN (:Nemotecnico) AND TINS."IdClasificacionTipoInstrumento" = 'FM';