UPDATE "BSCL.Plataforma.BD::MER.DeterioroEmisor"
SET  	"Emisor" = :Emisor,
		"FlagDeterioro" = :FlagDeterioro,
		"PorcentajeDeterioro" = :PorcentajeDeterioro,
		"Fase" = :Fase,
		"IdEstado" = :IdEstado
WHERE "Emisor" = :Emisor
AND "IdEstado" = 'ACT'; 
