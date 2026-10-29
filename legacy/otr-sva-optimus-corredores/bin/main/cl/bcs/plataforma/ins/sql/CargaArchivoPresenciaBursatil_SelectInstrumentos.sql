SELECT "IdInstrumento","IdMoneda","Nemotecnico","IdClasificacionTipoInstrumento" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 
WHERE "Nemotecnico" IN (:Nemotecnicos)