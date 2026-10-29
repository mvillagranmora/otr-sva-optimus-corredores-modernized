SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" cascade;

drop table "BSCL.Plataforma.BD::AUT.EtapaEvento" cascade;

drop table "BSCL.Plataforma.BD::AUT.Evento" cascade;

drop table "BSCL.Plataforma.BD::AUT.AutorizadoEtapa" cascade;

drop table "BSCL.Plataforma.BD::AUT.Etapa" cascade;

drop table "BSCL.Plataforma.BD::AUT.TipoEvento" cascade;

drop table "BSCL.Plataforma.BD::AUT.TipoAutorizacion" cascade;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.Plataforma.BD::AUT.AutorizadoEtapa" (
   "IdAutorizadoEtapa"  BIGINT               not null,
   "IdEtapa"            BIGINT               not null,
   "IdUsuario"          BIGINT               default '0' not null,
   "IdRol"              BIGINT               default '0' not null,
   "IdUsuarioReemplaza" BIGINT               default '0' not null,
   "ReemplazaDesde"     DATE                 default '0001-01-01' not null,
   "ReemplazaHasta"     DATE                 default '0001-01-01' not null,
   "IdEstado"           NVARCHAR(3)          not null,
   primary key ("IdAutorizadoEtapa")
);

create column table "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" (
   "IdAutorizadoEtapaEvento" BIGINT          not null,
   "IdEtapaEvento"      BIGINT               not null,
   "IdUsuario"          BIGINT               default '0' not null,
   "IdRol"              BIGINT               default '0' not null,
   "IdUsuarioReemplaza" BIGINT               default '0' not null,
   "ReemplazaDesde"     DATE                 default '0001-01-01' not null,
   "ReemplazaHasta"     DATE                 default '0001-01-01' not null,
   "Observaciones"      NVARCHAR(260)        not null,
   "FechaHoraCambioEstado" DATE              default '0001-01-01' not null,
   "IdEstado"           NVARCHAR(3)          not null,
   primary key ("IdAutorizadoEtapaEvento")
);


create column table "BSCL.Plataforma.BD::AUT.Etapa" (
   "IdEtapa"            BIGINT               not null,
   "IdTipoEvento"       NVARCHAR(6)          not null,
   "Codigo"             NVARCHAR(40)         not null,
   "Nombre"             NVARCHAR(100)        not null,
   "Orden"              INTEGER              not null,
   "TipoAutorizacion"   NVARCHAR(1)          default 'T' not null,
   "MinimoAutorizan"    INTEGER              default '0' not null,
   "IdEstado"           NVARCHAR(3)          not null,
   primary key ("IdEtapa")
);


create column table "BSCL.Plataforma.BD::AUT.EtapaEvento" (
   "IdEtapaEvento"      BIGINT               not null,
   "IdEvento"           BIGINT               not null,
   "IdEtapa"            BIGINT               not null,
   "Codigo"             NVARCHAR(40)         not null,
   "Nombre"             NVARCHAR(100)        not null,
   "Orden"              INTEGER              not null,
   "TipoAutorizacion"   NVARCHAR(1)          default 'T' not null,
   "MinimoAutorizan"    INTEGER              default '0' not null,
   "IdUsuarioCambioEstado" BIGINT            default '0' not null,
   "IdRolCambioEstado"  BIGINT               default '0' not null,
   "FechaHoraCambioEstado" DATE              default '0001-01-01' not null,
   "IdEstado"           NVARCHAR(3)          not null,
   primary key ("IdEtapaEvento")
);


