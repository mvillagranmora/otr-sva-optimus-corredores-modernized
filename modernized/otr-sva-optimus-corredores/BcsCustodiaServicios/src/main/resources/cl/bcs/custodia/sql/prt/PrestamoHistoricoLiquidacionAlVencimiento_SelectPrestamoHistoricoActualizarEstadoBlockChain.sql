SELECT 
	  prt."IdCartera"
	, prt."FolioSistemaExterno" AS "LAID"
	, prt."FolioCartera" AS "FolioPrestamo"
	, prt."EstadoLiquidacion"
FROM	"BSCL.Custodia.BD::CUS.Cartera" prt
LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamoHistorico" AS asig ON
		asig."FolioCarteraPrestamo" = prt."FolioCartera"
	AND IFNULL(asig."CantidadPorAsignar", 0) >= IFNULL(:p_CantidadPorAsignar, IFNULL(asig."CantidadPorAsignar", 0))
WHERE prt."IdTipoInstrumento" = 'PRT'
	AND prt."IdEstado" <> 'VIG'
	AND ( prt."FolioSistemaExterno" IS NOT NULL AND prt."FolioSistemaExterno" <> '' AND  prt."FolioSistemaExterno" <> '0' )
	AND prt."IdEmpresa" = :p_IdEmpresa
	AND ( prt."FolioCartera" = :p_FolioCartera OR :p_FolioCartera IS NULL )
	AND prt."FechaOperacion" BETWEEN IFNULL(:p_FechaConsultaDesde, prt."FechaOperacion") AND IFNULL(:p_FechaConsultaHasta, prt."FechaOperacion")
	AND ( prt."IdEstado" = :p_IdEstado OR :p_IdEstado IS NULL )
	AND ( prt."IdTipoPrestamo" = :p_IdTipoPrestamo OR :p_IdTipoPrestamo IS NULL )
	AND ( prt."IdCliente" = :p_IdCliente OR :p_IdCliente IS NULL )
	AND ( prt."IdPortafolio" = :p_IdPortafolio OR :p_IdPortafolio IS NULL );