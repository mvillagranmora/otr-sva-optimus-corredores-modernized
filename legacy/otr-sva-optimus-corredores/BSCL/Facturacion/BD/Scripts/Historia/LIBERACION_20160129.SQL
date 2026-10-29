-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

set schema bscl;

--
--
-- LIBERACION SEMANA: 29-01-2016
--

alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 drop ("IdMonedaOrigen");

alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("IdMoneda" NVARCHAR(3) DEFAULT '');

alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("IdMonedaComprobante" NVARCHAR(3) DEFAULT '' NOT NULL);

alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("IdMonedaLiquidacion" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("MontoComprobante" DECIMAL(26,8) DEFAULT 0 NOT NULL); 
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("MontoLiquidacion" DECIMAL(26,8) DEFAULT 0);   
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("MontoCobroComprobante"  DECIMAL(26,8) DEFAULT 0);   
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
 add ("MontoCobroLiquidacion"  DECIMAL(26,8) DEFAULT 0);  
  
  

alter table "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdMoneda" NVARCHAR(3) DEFAULT '');  
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdMonedaComprobanteDetalle" NVARCHAR(3) DEFAULT '' NOT NULL);   
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdMonedaLiquidacion"  NVARCHAR(3) DEFAULT '');   
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("MontoComprobanteDetalle" DECIMAL(26,8) DEFAULT 0 NOT NULL);     
 
alter table "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("MontoLiquidacion" DECIMAL(26,8) DEFAULT 0);     
 
 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 drop ("IdMonedaOrigen");
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 drop ("IdMonedaDestino"); 


alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("MontoMovimientoComprobanteDetalle" DECIMAL(26,8) DEFAULT 0 NOT NULL);
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("MontoLiquidacion" DECIMAL(26,8) DEFAULT 0); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdMoneda" NVARCHAR(3) DEFAULT '');   
  
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdMonedaMovimientoComprobanteDetalle" NVARCHAR(3) DEFAULT '' NOT NULL);   
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdMonedaLiquidacion" NVARCHAR(3) DEFAULT '');
 



alter table "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
 add ("IdMonedaComprobanteCobro" NVARCHAR(3) DEFAULT '');   
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
 add ("IdMonedaLiquidacion" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
 add ("MontoComprobanteCobro" DECIMAL(26,8) DEFAULT 0 NOT NULL);
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
 add ("MontoLiquidacion" DECIMAL(26,8) DEFAULT 0);  
 
 
 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdEmisor" NVARCHAR(40) DEFAULT '');  
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("EsFungible" NVARCHAR(1) DEFAULT 'N' NOT NULL);  
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 alter ("IdTipoReajuste" NVARCHAR(3) NOT NULL);   
 
 
   
 
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
VALUES (52,'CATCIF','CTA.CargoAbonoMovimiento','FAC','INIF','COMIIF','TOTAL');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
VALUES (53,'ABTVRF','CTA.CargoAbonoMovimiento','FAC','INRF','VENRF','TOTAL');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
VALUES (54,'ABTVIF','CTA.CargoAbonoMovimiento','FAC','INIF','VENIIF','TOTAL');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVRF','AB','CTA','TOTAL FACTURA VENTA RF','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCIF','CA','CTA','TOTAL FACTURA COMPRA IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVIF','AB','CTA','TOTAL FACTURA VENTA IF','VIG'); 
 
  
 
--Vistas
/*"BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"
"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePagoComprobante"
"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMovimientoComprobanteFormaDePago"
"BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"
"BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante"
"BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros"
"BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle"
"BSCL.Facturacion.BD.Servicios::TotalCobrosMovimientosComprobantes"
"BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobros"
*/
commit;


