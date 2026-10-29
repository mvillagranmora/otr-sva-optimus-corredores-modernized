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

-- ** LIBREACION  RELACIONADOS /  (Inicio) **



​ --Modificacion de estructura
SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" add ("IdEmpresa" BIGINT);


DELETE FROM  "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado";

INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,1,'JEFCUS','Jefe de Custodia');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,2,'PERADM','Personal Administrativo');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,3,'TRA','Trader');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,4,'TES','Tesorero');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,5,'AGE','Agente');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,6,'SUP','Supervisor');


SET SCHEMA "BSCL"; --BORRAR TABLA DEL MODELO
DROP TABLE "BSCL.Plataforma.BD::PLA.Entidad";


SET SCHEMA "BSCL";	--BORRAR VISTA
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarConstante"

SET SCHEMA "BSCL";	--BORRAR TABLA DEL MODELO
DROP TABLE "BSCL.Plataforma.BD::REG.Constante";

---CREACION DE TABLAS
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoGenero" (
		 "IdTipoGenero" 	 NVARCHAR(2) 	 not null,
		 "Descripcion"	 	 NVARCHAR(100)   not null default '',
		 PRIMARY KEY ("IdTipoGenero")
	);
	
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoPersona" (
		 "IdTipoPersona" 	 NVARCHAR(2) 	 not null,
		 "Descripcion" 		 NVARCHAR(100)   not null default '',
		 PRIMARY KEY ("IdTipoPersona")
	);	

	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" (
		 "IdTipoMedioContacto" 	 	 NVARCHAR(8) 	 not null,
		 "Descripcion" 	 			 NVARCHAR(100)   not null default '',
		 "SubTipo"					 NVARCHAR(8)    not null default '',
		 PRIMARY KEY ("IdTipoMedioContacto")
	);

	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" (
		 "IdPropositoMedioContacto"	 		 NVARCHAR(8) 	 not null,
		 "Descripcion" 						 NVARCHAR(100)   not null default '',
		 PRIMARY KEY ("IdPropositoMedioContacto")
	);
	
---INSERCION EN TABLAS

DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoGenero";
DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoPersona";
DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoMedioContacto";
DELETE FROM "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto";

INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoGenero" ("IdTipoGenero","Descripcion")
VALUES ('F','Femenino');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoGenero" ("IdTipoGenero","Descripcion")
VALUES ('M','Masculino');

INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
VALUES ('N','Persona Natural');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
VALUES ('J','Persona Juridica');

INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DFISICA','Dirección','DIRFIS');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DCASILLA','Casilla','DIRFIS');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('TFIJO','Telefono Fijo','TELE');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('TMOVIL','Telefono Celular','TELE');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DEMAIL','Email','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DWEB','Página Web','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DTWIT','Twitter','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DFACE','FaceBook','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DGOOGLE','Google +','DIRELEC');

INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('PART','Particular');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('COME','Comercial');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('RECA','Recados');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('FACT','Facturación');

------------------------------ Servicios Mantenedor Tipo Relacion -----------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(180,'BSCL.SocioNegocio.BD.Servicios::REL.CrearTipoRelacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(181,'BSCL.SocioNegocio.BD.Servicios::REL.ModificarTipoRelacion',0,'P','');




SET SCHEMA "BSCL";
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CLIENTE-ESTADO', 'CLIENTE-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CONTRAPARTE-ESTADO', 'CONTRAPARTE-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('EMPLEADO-ESTADO', 'EMPLEADO-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PERSONA-ESTADO', 'PERSONA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PROVEEDOR-ESTADO', 'PROVEEDOR-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CUENTABANCARIA-ESTADO', 'CUENTABANCARIA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DOCUMENTOIDENTIDAD-ESTADO', 'DOCUMENTOIDENTIDAD-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('NOMBREFANTASIA-ESTADO', 'NOMBREFANTASIA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TARJETABANCARIA-ESTADO', 'TARJETABANCARIA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ATRIBUTO', 'ATRIBUTO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ORDENAUTORIZADO-ESTADO', 'ORDENAUTORIZADO-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DIRECCIONELECTRONICA', 'DIRECCIONELECTRONICA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DIRECCIONFISICA', 'DIRECCIONFISICA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DOCUMENTOIDENTIDAD', 'DOCUMENTOIDENTIDAD');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TARJETABANCARIA', 'TARJETABANCARIA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TELEFONO', 'TELEFONO');






-- ** LIBREACION  RELACIONADOS /  (Termino) **



--################################################
--       JG.CAMBIO OPERADORES - SE MUEVE MODELO 
--################################################

SET SCHEMA "BSCL";
DROP TABLE "BSCL.SocioNegocio.BD::OPE.Operador"; 
DROP SEQUENCE "BSCL.SocioNegocio.BD.Secuencias::Operador";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::OPE.ConfirmarModificarOperadorDatosGenerales";
DROP PROCEDURE  "BSCL.Operador.BD.Servicios::CrearOperador";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::OPE.ModificarOperadorDatosGenerales";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::OPE.RechazaAutorizacionOperadorDatosGenerales";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::OPE.RequiereAutorizacionOperadorDatosGenerales";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::OPE.ConsultarEstadoOperador"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::OPE.ConsultarOperador"; 
DELETE FROM "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" WHERE "IdTipoSocioDeNegocio" = 'OPE';

--------------------------------------------------
--      FIN CAMBIO OPERADORES 
--------------------------------------------------

alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("IdTipoBloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("FechaBloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("ComentarioBloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);


--################################################
--       JG.CAMBIOS N380 (JIRA: SAP-2439)
--################################################

SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::PER.Persona"
ADD("IdPaisNacionalidadPrincipal" BIGINT CS_FIXED);
ALTER TABLE "BSCL.SocioNegocio.BD::PER.Persona"
ADD("GlobalLegalEntity"	NVARCHAR(520) NOT NULL DEFAULT '');

--------------------------------------------------
--      FIN CAMBIOS N380 
--------------------------------------------------


alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("IdTipoBloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("FechaBloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("ComentarioBloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("IdTipoDesbloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("FechaDesbloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("ComentarioDesbloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);


