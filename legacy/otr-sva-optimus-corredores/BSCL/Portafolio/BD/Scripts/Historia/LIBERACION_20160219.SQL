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

--################################################
--       JG.CAMBIO DE ESTADO PORTAFOLIO  
--################################################
SET SCHEMA BSCL;
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAEPOR','','POR','CAMBIO ESTADO PORTAFOLIO','VIG');

INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('CAEPOR',1,'SOC','AUT_POR_CAE','Cambio de Estado Portafolio',1,'S','' ,'' ,0,0,'VIG');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(178,'BSCL.Portafolio.BD.Servicios::ModificarEstadoPortafolio',0,'P','');
--------------------------------------------------
--      FIN CAMBIO DE ESTADO PORTAFOLIO  
--------------------------------------------------


--################################################
--       JG.CORRECCIONES USO PORTAFOLIO 
--################################################
SET SCHEMA BSCL;
DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 124;
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::CrearUsoPortafolio";
--------------------------------------------------
--      FIN CORRECCIONES USO PORTAFOLIO 
--------------------------------------------------


--################################################
--       JG.MODIF.EN CAMBIOS ESTADO PORTAFOLIO
--################################################
SET SCHEMA BSCL;
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
	ADD ("IdTipoDesbloqueo" NVARCHAR(40));
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
	ADD ("FechaDesbloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
	ADD ("ComentarioDesbloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);
--------------------------------------------------
--      FIN MODIF.EN CAMBIOS ESTADO PORTAFOLIO 
--------------------------------------------------

