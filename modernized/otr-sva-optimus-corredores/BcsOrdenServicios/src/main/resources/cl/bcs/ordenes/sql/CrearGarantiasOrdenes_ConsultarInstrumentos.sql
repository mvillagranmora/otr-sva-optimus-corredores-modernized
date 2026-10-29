SELECT INS."IdInstrumento",INS."IdTipoInstrumento",TIP."IdClasificacionTipoInstrumento",
"Nemotecnico","IdEmisor","IdTipoReajuste","FechaEmision","FechaVencimiento","IdMoneda"
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS 
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIP ON INS."IdTipoInstrumento" = TIP."IdTipoInstrumento"
WHERE "IdInstrumento" IN (:Instrumentos)
