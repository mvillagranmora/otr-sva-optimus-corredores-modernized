SELECT 	 "IdRecaudacionCliente"
		,"IdContratoSAAPV"
		,"FechaCreacion"
		,"FechaModificacion" 
FROM "BSCL.APV.BD::REC.AsignacionContratoRecaudacion" 
WHERE "IdRecaudacionCliente" = :IdRecaudacionCliente;