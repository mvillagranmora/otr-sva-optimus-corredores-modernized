SELECT 
	COM."IdMovimientoComprobante",
	BANCO."IdMovimientoComprobanteFormaDePago",
	BANCO."NumeroCuentaBancaria" AS "NumeroCuentaBancariaAntigua",
	BANCO."IdEstado" AS "IdEstadoAntiguo",
	COM."IdCliente",
	COM."NombreLargoCliente" AS "NombreCliente",
	COM."DocumentoIdentidad",
	COM."IdPortafolio"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS COM
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" AS BANCO
ON COM."IdMovimientoComprobante" = BANCO."IdMovimientoComprobante"
WHERE BANCO."IdMovimientoComprobanteFormaDePago" IN (:IdMovimientoComprobanteFormaDePago);