SELECT  INS."IdInstrumento",
		INS."Nemotecnico",
		INS."RazonSocial",
		INS."FechaEmision",
		INS."FechaVencimiento",
		INS."IdEmisor",
		INS."IdTipoReajuste",
		INS."IdMoneda",
		INS."IdTipoInstrumento" 
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
WHERE INS."Nemotecnico" IN (:p_Nemo)
		AND (
			:p_IdInstrumento IS NULL 
			OR INS."IdInstrumento" IN (:p_IdInstrumento)
		);