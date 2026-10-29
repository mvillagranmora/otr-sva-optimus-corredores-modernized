SELECT 
	"IdInstrumento", 
	"Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
WHERE "Nemotecnico" IN (:Nemotecnico)
  AND "IdEstado" = 'ACT'