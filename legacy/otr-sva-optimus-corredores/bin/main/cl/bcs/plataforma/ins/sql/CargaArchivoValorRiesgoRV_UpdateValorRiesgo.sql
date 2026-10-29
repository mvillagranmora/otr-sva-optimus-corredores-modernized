UPDATE "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"
SET "ValorClasificacionRiesgo" = :Valor
WHERE ("IdInstrumento", "IdEntidadClasificadoraRiesgo") = (
SELECT VCR."IdInstrumento",VCR."IdEntidadClasificadoraRiesgo"
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" VCR
ON INS."IdInstrumento" = VCR."IdInstrumento"
WHERE INS."Nemotecnico" = :Nemotecnico
AND VCR."IdEntidadClasificadoraRiesgo" = :Entidad
);