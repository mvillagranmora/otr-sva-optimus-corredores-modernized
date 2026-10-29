SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::MAN.GrupoMantenedor" cascade;
drop table "BSCL.Plataforma.BD::MAN.Mantenedor" cascade;
drop table "BSCL.Plataforma.BD::MAN.AtributoMantenedor" cascade;
drop table "BSCL.Plataforma.BD::MAN.AccionMantenedor" cascade;

 
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

create column table "BSCL.Plataforma.BD::MAN.GrupoMantenedor" ( 
   "IdGrupoMantenedor"     NVARCHAR(6)   not null,
   "NombreGrupoMantenedor" NVARCHAR(100) not null,
   "IdEstado"          	   NVARCHAR(3)   not null,
   primary key ("IdGrupoMantenedor")
);


create column table "BSCL.Plataforma.BD::MAN.Mantenedor" (
   "IdMantenedor"  		    NVARCHAR(6) not null,
   "IdGrupoMantenedor"      NVARCHAR(6) not null,
   "CodigoMantenedor"       NVARCHAR(6) not null,
   "NombreMantenedor"       NVARCHAR(100) not null,
   "TablaMantenedor"        NVARCHAR(520) not null,
   "IdEstado"               NVARCHAR(3)   not null,
   primary key ("IdMantenedor", "IdGrupoMantenedor")
);


create column table "BSCL.Plataforma.BD::MAN.AtributoMantenedor" (
   "IdAtributoMantenedor" BIGINT not null,
   "IdMantenedor"      	  NVARCHAR(6) not null,
   "NombreDiccionario"    NVARCHAR(100) default '',
   "VisibleGrilla"        NVARCHAR(1) not null,
   "VisibleAgregar"       NVARCHAR(1) not null,
   "VisibleModificar"     NVARCHAR(1) not null,
   "Orden"                SMALLINT default '1',
   "ComponenteUI"         NVARCHAR(100) default '',
   "Formato"              NVARCHAR(100) default '',   
   "NombreCampo"          NVARCHAR(100) default '',
   "TipoDatoCampo"        NVARCHAR(40) default '',
   "LargoDatoCampo"       NVARCHAR(10) default '',
   "ValorDefectoCampo"    NVARCHAR(40) default '',
   "RequeridoCampo"       NVARCHAR(1) default 'N',
   "TipoCampo"            NVARCHAR(2) default '',
   primary key ("IdAtributoMantenedor")
);

create column table "BSCL.Plataforma.BD::MAN.AccionMantenedor" (
   	"IdAccionMantenedor" BIGINT not null,
   	"IdMantenedor"       NVARCHAR(6) not null,
   	"NombreDiccionario"  NVARCHAR(100) not null,
	"Visible"            NVARCHAR(1) not null,
   	"Orden"           	 SMALLINT default '1',
   	"Tipo"               NVARCHAR(40) not null,
   	"ServicioBD"         BIGINT,
   primary key ("IdAccionMantenedor")
);

/*create column table "BSCL.Plataforma.BD::MAN.Prueba" (
   	"NumFila"            BIGINT not null,
   	"Columna"            NVARCHAR(40) not null,
   	"Valor"              NVARCHAR(520) not null

);*/
					
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

/*alter table "BSCL.Plataforma.BD::BAN.CuentaBancaria"
   add constraint "FK_Banco_CuentaBancaria" foreign key ("IdBanco")
      references "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::BAN.CuentaBancaria"
   add constraint "FK_TipoCuentaBancaria_CuentaBancaria" foreign key ("IdTipoCuentaBancaria")
      references "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" ("IdTipoCuentaBancaria") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::BAN.CuentaBancaria"
   add constraint "FK_TipoMonedaCuenta_CuentaBancaria" foreign key ("IdMonedaCuentaBancaria")
      references "BSCL.Plataforma.BD::BAN.TipoMonedaCuenta" ("IdTipoMonedaCuenta") on delete restrict on update restrict;
*/
/* Table: */
/*alter table "BSCL.Plataforma.BD::BAN.TarjetaBancaria"
   add constraint "FK_Banco_TarjetaBancaria" foreign key ("IdBanco")
      references "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::BAN.TarjetaBancaria"
   add constraint "FK_TipoTarjetaBancaria_TarjetaBancaria" foreign key ("IdTipoTarjetaBancaria")
      references "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" ("IdTipoTarjetaBancaria") on delete restrict on update restrict;
*/

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
/*drop sequence "BSCL.Plataforma.BD.Secuencias::Moneda" cascade;*/

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

/*create sequence "BSCL.Plataforma.BD.Secuencias::Moneda" 
	start     with 10 
	increment by 1
	minvalue  1;*/
	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
