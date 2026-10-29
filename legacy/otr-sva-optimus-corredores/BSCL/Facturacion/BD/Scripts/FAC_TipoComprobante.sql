SET SCHEMA "BSCL"; 
 
DELETE FROM  "BSCL.Facturacion.BD::FAC.TipoComprobante"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('FAC','FAC','FACTURA','ACT','2015-07-23 15:46:37.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NCR','NCR','NOTA DE CREDITO','ACT','2015-07-23 15:46:37.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('FAE','FAE','FACTURA ELECTRONICA','ACT','2015-07-23 15:46:37.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NCE','NCE','NOTA DE CREDITO ELECTRONICA','ACT','2015-07-23 15:46:37.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NDE','NDE','NOTA DE DEBITO ELECTRONICA','ACT','2015-07-23 15:46:37.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('FAO','FAO','FACTURA DE OTROS CONCEPTOS','ACT','2015-09-11 15:46:34.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NCO','NCO','NOTA DE CREDITO OTROS CONCEPTOS','ACT','2015-09-11 15:46:34.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('FOE','FOE','FACTURA DE OTROS CONCEPTOS ELECTRONICA','ACT','2015-09-11 15:46:34.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NOE','FOE','NOTA DE CREDITO OTROS CONCEPTOS ELECTRONICA','ACT','2015-09-11 15:46:34.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoComprobante" ( "IdTipoComprobante","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NDB','NDB','NOTA DE DEBITO','ACT','2015-09-11 15:46:34.000000000');
commit;
