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
-- LIBERACION SEMANA: 24-03-2016 (Se adelanta un día)
--


--################################################
--    JG - Modelo Pais y uso division pais
--################################################

ALTER TABLE "BSCL.Plataforma.BD::REG.Pais" 
ADD ("NombreDivisionPais" NVARCHAR(100) not null default '');


UPDATE "BSCL"."BSCL.Plataforma.BD::REG.ClasificacionDivisionPais"
SET "TipoClasificacionDivisionPais" = 'Región' WHERE "TipoClasificacionDivisionPais" = 'REGION';


UPDATE "BSCL.Plataforma.BD::REG.Pais"  SET "NombreDivisionPais" = 'Comuna' where "IdPais" = 44;

update "BSCL.Plataforma.BD::REG.Pais" SET "NombreDivisionPais" = 'Distrito' where "IdPais" = 166;

--------------------------------------------------
--      FIN Modelo Pais y uso division pais
--------------------------------------------------

--################################################
--    EE - Modificación Job Manager
--################################################
alter table "BSCL.Plataforma.BD::BAT.TipoPrograma" 
add ("Nodo" NVARCHAR(1) NOT NULL DEFAULT '');
alter table "BSCL.Plataforma.BD::BAT.Programa" 
add ("Nodo" NVARCHAR(1) NOT NULL DEFAULT '');
alter table "BSCL.Plataforma.BD::BAT.Programa" 
add ("Bloqueo" NVARCHAR(1) NOT NULL DEFAULT 'N');

UPDATE "BSCL.Plataforma.BD::BAT.TipoPrograma" SET "Nodo"='A' WHERE "IdTipoPrograma"='VALORIZADOR';

INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'ConcurrenciaValorizador', 'Concurrencia de Valorizador', '4');
commit;

--########################################################################
--    NR - Incorporación de Tipo al medio de pago, e inserción de datos
--########################################################################
RENAME COLUMN "BSCL.Plataforma.BD::PAG.MedioDePago"."Codigo" TO "TipoMedioDePago";

UPDATE "BSCL.Plataforma.BD::PAG.PreferenciaPago" SET "IdMedioDePago" = 'TDP' WHERE "IdMedioDePago" = 'TRA';
DELETE FROM "BSCL.Plataforma.BD::PAG.MedioDePago" WHERE "IdMedioDePago" = 'TRA';

INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TDP','TRA','Transferencia DVP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TLR','TRA','Transferencia LBTR','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TSW','TRA','Transferencia Swift','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('BDE','BDE','Boleta de Depósito','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('EBA','EBA','Extra Banco','ACT'); 


DELETE FROM "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" WHERE "IdMedioDePagoYModalidadDePago" = 7;
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(7,'TDP','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(8,'TLR','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(9,'TSW','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(10,'BDE','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(11,'EBA','MODDEP','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(256,'BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGenerarPartidaIngresos',0,'P','');
 

-- Borra la modalidad "Depósito" para "Efectivo"
DELETE 
FROM "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago"
WHERE "IdMedioDePago" = 'EFE' 
AND "IdModalidadDePago" = 'MODDEP'


Delete From "BSCL"."BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" where "IdMedioDePago" = 'CHE' and "IdModalidadDePago" = 'MODDEP';
Delete  From "BSCL"."BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" where "IdMedioDePago" = 'VVI' and "IdModalidadDePago" = 'MODDEP';



--########################################################################
--    NR - Incorporación de nuevos estados 
--########################################################################

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado","Nombre") 
VALUES ('DEP','Depositado');

--########################################################################
--    NR - Incorporación de nuevos conceptos 
--########################################################################

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MULCPT','ST','TES','MULTIPLE CONCEPTOS','VIG'); --Concepto de sistema
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('PARING','ST','TES','PARTIDA INGRESO','VIG'); --Concepto de sistema
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('PAREGR','ST','TES','PARTIDA EGRESO','VIG'); --Concepto de sistema
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EFTIVO','ST','TES','EFECTIVO','VIG'); --Concepto de sistema
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MIBNCO','ST','TES','MISMO BANCO','VIG'); --Concepto de sistema
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('OTBNCO','ST','TES','OTRO BANCO','VIG'); --Concepto de sistema


--########################################################################
--    NR- new servicios
--########################################################################
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(255,'BSCL.Tesoreria.BD.Servicios::CrearDeposito',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(259,'BSCL.Tesoreria.BD.Servicios::CertificarDepositos',0,'P','');

--########################################################################
--    MMACHMAR - Servicio asignación desde carga archivo
--########################################################################

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(245,'BSCL.Orden.BD.Servicios::CrearAsignacionMM',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(246,'BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoAsignacion',0,'P','');


--########################################################################
--    CFLORES - Perfil comercial Campos de Auditoria y Autorizacion
--########################################################################



SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.Plataforma.BD::COM.DetallePerfilCobros" add ( "FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															 	"FechaModificacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															 	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
															 	"IdUsuarioCreador"  				BIGINT  DEFAULT 0);

													  
SET SCHEMA "BSCL"; --Agregar campo													  

alter table "BSCL.Plataforma.BD::COM.PerfilCobros" add ("FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
														"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
														"IdUsuarioCreador"  				BIGINT  DEFAULT 0);


SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.Plataforma.BD::COM.DetallePerfilProductos" add (  "FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															 		"FechaModificacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															 		"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
															 		"IdUsuarioCreador"  				BIGINT  DEFAULT 0);
															 	


SET SCHEMA "BSCL"; --Agregar campo													  

alter table "BSCL.Plataforma.BD::COM.PerfilProductos" add ( "FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
															"IdUsuarioCreador"  				BIGINT  DEFAULT 0);


SET SCHEMA "BSCL";
alter table "BSCL.Plataforma.BD::COM.GrupoActivos"	  add ( "FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
															"IdUsuarioCreador"  				BIGINT  DEFAULT 0);




alter table "BSCL.Plataforma.BD::COM.DetalleGrupoActivos" add ( "FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															 	"FechaModificacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
															 	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
															 	"IdUsuarioCreador"  				BIGINT  DEFAULT 0);
			

----PERFIL COBRO
SET SCHEMA "BSCL";													  
UPDATE "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento" SET "IdModulo"='PLA' WHERE "IdTipoEvento"='MCPCOB';													  
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Concepto" SET "IdModulo"='PLA' WHERE "IdConcepto"='MCPCOB';

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MDPCOB',1,'PLA','AUT_DPCOB_MOD','Modificación Detalle Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');	
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MDPCOB','PC','PLA','MODIFICACION DETALLE PERFIL COBRO','VIG'); 

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NDPCOB',1,'PLA','AUT_DPCOB_NUE','Nuevo Detalle Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCPCOB',1,'PLA','AUT_CPCOB_NUE','Nuevo Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');	


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDPCOB','PC','PLA','NUEVO DETALLE PERFIL COBRO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPCOB','PC','PLA','NUEVO PERFIL COBRO','VIG'); 


----PERFIL PRODUCTO
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NDPPRO',1,'PLA','AUT_DPPRO_NUE','Nuevo Detalle Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCPPRO',1,'PLA','AUT_CPPRO_NUE','Nuevo Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MDPPRO',1,'PLA','AUT_DPPRO_MOD','Modificación Detalle Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCPPRO',1,'PLA','AUT_CPPRO_MOD','Modificación Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDPPRO','PC','PLA','NUEVO DETALLE PERFIL PRODUCTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPPRO','PC','PLA','NUEVO PERFIL PRODUCTO','VIG'); 


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MDPPRO','PC','PLA','MODIFICACION DETALLE PERFIL PRODUCTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCPPRO','PC','PLA','MODIFICACION PERFIL PRODUCTO','VIG'); 

----GRUPO ACTIVOS
SET SCHEMA "BSCL";
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NDGACT',1,'PLA','AUT_DGACT_NUE','Nuevo Detalle Grupo Activos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCGACT',1,'PLA','AUT_CGACT_NUE','Nuevo Grupo Activos',1,'S','' ,'' ,0,0,'VIG');	

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MDGACT',1,'PLA','AUT_DGACT_MOD','Modificación Detalle Grupo Activos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCGACT',1,'PLA','AUT_CGACT_MOD','Modificación Grupo Activos',1,'S','' ,'' ,0,0,'VIG');	


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDGACT','PC','PLA','NUEVO DETALLE GRUPO ACTIVO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCGACT','PC','PLA','NUEVO GRUPO ACTIVO','VIG'); 


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MDGACT','PC','PLA','MODIFICACION DETALLE GRUPO ACTIVO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCGACT','PC','PLA','MODIFICACION GRUPO ACTIVO','VIG'); 

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" (
   "IdConcepto"				NVARCHAR(6)     not null,
   "RequiereAutorizacion"	NVARCHAR(1)		not null,
   PRIMARY KEY("IdConcepto")
);


insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MCPCOB','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MDPCOB','N');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NCPCOB','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NDPCOB','N');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NCPPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NDPPRO','N');
												  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MCPPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MDPPRO','N');


insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NCGACT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NDGACT','N');
												  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MCGACT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MDGACT','N');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUGDIE','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MOGDIE','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODCLI','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODCNT','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAECNT','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODEMP','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEEMP','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODCTB','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUECTB','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEPER','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODPER','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDIE','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDIE','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDIF','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDIF','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDOC','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDOC','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDID','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUENFA','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAENFA','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODTJB','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUETJB','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODTEL','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUETEL','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEREL','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODREL','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEPRO','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODPRO','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NAUTOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEPOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEPOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODPOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDUP','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDUP','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENROLC','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENRCNT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENROLE','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENRPER','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENRPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAECLI','N');

INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('AUTORI','Autorizaciones','ACT');

INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('PORTAF','AUTORI','PORTAF','Requiere Autorización','"BSCL.Plataforma.BD::PLA.RequiereAutorizacion"','ACT');

INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(51,'PORTAF','UI_CONCEPTO','S','N','N',1,'bcs-input-texto','','IdConcepto','NVARCHAR','6','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(52,'PORTAF','UI_REQUIERE_AUTORIZACION','S','N','S',2,'bcs-input-texto','','RequiereAutorizacion','NVARCHAR','1','N','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(53,'PORTAF','UI_REQUIERE_AUTORIZACION','S','N','N',3,'bcs-input-texto','','NombreModulo','NVARCHAR','1','N','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(54,'PORTAF','UI_REQUIERE_AUTORIZACION','S','N','N',4,'bcs-input-texto','','NombreConcepto','NVARCHAR','1','N','S','');

INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(29,'PORTAF','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(30,'PORTAF','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(31,'PORTAF','UI_ELIMINAR','S',3,'ELIMINAR',3);												  
		
--########################################################################
--   JG -  MODELO FACTURACION
--########################################################################											  
													  												  
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(247,'BSCL.Plataforma.BD.Servicios::COM.CrearModeloFacturacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(248,'BSCL.Plataforma.BD.Servicios::COM.ModificarModeloFacturacion',0,'P','');
													  
ALTER TABLE "BSCL.Plataforma.BD::PAG.PreferenciaPago"
ADD("UsaTarjetaBancaria" NVARCHAR(1) not null default 'N');

--########################################################################
--   KH -  MODELO TRIBUTARIO
--########################################################################											  
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(249,'BSCL.Normativa.BD.Servicios::DJ1885.GrabarDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(250,'BSCL.Normativa.BD.Servicios::DJ1885.EliminarDetalles',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(251,'BSCL.Normativa.BD.Servicios::DJ1885.ObtenerTotales',0,'P','');


--################################################
--    KH - Valorizacion RF
--################################################

CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::INS.TareaValorizacion" (
	 --- DATOS TAREA
	 "IdTarea" 						BIGINT 			not null,
	 "GrupoValorizacion"			BIGINT			not null,	 
	 "FechaSolicitud" 				NVARCHAR(40) 	default '' 	not null,
 	 "IdEstado" 					NVARCHAR(3) 	default '' 	not null,
	 
	 -- DATOS INTRUMENTO/CARTERA
	 "IdTipoInstrumento"			NVARCHAR(3) 	default '' 	not null,
	 "IdInstrumento" 				BIGINT 			not null,
	 "FolioCartera"       			BIGINT			not null,	 	 
	 "Nemotecnico" 					NVARCHAR(40)	default '' 	not null,
	 "Nominales"					DECIMAL(26,8) 	default '0' not null,
	 "IdMoneda"						NVARCHAR(10)	default '' 	not null,
	 
	 -- DATOS VALORIZADOR
	 "TipoValorizacion"				NVARCHAR(1) 	default 'N'	not null, ---- 
	 "OpcionValorizacion" 			NVARCHAR(100) 	default '' 	not null,
	 
	 -- ENTRADAS VALORIZADOR 
	 "FechaHoy" 					DATE            default '0001-01-01' not null,
     "FechaManana" 					DATE            default '0001-01-01' not null,     	 
	 "TasaCompra" 					DECIMAL(26,8)   default '0' not null, 
	 "TasaMercado" 					DECIMAL(26,8)   default '0' not null, 	 
	 "TasaInteresFiscal"			DECIMAL(26,8)   default '0' not null, 
	 	 
	 "PrecioACompraHoy" 			DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonLocalACompraHoy" 	DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonInsACompraHoy" 	DECIMAL(26,8)   default '0' not null, 
	 "DurationACompraHoy"			DECIMAL(26,8)   default '0' not null, 
	 "ConvexidadACompraHoy"			DECIMAL(26,8)   default '0' not null, 
	 
	 "PrecioACompraMan" 			DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonLocalACompraMan" 	DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonInsACompraMan" 	DECIMAL(26,8)   default '0' not null,
	 "DurationACompraMan"			DECIMAL(26,8)   default '0' not null, 
	 "ConvexidadACompraMan"			DECIMAL(26,8)   default '0' not null, 
	 
	 "PrecioAMercadoHoy" 			DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonLocalAMdoHoy" 		DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonInsAMdoHoy" 		DECIMAL(26,8)   default '0' not null,
	 "DurationAMercadoHoy"			DECIMAL(26,8)   default '0' not null, 
	 "ConvexidadAMercadoHoy"		DECIMAL(26,8)   default '0' not null, 	 

	 "PrecioAMercadoMan" 			DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonLocalAMdoMan" 		DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonInsAMdoMan" 		DECIMAL(26,8)   default '0' not null,
	 "DurationAMercadoMan"			DECIMAL(26,8)   default '0' not null, 
	 "ConvexidadAMercadoMan"		DECIMAL(26,8)   default '0' not null, 	 

	 "PrecioATIFHoy" 				DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonLocalATIFHoy" 		DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonInsATIFHoy" 		DECIMAL(26,8)   default '0' not null,
	 "DurationATIFHoy"				DECIMAL(26,8)   default '0' not null, 
	 "ConvexidadATIFHoy"			DECIMAL(26,8)   default '0' not null, 	 

	 "PrecioATIFMan" 				DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonLocalATIFMan" 		DECIMAL(26,8)   default '0' not null, 
	 "MontoValMonInsATIFMan" 		DECIMAL(26,8)   default '0' not null,
	 "DurationATIFMan"				DECIMAL(26,8)   default '0' not null, 
	 "ConvexidadATIFMan"			DECIMAL(26,8)   default '0' not null, 	 
 
	 -- SALIDAS	 
	 PRIMARY KEY ("IdTarea")
);


--################################################
--    KH - PRECIOS
--################################################

ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ADD("VariacionDiaria" DECIMAL(26,8)  default 0 not null);
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ADD("VolumenTransado" DECIMAL(26,8)  default 0 not null);
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ADD("MontoTransado"   DECIMAL(26,8)  default 0 not null);


--################################################
--    CE - LOG
--################################################
alter table "BSCL.Plataforma.BD::PLA.Log"
       alter ("FechaLog" DATE);

alter table "BSCL.Plataforma.BD::PLA.Log"
       add ("FechaRegistro" TIMESTAMP);
