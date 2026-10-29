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
-- LIBERACION SEMANA: 28-06-2016
--

--####################################################################################
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1235,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante',0,'Q','Consulta movimiento comprobante');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1236,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros',0,'Q','Consulta movimiento comprobante cobros');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1237,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago',0,'Q','Consulta movimiento comprobante forma de pago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1238,'BSCL.Facturacion.BD.Vistas::ConsultarTipoTransaccion',0,'Q','Consulta tipo transaccion de facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1239,'BSCL.Facturacion.BD.Vistas::ConsultarEstadoFacturacion',0,'Q','Consulta estado facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1240,'BSCL.Facturacion.BD.Vistas::ConsultarComprobante',0,'Q','Consulta comprobante');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1241,'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle',0,'Q','Consulta comprobante detalle');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1242,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle',0,'Q','Consulta movimiento comprobante detalle');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1243,'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros',0,'Q','Consulta comprobante cobros');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1244,'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePago',0,'Q','Consulta comprobante forma de pago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1245,'BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante',0,'Q','Consulta tipo comprobante facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1246,'BSCL.Facturacion.BD.Vistas::ConsultarEstadoComprobante',0,'Q','Consulta estado comprobante facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1247,'BSCL.Facturacion.BD.Vistas::ConsultarModoFacturacion',0,'Q','Consulta modo facturacion');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdAsignacionRef" BIGINT DEFAULT 0);
 
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(48,'FECMCX','','FAE','INCX','COMCFX','TOTAL');  
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(49,'FEVNCX','','FAE','INCX','VENCFX','TOTAL');

--Plataforma
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FECMCX','','FAC','FACTURA ELECTRONICA DE COMPRA DE CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FEVNCX','','FAC','FACTURA ELECTRONICA DE VENTA DE CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','VIG');
 
