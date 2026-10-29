
SELECT 
	ComDet."IdComprobante",
	Com."FolioComprobante",
	ComDet."IdTipoMovimiento",
	ComDet."IdEstado",
	Com."IdComprobante" AS "IdComprobanteFacturacion",
	ABS(Com."MontoLiquidacionFinal") AS "MontoFactura",
	SUM(ABS(ComDet."Monto")) AS "MontoTesoreria"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComDet
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS Com
ON ComDet."IdComprobanteOrigen" = Com."IdComprobante"
WHERE Com."FolioComprobante" IN (:folio)
GROUP BY 
	ComDet."IdComprobante",
	Com."FolioComprobante",
	ComDet."IdEstado",
	Com."IdComprobante",
	Com."MontoLiquidacionFinal",
	ComDet."IdTipoMovimiento",
	ComDet."Monto"