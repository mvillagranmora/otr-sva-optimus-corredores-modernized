SELECT 
"IdTipoInstrumento"
,"IdEntidadClasificadoraRiesgo"
FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
WHERE "IdTipoInstrumento" IN (:IdTipoInstrumento) AND "EntidadClasificadoraRiesgoPrincipal" = 'S'