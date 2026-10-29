SELECT TOP 1
	CASE WHEN (COALESCE(TO_CHAR(EJE."FechaFin",'YYYY-MM-DD HH24:MI:SS'),'') = '0001-01-01 00:00:00') THEN 'S' ELSE 'N' END AS "PautaEnEjecucion"
	,COALESCE(DEJ."Estado",'') AS "EstadoProcesoCambioFecha"
FROM "BSCL.Plataforma.BD::PAU.Ejecucion" AS EJE
LEFT JOIN "BSCL.Plataforma.BD::PAU.EjecucionDetalle" AS DEJ
	ON EJE."FechaSistema" = DEJ."FechaSistema"
	AND DEJ."CodigoProceso" = 'CAMBIO_FECHA_SISTEMA'
ORDER BY EJE."FechaSistema" DESC;