SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Plataforma.BD::REQ.CondicionOperativa" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa"	CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.TipoEtapa" 					CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.TipoComportamiento" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.RequisitoOperativo" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo" 	CASCADE;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


/*****************************
PLANTILLA CONDICIONES OPERATIVAS 
*****************************/

create column table "BSCL.Plataforma.BD::REQ.CondicionOperativa" (
	"IdCondicionOperativa"			NVARCHAR(40)	NOT NULL,
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"RequiereAtributo1"				NVARCHAR(1)		NOT NULL DEFAULT 'N',
	"RequiereAtributo2"				NVARCHAR(1)		NOT NULL DEFAULT 'N',	
	"IdTipoComportamientoDefecto"	NVARCHAR(40)	NOT NULL DEFAULT '', -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdCondicionOperativa")
);

CREATE COLUMN TABLE  "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" (
 	"IdEstadoCondicionOperativa" 	BIGINT  	 	NOT NULL,
 	"IdEstado" 						NVARCHAR(3) 	NOT NULL,
 PRIMARY KEY ("IdEstadoCondicionOperativa")
);
 

create column table "BSCL.Plataforma.BD::REQ.TipoEtapa" (
	"IdTipoEtapa"					NVARCHAR(40)	NOT NULL,
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdTipoEtapa")
);

create column table "BSCL.Plataforma.BD::REQ.TipoComportamiento" (
	"IdTipoComportamiento"			NVARCHAR(40)	NOT NULL, -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdTipoComportamiento")
);

create column table "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" (
	"IdTipoOperacionUsuario"			NVARCHAR(40)	NOT NULL,
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdTipoOperacionUsuario")
);

create column table "BSCL.Plataforma.BD::REQ.RequisitoOperativo" (
	"IdRequisitoOperativo"			BIGINT			NOT NULL,
	"IdModeloRequisitoOperativo"	NVARCHAR(40)	NOT NULL DEFAULT '', -- POR EL MOMENTO SOLO UNO, EL BASE DE LA CORREDORA.
	"IdCondicionOperativa"			NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoEtapa"					NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoComportamiento"			NVARCHAR(40)	NOT NULL DEFAULT '',
	--"IdTipoOperacionUsuario"		NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoCanalIngreso"			VARCHAR(3)      NOT NULL DEFAULT '0', -- 0 todos.
	"IdMoneda"						NVARCHAR(3)    	NOT NULL DEFAULT '0', -- 0 todos.
	"ValorAtributo1"				NVARCHAR(255)	NOT NULL DEFAULT '',
	"ValorAtributo2"				NVARCHAR(255)	NOT NULL DEFAULT '',	
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdRequisitoOperativo")
);

create column table "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo" (
	"IdModeloRequisitoOperativo"	NVARCHAR(40)	NOT NULL DEFAULT '', -- POR EL MOMENTO SOLO UNO.
	"Descripcion"					NVARCHAR(100)	NOT NULL DEFAULT '',	
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdModeloRequisitoOperativo")
);

/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

/*==============================================================*/
/* Tipos:                                                       */
/*==============================================================*/


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
