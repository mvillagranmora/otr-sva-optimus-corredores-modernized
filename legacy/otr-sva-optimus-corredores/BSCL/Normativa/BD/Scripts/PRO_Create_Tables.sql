SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente"	 CASCADE;
drop table "BSCL.Normativa.BD::PRO.Proceso" 					 CASCADE;



/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
 CREATE COLUMN TABLE 
 "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" (
   "IdEstadistica"					BIGINT			NOT NULL ,
   "IdEmpresa" 					    BIGINT CS_FIXED NOT NULL ,
  -- "IdModulo"						NVARCHAR(3)		NOT NULL ,
   "IdCliente"					    BIGINT			NOT NULL ,
   --"IdPortafolio"					BIGINT			NOT NULL ,
   "MontoTotalDias"        			DECIMAL(26,8) 	NOT NULL DEFAULT '0',
   "CantidadDias"        			BIGINT 			NOT NULL DEFAULT '0',
   "MontoPromedioDiario"   			DECIMAL(26,8) 	NOT NULL DEFAULT '0',
   "MontoMaximo"		   			DECIMAL(26,8) 	NOT NULL DEFAULT '0',
   "IdMoneda"                		NVARCHAR(3)		NOT NULL DEFAULT '',
   "IdTipoInstrumento"       		NVARCHAR(3)		NOT NULL DEFAULT '',
   "IdClasificacionTipoInstrumento" NVARCHAR(3)		NOT NULL DEFAULT '',
  -- "FechaOperacion"					NVARCHAR(10)	NOT NULL DEFAULT '',--'YYYY-MM-DD'
	---Auditoria
   "FechaIngreso"       			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     				SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaModificacion" 				SECONDDATE 		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   PRIMARY KEY ("IdEmpresa","IdCliente","IdTipoInstrumento","IdClasificacionTipoInstrumento")
);

  
 CREATE COLUMN TABLE 
 "BSCL.Normativa.BD::PRO.Proceso" (
   "IdProceso"						BIGINT			NOT NULL ,
   "IdEmpresa" 					    BIGINT 			NOT NULL DEFAULT 0,
   "IdModulo"						NVARCHAR(3)		NOT NULL DEFAULT '',
   "IdNormativa"              		NVARCHAR(15)    NOT NULL DEFAULT '',
   "IdTipoDocumento" 				NVARCHAR(10) 	NOT NULL DEFAULT '',
   "IdTipoTarea"  	              	NVARCHAR(40)	NOT NULL DEFAULT '', --Tarea de inscripcion en BAT
	--Procedencia
	"IdSolicitudCircular"	   		BIGINT			NOT NULL	DEFAULT 0,
	"IdProcesoBatch"			  	BIGINT 			NOT NULL	DEFAULT 0,
	---Auditoria
   "IdEstado" 						NVARCHAR(3) 	DEFAULT 'TER' 				  NOT NULL,
   "Descripcion"					NVARCHAR(200)   NULL,
   "FechaIngreso"       			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     				SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   PRIMARY KEY ("IdProceso")
);
 
 
 
 
 
									    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/

DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::PRO.IdEstadistica"	CASCADE;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::PRO.IdEstadistica"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::PRO.IdProceso"	CASCADE;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::PRO.IdProceso"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit





