SET SCHEMA BSCL; 

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::BAT.TipoPrograma" cascade;
drop table "BSCL.Plataforma.BD::BAT.Agenda" cascade;
drop table "BSCL.Plataforma.BD::BAT.ParametrosAgenda" cascade;
drop table "BSCL.Plataforma.BD::BAT.Programa" cascade;
drop table "BSCL.Plataforma.BD::BAT.ParametrosPrograma" cascade;

drop sequence "BSCL.Plataforma.BD.Secuencias::ParametrosAgenda" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::Programa" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::Agenda" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia" cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

/* ************************************

   TABLAS MAESTRAS

************************************ */

/*
ASIG_CRONO |  tarea para la asignacion cronologica  de ordenes |  ??   | ORDENES

*/
-- Define los tipos de tareas conocidos por el administrador de tarea
 CREATE ROW  TABLE "BSCL.Plataforma.BD::BAT.TipoPrograma" (
	 "IdTipoPrograma" 			NVARCHAR(40) 			NOT NULL ,
	 "Descripcion" 			NVARCHAR(80)		 	NOT NULL DEFAULT '', 
	 "MaximoIntentos" 		INTEGER 				DEFAULT 0 ,
	 "Modulo" 				NVARCHAR(40)			NOT NULL,
	 "NombreClasePrograma"  NVARCHAR(200) 		NOT NULL,
	 "TipoClasePrograma"	NVARCHAR(40)		NOT NULL,	
	 "EsPauta"				NVARCHAR(1)			NOT NULL DEFAULT 'N'
	 PRIMARY KEY ("IdTipoPrograma")
 );
	
 
/* ************************************

   TABLAS TRABAJO

************************************ */
--
CREATE ROW TABLE "BSCL.Plataforma.BD::BAT.Agenda" (
	 "IdAgenda" 			BIGINT				NOT NULL ,
	 "IdTipoPrograma" 	    	NVARCHAR(40)		NOT NULL ,
	 "EsDemonio"   			NVARCHAR(1)			NOT NULL DEFAULT 'N',  --S/N 
	 "DiaDeMes" 			NVARCHAR(2) 		NOT NULL ,  --01 AL 31 o ** 
	 "DiaDeSemana" 			NVARCHAR(1) 		NOT NULL ,  --1  AL 7  o *  1=Lunes
	 "Mes" 					NVARCHAR(2) 		NOT NULL ,  --01 AL 12 o **
	 "Hora" 				NVARCHAR(2) 		NOT NULL ,  --00 AL 23 o **
	 "Minuto" 				NVARCHAR(2) 		NOT NULL ,  --00 AL 59 o **
	 "SoloDiasHabiles" 		NVARCHAR(1) 		NOT NULL DEFAULT 'N',  --S/N
	 "SoloPrincipioMes" 	NVARCHAR(1) 		NOT NULL DEFAULT 'N',  --S/N
	 "SoloFinDeMes" 		NVARCHAR(1) 		NOT NULL DEFAULT 'N',  --S/N
	 "habilitado" 			NVARCHAR(1) 		NOT NULL DEFAULT 'S', --S/N
	 PRIMARY KEY ("IdAgenda")
 );
 
 CREATE COLUMN TABLE "BSCL.Plataforma.BD::BAT.LogPrograma" (
	 "FechaLog"		TIMESTAMP 		NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "IdTipoPrograma"	NVARCHAR(100)	NOT NULL DEFAULT '',
	 "IdPrograma"	BIGINT			NOT NULL DEFAULT 0,
	 "Mensaje"		NVARCHAR(5000) NOT NULL DEFAULT ''
 );





 CREATE ROW TABLE "BSCL.Plataforma.BD::BAT.ParametrosAgenda" (
	 "IdAgenda" 			BIGINT			NOT NULL ,
	 "Orden" 				INTEGER			NOT NULL , --
	 "NombreParametro" 		NVARCHAR(100) 	NOT NULL , --
	 "TipoDato" 			NVARCHAR(20)	NOT NULL , -- STRING, INT, DATE(yyyy-mm-dd), SECONDATE(yyyy-mm-dd hh24:hh:ss), DECIMAL
	 "Valor" 				NVARCHAR(200) 	NOT NULL  --
	 
 );


