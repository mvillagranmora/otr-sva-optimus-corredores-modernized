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
-- LIBERACION SEMANA: 30-05-2016
--



--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCECAC','','FAC', 'NOTA CREDITO ELECTRONICA COMPRA ACCIONES NACIONALES','VIG');
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(42,'NCECAC','','NCE','INAC','COMACN','TOTAL');
insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCEVAC','','FAC', 'NOTA CREDITO ELECTRONICA VENTA ACCIONES NACIONALES','VIG');
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(43,'NCEVAC','','NCE','INAC','VENACN','TOTAL');
insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCRVAC','','FAC', 'NOTA CREDITO VENTA ACCIONES NACIONALES','VIG');
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(44,'NCRVAC','','NCR','INAC','VENACN','TOTAL');
insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCRCAC','','FAC', 'NOTA CREDITO COMPRA ACCIONES NACIONALES','VIG');
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(45,'NCRCAC','','NCR','INAC','COMACN','TOTAL');

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::TotalCobrosMovimientosComprobantes";

create sequence "BSCL.Facturacion.BD.Secuencias::CuentaInversion" 
	start     with 10 
	increment by 1
	minvalue  1;	
	
--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------







