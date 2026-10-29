SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"; 

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte" (
 "IdContraparte" 			BIGINT  NOT NULL ,
 "IdPersona" 				BIGINT ,
 "IdEmpresa" 				BIGINT ,
 "CodigoInscripcion"		NVARCHAR(40),
 "Emisor" 					NVARCHAR(1) DEFAULT 'N',
 "Depositos" 				NVARCHAR(1) DEFAULT 'N',
 "Bolsa" 					NVARCHAR(1) DEFAULT 'N',
 "Corredora" 				NVARCHAR(1) DEFAULT 'N',
 "CamaraDeCompensacion" 	NVARCHAR(1) DEFAULT 'N',
 "Banco" 					NVARCHAR(1) DEFAULT 'N', 
 --"IdBanco"  				BIGINT      DEFAULT 0, 
 "Boveda"					NVARCHAR(1) DEFAULT 'N', 
 --"IdBoveda"  				BIGINT      DEFAULT 0, 				
 "IdEstado" 				NVARCHAR(3) DEFAULT 'ACT', 
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 "FechaIngreso" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdUsuarioCreador"			BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 			NVARCHAR(40),
 "FechaBloqueo" 			SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 		NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 		NVARCHAR(40),
 "FechaDesbloqueo" 			SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 	NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdOrigen"                 BIGINT DEFAULT 0 NOT NULL,
 "IdCarga"                  BIGINT DEFAULT 0 NO NULL,
 PRIMARY KEY ("IdContraparte"))  ;


/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
drop sequence "BSCL.SocioNegocio.BD.Secuencias::Contraparte";

create sequence "BSCL.SocioNegocio.BD.Secuencias::Contraparte" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;

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
