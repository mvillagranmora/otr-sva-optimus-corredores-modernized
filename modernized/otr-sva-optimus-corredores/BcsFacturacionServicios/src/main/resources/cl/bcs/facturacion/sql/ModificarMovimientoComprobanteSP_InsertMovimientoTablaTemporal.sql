INSERT INTO "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante"
(
	"IdMovimientoComprobante",
	"IdPortafolio",
	"IdFormaCustodia",
	"IdBoveda",
	"EsComprobIndividual",
	"Glosa",
	"MontoMovimientoComprobante"
)
VALUES (
	:IdMovimientoComprobante,
	:IdPortafolio,
	:IdFormaCustodia,
	:IdBoveda,
	:EsComprobIndividual,
	:Glosa,
	:MontoMovimientoComprobante
);