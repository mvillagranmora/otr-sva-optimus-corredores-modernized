SET SCHEMA "BSCL"; 
 
DELETE FROM  "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPOROPERACION','SEPARA MOVIMIENTOS POR COMPRA/VENTA','S');
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPORPRODUCTO','SEPARA POR PRODUCTO','S');
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPORBOLSA','SEPARA POR BOLSA','S');
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPORAGENTE','SEPARA POR AGENTE','S');
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPORSUCURSAL','SEPARA POR SUCURSAL','S');
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPORFECHALIQ','SEPARA POR FECHA DE LIQUIDACION','S');
INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SEPARAPORMONEDA','SEPARA POR MONEDA','S');


commit;
