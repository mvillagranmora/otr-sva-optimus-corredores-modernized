Update MOV
	Set "IdEstadoFacturacion" = 'FAC',
	"IdComprobante" = :IdComprobante
	From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MOV
	WHERE MOV."IdMovimientoComprobante" = :IdMovimientoComprobante
	
	
	   
	