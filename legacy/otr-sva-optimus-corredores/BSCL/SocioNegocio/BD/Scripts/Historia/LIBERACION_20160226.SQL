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
-- LIBERACION SEMANA: 26-02-2016
--





-- ** LIBREACION  RELACIONADOS /  (Inicio) **


​ --Modificacion de estructura


SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("IdPersonaEmpleador" BIGINT CS_FIXED  DEFAULT 0);


SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("CodigoTipoCargoEmpleado" NVARCHAR(6) DEFAULT '');



SET SCHEMA "BSCL";
DELETE FROM  "BSCL.SocioNegocio.BD::REL.TipoRelacion"; 
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('APO',1,'S','CLIENTE','Apoderado','ATAPO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ATAPO',1,'S','PERSONA','Autoriza Apoderado','APO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINPRO',1,'S','CLIENTE','Vinculado por propiedad al intermediario','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINLAB',1,'S','EMPLEADO','​Vinculado laboralmente al intermediario','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINADM',1,'S','CLIENTE','​Vinculado por administración al intermediario','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('CLIOPE',1,'S','CLIENTE','Operador','EM','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ENTGEM',1,'S','PERSONA','Entidad del Grupo Empresarial','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('MIADMS',1,'S','PERSONA','Miembro de la Administracion controle >=10% sociedad','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('CY',1,'S','PERSONA','Conyuge','','S','ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('REP',1,'S','PERSONA','Representante Legal','','S','ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('CEMDOR',1,'N','CORREDOR','Empleador','','S','ACT','EMPLEADO'); 	--CORREDOR/EMPLEADOR
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('PEMDOR',1,'N','PERSONA','Empleador','','S','ACT','EMPLEADO');	--PERSONA/EMPLEADOR
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('AUTZDO',1,'S','PERSONA','Autorizado a dar órdenes','','S','ACT','AUTORIZADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('AUTDOR',1,'S','PERSONA','Autorizador a dar órdenes','','S','ACT','AUTORIZADO');





-- ** LIBREACION  RELACIONADOS /  (Termino) **


-- CAMBIO DE ESTADO SOCIO DE NEGOCIO

alter table "BSCL.SocioNegocio.BD::CLI.Cliente" add ("IdTipoDesbloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::CLI.Cliente" add ("FechaDesbloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::CLI.Cliente" add ("ComentarioDesbloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);

alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("IdTipoBloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("FechaBloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("ComentarioBloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("IdTipoDesbloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("FechaDesbloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::EMP.Empleado" add ("ComentarioDesbloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);

alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("IdTipoBloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("FechaBloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("ComentarioBloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("IdTipoDesbloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("FechaDesbloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ("ComentarioDesbloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);


alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("IdTipoBloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("FechaBloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("ComentarioBloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("IdTipoDesbloqueo" NVARCHAR(40));
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("FechaDesbloqueo" SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL);
alter table "BSCL.SocioNegocio.BD::CNT.Contraparte" add ("ComentarioDesbloqueo" NVARCHAR(520) DEFAULT '' NOT NULL);

-- FIN CAMBIO DE ESTADO SOCIO DE NEGOCIO


--Eliminar SP deprecado:
DROP PROCEDURE "BSCL.SocioNegocio.BD::REL.Servicios::ModificarRelacionPersona";
