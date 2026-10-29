SELECT 
	M."CodigoModulo",
	M."IdEstado" AS "Estado",
	M."CodigoUsuario",
	COALESCE(PLAM."Nombre", M."CodigoModulo") AS "NombreModulo"
FROM "BSCL.Plataforma.BD::PAU.Modulo" AS M
LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS PLAM ON PLAM."IdModulo" = M."CodigoModulo" AND PLAM."IdEstado" = 'ACT'
WHERE M."CodigoModulo" IN (:p_IdsModulo) AND M."IdEmpresa" = :p_IdEmpresa;