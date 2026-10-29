SELECT
	MOVCTA."IdComprobante",
	COMDET."NemotecnicoInstrumento"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS MOVCTA
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS COMDET
	ON COMDET."IdComprobante" = MOVCTA."IdComprobante"
WHERE MOVCTA."IdComprobante" IN (:idsComprobante);