SELECT 
	FLG."IdDeterioroEmisor",
	FLG."Emisor",
	FLG."FlagDeterioro",
	FLG."PorcentajeDeterioro",
	FLG."Fase"
FROM "BSCL.Plataforma.BD::MER.DeterioroEmisor" FLG
WHERE FLG."IdDeterioroEmisor" IN (:IdDeterioroEmisor);