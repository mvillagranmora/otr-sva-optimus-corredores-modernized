SET SCHEMA "BSCL"; 
 
DELETE FROM  "BSCL.Facturacion.BD::FAC.TipoConvenio"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoConvenio" ( "IdTipoConvenio","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('SCV','SCV','SIN CONVENIO','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoConvenio" ( "IdTipoConvenio","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('FAC','FAC','FACTURAR','ACT','2015-07-22 16:27:07.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoConvenio" ( "IdTipoConvenio","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NFA','NFA','NO FACTURAR','ACT','2015-07-22 16:27:07.000000000');


commit;
