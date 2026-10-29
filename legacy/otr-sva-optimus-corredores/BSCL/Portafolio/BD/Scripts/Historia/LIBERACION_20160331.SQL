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
-- LIBERACION SEMANA: 24-03-2016
--

--################################################
--       <Cflores>.<Incorporación flijo Autorizaciones a Crear y modificar Portafolio
--################################################



insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEPOR',1,'SOC','AUT_POR_NUE','Nuevo Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODPOR',1,'SOC','AUT_POR_MOD','Modificación del Portafolio',1,'S','' ,'' ,0,0,'VIG');


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEPOR','MP','SOC','NUEVO PORTAFOLIO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODPOR','MP','SOC','MODIFICACION DE PORTAFOLIO','VIG'); 

INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(5,'PEN','PORTAFOLIO');

/*
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(37,'ACT','PORTAFOLIO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(38,'BLO','PORTAFOLIO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(39,'PEN','PORTAFOLIO');
*/

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------

--################################################
--     JG - Preferencias de Pago Portafolio
--################################################

INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"("IdEstadoPortafolio","IdEstado","NombreEntidad")
VALUES(6,'ACT','PREFERENCIAPAGO');
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"("IdEstadoPortafolio","IdEstado","NombreEntidad")
VALUES(7,'INA','PREFERENCIAPAGO');

ALTER table "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
ADD("IdTarjetaBancaria" BIGINT not null default 0);

--------------------------------------------------
--      FIN  - Preferencias de Pago Portafolio
--------------------------------------------------


