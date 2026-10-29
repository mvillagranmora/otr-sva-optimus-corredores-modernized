
------------------------------------------------------------------------------
-- Inserta tabla de entrada de comprobantes a liquidar en tabla global 
------------------------------------------------------------------------------

INSERT INTO  "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_ComprobantesALiquidar"
(
	"IdComprobante",
	"IdTipoMovimiento",
	"AfectaCuentaInversion"
)
VALUES
(
	:IdComprobante,
	:IdTipoMovimiento,
	:AfectaCuentaInversion
);
