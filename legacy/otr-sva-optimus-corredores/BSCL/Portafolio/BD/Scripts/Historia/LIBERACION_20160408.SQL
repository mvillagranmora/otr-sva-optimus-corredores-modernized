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
--       <Cflores>.<Incorporación flujo Autorizaciones a Preferencias de Pago
--################################################





SET SCHEMA "BSCL";
alter table "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"	  add ( "FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
																		"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL);


SET SCHEMA "BSCL";

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCPPAG',1,'PLA','AUT_CPPAG_NUE','Nueva Preferencia de Pago',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCPPAG',1,'PLA','AUT_CPPAG_MOD','Modificación Preferencia de Pago',1,'S','' ,'' ,0,0,'VIG');	


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPPAG','PC','PLA','NUEVA PREFERENCIA DE PAGO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCPPAG','PC','PLA','MODIFICACION PREFERENCIA DE PAGO','VIG'); 

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NCPPAG','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MCPPAG','N');


--------------------------------------------------
--      FIN Preferencias de Pago
--------------------------------------------------


--################################################
--       MMACHMAR - Preferencias Facturación
--################################################

SET SCHEMA "BSCL";

RENAME COLUMN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio"."SeparaFacturaPorTransaccion" TO "SeparaLineaFacturaPorTransaccion";
RENAME COLUMN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio"."FusionaFacturaPorPrecio" TO "FusionaLineaFacturaPorPrecio";

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("OperaContraCuentaInversion" NVARCHAR(1) NOT NULL DEFAULT 'N');

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("NeteaIngresosEgresosTesoreria" NVARCHAR(1) NOT NULL DEFAULT 'N');

--------------------------------------------------
--      FIN Preferencias Facturación
--------------------------------------------------


--################################################
--       <Cflores>.<Incorporación Tipos de Autorizados y Relacionados
--################################################


SET SCHEMA "BSCL";
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.TipoAutorizado" (
   "IdEmpresa"                  BIGINT       	   NOT NULL,
   "IdTipoAutorizado"           NVARCHAR(40)       NOT NULL, 
   "NombreTipoAutorizado"       NVARCHAR(100)      NOT NULL,
   "IdTipoRelacion" 			NVARCHAR(6)		   NOT NULL DEFAULT '', 
   PRIMARY KEY ("IdEmpresa","IdTipoAutorizado")
);

insert into "BSCL"."BSCL.Portafolio.BD::POR.TipoAutorizado" 
("IdEmpresa", "IdTipoAutorizado", "NombreTipoAutorizado","IdTipoRelacion")
values(1,'ORDEN-CREAR','Crear Órdenes','AUTZDO');

insert into "BSCL"."BSCL.Portafolio.BD::POR.TipoAutorizado" 
("IdEmpresa", "IdTipoAutorizado", "NombreTipoAutorizado","IdTipoRelacion")
values(1,'ORDEN-CONSULTAR','Consultar Órdenes','AUTZDO');

insert into "BSCL"."BSCL.Portafolio.BD::POR.TipoAutorizado" 
("IdEmpresa", "IdTipoAutorizado", "NombreTipoAutorizado","IdTipoRelacion")
values(1,'ORDEN-FIRMAR','Firmar Órdenes','AUTZDO');

SET SCHEMA "BSCL"; --Relacion de vuelta dinamica de Autorizacion
UPDATE "BSCL.SocioNegocio.BD::REL.TipoRelacion" SET "IdTipoRelacionDestino"= 'AUTDOR' WHERE "IdTipoRelacion"='AUTZDO';




--------------------------------------------------
--      FIN Preferencias Autorizados
--------------------------------------------------

--################################################
--       <Cflores>.<Incorporación campos de uso en Portafolio
--################################################

SET SCHEMA "BSCL";
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD( "IdDireccionEnvioCorrespondencia"			BIGINT CS_FIXED			 NOT NULL DEFAULT 0,
	 "IdTelefonoContacto"						BIGINT CS_FIXED 		 NOT NULL DEFAULT 0);
--------------------------------------------------
--      FIN Incorporación campos de uso en Portafolio
--------------------------------------------------


--################################################
--       <CAPONTE>.<Incorporación Motivo, Ambito y Relevancia anotaciones portafolio
--################################################

CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.AtributoAnotacion" (
   	"IdEmpresa"							BIGINT          NOT NULL,
	"IdAtributoAnotacion"				NVARCHAR(6)		NOT NULL, 
	"TipoAtributoAnotacion"				NVARCHAR(10)	NOT NULL,
   	"NombreAtributoAnotacion"			NVARCHAR(100)	NOT NULL,
  	PRIMARY KEY ("IdEmpresa","IdAtributoAnotacion")
);

DELETE FROM "BSCL.Portafolio.BD::POR.Anotacion";

ALTER table "BSCL.Portafolio.BD::POR.Anotacion"
ADD("IdMotivoAnotacion" NVARCHAR(6) not null);

ALTER table "BSCL.Portafolio.BD::POR.Anotacion"
ADD("IdRelevanciaAnotacion" NVARCHAR(6) not null);

ALTER table "BSCL.Portafolio.BD::POR.Anotacion"
ADD("IdAmbitoAnotacion" NVARCHAR(6) not null);

insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'ENVCAR','MOTIVO','Envío Cartola');
insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'EVTBUR','MOTIVO','Evento Bursátil');
insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'AVICOM','MOTIVO','Avisos Comerciales');
insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'RECORD','MOTIVO','Recordatorio');
insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'OTROS','MOTIVO','Otros');

insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'ALTA','RELEVANCIA','Alta');
insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'NORMAL','RELEVANCIA','Normal');

insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'PUBLIC','AMBITO','Público');
insert into "BSCL"."BSCL.Portafolio.BD::POR.AtributoAnotacion" 
("IdEmpresa", "IdAtributoAnotacion", "TipoAtributoAnotacion", "NombreAtributoAnotacion")
values(1,'PRIVAD','AMBITO','Privado');


