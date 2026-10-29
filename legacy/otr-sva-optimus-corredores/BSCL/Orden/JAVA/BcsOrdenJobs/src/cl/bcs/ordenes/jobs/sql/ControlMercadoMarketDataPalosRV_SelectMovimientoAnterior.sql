SELECT * FROM (
	SELECT "IdAsignacion",
		"IdTransaccion",
		"IdMovimientoComprobante",
		"IdPortafolio",
		"IdCliente",
		"IdEstadoFacturacion",
	RANK() OVER (PARTITION BY "IdTransaccion" ORDER BY "IdMovimientoComprobante" DESC) AS "Rank"
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
	WHERE "IdTransaccion" IN (:IdTransaccion) AND "IdEstadoFacturacion" = 'ANU'
) R 
WHERE "Rank" = 1;