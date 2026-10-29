SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

drop table "BSCL.Plataforma.BD::TRA.ConfiguracionOperador"				cascade;
drop table "BSCL.Plataforma.BD::TRA.HomologacionOperador"				cascade;
drop table "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador"		cascade;
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

alter table "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" alter ()

create column table "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" ( 
	"CodigoOperador"			NVARCHAR(40)		not null,
	"UtilizaPortafolio"			NVARCHAR(1)			not null default 'N',
	--"IdSocioNegocio"			BIGINT				not null default 0,
	"IdPortafolio"				BIGINT				not null default 0,
	"ReservaTransaccion"		NVARCHAR(1)			not null default 'N',
	"IdEstado"					NVARCHAR(3)    		not null default 'ACT',
	"FechaCreacion"				DATE				not null default '0001-01-01',
	"FechaModificacion"			DATE				not null default '0001-01-01',
	"IdUsuarioCrea"				BIGINT				not null default 0, 
	"IdUsuarioModifica"			BIGINT				not null default 0,
	"IdEmpresa"					BIGINT				not null default 0,
	"IdContraparte" 			BIGINT 				NOT NULL
	primary key ("CodigoOperador","IdEmpresa")
);

create column table "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" ( 
	"CodigoOperador"					NVARCHAR(40)	not null,
	"IdTipoInstrumento" 				NVARCHAR(3) 	not null,
	"AsignacionAuto"					NVARCHAR(1)		not null default 'N',
	"MovimientoFacturarAuto"			NVARCHAR(1)		not null default 'N',
	"IdEstado"							NVARCHAR(3)    	not null default 'ACT',
	"FechaCreacion"						DATE			not null default '0001-01-01',
	"FechaModificacion"					DATE			not null default '0001-01-01',
	"IdUsuarioCrea"						BIGINT			not null default 0, 
	"IdUsuarioModifica"					BIGINT			not null default 0,
	"IdEmpresa"							BIGINT			not null default 0,
	primary key ("CodigoOperador","IdTipoInstrumento","IdEmpresa")
);


create column table "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ( 
	"IdTipoInstrumento" 				NVARCHAR(3) 	not null,
	"IdEstado"							NVARCHAR(3)    	not null default 'ACT',
	"FechaCreacion"						DATE			not null default '0001-01-01',
	"FechaModificacion"					DATE			not null default '0001-01-01',
	"IdUsuarioCrea"						BIGINT			not null default 0, 
	"IdUsuarioModifica"					BIGINT			not null default 0,
	"IdEmpresa"							BIGINT			not null default 0,
	primary key ("IdTipoInstrumento","IdEmpresa")
);

DELETE FROM "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion";
INSERT INTO "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ("IdTipoInstrumento","IdEstado","FechaCreacion","FechaModificacion","IdUsuarioCrea","IdUsuarioModifica","IdEmpresa")	
VALUES('ACN','ACT',NOW(),NOW(),1,1,1);
INSERT INTO "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ("IdTipoInstrumento","IdEstado","FechaCreacion","FechaModificacion","IdUsuarioCrea","IdUsuarioModifica","IdEmpresa")	
VALUES('MIL','ACT',NOW(),NOW(),1,1,1);
INSERT INTO "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ("IdTipoInstrumento","IdEstado","FechaCreacion","FechaModificacion","IdUsuarioCrea","IdUsuarioModifica","IdEmpresa")	
VALUES('PAG','ACT',NOW(),NOW(),1,1,1);



create column table "BSCL.Plataforma.BD::TRA.HomologacionOperador" ( 
	"IdHomologacionOperador"	BIGINT				not null,
	"CodigoOperador"			NVARCHAR(10)		not null default '',
	"IdBolsa"					NVARCHAR(10)		not null default '',
	"CodigoHomologacion"		NVARCHAR(10)		not null default '',
	"IdEstado"					NVARCHAR(3)    		not null default 'ACT',
	"FechaCreacion"				DATE				not null default '0001-01-01',
	"FechaModificacion"			DATE				not null default '0001-01-01',
	"IdUsuarioCrea"				BIGINT				not null default 0, 
	"IdUsuarioModifica"			BIGINT				not null default 0,
	"IdEmpresa"					BIGINT				not null default 0,
	primary key ("IdHomologacionOperador")
);
 

/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/

drop sequence "BSCL.Plataforma.BD.Secuencias::HomologacionOperador";
create sequence "BSCL.Plataforma.BD.Secuencias::HomologacionOperador" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;


/*==============================================================*/
/* Tipos:                                                       */
/*==============================================================*/

-- pew!

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

-- pew!


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
