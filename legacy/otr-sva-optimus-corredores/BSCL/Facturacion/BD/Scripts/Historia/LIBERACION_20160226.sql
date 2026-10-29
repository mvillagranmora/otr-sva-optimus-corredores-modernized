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
-- LIBERACION SEMANA: 19-02-2016
--
create sequence "BSCL"."BSCL.Facturacion.BD.Secuencias::Log" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL"."BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoOtrosConceptos"
	start     with 10 
	increment by 1
	minvalue  1;	
	
create sequence "BSCL"."BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronicaOtrosConceptos" 
	start     with 10 
	increment by 1
	minvalue  1;	

create sequence "BSCL"."BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaOtrosConceptos" 
	start     with 10 
	increment by 1
	minvalue  1;
	
drop sequence "BSCL"."BSCL.Facturacion.BD.Secuencias::MovimientoFormaDePago" cascade;	
		
	

update "BSCL"."BSCL.Plataforma.BD::PLA.Concepto"
set "Nombre" = 'MOVIMIENTO DE EGRESO'
Where "IdConcepto" = 'MOVEGR'

update "BSCL"."BSCL.Plataforma.BD::PLA.Concepto"
set "Nombre" = 'MOVIMIENTO DE INGRESO'
Where "IdConcepto" = 'MOVING'
