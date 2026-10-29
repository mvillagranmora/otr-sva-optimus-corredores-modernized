UPDATE "BSCL.Plataforma.BD::MER.IndiceFinanciero"
	SET "Valor" 			 = COALESCE(:Valor,"Valor"),
		"FechaActualizacion" = CURRENT_TIMESTAMP
WHERE "Fecha" = :Fecha AND "IdIndiceFinanciero" = :IdIndiceFinanciero