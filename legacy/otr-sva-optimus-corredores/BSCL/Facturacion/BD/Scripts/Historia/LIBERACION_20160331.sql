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
-- LIBERACION SEMANA: 04-03-2016
--



--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

-- PEW PEW!
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(26,'FAC','FAC.ComprobanteDetalle');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(27,'EMI','FAC.ComprobanteDetalle');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(28,'ENV','FAC.ComprobanteDetalle');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(29,'ANU','FAC.ComprobanteDetalle');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(30,'ERR','FAC.ComprobanteDetalle');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(31,'BLO','FAC.ComprobanteDetalle');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(32,'PEN','FAC.ComprobanteDetalle');


Alter Table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
drop ("IdTipoTituloMovimiento");

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdFormaCustodia" NVARCHAR(3) DEFAULT '');
 
drop table "BSCL.Facturacion.BD::FAC.TipoTituloMovimiento" cascade;

drop sequence "BSCL.Facturacion.BD.Secuencias::TipoTituloMovimiento" cascade;

DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::TipoTituloMovimientoExiste";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(233,'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante',0,'P','');
 
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(257,'BSCL.Facturacion.BD.Servicios::CrearComprobanteNotaDeCredito',0,'P','');


--- Cambio para incorporar los nuevos cambios en tesoreria
drop sequence "BSCL.Facturacion.BD.Secuencias::Tesoreria" cascade;
create sequence "BSCL.Facturacion.BD.Secuencias::Tesoreria" 
	start     with 10 
	increment by 1
	minvalue  1;	

ALTER TABLE "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ADD ("IdTipoComprobante" NVARCHAR(3) DEFAULT '');
ALTER TABLE "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ADD ("IdTipoProducto" NVARCHAR(4) DEFAULT '');
ALTER TABLE "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ADD ("IdTipoOperacion" NVARCHAR(6) DEFAULT '');
ALTER TABLE "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ADD ("CodigoPreferencia" NVARCHAR(40) DEFAULT '');


--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
