SET SCHEMA "BSCL"; 
 
DELETE FROM  "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" ( "IdTipoPagoCobroMovimiento","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('DEP','SC','SIN CONVENIO','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" ( "IdTipoPagoCobroMovimiento","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('RET','FA','FACTURAR','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" ( "IdTipoPagoCobroMovimiento","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('VIE','NF','NO FACTURAR','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" ( "IdTipoPagoCobroMovimiento","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('CCI','CCI','CUENTA INVERSION','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" ( "IdTipoPagoCobroMovimiento","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('FMM','FFM','FONDO MUTUOS','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" ( "IdTipoPagoCobroMovimiento","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('ENV','ENV','ENVIAR','ACT','2015-07-22 16:27:07.000000000');


commit;
