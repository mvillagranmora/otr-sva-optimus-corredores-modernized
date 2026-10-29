SELECT SUM("CantidadOrdenes") AS "CantidadOrdenes",SUM("CantidadSimultaneas") AS "CantidadSimultaneas",SUM("CantidadPrestamos") AS "CantidadPrestamos",SUM("CantidadGarantias") AS "CantidadGarantias"
FROM (
		SELECT 	COUNT(1) AS "CantidadOrdenes", TO_BIGINT(0) AS "CantidadSimultaneas",TO_BIGINT(0) AS "CantidadPrestamos",TO_BIGINT(0) AS "CantidadGarantias" 
		FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" 
		WHERE 	"IdInstrumento"	= :IdInstrumento	and
				"OrdenVigente" = 'S'                and
				"IdEstadoOrden" NOT IN ('RZD','ANU','VCD')
	UNION ALL
		SELECT 	TO_BIGINT(0) AS "CantidadOrdenes", COUNT(1) AS "CantidadSimultaneas",TO_BIGINT(0) AS "CantidadPrestamos",TO_BIGINT(0) AS "CantidadGarantias"
		FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes"
		WHERE	"suby_IdInstrumento"		= :IdInstrumento
			AND "FechaVencimientoOperacion" = :FechaLimite
	UNION ALL
		SELECT 	TO_BIGINT(0) AS "CantidadOrdenes",TO_BIGINT(0) AS "CantidadSimultaneas", COUNT(1) AS "CantidadPrestamos",TO_BIGINT(0) AS "CantidadGarantias"
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
		WHERE	"IdInstrumentoSubyacente"	= :IdInstrumento
	UNION ALL
		SELECT 	TO_BIGINT(0) AS "CantidadOrdenes",TO_BIGINT(0) AS "CantidadSimultaneas", TO_BIGINT(0) AS "CantidadPrestamos",COUNT(1) AS "CantidadGarantias"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
		WHERE "IdInstrumento" = :IdInstrumento
		AND "IdEstado" = 'VIG'
);