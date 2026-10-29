SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::PAG.MedioDePago" 	 cascade;
DROP TABLE "BSCL.Plataforma.BD::PAG.ModalidadDePago" cascade;
DROP TABLE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago"	cascade;

DROP TABLE "BSCL.Plataforma.BD::PAG.PreferenciaPago"	cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PAG.MedioDePago" (
	 "IdMedioDePago" 			NVARCHAR(3)   	NOT NULL ,
	 "TipoMedioDePago" 			NVARCHAR(3)   	NOT NULL DEFAULT '',
	 "Nombre" 		 			NVARCHAR(100) 	NOT NULL DEFAULT '',
	 "NombreTipoMedioDePago"	NVARCHAR(100) 	NOT NULL DEFAULT '',
	 "UsaCuentaBancaria"		NVARCHAR(1)		NOT NULL DEFAULT 'N',
	 "UsaTarjetaBancaria"		NVARCHAR(1)		NOT NULL DEFAULT 'N',
	 "IdEstado" 	 			NVARCHAR(3)   	NOT NULL DEFAULT '',
	 PRIMARY KEY ("IdMedioDePago")
 );
 
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PAG.ModalidadDePago" (
	"IdModalidadDePago" NVARCHAR(6)   NOT NULL ,
	"Nombre" 		 	NVARCHAR(100) NOT NULL DEFAULT '',
	"IdEstado" 	 		NVARCHAR(3)   NOT NULL DEFAULT '',
	PRIMARY KEY ("IdModalidadDePago")
 );
 
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" (
	"IdMedioDePagoYModalidadDePago"	BIGINT CS_FIXED NOT NULL ,
	"IdMedioDePago" 				NVARCHAR(3)   	NOT NULL ,
	"IdModalidadDePago" 			NVARCHAR(6)  	NOT NULL ,
	"IdEstado" 	 					NVARCHAR(3)   	NOT NULL DEFAULT '',
	PRIMARY KEY ("IdMedioDePagoYModalidadDePago")
 ); 
 
 
 
 create column table "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago"			NVARCHAR(40)	not null, 
	"IdTipoOperacion" 			NVARCHAR(6) 	not null default '',
	"IdMedioDePago"				NVARCHAR(3)		not null default '',
	"IdMoneda" 					NVARCHAR(3)		not null default '',
	"UsaPersonaRelacionada"		NVARCHAR(1)		not null default 'N',
   	"FechaCreacion" 			SECONDDATE  	not null default '0001-01-01 00:00:00',
	"FechaModificacion" 		SECONDDATE 		not null default '0001-01-01 00:00:00',
	"IdUsuarioCreador"  		BIGINT  		not null default 0,
	"IdEstado"					NVARCHAR(3)   	not null default 'ACT', -- 'ACT', 'INA'
	"IdEmpresa"					BIGINT         	not null default 0,
	primary key ("IdPreferenciaPago")
);

 
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

	


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
	
	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit


