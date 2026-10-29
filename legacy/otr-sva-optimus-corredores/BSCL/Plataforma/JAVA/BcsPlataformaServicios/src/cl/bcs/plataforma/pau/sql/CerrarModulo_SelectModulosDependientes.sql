SELECT
	M."CodigoModulo"
	,M."IdEmpresa" AS "IdEmpresa"
	, COALESCE(PLAM."Nombre", M."CodigoModulo") AS "NombreModulo"
	,CASE WHEN COALESCE(P1."IdEstado",'CER') = 'CER' AND
	COALESCE(P2."IdEstado",'CER') = 'CER' AND
	COALESCE(P3."IdEstado",'CER') = 'CER' AND
	COALESCE(P4."IdEstado",'CER') = 'CER' AND
	COALESCE(P5."IdEstado",'CER') = 'CER' THEN 'OK' ELSE 'ERR' END AS "Resultado"
FROM "BSCL.Plataforma.BD::PAU.Modulo" AS M
LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS PLAM ON PLAM."IdModulo" = M."CodigoModulo" AND PLAM."IdEstado" = 'ACT'
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS P1 ON (M."CodigoModuloDependenciaCierre1" = P1."CodigoModulo" AND :p_IdEmpresa = P1."IdEmpresa")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS P2 ON (M."CodigoModuloDependenciaCierre2" = P2."CodigoModulo" AND :p_IdEmpresa = P2."IdEmpresa")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS P3 ON (M."CodigoModuloDependenciaCierre3" = P3."CodigoModulo" AND :p_IdEmpresa = P3."IdEmpresa")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS P4 ON (M."CodigoModuloDependenciaCierre4" = P4."CodigoModulo" AND :p_IdEmpresa = P4."IdEmpresa")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS P5 ON (M."CodigoModuloDependenciaCierre5" = P5."CodigoModulo" AND :p_IdEmpresa = P5."IdEmpresa")
WHERE M."CodigoModulo" IN (:p_IdsModulo);