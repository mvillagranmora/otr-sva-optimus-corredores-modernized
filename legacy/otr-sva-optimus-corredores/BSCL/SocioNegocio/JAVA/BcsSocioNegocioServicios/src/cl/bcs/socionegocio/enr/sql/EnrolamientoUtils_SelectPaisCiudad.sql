SELECT rp."IdPais"
	 , rp."UsaDivisionPais"
	 , rc."IdCiudad"
FROM "BSCL.Plataforma.BD::REG.Ciudad" rc
	 LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" rp ON (rp."IdPais" = rc."IdPais" AND rp."IdPais" IS NOT NULL)
WHERE rc."IdCiudad" IN (:CiudadIds);