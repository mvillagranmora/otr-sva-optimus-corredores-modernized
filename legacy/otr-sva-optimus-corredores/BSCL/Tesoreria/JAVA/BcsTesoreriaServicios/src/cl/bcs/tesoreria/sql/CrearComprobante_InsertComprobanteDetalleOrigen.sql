
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"
( 
	"IdComprobanteDetalleOrigen",
	"IdComprobanteDetalle",
	"IdIngresoEgreso",
	"IdComprobante"
)
SELECT  "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalleIngresoEgreso".NEXTVAL,
		MI."IdComprobanteDetalle",
		MI."IdIngresoEgreso",
		MI."IdComprobante"
 FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen" as MI;