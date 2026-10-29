UPDATE "BSCL.APV.BD::SCS.PortafolioContratoSAAPV"
	SET 
		"IdPortafolio" = :IdPortafolioNuevo,
		"MontoPesos" = :MontoPesos, 
		"MontoUF"   = :MontoUF
WHERE "IdPortafolio" = :IdPortafolio AND "IdContratoSAAPV" = :IdContratoSAAPV;