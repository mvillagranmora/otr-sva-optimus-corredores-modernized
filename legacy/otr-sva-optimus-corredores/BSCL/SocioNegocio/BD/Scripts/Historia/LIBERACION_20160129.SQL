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

set schema bscl;

--
--
-- LIBERACION SEMANA: 29-01-2016
--




-- ** LIBREACION  Socio de Negocio / Autorizaciones (Inicio) **

SET SCHEMA "BSCL"; 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(130,'BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion',0,'P','');;

SET SCHEMA "BSCL"; 
UPDATE "BSCL"."BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" SET "IdEstado"='VCD' WHERE "IdEstadoSocioDeNegocio"=10


SET SCHEMA "BSCL";
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(23,'PEN','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(24,'PEN','CLIENTE');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(25,'PEN','RELACION');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(26,'PEN','EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(27,'PEN','DOCUMENTOIDENTIDAD');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(28,'PEN','CUENTABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(29,'PEN','TELEFONO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(30,'PEN','DIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(31,'PEN','DIRECCIONFISICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(32,'PEN','TARJETABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(33,'PEN','PROVEEDOR');




SET SCHEMA "BSCL";
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENRPER',1,'SOC','AUT_PER_ENRO','Enrolamiento de Persona',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENRPER','EP','SOC','ENROLAMIENTO DE PERSONA','VIG');




SET SCHEMA "BSCL"; --Modificar campo
ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" ALTER ("CodigoTipoCargoEmpleado" NVARCHAR(40)    default '' );

--borrar Secuencia de QA

SET SCHEMA "BSCL";
drop sequence "BSCL.Cliente.BD.Secuencias::Cliente";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::PersonaMedioContacto";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::ClasificacionDireccion";

-- ** LIBREACION  Socio de Negocio / Autorizaciones (Termino) **









commit;


