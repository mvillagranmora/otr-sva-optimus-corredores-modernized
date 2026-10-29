SELECT 
	"IdInstrumento",
	"DividendoOtraMoneda" 
FROM "BSCL.Plataforma.BD::INS.Instrumento"
WHERE "IdInstrumento" = :IdInstrumento