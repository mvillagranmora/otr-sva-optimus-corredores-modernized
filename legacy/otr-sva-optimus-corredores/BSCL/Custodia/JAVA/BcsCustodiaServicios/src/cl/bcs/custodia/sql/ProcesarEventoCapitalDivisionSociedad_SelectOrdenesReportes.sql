SELECT Sum("Cantidad") AS "Cantidad"
FROM (
		SELECT 	COUNT(1)				AS "Cantidad"
		FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" 
		WHERE 	"IdInstrumento"	= :IdInstrumento	and
				"OrdenVigente" = 'S'
	UNION ALL
		SELECT 	COUNT(1)				AS "Cantidad"
		FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes"
		WHERE	"suby_IdInstrumento"		= :IdInstrumento
			AND "FechaVencimientoOperacion" = :FechaLimite
	UNION ALL
		SELECT 	COUNT(1)				AS "Cantidad" 
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
		WHERE	"IdInstrumentoSubyacente"	= :IdInstrumento
	UNION ALL
		SELECT 	COUNT(1)				AS "Cantidad"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
		WHERE "IdInstrumento" = :IdInstrumento
		AND "IdEstado" = 'VIG'
);