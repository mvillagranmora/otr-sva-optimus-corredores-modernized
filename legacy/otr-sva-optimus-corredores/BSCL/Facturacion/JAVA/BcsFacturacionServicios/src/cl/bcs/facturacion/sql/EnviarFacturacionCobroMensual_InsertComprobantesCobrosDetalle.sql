-- comprobantes de origen con las facturas generadas por concepto de COMISIONES MENSUALES

INSERT INTO "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle"
( "IdComprobanteComisionMensualDetalle", 
  "IdContratoComision", 
  "IdComprobante", 
  "IdComprobanteComisionMensual",
  "IdMovimientoComprobanteComisionMensual" )
VALUES( "BSCL.Facturacion.BD.Secuencias::FAC.ComprobanteComisionMensualDetalle".NEXTVAL, 
		:IdContratoComision, 
		:IdComprobante, 
		:IdComprobanteComisionMensual,
		:IdMovimientoComprobanteComisionMensual );
