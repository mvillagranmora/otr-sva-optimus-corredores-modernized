SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::GTA.CoberturaGarantia";
drop table "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia";
drop table "BSCL.Plataforma.BD::PLA.ExigibilidadTipoCartera";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.Plataforma.BD::GTA.CoberturaGarantia" (
	"IdCoberturaGarantia"            BIGINT       not null,
	"IdBolsa"			             NVARCHAR(6)  not null,
    "IdTipoInstrumentoExigeGarantia" NVARCHAR(3)  not null default '',
	"IdInstrumentoExigeGarantia"     BIGINT       not null,
    "IdTipoInstrumentoComoGarantia"  NVARCHAR(3)  not null default '',
    "IdTipoClasificacionRiesgo"  	 NVARCHAR(4)  not null default '',
	"PlazoTope"					 	 INTEGER      not null default '0',
	"UnidadPlazo"				 	 NVARCHAR(1)  not null default 'A', --A: año M: mes D: dia
	"CoberturaEntidad"			 	 DECIMAL(5,2) not null default '0',   
	"CoberturaCorredora"	     	 DECIMAL(5,2) not null default '0',   
    "IdEstado"                   	 NVARCHAR(3)  not null,
    "IdEntidadClasificadoraRiesgo"   NVARCHAR(10) not null default '',
    "ClasificacionRiesgo"            NVARCHAR(20) not null default '',
	primary key ("IdCoberturaGarantia")
   );

create column table "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" (
    "IdExigibilidadGarantia"                BIGINT       not null,
	"IdBolsa"			                    NVARCHAR(6)  not null,
    "IdTipoInstrumento"                     NVARCHAR(3)  not null default '', -- id de tipo instrumento que exige garantia
	"IdTipoInstrumentoSubyacente"           NVARCHAR(3)  not null default '', -- id tipo instrumento con el cual se garantiza el instrumento que requiere garantia
	"Nemo"			                        NVARCHAR(40) not null default '',
    "IdTipoClasificacionRiesgo"             NVARCHAR(40) not null default '',	
	"PlazoTope"					            INTEGER      not null default '0',
	"UnidadPlazo"				            NVARCHAR(1)  not null default 'A', --A:año M:mes D:dia
	"ExigibilidadEntidad"			        DECIMAL(5,2) not null default '0',   
	"ExigibilidadCorredora"	                DECIMAL(5,2) not null default '0',   
    "IdEstado"                              NVARCHAR(3)  not null, 
    "IdTipoOperacionTipoInstrumento"        NVARCHAR(6)  not null default '',
    "IdEntidadClasificadoraRiesgo"  		NVARCHAR(10) not null default '',
    "ClasificacionRiesgo"                   NVARCHAR(20) not null default '',					
    "IdInstrumento"                         BIGINT not null default 0,
primary key ("IdExigibilidadGarantia")
   ); 
   
create column table"BSCL.Plataforma.BD::PLA.ExigibilidadTipoCartera" (
	 "ExigibilidadTipoCartera" BIGINT,
	 "IdTipoInstrumento" 	   NVARCHAR(6) NOT NULL ,
	 "IdInstrumento"           NVARCHAR(40) NOT NULL,
	 "CodigoEntidad" 	       NVARCHAR(40) NOT NULL,
	 "GlosaCodigoEntidad"      NVARCHAR(100) NOT NULL,
	 "IdEstado"		           NVARCHAR(3) NOT NULL default 'ACT', 
	 primary key ("ExigibilidadTipoCartera")
);
   
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */
alter table "BSCL.Plataforma.BD::GTA.CoberturaGarantia"
   add constraint  FK_Bolsa_CobertutaGarantia foreign key ("IdBolsa")
      references "BSCL"."BSCL.Plataforma.BD::INS.Bolsa" ("IdBolsa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::GTA.CoberturaGarantia"
   add constraint  FK_TipoInstrumentoExigeGarantia_CobertutaGarantia foreign key ("IdTipoInstrumentoExigeGarantia")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::GTA.CoberturaGarantia"
   add constraint  FK_TipoInstrumentoComoGarantia_CobertutaGarantia foreign key ("IdTipoInstrumentoComoGarantia")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;


/* Table: */
alter table "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia"
   add constraint  FK_Bolsa_ExigibilidadGarantia foreign key ("IdBolsa")
      references "BSCL"."BSCL.Plataforma.BD::INS.Bolsa" ("IdBolsa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia"
   add constraint  FK_TipoInstrumento_ExigibilidadGarantia foreign key ("IdTipoInstrumento")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia"
   add constraint  FK_TipoInstrumentoSubyacente_ExigibilidadGarantia foreign key ("IdTipoInstrumentoSubyacente")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;



/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
--create index "INX01_" on "BSCL.Plataforma.BD::GTA.("");


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
Commit

