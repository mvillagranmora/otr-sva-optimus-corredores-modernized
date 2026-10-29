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
-- LIBERACION SEMANA: 28/05/2016
--


-- ################## JG - CUENTAS DE CUSTODIOS EN PORTAFOLIO ######################

--drop table "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio";
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"(
	"IdCuentaCustodioPortafolio"  	BIGINT    		NOT NULL,
  	"IdPortafolio"           		BIGINT  		NOT NULL 	DEFAULT 0, 
   	"IdBoveda"       				NVARCHAR(3) 	NOT NULL 	DEFAULT '',
   	"Codigo" 						NVARCHAR(40)	NOT NULL 	DEFAULT '', 
   	"FechaIngreso"       			SECONDDATE      NOT NULL   	DEFAULT '0001-01-01 00:00:00',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  		NOT NULL 	DEFAULT 0,
	"IdEstado"						NVARCHAR(3)   	not null default 'ACT', -- 'ACT','INA'
   	PRIMARY KEY ("IdCuentaCustodioPortafolio")
);


ALTER table "BSCL"."BSCL.Portafolio.BD::POR.Anotacion"
ADD("Borrado" NVARCHAR(6) not null default 'N');
