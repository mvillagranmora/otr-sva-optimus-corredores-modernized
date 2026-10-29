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
-- LIBERACION SEMANA: 06/05/2016
--

--################################################
-- JG. MODELO ANTIGUO FACT. DEPRECADO
--################################################

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("IdModeloFacturacionBase");
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("SeparaLineaFacturaPorTransaccion");
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("SeparaFacturaPorInstrumento"); 
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("FusionaLineaFacturaPorPrecio"); 
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("IdModoFacturacion");
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("IdEntidadModoFacturacion");
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("OperaContraCuentaInversion");
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP ("NeteaIngresosEgresosTesoreria");

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaFacturacionContrato"; 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaFacturacionConvenio"; 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaFacturacionNormal";

--################################################
-- JG. MODELO ANTIGUO USO PORTAFOLIO DEPRECADO
--################################################

DROP TABLE "BSCL.Portafolio.BD::POR.UsoPortafolio" CASCADE;
DROP SEQUENCE "BSCL.Portafolio.BD.Secuencias::UsoPortafolio";
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarUsoPortafolio";
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ModificarUsoPortafolio";
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarUsoPortafolio";
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::RequiereAutorizacionUsoPortafolio";