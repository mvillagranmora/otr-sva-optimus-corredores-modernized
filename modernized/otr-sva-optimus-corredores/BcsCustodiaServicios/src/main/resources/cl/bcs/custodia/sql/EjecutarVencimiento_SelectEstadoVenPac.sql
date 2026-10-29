SELECT
	PROC."IdEmpresa",
	EJE."FechaSistema",
	PROC."CodigoProceso",
	TIPO."Descripcion",
	EJEDET."FechaFin",
	EJEDET."Estado",
	EJEDET."FechaInicio",
	EJEDET."Mensaje"
FROM
	"BSCL.Plataforma.BD::PAU.Ejecucion" EJE
LEFT JOIN "BSCL.Plataforma.BD::PAU.EjecucionDetalle" EJEDET ON
	EJE."FechaSistema" = EJEDET."FechaSistema"
	AND EJE."IdEmpresa" = EJEDET."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::PAU.Proceso" PROC ON
	PROC."CodigoProceso" = EJEDET."CodigoProceso"
LEFT JOIN "BSCL.Plataforma.BD::BAT.TipoPrograma" TIPO ON
	TIPO."IdTipoPrograma" = PROC."CodigoProceso"
WHERE
	PROC."IdEmpresa" = :IdEmpresa
	AND EJE."FechaSistema" = :Fecha
	AND PROC."CodigoModulo" = 'SIS'
	AND PROC."CodigoProceso" = 'VENCIMIENTOS_PACTOS'
ORDER BY
	"Orden";