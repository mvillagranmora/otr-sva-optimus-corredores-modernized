SELECT 
	FLG."IdDeterioroEmisor",
	FLG."Emisor",
	FLG."FlagDeterioro",
	FLG."PorcentajeDeterioro",
	FLG."Fase",
	FLG."IdEstado"
FROM "BSCL.Plataforma.BD::MER.DeterioroEmisor" FLG
WHERE FLG."IdEstado" = 'ACT';