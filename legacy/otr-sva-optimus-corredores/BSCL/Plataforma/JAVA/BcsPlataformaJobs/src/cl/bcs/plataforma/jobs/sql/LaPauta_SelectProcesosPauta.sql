SELECT * FROM (
	SELECT PROC."CodigoProceso", PROC."NombreClase",COALESCE(PROC."Orden",0) AS "Orden", PROC."DetienePauta", IFNULL(DEP."RequiereTipoPrograma",'') AS "CodigoProcesoDependencia"
	FROM "BSCL.Plataforma.BD::PAU.Proceso" PROC
	LEFT JOIN "BSCL.Plataforma.BD::PAU.Dependencia" DEP ON PROC."CodigoProceso" = DEP."IdTipoPrograma"
	WHERE "CodigoModulo" = 'SIS'
	AND "IdEstado" = 'ACT'
	AND "IdEmpresa" = :IdEmpresa
	AND COALESCE("Orden",0) >= 0
	AND (PROC."CodigoProceso" IN (:CodigoProceso) OR 1 = :FiltroCodigoProceso )
	UNION ALL
	(
		SELECT ('Z' || PAUADI."OrdenFinDeMesInhabil" || '_' || PAUADI."CodigoProceso") AS "CodigoProceso", PAUADI."NombreClase", PAUCFS."Orden", PAUADI."DetienePauta", '' AS "CodigoProcesoDependencia" 
		FROM "BSCL.Plataforma.BD::PAU.Proceso" PAUADI INNER JOIN "BSCL.Plataforma.BD::PAU.Proceso" PAUCFS ON PAUCFS."CodigoProceso" = 'CAMBIO_FECHA_SISTEMA'
		WHERE :EjecutarDiaInhabil = 'S' AND PAUADI."EjecutarFinDeMesInhabil" = 'S' AND PAUADI."OrdenFinDeMesInhabil" IS NOT NULL AND PAUADI."CodigoModulo" = 'SIS' AND PAUADI."IdEstado" = 'ACT' AND PAUADI."IdEmpresa" = :IdEmpresa
		ORDER BY PAUADI."OrdenFinDeMesInhabil" ASC
	)
)
ORDER BY "Orden","CodigoProceso";