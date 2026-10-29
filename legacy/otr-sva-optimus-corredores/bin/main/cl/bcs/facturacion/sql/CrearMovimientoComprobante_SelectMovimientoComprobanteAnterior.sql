WITH movimiento_comprobante AS (
SELECT
	"IdComprobanteReferencia" ,
	SUM("MontoMovimientoComprobante") AS "Monto"
FROM
	"BSCL.Facturacion.BD::FAC.MovimientoComprobante"
WHERE
	"IdComprobanteReferencia" IN (:IdsComprobante)
	AND "IdEstadoFacturacion" <> 'ANU'
GROUP BY
	"IdComprobanteReferencia"
),
comprobante AS (
SELECT
	"IdComprobante",
	"Monto"
FROM
	"BSCL.Facturacion.BD::FAC.Comprobante"
WHERE
	"IdComprobante" IN (:IdsComprobante)
)
SELECT
	MC."IdComprobanteReferencia",
	C."Monto" - MC."Monto" AS "Saldo"
FROM
	comprobante C
INNER JOIN movimiento_comprobante MC ON MC."IdComprobanteReferencia" = C."IdComprobante";