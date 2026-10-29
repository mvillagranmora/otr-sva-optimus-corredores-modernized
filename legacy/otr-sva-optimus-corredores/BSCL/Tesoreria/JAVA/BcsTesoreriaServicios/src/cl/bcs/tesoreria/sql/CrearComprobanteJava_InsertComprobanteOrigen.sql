INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"
	("IdComprobanteDetalleOrigen", 
	"IdComprobanteDetalle", 
	"IdIngresoEgreso", 
	"IdComprobante")
VALUES
	(:p_IdComprobanteDetalleOrigen,
	 :p_IdComprobanteDetalle,
	 :p_IdIngresoEgreso,
	 :p_IdComprobante);	