CREATE ROW TABLE "BSCL.Plataforma.BD::BAT.Programa" (
	 "IdPrograma" 			BIGINT			NOT NULL ,
	 "IdGrupoParametro" 	BIGINT			NULL, --ID de grupo de parametros utilizados para el programa
	 "IdAgenda" 			BIGINT			, --Existen Programas on demand que no tienen Agenda
	 "IdTipoPrograma" 			NVARCHAR(40) 	NOT NULL ,	  
	 "Intento" 				INTEGER 		DEFAULT 0 ,--Numero del intento
	 "IdUsuario" 			NVARCHAR(40) 	NOT NULL , --Id Usuario o Sistema que inicia el programa	 
	 "idEstado" 			NVARCHAR(3) 	NOT NULL , --PEN PENDIENTE / ASI ASIGNADA EJECUCION / TER TERMINADO / ERR ERROR
	 "Resultado" 			NVARCHAR(2000) 	NOT NULL DEFAULT '', --Mensaje de salida	
	 "fechaCreacion" 		SECONDDATE 		NOT NULL , --Fecha Creacion
	 "fechaEjecucion" 		SECONDDATE 		DEFAULT '0001-01-01 00:00:00', --Fecha Inicio Ejecucion
	 "fechaFin" 			SECONDDATE		DEFAULT '0001-01-01 00:00:00', --Fecha Termino o Error	
 	 "fechaSistema" 		DATE			DEFAULT '0001-01-01',   
 	 "IdProgramaReprocesado"BIGINT			NOT NULL DEFAULT 0,
	 PRIMARY KEY ("IdPrograma")
 );


 CREATE COLUMN TABLE "BSCL.Plataforma.BD::BAT.ParametrosPrograma" (
 	 "IdSecuencia"			BIGINT			NOT NULL ,
 	 "IdGrupoParametro" 	BIGINT			NOT NULL ,
	 "IdPrograma" 			BIGINT			NOT NULL ,
	 "Orden" 				INTEGER			NOT NULL , --
	 "NombreParametro" 		NVARCHAR(100) 	NOT NULL ,
	 "Valor" 				NVARCHAR(3000) 	NOT NULL , --
	 PRIMARY KEY ("IdSecuencia")
 );
 
 
 


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */
 
alter table "BSCL.Plataforma.BD::BAT.Programa"
add constraint "FK_Programa_TipoPrograma" foreign key ("IdTipoPrograma")
references "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma") on delete restrict on update restrict;
 

alter table "BSCL.Plataforma.BD::BAT.Agenda"
add constraint "FK_Agenda_TipoTarea" foreign key ("IdTipoPrograma")
references "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::BAT.ParametrosPrograma"
add constraint "FK_ParametrosPrograma_Programa" foreign key ("IdPrograma")
references "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma") on delete restrict on update restrict;
 

alter table "BSCL.Plataforma.BD::BAT.ParametrosAgenda"
add constraint "FK_ParametrosAgenda_Agenda" foreign key ("IdAgenda")
references "BSCL.Plataforma.BD::BAT.Agenda" ("IdAgenda") on delete restrict on update restrict;


ALTER TABLE "BSCL.Plataforma.BD::BAT.Agenda" 
ADD ("SiguienteFechaDeEjecucion" SECONDDATE);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Agenda" 
ADD ("UltimaFechaDeEjecucion" SECONDDATE);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ALTER ("Resultado" NVARCHAR(2000));
  
/*==============================================================*/
/* secuence:                                                  */
/*==============================================================*/

/* Table: */


create sequence "BSCL.Plataforma.BD.Secuencias::Agenda" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::ParametrosAgenda" increment by 1 start with 50000 minvalue 50000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::Programa" increment by 1 start with 60000 minvalue 60000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma" increment by 1 start with 70000 minvalue 70000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia" increment by 1 start with 90000 minvalue 70000 maxvalue 4611686018427387903 no cycle;

commit
