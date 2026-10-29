SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.SocioNegocio.BD::EMP.Empleado"; 
DROP TABLE "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"; 
--DROP TABLE "BSCL.SocioNegocio.BD::EMP.EstadoEmpleado"; 

drop sequence "BSCL.SocioNegocio.BD.Secuencias::Empleado";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" (
 "IdEmpleado" BIGINT  NOT NULL ,
 "IdPersona" BIGINT ,
 "CodigoEmpleado" NVARCHAR(40),
 "IdEstado" NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdEmpresa" BIGINT ,
 "CodigoTipoCargoEmpleado"  NVARCHAR(6)    default '' ,
 "DetalleCargo"						NVARCHAR(40) DEFAULT ''  NOT NULL,
 "IdEmpleadoSupervisor" BIGINT ,
 "IdSucursal" BIGINT ,
 "UtilizaPlantillaEmpleado" NVARCHAR(1) DEFAULT 'N',
 "IdPlantillaEmpleado" NVARCHAR(40) DEFAULT '',
 "FechaInicioCargo" DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaTerminoCargo" DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaIngreso" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL , 
 "IdCarga" BIGINT DEFAULT 0 NOT NULL,
 "CodigoAgenteMigracion" NVARCHAR(40) DEFAULT '' NOT NULL,
 "CodigoSupervisorAgenteMigracion" NVARCHAR(40) DEFAULT '' NOT NULL,
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"   BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 					NVARCHAR(40),
 "FechaBloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 				NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 				NVARCHAR(40),
 "FechaDesbloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 			NVARCHAR(520) DEFAULT '' NOT NULL,
 "RestringeCarteraCliente"			NVARCHAR(1) DEFAULT 'S',
 PRIMARY KEY ("IdEmpleado"))  ;


--Agregar CodigoOperador para caso Internos.
-- Asociado al cargo. => en tipo cargo agregar campo UsaCodigoOperador

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" (
 "IdEmpresa" BIGINT ,
 "IdTipoCargoEmpleado" BIGINT  NOT NULL ,
 "CodigoTipoCargoEmpleado" NVARCHAR(6),
 "NombreTipoCargoEmpleado" NVARCHAR(520), 
 "Borrado" 				   NVARCHAR(1) DEFAULT 'N',
 "UtilizaDetalle"		   NVARCHAR(1) DEFAULT 'N'  NOT NULL,
 PRIMARY KEY ("IdTipoCargoEmpleado"))  ;


CREATE COLUMN TABLE "BSCL.Plataforma.BD::Empleado.SucursalAtencion" (
     "IdSucursalAtencion"   BIGINT,
     "IdEmpresa"            BIGINT,
     "IdSucursal"           BIGINT,
     "IdEmpleado"           BIGINT,
     "Borrado"              NVARCHAR(1),
     PRIMARY KEY ("IdSucursalAtencion")
);

/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
create sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Empleado" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
