UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
	SET "IdEstado"	= :IdEstado,
		"IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdPortafolio" = :IdPortafolio AND "Proposito" = 'PROPOSITOAPV';