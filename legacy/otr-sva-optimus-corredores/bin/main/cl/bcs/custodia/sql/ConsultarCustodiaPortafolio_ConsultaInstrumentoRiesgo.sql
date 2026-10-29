SELECT
 "ValorClasificacionRiesgo"
,"IdInstrumento"
,"IdEntidadClasificadoraRiesgo"
FROM "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"
WHERE "IdEntidadClasificadoraRiesgo" ='BCS' AND "IdInstrumento" IN (:IdInstrumento)

