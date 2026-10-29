
---------------------------------------------------------------------------
-- Insert datos de entrada de formas de pago a liquidar en tabla global
---------------------------------------------------------------------------

INSERT INTO  "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_FormasDePagoALiquidar"
(
	"IdComprobante",
	"IdComprobanteFormaDePago"
)
VALUES
(
	:IdComprobante,
	:IdComprobanteFormaDePago
);