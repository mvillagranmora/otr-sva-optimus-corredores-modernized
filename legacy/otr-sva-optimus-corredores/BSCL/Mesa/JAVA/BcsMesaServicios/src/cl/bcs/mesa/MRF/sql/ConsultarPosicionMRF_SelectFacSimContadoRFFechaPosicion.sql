SELECT	"IdTipoProducto"
		,"IdTipoOperacion"
		,"IdEstadoComprobante" as "IdEstado"
		,"IdPortafolio"
		,SUM("MontoMovimientoComprobante") AS "MtoEvento"
FROM (
	SELECT
			MvtoCmbte."IdTipoProducto",
			MvtoCmbte."IdTipoOperacion",
			CASE WHEN Cmbte."IdEstadoComprobante" IN ('ENV') THEN 'FAC' ELSE Cmbte."IdEstadoComprobante" END AS "IdEstadoComprobante",
			Cmbte."IdPortafolio",
			MvtoCmbte."MontoMovimientoComprobante"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte
	    ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion
	    ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla
	    ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte
	    ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla
	    ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
	WHERE 	Cmbte."FechaLiquidacion" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') AND
			Cmbte."IdTipoProducto" IN (:IdTipoProducto) AND
			Cmbte."IdTipoOperacion" IN (:IdTipoOperacion) AND
			Cmbte."IdEstadoComprobante" NOT IN ('ANU') AND
			MvtoCmbte."EsAPlazo" = 'N' AND
			Cmbte."IdPortafolio" IN (:IdPortafolio) AND
			Cmbte."IdTipoOperacion" NOT IN  ('ANSIMC','ANSIMV')
			AND Cmbte."IdTipoProducto" NOT IN ('INIF','INRF')
	)
GROUP BY "IdTipoProducto", "IdTipoOperacion", "IdEstadoComprobante", "IdPortafolio"
UNION ALL
SELECT "IdTipoProducto" ,"EGRIdTipoOperacion" AS "IdTipoOperacion",'FAC' AS "IdEstado" ,"IdPortafolio" ,SUM("MontoLiquidacion") AS  "MtoEvento"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" sim
WHERE "EGRFechaLiquidacion" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') AND
sim."IdTipoProducto" IN (:IdTipoProducto) AND
sim."EGRIdTipoOperacion" IN (:IdTipoOperacion) AND
sim."EGRIdTipoOperacion" NOT IN( 'ANCSIM','ANVSIM') AND
sim."IdEstado"  NOT IN ('ANU','VCD','LIQ') AND
sim."IdPortafolio" IN (:IdPortafolio)
GROUP BY "IdTipoProducto", "EGRIdTipoOperacion", "IdEstado", "IdPortafolio"
UNION ALL
SELECT	"IdTipoProducto"
		,"IdTipoOperacion"
		,"IdEstadoComprobante" as "IdEstado"
		,"IdPortafolio"
		,SUM("MontoMovimientoComprobante") AS "MtoEvento"
FROM (
	SELECT
			MvtoCmbte."IdTipoProducto",
			MvtoCmbte."IdTipoOperacion",
			CASE WHEN Cmbte."IdEstadoComprobante" IN ('ENV') THEN 'FAC' ELSE Cmbte."IdEstadoComprobante" END AS "IdEstadoComprobante",
			Cmbte."IdPortafolio",
			MvtoCmbte."MontoMovimientoComprobante"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte
	    ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion
	    ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla
	    ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte
	    ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla
	    ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
	WHERE 	CAST(Cmbte."FechaComprobante" AS DATE) = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') AND
			Cmbte."FechaLiquidacion" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') AND
			Cmbte."IdTipoProducto" IN (:IdTipoProducto) AND
			Cmbte."IdTipoOperacion" IN (:IdTipoOperacion) AND
			Cmbte."IdEstadoComprobante" NOT IN ('ANU') AND
			MvtoCmbte."EsAPlazo" = 'N' AND
			Cmbte."IdPortafolio" IN (:IdPortafolio)
			AND Cmbte."IdTipoProducto" IN ('INIF','INRF')
	)
GROUP BY "IdTipoProducto", "IdTipoOperacion", "IdEstadoComprobante", "IdPortafolio"
