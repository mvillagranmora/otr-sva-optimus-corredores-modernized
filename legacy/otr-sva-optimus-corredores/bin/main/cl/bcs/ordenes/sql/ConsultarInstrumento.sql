SELECT	"IdInstrumento",
		"Nemotecnico",
		"IdEmisor",
		"IdMoneda",
		"IdTipoInstrumento"
	FROM "BSCL.Plataforma.BD::INS.Instrumento" AS inst
	WHERE inst."Nemotecnico"  IN (:Nemotecnico) 