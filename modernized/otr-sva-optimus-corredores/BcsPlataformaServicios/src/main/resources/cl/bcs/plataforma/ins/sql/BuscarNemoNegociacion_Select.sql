SELECT
	inn."IdInstrumento"
	,inn."Nemotecnico"
	,inn."IdInstrumentoRaiz"
	,inn."IdMoneda"
	,inn."IdCondicionLiquidacion"
FROM "BSCL.Plataforma.BD::INS.Instrumento" inn
WHERE 
	(inn."IdInstrumento" IN (:IdInstrumento)) OR  (inn."IdInstrumentoRaiz" IN (:IdInstrumento))
	AND inn."IdEstado" = 'ACT'
	AND inn."EstaInscrito" = 'S';