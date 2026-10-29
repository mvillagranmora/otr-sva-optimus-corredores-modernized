SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.SocioNegocio.BD::PRO.Proveedor"; 
DROP TABLE "BSCL.SocioNegocio.BD::PRO.AreaProveedor";

drop sequence "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Proveedor";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PRO.Proveedor" (
 "IdProveedor" 				BIGINT  NOT NULL ,
 "IdPersona" 				BIGINT , 
 "IdEmpresa" 				BIGINT 		NOT NULL,
 "IdAreaProveedor"			BIGINT 		DEFAULT 0,
 "IdEstado" 				NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "FechaIngreso" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion"		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL , 
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
  "IdUsuarioCreador"   BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 					NVARCHAR(40),
 "FechaBloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 				NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 				NVARCHAR(40),
 "FechaDesbloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 			NVARCHAR(520) DEFAULT '' NOT NULL,
 PRIMARY KEY ("IdProveedor"))  ;

 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PRO.AreaProveedor" (
		 "IdAreaProveedor" 		BIGINT CS_FIXED  NOT NULL ,
		 "NombreArea"	 		NVARCHAR(100)    NOT NULL DEFAULT '',
		 "IdEstado" 			NVARCHAR(3)		 NOT NULL DEFAULT 'ACT',
		 "Borrado" 				NVARCHAR(1) DEFAULT 'N',
		 PRIMARY KEY ("IdAreaProveedor")
 );
 
 
/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
create sequence "BSCL.SocioNegocio.BD.Secuencias::Proveedor" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;

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
