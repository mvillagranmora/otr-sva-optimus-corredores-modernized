
-- Facturas generadas a partir de los comprobantes de origen con el calculo de cobros

INSERT INTO "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual"
( "IdComprobanteComisionMensual",
  "IdMovimientoComprobanteComisionMensual",
  "IdMoneda",
  "Monto", 
  "MontoIva", 
  "MontoComision", 
  "MontoDerecho")
VALUES( :IdComprobanteComisionMensual, 
		:IdMovimientoComprobanteComisionMensual,
		:IdMoneda, 
		:Monto, 
		:MontoIva, 
		:MontoComision, 
		:MontoDerecho);
