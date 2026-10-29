SELECT CDO."IdIngresoEgreso"
	,COALESCE(CD."IdComprobante", 0) AS "IdComprobante"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON (CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle")
WHERE CDO."IdIngresoEgreso" IN (:IdIngresoEgreso)

--SELECT * FROM "BSCL.Tesoreria.BD::TES.Comprobante"