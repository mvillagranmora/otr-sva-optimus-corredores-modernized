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
-- LIBERACION SEMANA: 20-05-2016
DROP TABLE  "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte"; 
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte"(
 "IdContraparte" 			BIGINT  NOT NULL ,
 "IdPersona" 				BIGINT ,
 "IdEmpresa" 				BIGINT ,
 "CodigoInscripcion"		NVARCHAR(40),
 "Emisor" 					NVARCHAR(1) DEFAULT 'N',
 "Depositos" 				NVARCHAR(1) DEFAULT 'N',
 "Bolsa" 					NVARCHAR(1) DEFAULT 'N',
 "Corredora" 				NVARCHAR(1) DEFAULT 'N',
 "CamaraDeCompensacion" 	NVARCHAR(1) DEFAULT 'N',
 "Banco" 					NVARCHAR(1) DEFAULT 'N', 
 "Boveda"					NVARCHAR(1) DEFAULT 'N', 			
 "IdEstado" 				NVARCHAR(3) DEFAULT 'ACT', 
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 "FechaIngreso" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdUsuarioCreador"			BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 			NVARCHAR(40),
 "FechaBloqueo" 			SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 		NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 		NVARCHAR(40),
 "FechaDesbloqueo" 			SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 	NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdOrigen"                 BIGINT DEFAULT 0 NOT NULL,
 "IdCarga"                  BIGINT DEFAULT 0 NOT NULL,
 PRIMARY KEY ("IdContraparte"));
 
 
 DROP TABLE  "BSCL"."BSCL.Homologacion.BD::HOM.Empleado"; 
 CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Empleado" (
 "IdEmpleado" BIGINT  NOT NULL ,
 "IdPersona" BIGINT ,
 "CodigoEmpleado" NVARCHAR(40),
 "IdEstado" NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdEmpresa" BIGINT ,
 "CodigoTipoCargoEmpleado"  NVARCHAR(6)    default '' ,
 "DetalleCargo"						NVARCHAR(40) DEFAULT ''  NOT NULL,
 "IdEmpleadoSupervisor" BIGINT ,
 "IdSucursal" BIGINT ,
 "UtilizaPlantillaEmpleado" NVARCHAR(1) DEFAULT 'N',
 "IdPlantillaEmpleado" NVARCHAR(40) DEFAULT '',
 "FechaInicioCargo" DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaTerminoCargo" DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaIngreso" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL , 
 "IdCarga" BIGINT DEFAULT 0 NOT NULL,
 "CodigoAgenteMigracion" NVARCHAR(40) DEFAULT '' NOT NULL,
 "CodigoSupervisorAgenteMigracion" NVARCHAR(40) DEFAULT '' NOT NULL,
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"   BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 					NVARCHAR(40),
 "FechaBloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 				NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 				NVARCHAR(40),
 "FechaDesbloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 			NVARCHAR(520) DEFAULT '' NOT NULL,
 "MarcaRepetido"                    BIGINT DEFAULT 0 NOT NULL
)  ;


--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

-- PEW PEW!

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
