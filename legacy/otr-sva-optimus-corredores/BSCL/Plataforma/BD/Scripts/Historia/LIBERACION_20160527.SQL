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
--



--################################################
--  CFLORES - Limites
--################################################

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarClasificacionRiesgoInstrumento"; 


INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPCLASIFRIESGO',1,'LÍMITE CUENTA PROPIA CLAS. RIESGO','N','ADV','RV','OPERACION','ACT','LICCTP',1); 

---EVALUACIONES
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LICCTP',1,'PLA','AUT_LIM_LICCTP','Evaluación Limite Clas. Riesgo Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LICCTP','LI','PLA','EVALUACION LIMITE CLAS. RIESGO CUENTA PROPIA','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LICCTP','S');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIRCTP',1,'PLA','AUT_LIM_LIRCTP','Evaluación Limite Margen de Tasa Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIRCTP','LI','PLA','EVALUACION LIMITE MARGE DE TASA CUENTA PROPIA','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIRCTP','S');

---MANTENCION
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NRLCTP',1,'PLA','AUT_NRLCTP_NUE','Agregar Rango Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NRLCTP','LI','PLA','NUEVO RANGO LIMITE CUENTA PROPIA','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NRLCTP','S');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MRLCTP',1,'PLA','AUT_MRLCTP_MOD','Modificar Rango Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MRLCTP','LI','PLA','MODIFICACION RANGO LIMITE CUENTA PROPIA','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MRLCTP','S');

SET SCHEMA "BSCL"; --Agregar campoS
alter table "BSCL.Plataforma.BD::LIM.RangosMontoPlazo" add ("IdEstado"       			NVARCHAR(3) not null Default '', 
														    "FechaIngreso"       		SECONDDATE          default '0001-01-01 00:00:00' not null,
														    "FechaCreacion"     		SECONDDATE          default '0001-01-01 00:00:00' not null,
														    "FechaModificacion"  		SECONDDATE          default '0001-01-01 00:00:00' not null,
														    "IdEstadoAutorizacion" 		NVARCHAR(3)         default 'PEN' not null,
														    "Borrado" 					NVARCHAR(1) DEFAULT 'N',
														    "IdUsuarioCreador"   		BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; --crear secuencia
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdRangosMontoPlazo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	       
       
--################################################
--  CFLORES - FIN Limites
--################################################


