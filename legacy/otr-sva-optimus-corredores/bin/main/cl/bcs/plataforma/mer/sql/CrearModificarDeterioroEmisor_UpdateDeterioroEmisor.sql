UPDATE "BSCL.Plataforma.BD::MER.DeterioroEmisor"
SET  	"Emisor" = :Emisor,
		"FlagDeterioro" = :FlagDeterioro,
		"PorcentajeDeterioro" = :PorcentajeDeterioro,
		"Fase" = :Fase
WHERE "IdDeterioroEmisor" = :IdDeterioroEmisor; 
