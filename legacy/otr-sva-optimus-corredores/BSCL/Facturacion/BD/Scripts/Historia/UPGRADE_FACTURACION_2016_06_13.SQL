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
-- LIBERACION SEMANA: 13-06-2016
--

--####################################################################################
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdInstrumentoRelacionado" BIGINT DEFAULT 0);
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdTipoInstrumentoRelacionado" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdClasificacionTipoInstrumentoRelacionado" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("NombreTipoInstrumentoRelacionado" NVARCHAR(100) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("NombreClasificacionTipoInstrumentoRelacionado" NVARCHAR(100) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("NemotecnicoRelacionado" NVARCHAR(40) DEFAULT '');

