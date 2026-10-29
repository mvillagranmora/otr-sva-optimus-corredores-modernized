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
-- LIBERACION SEMANA: 27-05-2016
--

--####################################################################################
-- <NRM> 
--####################################################################################

RENAME COLUMN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"."FechaContable" TO "FechaAfectaSaldo";
RENAME COLUMN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"."SaldoContable" TO "SaldoDisponible";
RENAME COLUMN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"."SaldoLiquidacion" TO "SaldoContable";
