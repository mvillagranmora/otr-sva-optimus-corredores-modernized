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
-- LIBERACION SEMANA: 12-02-2016
--

-- ** LIBREACION  RELACIONADOS /  (Inicio) **


 --Modificacion de estructura
SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ("IdPersonaConyuge" 		BIGINT CS_FIXED  DEFAULT 0,
													 "IdPersonaRepresentante"	BIGINT CS_FIXED  DEFAULT 0 );

SET SCHEMA "BSCL";
DELETE FROM  "BSCL.SocioNegocio.BD::REL.TipoRelacion"; 

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('GG',1,'S','PERSONA','Gerente General','EMPDOR','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('GA',1,'S','PERSONA','Gerente de Area o Subgerente','EMPDOR','N','ACT','RELACIONADOS');
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
VALUES('EMPDO',1,'S','PERSONA','Empleado','EMPDOR','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('EMPDOR',1,'S','PERSONA','Empleador','EMPDO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ENTGEM',1,'S','PERSONA','Entidad del Grupo Empresarial art. 100','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('MIADMS',1,'S','PERSONA','Miembro de la Administracion controle >=10% sociedad art. 100','','N','ACT','RELACIONADOS');

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('CY',1,'S','PERSONA','Conyuge','','S','ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('REP',1,'S','PERSONA','Representante Legal','','S','ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('CEMDOR',1,'N','CORREDOR','Empleador','','S','ACT','EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('AUTZDO',1,'S','PERSONA','Autorizado a dar órdenes','','S','ACT','AUTORIZADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('AUTDOR',1,'S','PERSONA','Autorizador a dar órdenes','','S','ACT','AUTORIZADO');



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(152,'BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(153,'BSCL.SocioNegocio.BD.Servicios::REL.ModificarRelacionPersona',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(156,'BSCL.Portafolio.BD.Servicios::CrearAutorizadoOrden',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(162,'BSCL.Portafolio.BD.Servicios::ModificarEstadoAutorizadoOrden',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(176,'BSCL.SocioNegocio.BD.Servicios::REL.ConsultarRelacionPersona',0,'P','');

-- ** LIBREACION  RELACIONADOS /  (Termino) **








--####################################################
--			JG.USO DE PLANTILLA EMPLEADO
--####################################################

ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" 
ADD ("UtilizaPlantillaEmpleado" NVARCHAR(1) DEFAULT 'N');

ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" 
ADD ("IdPlantillaEmpleado" NVARCHAR(40) DEFAULT '');

-------------------------------------------------------
--			FIN USO DE PLANTILLA EMPLEADO
-------------------------------------------------------



