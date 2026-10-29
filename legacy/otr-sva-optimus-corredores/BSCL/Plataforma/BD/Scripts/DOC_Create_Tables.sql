SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::DOC.TipoDocumento" cascade;
drop table "BSCL.Plataforma.BD::DOC.Anexo" cascade;
drop table "BSCL.Plataforma.BD::DOC.Documento" cascade;

drop sequence "BSCL.Plataforma.BD.Secuencias::Documento";
drop sequence "BSCL.Plataforma.BD.Secuencias::Anexo";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.Plataforma.BD::DOC.TipoDocumento" (
   "IdTipoDocumento"  		NVARCHAR(40)               	not null,
   "IdEmpresa"    			BIGINT                		not null,
   "NombreDocumento"  		NVARCHAR(100)               not null,
   "NumeroFirmantesEmpresa" INT             default '1' not null,
   "NumeroFirmantesCliente" INT             default '1' not null,
   "Clasificacion" 			NVARCHAR(100)    			not null,
   "ValidaFechaVigencia"    NVARCHAR(1)                 		,
   primary key ("IdTipoDocumento","IdEmpresa")
);

create column table "BSCL.Plataforma.BD::DOC.Anexo" (
   	"IdAnexo"  					BIGINT  							not null,
   	"IdDocumento"            	BIGINT  							not null,
   	"IdEstado"					NVARCHAR(3)							NOT NULL,
   	"NumeroFirmantesEmpresa"    INT  			DEFAULT '0' 		not null,
   	"NumeroFirmantesCliente"    INT				DEFAULT '0' 		not null,
   	"NombreFirmanteEmpresa1"    NVARCHAR(100)      			,
    "NombreFirmanteEmpresa2"    NVARCHAR(100)       		,
    "NombreFirmanteEmpresa3"    NVARCHAR(100)       		,
    "NombreFirmanteCliente1"    NVARCHAR(100)       		,
    "NombreFirmanteCliente2"    NVARCHAR(100)         		,
    "NombreFirmanteCliente3"    NVARCHAR(100)         		,
   	"FirmaEmpresa1" 			NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaEmpresa2"     		NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaEmpresa3"     		NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaCliente1"           	NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaCliente2"           	NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaCliente3"          	NVARCHAR(1)		DEFAULT 'N'			not null,
   	"IdUsuarioModifica"      	BIGINT         								,
	"FechaIngreso"           	SECONDDATE          				not null,
	"FechaModificacion"         SECONDDATE   default '0001-01-01'	not null,
	"FechaVigencia"           	SECONDDATE          		,
	"DocumentoFisicoReferencia"	NVARCHAR(100)				,		
   primary key ("IdAnexo")
);

create column table "BSCL.Plataforma.BD::DOC.Documento" (
   "IdDocumento"  		BIGINT               not null,
   "IdTipoDocumento"    NVARCHAR(40)         not null,
   "IdEmpresa"          BIGINT               default '0' not null,
   "IdCliente"          BIGINT               default '0' not null,
   "IdUltimoAnexo" 		BIGINT               default '0' not null,
   primary key ("IdDocumento")
);



/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

alter table "BSCL.Plataforma.BD::DOC.Documento"
   add constraint "FK_TipoDocumento" foreign key ("IdTipoDocumento", "IdEmpresa")
      references "BSCL.Plataforma.BD::DOC.TipoDocumento" ("IdTipoDocumento", "IdEmpresa") on delete restrict on update restrict;

/* Table: */

alter table "BSCL.Plataforma.BD::DOC.Documento"
   add constraint "FK_Cliente" foreign key ("IdCliente")
      references "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente") on delete restrict on update restrict;

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Documento"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Anexo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

/* Table: */


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
