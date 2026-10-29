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

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 08-04-2016
--



--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 drop ("CodigoMedioDePago");

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 drop ("NumeroCheque");
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 drop ("Sucursal");
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 drop ("FechaVctoDocumento");  
 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
alter ("NumeroCuentaBancaria" NVARCHAR(100) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
alter ("IdTipoTarjetaBancaria" NVARCHAR(40) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
alter ("NombreTipoTarjetaBancaria" NVARCHAR(100) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
alter ("NumeroTarjetaBancaria" NVARCHAR(100) DEFAULT '');



alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 add ("FechaVctoTarjetaBancaria" SECONDDATE DEFAULT '0001-01-01');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 add ("NumeroSerie" NVARCHAR(40) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 add ("NumeroDocumento" BIGINT DEFAULT 0);
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
 add ("FechaDocumentoPago" SECONDDATE DEFAULT '0001-01-01');
 



insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(24,'FEPRES','','FAE','','PRESTA','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(25,'FOPRES','','FAO','','PRESTA','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(26,'FECMAX','','FAE','INAX','COMACX','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(27,'FEVNAX','','FAE','INAX','VENACX','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(28,'FECMMI','','FAE','INMI','COMMIL','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(29,'FEVNMI','','FAE','INMI','VENMIL','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(30,'FECMRF','','FAE','INRF','COMRF','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(31,'FEVNRF','','FAE','INRF','VENRF','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(32,'FECMIF','','FAE','INIF','COMIF','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(33,'FEVNIF','','FAE','INIF','VENIF','TOTAL');
--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
