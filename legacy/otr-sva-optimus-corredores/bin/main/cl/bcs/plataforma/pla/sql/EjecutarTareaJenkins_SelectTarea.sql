SELECT "Ruta","Token"
FROM "BSCL.Plataforma.BD::PLA.TareaJenkins"
WHERE "TipoTarea"=:TipoTarea AND "IdEstado"='ACT';