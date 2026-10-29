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
-- LIBERACION SEMANA: 13/05/2016
--

--################################################
-- MMACHMAR - Deteccion de uso de reglas
--################################################

SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Portafolio.BD.Reglas::PortafolioExiste";


--################################################
-- JG. PREFERENCIA FACTURACION POR DEFECTO
--################################################
ALTER TABLE "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" 
ADD("PorDefecto" NVARCHAR(1) not null default 'N');
 