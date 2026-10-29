SELECT
	"IdContratoComision"
	"IdComprobante",
	"IdComprobanteComisionMensual",
	"IdMovimientoComprobanteComisionMensual"
FROM
	"BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle"
WHERE
	"IdMovimientoComprobanteComisionMensual" IN (:p_IdMovimientos)