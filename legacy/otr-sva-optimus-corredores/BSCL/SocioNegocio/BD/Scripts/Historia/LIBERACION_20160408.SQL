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
-- LIBERACION SEMANA: 08-04-2016
--

--################################################
--    JG - CAMBIOS EN TELEFONO / CONTRAPARTE
--################################################

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoTelefono" (
 "IdTipoTelefono"				NVARCHAR(40) 	NOT NULL ,
 "Nombre"						NVARCHAR(100) 	NOT NULL DEFAULT '',
  PRIMARY KEY ("IdTipoTelefono")
);

INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoTelefono" ("IdTipoTelefono","Nombre")
VALUES('FIJO','Fijo');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoTelefono" ("IdTipoTelefono","Nombre")
VALUES('MOVIL','Móvil');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoTelefono" ("IdTipoTelefono","Nombre")
VALUES('OTRO','Otro');

ALTER TABLE "BSCL.SocioNegocio.BD::PER.Telefono" 
ADD("IdTipoTelefono" NVARCHAR(40) DEFAULT 'OTRO' NOT NULL);


RENAME COLUMN "BSCL.SocioNegocio.BD::CNT.Contraparte"."Codigo" TO "CodigoInscripcion";

RENAME COLUMN "BSCL.SocioNegocio.BD::SOC.Enrolamiento"."CNT_Codigo" TO "CNT_CodigoInscripcion";

DROP TYPE  "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" ;

CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
"IdEnrolamiento"			BIGINT,
"IdEmpresa" 				BIGINT,
"IdUsuario" 				BIGINT,
"IdTipoEntidad" 			NVARCHAR(100),
"IdEntidadEnrolada" 		BIGINT,
"IdTipoDocumentoIdentidad"  NVARCHAR(1),
"DocumentoIdentidad" 		NVARCHAR(40),
"Tipo" 						NVARCHAR(2),
"Nombres" 					NVARCHAR(100),
"ApellidoPaterno" 			NVARCHAR(100),
"ApellidoMaterno" 			NVARCHAR(100),
"RazonSocial" 				NVARCHAR(100),
"IdPaisNacimiento" 			BIGINT,
"FechaNacimiento" 			DATE,
"Sexo" 						NVARCHAR(2),
"EstadoCivil" 				NVARCHAR(2),
"IdCiudad" 					BIGINT,
"IdDivisionPais" 			BIGINT,
"IdTipoTelefono" 			NVARCHAR(40),
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"DireccionCalle" 			NVARCHAR(100),
"DireccionNumero" 			NVARCHAR(40),
"DireccionDepartamento" 	NVARCHAR(40),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista"	NVARCHAR(40),
"CLI_IdTipoCliente"				NVARCHAR(40),
"POR_IdPerfilComercial"			NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE,
"CNT_CodigoInscripcion" 		NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT
 );
 
ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
ADD("IdTipoTelefono" NVARCHAR(40) not null DEFAULT 'OTRO');
 
 
 --################################################
--       MMACHMAR - Cliente - Datos Generales
--################################################

SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" 
ADD ("CodigoEjecutivoExterno" NVARCHAR(40) DEFAULT '' NOT NULL);

--------------------------------------------------
--      FIN Cliente - Datos Generales
--------------------------------------------------
 
 
  --################################################
--       CFLORES - Persona - Cargos Empleado
--################################################


SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" 
ADD ("UtilizaDetalle"		   NVARCHAR(1) DEFAULT 'N'  NOT NULL);


SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::PER.Persona" 
ADD ("DetalleCargo"						NVARCHAR(40) DEFAULT ''  NOT NULL);

SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" 
ADD ("DetalleCargo"						NVARCHAR(40) DEFAULT ''  NOT NULL);

 


SET SCHEMA "BSCL";
DELETE FROM  "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado";

INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )  
VALUES(1,1,'JEFCUS','JEFE DE CUSTODIA','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,2,'PERADM','PERSONAL ADMINISTRATIVO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,3,'TRA','TRADER','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,4,'TES','TESORERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )  
VALUES(1,5,'AGE','AGENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )  
VALUES(1,6,'SUP','SUPERVISOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )   
VALUES(1,7,'OPEOTC','OPERADOR','N');--Operador otra corredora
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,8,'ASEFIN','ASESOR FINANCIERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,9,'DIR','DIRECTOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,10,'GERENT','GERENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )   
VALUES(1,11,'GERGEN','GERENTE GENERAL','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,12,'GERSUB','GERENTE DE AREA O SUBGERENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,13,'ADMIN','ADMINISTRADOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,14,'EMPLEA','EMPLEADO','S');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,15,'JEFMES','JEFE MESA DE DINERO','N');
 
 --------------------------------------------------
--      FIN Persona - Cargos Empleado
--------------------------------------------------
 
--################################################
--       JG - Tipo Cliente
--################################################
 
 SET SCHEMA "BSCL";
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
 "IdTipoCliente" 			NVARCHAR(40)	NOT NULL ,
 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
 "IdEmpresa" 				BIGINT  		NOT NULL,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdTipoCliente"));

INSERT INTO  "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
"IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('FAMILYOFFICE','Family Office',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

 INSERT INTO  "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
 "IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
 VALUES('RETAIL','Retail',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
 
INSERT INTO  "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
"IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INSTITUCIONAL','Institucional',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
 
--------------------------------------------------
--      FIN - Tipo Cliente
--------------------------------------------------


 --################################################
--       MMACHMAR - Tipo cliente en Cliente y Enrolamiento
--################################################

SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" 
ADD ("IdTipoCliente" NVARCHAR(40) NOT NULL DEFAULT '');

ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
ADD("CLI_IdTipoCliente" NVARCHAR(40) NOT NULL DEFAULT '');

--------------------------------------------------
--      FIN Cliente - Tipo cliente en Cliente y Enrolamiento
--------------------------------------------------
