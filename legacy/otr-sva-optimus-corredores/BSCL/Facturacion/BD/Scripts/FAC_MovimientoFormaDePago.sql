SET SCHEMA "BSCL"; 
 
DELETE FROM  "BSCL.Facturacion.BD::FAC.MovimientoFormaDePago"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoFormaDePago" ( "IdMvimientoFormaDePago","IdMedioDePago","IdMovimientoComprobante","IdBeneficiario","NombreLargoBeneficiario","SecuenciaAgrupacion","NombreMedioDePago","IdCuentaBancaria","NombreTipoCuentaBancaria","IdTarjetaBancaria","NombreTipoTarjetaBancaria","IdBanco","NombreBanco","NumeroMedioDePago","Sucursal","FechaVctoDocumento","MontoLiquidacionFormaDePago","IdEstadoMovimientoFormadePago","FechaIngresoRegistro" )  
VALUES(11,'EFE',3,1,'aadasdad',0,'',0,'',0,'',0,'','','','2015-07-31 00:00:00.000000000',1.00000000,'ING','2015-08-13 14:27:18.956000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoFormaDePago" ( "IdMovimientoFormaDePago","IdMedioDePago","IdMovimientoComprobante","IdBeneficiario","NombreLargoBeneficiario","SecuenciaAgrupacion","NombreMedioDePago","IdCuentaBancaria","NombreTipoCuentaBancaria","IdTarjetaBancaria","NombreTipoTarjetaBancaria","IdBanco","NombreBanco","NumeroMedioDePago","Sucursal","FechaVctoDocumento","MontoLiquidacionFormaDePago","IdEstadoMovimientoFormadePago","FechaIngresoRegistro" )  
VALUES(12,'EFE',3,1,'aadasdad',0,'',0,'',0,'',0,'','','','2015-07-31 00:00:00.000000000',1.00000000,'ING','2015-08-13 14:31:42.327000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoFormaDePago" ( "IdMovimientoFormaDePago","IdMedioDePago","IdMovimientoComprobante","IdBeneficiario","NombreLargoBeneficiario","SecuenciaAgrupacion","NombreMedioDePago","IdCuentaBancaria","NombreTipoCuentaBancaria","IdTarjetaBancaria","NombreTipoTarjetaBancaria","IdBanco","NombreBanco","NumeroMedioDePago","Sucursal","FechaVctoDocumento","MontoLiquidacionFormaDePago","IdEstadoMovimientoFormadePago","FechaIngresoRegistro" )  
VALUES(13,'EFE',3,1,'aadasdad',0,'Efectivo',0,'',0,'',0,'','','','2015-07-31 00:00:00.000000000',1.00000000,'ING','2015-08-13 14:32:28.959000000');
INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoFormaDePago" ( "IdMovimientoFormaDePago","IdMedioDePago","IdMovimientoComprobante","IdBeneficiario","NombreLargoBeneficiario","SecuenciaAgrupacion","NombreMedioDePago","IdCuentaBancaria","NombreTipoCuentaBancaria","IdTarjetaBancaria","NombreTipoTarjetaBancaria","IdBanco","NombreBanco","NumeroMedioDePago","Sucursal","FechaVctoDocumento","MontoLiquidacionFormaDePago","IdEstadoMovimientoFormadePago","FechaIngresoRegistro" )  
VALUES(14,'EFE',3,1,'aadasdad',0,'Efectivo',0,'',0,'',0,'','','','2015-07-31 00:00:00.000000000',1.00000000,'ING','2015-08-13 14:33:40.279000000');


commit;
