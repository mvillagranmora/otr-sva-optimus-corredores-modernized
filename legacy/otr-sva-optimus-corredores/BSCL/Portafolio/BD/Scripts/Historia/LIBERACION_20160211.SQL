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


--####################################################
--			CF.AUTORIZADO
--####################################################

SET SCHEMA "BSCL"; 
DROP TABLE 		"BSCL.Portafolio.BD::POR.Autorizado";
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.Autorizado" (
   	"IdAutorizado"					BIGINT			NOT NULL, 
   	"IdPortafolio"					BIGINT			NOT NULL 	DEFAULT 0,
   	"IdPersona" 					BIGINT   		NOT NULL ,
   	"IdTipoAutorizado"				NVARCHAR(40) 	NOT NULL	DEFAULT 'ORDEN-CREAR',
   	"IdEstado"						NVARCHAR(3) 	NOT NULL 	DEFAULT 'ACT',
   	"IdEstadoAutorizacion"			NVARCHAR(3)		NOT NULL	DEFAULT 'PEN',
   	"FechaIngreso"       			SECONDDATE      NOT NULL    DEFAULT '0001-01-01 00:00:00',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  DEFAULT 0,
  	PRIMARY KEY ("IdAutorizado")
);

SET SCHEMA "BSCL"; 
DROP SEQUENCE 	"BSCL.Portafolio.BD.Secuencias::Autorizado";
create sequence "BSCL.Portafolio.BD.Secuencias::Autorizado" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

SET SCHEMA "BSCL"; 
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(3,'ACT','ORDENAUTORIZADO');
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(4,'BLO','ORDENAUTORIZADO');


-------------------------------------------------------
--			FIN AUTORIZADO
-------------------------------------------------------


--####################################################
--			JG. USO NOMBRE FANTASIA
--####################################################


INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('NFANTASIA1',1,'NFANTASIA','Nombre de Fantasía','Principal','ACT','N','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('POR','POR','Portafolio','ACT');

-------------------------------------------------------
--			FIN USO NOMBRE FANTASIA
-------------------------------------------------------
