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
-- LIBERACION SEMANA: 28-05-2016
-- LIBERACION SEMANA: 03-06-2016

--################################################
-- JG - DOF
--################################################

ALTER TABLE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos"
ADD("IdEstadoAutorizacion"			NVARCHAR(3) 	DEFAULT '' NOT NULL); 

ALTER TABLE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos"
ADD ("OperacionInscripcion" NVARCHAR(40) NOT NULL DEFAULT '');

ALTER TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"
ADD ( "IdOrigen" BIGINT NOT NULL DEFAULT 0);

ALTER TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"
ADD ( "IdCarga" BIGINT NOT NULL DEFAULT 0);


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (328, 'BSCL.Plataforma.BD.Servicios::COM.CrearTipoOrigenFondo' , 0,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (329, 'BSCL.Plataforma.BD.Servicios::COM.ModificarTipoOrigenFondo' , 0,  'P', ''); 

--################################################
-- JG - PER
--################################################
 
 ALTER TABLE "BSCL.SocioNegocio.BD::PER.Persona"
 ADD( "IdNombreFantasiaPrincipal" BIGINT DEFAULT 0 NOT NULL );
 
 --################################################
-- JG - RELACION - % Participación sociedad.
--################################################
 
 ALTER TABLE "BSCL.SocioNegocio.BD::REL.TipoRelacion"
 ADD( "UsaPorcentajeParticipacion" 	NVARCHAR(1)	default 'N' not null);
 
 ALTER TABLE "BSCL.SocioNegocio.BD::REL.RelacionPersona"
 ADD( "PorcentajeParticipacion" DECIMAL(26,8)  DEFAULT 0 not null);
 
 
--################################################
-- ERIC ALARCON - PER 10-06-2014
--################################################ 
 
--INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
--VALUES ('A','Ambas'); --JG, revisar.
 
