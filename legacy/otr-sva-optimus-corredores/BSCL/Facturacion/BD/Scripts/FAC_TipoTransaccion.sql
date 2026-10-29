SET SCHEMA "BSCL"; 
 
DELETE FROM  "BSCL.Facturacion.BD::FAC.TipoTransaccion"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoTransaccion" ( "IdTipoTransaccion","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('NOR','NOR','NORMALES','ACT','2015-07-31 00:00:00.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoTransaccion" ( "IdTipoTransaccion","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('SIM','SIM','SIMULTANEAS','ACT','2015-07-31 00:00:00.000000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.TipoTransaccion" ( "IdTipoTransaccion","Codigo","Nombre","IdEstadoFacturacion","FechaIngresoRegistro" )  
VALUES('OPL','OPL','OPCION PLAZO','ACT','2015-07-31 00:00:00.000000000');


commit;
