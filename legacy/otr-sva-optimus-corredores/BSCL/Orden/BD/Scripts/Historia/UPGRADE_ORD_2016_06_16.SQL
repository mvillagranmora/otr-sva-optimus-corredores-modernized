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
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'MonedaSimultanea','Moneda de Operaciones Simultaneas','CLP'); 