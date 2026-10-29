SELECT POR."IdPortafolio"
	 , POR."IdPreferenciaPagoPortafolio"
	 , PAG."IdMoneda"
	 , PAG."IdTipoOperacion"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" POR
	 LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PAG ON (POR."IdPreferenciaPago" = PAG."IdPreferenciaPago" AND POR."IdPreferenciaPago" IS NOT NULL)
WHERE "IdPortafolio" IN (:IdsPortafolios)
  AND POR."PorDefecto" = 'S';