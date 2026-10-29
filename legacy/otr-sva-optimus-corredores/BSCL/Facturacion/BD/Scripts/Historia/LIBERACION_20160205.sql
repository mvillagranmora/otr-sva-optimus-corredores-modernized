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

SET schema BSCL;
--
--
-- LIBERACION SEMANA: 05-02-2016
--
Drop procedure "BSCL.Facturacion.BD.Servicios::AnularMvtoManualRV"

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(145,'BSCL.Facturacion.BD.Servicios::AnularMovimientoComprobante',0,'P','');

INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(24,'ACT','FAC.ComprobanteFormaDePago');
INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(25,'ANU','FAC.ComprobanteFormaDePago');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(149,'BSCL.Facturacion.BD.Servicios::AnularComprobante',0,'P','');

/*
"BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"
*/



