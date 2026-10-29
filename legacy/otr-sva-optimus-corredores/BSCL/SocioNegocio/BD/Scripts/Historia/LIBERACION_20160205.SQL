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
-- LIBERACION SEMANA: 05-02-2016
--



--Auditorias


 --Modificacion de estructura
SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.DireccionFisica" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.DireccionElectronica" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.Telefono" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.CuentaBancaria" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" add ("IdUsuarioCreador"   BIGINT  DEFAULT 0);



-- ** LIBREACION  Relacionados /  (Inicio) **


SET SCHEMA "BSCL"; --Renombrar campo
RENAME COLUMN "BSCL.SocioNegocio.BD::REL.TipoRelacion"."ClasificacionUI" TO "Administracion";


SET SCHEMA "BSCL"; --borrar campo
alter table "BSCL.SocioNegocio.BD::REL.RelacionPersona"	Drop  ("IdRelacionPersonaOrigen");	

SET SCHEMA "BSCL"; --Renombrar campo
RENAME COLUMN "BSCL.SocioNegocio.BD::REL.RelacionPersona" ."FechaIngresoRegistro" TO "FechaIngreso";


SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::REL.RelacionPersona" add ( "FechaCreacion" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL,
															  "FechaModificacion" 		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL,
															  "IdUsuarioCreador"	 	BIGINT  	DEFAULT 0);
SET SCHEMA "BSCL"; --Agregar campo															  
alter table "BSCL.SocioNegocio.BD::REL.RelacionPersona" add ("IdEstadoAutorizacion" 		NVARCHAR(3)     default 'PEN' not null);

SET SCHEMA "BSCL";
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(152,'BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(153,'BSCL.SocioNegocio.BD.Servicios::REL.ModificarRelacionPersona',0,'P','');


SET SCHEMA "BSCL"; --borrar campo
alter table "BSCL.SocioNegocio.BD::EMP.Empleado"	Drop  ("CodigoTipoCargoEmpleado");	

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("CodigoTipoCargoEmpleado" NVARCHAR(6));

SET SCHEMA "BSCL"; --borrar campo
alter table "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"	Drop  ("CodigoTipoCargoEmpleado");	

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" add ("CodigoTipoCargoEmpleado" NVARCHAR(6));


SET SCHEMA "BSCL"; --cargar tabla
DELETE FROM  "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado";
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,'JEFCUS','Jefe de Custodia');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(2,'PERADM','Personal Administrativo');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(3,'TRA','Trader');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(4,'TES','Tesorero');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(5,'AGE','Agente');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(6,'SUP','Supervisor');

SET SCHEMA "BSCL"; 
UPDATE "BSCL.SocioNegocio.BD::EMP.Empleado" SET "CodigoTipoCargoEmpleado"='JEFCUS'



SET SCHEMA "BSCL"; --Modificar campo
ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento" ALTER ("EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6));


SET SCHEMA "BSCL";
drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";
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
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"Direccion" 				NVARCHAR(100),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista" NVARCHAR(40),
"POR_IdPerfilComercial" 	NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE,
"CNT_Codigo" 					NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT
 );


SET SCHEMA "BSCL"; --cargar tabla
DELETE FROM  "BSCL.SocioNegocio.BD::REL.TipoRelacion"; 


INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('GG',1,'S','PERSONA','Gerente General','EMP','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('GA',1,'S','PERSONA','Gerente de Area o Subgerente','EMP','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('APO',1,'S','CLIENTE','Apoderado','ATAPO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ATAPO',1,'S','PERSONA','Autoriza Apoderado','ATAPO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINPRO',1,'S','CLIENTE','Vinculado por propiedad al intermediario','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINLAB',1,'S','EMPLEADO','​Vinculado laboralmente al intermediario','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINADM',1,'S','CLIENTE','​Vinculado por administración al intermediario','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('CLIOPE',1,'S','CLIENTE','Operador','EM','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('EM',1,'S','PERSONA','Empleado','EMP','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('EMP',1,'S','PERSONA','Empleador','EM','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ENTGEM',1,'S','PERSONA','Entidad del Grupo Empresarial art. 100','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('MIADMS',1,'S','PERSONA','Miembro de la Administracion controle >=10% sociedad art. 100','','N','ACT','RELACIONADOS');






-- ** LIBREACION  Relacionados /  (Termino) **



--####################################################
--			JG.USO DE PLANTILLA EMPLEADO
--####################################################

ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" 
ADD ("UtilizaPlantillaEmpleado" NVARCHAR(1) DEFAULT 'N');

ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" 
ADD ("IdPlantillaEmpleado" NVARCHAR(40) DEFAULT '');

-------------------------------------------------------
--			FIN USO DE PLANTILLA EMPLEADO
-------------------------------------------------------