create column table "BSCL.Plataforma.BD::AUT.Evento" (
   "IdEvento"           BIGINT               not null,
   "IdTipoEvento"       NVARCHAR(6)          not null,
   "IdGrupo"         	BIGINT               default '0' not null,
   "IdEmpresa"          BIGINT               not null,
   "IdModulo"           NVARCHAR(3)          not null,
   "IdUsuario"          BIGINT               not null,
   "Fecha"              DATE                 default '0001-01-01' not null,
   "FechaHoraVigencia"  DATETIME                 default '0001-01-01' not null,
   "FechaHoraRegistro"  DATETIME                 not null,
   "IdUsuarioSuperAutorizador" BIGINT        default '0' not null,
   "IdRolSuperAutorizador" BIGINT            default '0' not null,
   "EtapasSecuenciales" NVARCHAR(1)          default 'S' not null,
   "IdTipoOperacion"    NVARCHAR(6)          not null,
   "IdConcepto"         NVARCHAR(6)          not null,
   --"NumeroOperacionConcepto" BIGINT          default '0' not null,
   "NumeroOperacionConcepto" NVARCHAR(40)    default ''      not null,
   "FechaOperacionConcepto" DATETIME             default '0001-01-01' not null,
   "IdCliente"          BIGINT               default '0' not null,
   "IdFuncionario"      BIGINT               default '0' not null,
   "IdProveedor"        BIGINT               default '0' not null,
   "IdTipoProducto"     NVARCHAR(4)          not null,
   "IdClasificacionTipoInstrumento" NVARCHAR(2)          not null,
   "IdTipoInstrumento"  NVARCHAR(3)          not null,
   "IdInstrumento"      BIGINT               default '0' not null,
   "Nemo"               NVARCHAR(40)         not null,
   "Cantidad"           BIGINT               default '0' not null,
   "Monto"              DECIMAL(26,8)        default '0' not null,
   "Precio"             DECIMAL(26,8)        default '0' not null,
   "Tasa"               DECIMAL(26,8)        default '0' not null,
   "IdMoneda"           NVARCHAR(3)          not null,
   "Glosa"              NVARCHAR(260)        not null,
   "IdUsuarioCambioEstado" BIGINT               default '0' not null,
   "IdRolCambioEstado"  BIGINT               default '0' not null,
   "FechaHoraCambioEstado" DATETIME                 default '0001-01-01' not null,
   "IdEstado"           NVARCHAR(3)          not null,
   primary key ("IdEvento")
);

create column table "BSCL.Plataforma.BD::AUT.TipoEvento" (
   "IdTipoEvento"       NVARCHAR(6)          not null,
   "IdEmpresa"          BIGINT               not null,
   "IdModulo"           NVARCHAR(3)          not null,
   "Codigo"             NVARCHAR(40)         not null,
   "Nombre"             NVARCHAR(100)        not null,
   "HorasVigencia"      DECIMAL(5,2)         default '0' not null,
   "EtapasSecuenciales" NVARCHAR(1)          default 'S' not null,
   "IdTipoProducto"     NVARCHAR(4)          not null,
   "IdTipoOperacion"    NVARCHAR(6)          not null,
   "IdUsuarioSuperAutorizador" BIGINT        default '0' not null,
   "IdRolSuperAutorizador" BIGINT            default '0' not null,
   "IdEstado"           NVARCHAR(3)          not null,
  -- "RutaServicioConfirmar" 		NVARCHAR(520)	default '' not null,
  --"RutaServicioRechazar" 		NVARCHAR(520)	default '' not null,
  -- "ModoParametros" 			BIGINT        	default 0 not null, --1,2 (0 - sin parametros)  
   primary key ("IdTipoEvento")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" (
 "IdEstadoAutorizacion" BIGINT CS_FIXED NOT NULL ,
 "IdEstado" NVARCHAR(3) DEFAULT '' NOT NULL ,
 "NombreEntidad" NVARCHAR(100) DEFAULT '' NOT NULL , 
 PRIMARY KEY ("IdEstadoAutorizacion"))  ;
 
 
 CREATE COLUMN TABLE "BSCL.Plataforma.BD::AUT.TipoAutorizacion" (
 "IdTipoAutorizacion" NVARCHAR(1) NOT NULL ,
 "NombreTipoAutorizacion" NVARCHAR(100) NOT NULL , 
 PRIMARY KEY ("IdTipoAutorizacion"))  ;


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

alter table "BSCL.Plataforma.BD::AUT.AutorizadoEtapa"
   add constraint "FK_Etapa_AutorizadoEtapa" foreign key ("IdEtapa")
      references "BSCL.Plataforma.BD::AUT.Etapa" ("IdEtapa") on delete restrict on update restrict;

/* Table: */

alter table "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento"
   add constraint "FK_EtapaEvento_AutorizadoEtapaEvento" foreign key ("IdEtapaEvento")
      references "BSCL.Plataforma.BD::AUT.EtapaEvento" ("IdEtapaEvento") on delete restrict on update restrict;

/* Table: */

alter table "BSCL.Plataforma.BD::AUT.Etapa"
   add constraint "FK_TipoEvento_Etapa" foreign key ("IdTipoEvento")
      references "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento") on delete restrict on update restrict;

/* Table: */

alter table "BSCL.Plataforma.BD::AUT.EtapaEvento"
   add constraint "FK_Evento_EtapaEvento" foreign key ("IdEvento")
      references "BSCL.Plataforma.BD::AUT.Evento" ("IdEvento") on delete restrict on update restrict;

/* Table: */

alter table "BSCL.Plataforma.BD::AUT.Evento"
   add constraint "FK_TipoEvento_Evento" foreign key ("IdTipoEvento")
      references "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento") on delete restrict on update restrict;


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
