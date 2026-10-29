UPDATE "BSCL.APV.BD::TRA.DetalleTraspaso"
	SET "IdPortafolio"			= :IdPortafolio,
		"IdProductoAgrupador"	= :IdProductoAgrupador, 
		"IdRegimenTributario"   = :IdRegimenTributario, 
		"MontoPeso" 			= :MontoPeso, 
		"MontoUF"   			= :MontoUF,
		"FechaIngreso"			= :FechaIngreso
WHERE "IdTraspaso" = :IdTraspaso AND "IdDetalleTraspaso" = :IdDetalleTraspaso AND "IdEstado" = 'VIG';