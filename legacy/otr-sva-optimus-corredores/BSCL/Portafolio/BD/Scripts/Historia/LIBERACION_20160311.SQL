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
-- LIBERACION SEMANA: 11-03-2016
--

--################################################
--       CAPONTE.Anotaciones
--################################################



--------------------------------------------------
--      FIN Anotaciones
--------------------------------------------------
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.Anotacion" (
   	"IdAnotacion"					BIGINT			NOT NULL, 
   	"IdPortafolio"					BIGINT			NOT NULL,
   	"FechaAnotacion"				SECONDDATE		NOT NULL 	DEFAULT 0,
   	"Titulo"						NVARCHAR(100)	NOT NULL,
   	"Detalle"	 					NVARCHAR(500)   NOT NULL,
   	"FechaIngreso"       			SECONDDATE      NOT NULL    DEFAULT '0001-01-01 00:00:00',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  		DEFAULT 0,
  	PRIMARY KEY ("IdAnotacion")
);

alter table "BSCL.Portafolio.BD::POR.Anotacion"  drop constraint "FK_Portafolio_Anotacion";  
alter table "BSCL.Portafolio.BD::POR.Anotacion"
   add constraint "FK_Portafolio_Anotacion" foreign key ("IdPortafolio")
      references "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio");
	  
create sequence "BSCL.Portafolio.BD.Secuencias::Anotacion" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(218,'BSCL.Portafolio.BD.Servicios::CrearAnotacionPortafolio',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(219,'BSCL.Portafolio.BD.Servicios::ModificarAnotacionPortafolio',0,'P','');

--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

-- PEW PEW!

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
