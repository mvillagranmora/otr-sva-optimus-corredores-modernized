	SELECT "IdEntidadDestinoGarantia" 
	FROM "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
	WHERE "IdTipoInstrumento" = :IdTipoInstrumento and "IdBolsa" = :IdBolsa;