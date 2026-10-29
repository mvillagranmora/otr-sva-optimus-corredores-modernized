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
-- LIBERACION SEMANA: 13-05-2016
--

--################################################
-- JG - Ajustes Contraparte
--################################################
ALTER TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"
DROP ("IdBanco");
ALTER TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"
DROP ("IdBoveda");


--################################################
-- CFlores - Ajustes Limites
--################################################
SET SCHEMA "BSCL";

ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente"
ADD("IdPlantillaLimiteExtra"	NVARCHAR(40) NOT NULL 	DEFAULT '0');

update "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" set  "IdPlantillaLimiteExtra"=CONCAT('CLI_',CAST("IdCliente" as NVARCHAR));


--################################################
-- JG.DECLARACION DE ORIGEN DE FONDOS.
--################################################

DROP TABLE "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"; 

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" (
	 "IdTipoOperacionOrigenFondos" 	NVARCHAR(6)		NOT NULL , -- Corresponde al "IdTipoOperacion"
	 "Descripcion" 					NVARCHAR(100)	NOT NULL ,
	 "IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEmpresa" 					BIGINT  		NOT NULL,
	 "IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
PRIMARY KEY ("IdTipoOperacionOrigenFondos"));

 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" (
	 "IdDeclaracionOrigenFondos" 	BIGINT			NOT NULL ,
	 "IdCliente"				 	BIGINT			NOT NULL DEFAULT 0,
	 "IdTipoOperacionOrigenFondos"	NVARCHAR(40)	NOT NULL DEFAULT '',
	 "FechaDeclaracion"				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "Comentario"					NVARCHAR(500)	NOT NULL DEFAULT '',
	 "IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEmpresa" 					BIGINT  		NOT NULL,
	 "IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdDeclaracionOrigenFondos"));
 
 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" (
	 "IdTipoOrigenFondo" 				NVARCHAR(40)	NOT NULL ,
	 "IdDeclaracionOrigenFondos" 		BIGINT			NOT NULL DEFAULT 0,
	 "Valor"				 			DECIMAL(26,8) 	NOT NULL DEFAULT 0,
	 "IdUsuarioCreador"					BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 					SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 					SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdTipoOrigenFondo","IdDeclaracionOrigenFondos"));
 
 -- Posibilidad: ConceptoOrigenDeclaracion, en caso de querer customizar.

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" (
	 "IdTipoOrigenFondo" 			NVARCHAR(40)	NOT NULL ,
	 "Nombre"					 	NVARCHAR(100)	NOT NULL DEFAULT '',
	 "ValorDefecto"				 	DECIMAL(26,8) 	NOT NULL DEFAULT 0,
	 "IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEmpresa" 					BIGINT  		NOT NULL,
	 "IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
PRIMARY KEY ("IdTipoOrigenFondo"));

drop sequence "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos";
create sequence "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

DELETE FROM "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo";
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('HER','Herencia',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ISL','Indemnización de seguro y laboral',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('JDA','Juegos de azar',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('JYD','Jubilaciones y desahucios',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VBR','Venta bienes raíces',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VDP','Venta de propiedades',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VDV','Venta de vehículos',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('OTR','Otros',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

---
DELETE FROM "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos";
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMACN','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMCFI','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMMON','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMSIM','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMACX','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('APORFM','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMPAC','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ABNMAN','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');


--################################################
-- MMACHMAR - Deteccion de uso de reglas
--################################################
SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.SocioNegocio.BD.Reglas::PER.PersonaExiste";
