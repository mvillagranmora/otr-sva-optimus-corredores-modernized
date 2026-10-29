SELECT
		MvtoCmbte."IdTipoProducto",
		MvtoCmbte."IdTipoOperacion",
		CASE WHEN Cmbte."IdEstadoComprobante" IN ('ENV') THEN 'FAC' ELSE Cmbte."IdEstadoComprobante" END AS "IdEstado",
		Cmbte."IdPortafolio",
		SUM(MvtoCmbte."Monto") AS "MtoEvento"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte
    ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante"
INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion
    ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla
    ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte
    ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla
    ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
WHERE 	Cmbte."FechaLiquidacion" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') AND
		Cmbte."IdTipoProducto" IN (:IdTipoProducto) AND
		Cmbte."IdTipoOperacion" IN (:IdTipoOperacion) AND
		--Cmbte."IdEstadoComprobante" = :IdTipoComprobante AND
		Cmbte."IdEstadoComprobante" NOT IN ('ANU') AND
		Cmbte."IdCondicionLiquidacion" <> 'T0' AND --MvtoCmbte."EsAPlazo" = 'N' AND
		Cmbte."IdPortafolio" IN (:IdPortafolio)
		AND Cmbte."IdTipoProducto"!='INSI'
GROUP BY MvtoCmbte."IdTipoProducto", MvtoCmbte."IdTipoOperacion", Cmbte."IdEstadoComprobante", Cmbte."IdPortafolio"
UNION ALL
SELECT "IdTipoProducto" ,"IdTipoOperacion" ,'FAC' AS "IdEstado" ,"IdPortafolio" ,SUM("MontoLiquidacion") AS  "MtoEvento"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" sim
WHERE "EGRFechaLiquidacion" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD') AND
sim."IdTipoProducto" IN (:IdTipoProducto) AND
sim."EGRIdTipoOperacion" IN (:IdTipoOperacion) AND
sim."IdEstado"  NOT IN ('ANU') AND
sim."IdPortafolio" IN (:IdPortafolio)
GROUP BY "IdTipoProducto", "IdTipoOperacion", "IdEstado", "IdPortafolio"