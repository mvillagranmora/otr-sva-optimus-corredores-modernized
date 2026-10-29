SELECT
"IdEntidadClasificadoraRiesgo" || '_' || "IdInstrumento" AS "Id"
,"IdEntidadClasificadoraRiesgo"
,"ValorClasificacionRiesgo"
,"IdInstrumento"
FROM "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"
WHERE "IdInstrumento" IN (:IdInstrumento) AND "IdEntidadClasificadoraRiesgo" IN (:IdEntidadClasificadoraRiesgo)