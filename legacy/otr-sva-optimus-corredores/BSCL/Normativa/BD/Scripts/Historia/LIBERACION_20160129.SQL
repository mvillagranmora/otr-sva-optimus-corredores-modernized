-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIENE LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas.
--  * Debe incluir nuevas tablas, indices, secuencias, etc.
--  * Debe incluir INSERT de datos maestros necesarios para el funcionamiento.
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

set schema bscl;

--
--
-- LIBERACION SEMANA: 29-01-2016
--

drop index "BSCL"."BSCL.Normativa.BD::TRI.DetalleDocumento1891.IX01";

commit;
