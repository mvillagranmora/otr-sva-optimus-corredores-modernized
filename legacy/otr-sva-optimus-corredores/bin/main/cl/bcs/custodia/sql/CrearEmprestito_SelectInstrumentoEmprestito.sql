SELECT 
	"IdInstrumento",
	"Nemotecnico",
	"IdEmisor"
FROM "BSCL.Plataforma.BD::INS.Instrumento"
WHERE "IdTipoInstrumento" = :p_IdTipoInstrumento
AND "IdEstado" = 'ACT';