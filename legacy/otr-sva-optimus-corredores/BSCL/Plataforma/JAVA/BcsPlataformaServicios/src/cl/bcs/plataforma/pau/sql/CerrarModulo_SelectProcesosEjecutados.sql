SELECT
 	M."IdModulo" AS "CodigoModulo"
 	,P."CodigoProceso"
 	,P."DescripcionProceso"
 	,P."IdEmpresa" AS "IdEmpresa"
 	,M."Nombre" AS "NombreModulo"
 	,CASE WHEN COALESCE(CP."IdControlProceso",'-1') = '-1' THEN 'ERR' ELSE 'OK' END AS "Resultado"
FROM "BSCL.Plataforma.BD::PLA.Modulo" AS M
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Proceso" AS P ON (P."CodigoModulo" = M."IdModulo" AND P."IdEmpresa" =:p_IdEmpresa  AND P."EsApertura" = 'N' AND P."IdEstado" = 'ACT')
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.ControlProceso" AS CP ON (CP."CodigoModulo" = P."CodigoModulo" AND CP."CodigoProceso" = P."CodigoProceso" AND CP."FechaSistema" = :p_FechaSistema AND P."IdEmpresa" = :p_IdEmpresa )
WHERE M."IdModulo" IN (:p_IdsModulo) AND P."IdEstado" = 'ACT';