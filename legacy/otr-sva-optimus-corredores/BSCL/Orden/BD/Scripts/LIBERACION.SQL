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
-- LIBERACION SEMANA: 29-01-2016
--

--===========================================================


alter table "BSCL.Orden.BD::ORD.LibroOrdenes"
add ("UltimaFechaDeEjecucion" SECONDDATE not null default '0001-01-01 00:00:00');
alter table "BSCL.Orden.BD::ORD.LibroOrdenes"
add ("EstadoAsignacionAutomatica" NVARCHAR(3) not null default 'INA');

