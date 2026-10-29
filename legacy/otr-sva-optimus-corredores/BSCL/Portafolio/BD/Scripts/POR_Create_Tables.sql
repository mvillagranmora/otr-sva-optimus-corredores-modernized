SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE 		"BSCL.Portafolio.BD::POR.PreferenciaFacturacion" 			CASCADE; 
DROP TABLE 		"BSCL.Portafolio.BD::POR.PortafolioClienteUsoMedioContacto" CASCADE; 
DROP TABLE 		"BSCL.Portafolio.BD::POR.Portafolio" 						CASCADE; 
DROP TABLE 		"BSCL.Portafolio.BD::POR.EstadoPortafolio" 					CASCADE;
DROP TABLE 		"BSCL.Portafolio.BD::POR.TipoBloqueoPortafolio" 			CASCADE;  
DROP TABLE 		"BSCL.Portafolio.BD::POR.PropositoPortafolio"				CASCADE;
DROP TABLE 		"BSCL.Portafolio.BD::POR.Autorizado"	 					CASCADE;
DROP TABLE		"BSCL.Portafolio.BD::POR.Anotacion"							CASCADE;
DROP TABLE 		"BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"			CASCADE;
DROP TABLE 		"BSCL.Portafolio.BD::POR.EstadoPreferenciaPagoPortafolio" 	CASCADE;
DROP TABLE 		"BSCL.Portafolio.BD::POR.TipoAutorizado"				  	CASCADE;

DROP TABLE 		"BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"		  	CASCADE;


DROP SEQUENCE 	"BSCL.Portafolio.BD.Secuencias::Portafolio";
DROP SEQUENCE 	"BSCL.Portafolio.BD.Secuencias::PreferenciaFacturacion";
DROP SEQUENCE 	"BSCL.Portafolio.BD.Secuencias::Autorizado";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.PreferenciaFacturacion" (
	 "IdPreferenciaFacturacion" 				BIGINT CS_FIXED 		NOT NULL ,
	 "IdPortafolio" 							BIGINT CS_FIXED,
	 "IdTipoFacturacion" 						NVARCHAR(4),
 	 "IdFacturacion" 							BIGINT CS_FIXED,
	 "FechaCreacion" 							LONGDATE CS_LONGDATE 	DEFAULT '0001-01-01 00:00:00.0000000' NOT NULL , 
PRIMARY KEY ("IdPreferenciaFacturacion"))  ;


CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.PortafolioClienteUsoMedioContacto" (
	 "IdPortafolioClienteUsoMedioContacto" 		BIGINT CS_FIXED 		NOT NULL ,
	 "IdPortafolio" 							BIGINT CS_FIXED 		NOT NULL ,
	 "IdPersonaMedioContacto" 					BIGINT CS_FIXED 		NOT NULL , 
PRIMARY KEY ("IdPortafolioClienteUsoMedioContacto"))  ;

CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.Portafolio" (
	 "IdPortafolio" 							BIGINT CS_FIXED 		 NOT NULL,
	 "CodigoPortafolio" 						NVARCHAR(100),
	 "DescripcionPortafolio" 					NVARCHAR(100)			 NOT NULL 	DEFAULT '',
	 "Proposito" 								NVARCHAR(520) 			 NOT NULL 	DEFAULT '',
	 "IdCliente" 								BIGINT CS_FIXED 		 NOT NULL,
	 "IdEstado" 								NVARCHAR(3) 			 NOT NULL 	DEFAULT 'BLO',
	 "IdTipoBloqueo" 							BIGINT CS_FIXED,
	 "ComentarioBloqueo" 						NVARCHAR(520) 			 NOT NULL 	DEFAULT '',
	 "FechaBloqueo" 							SECONDDATE CS_SECONDDATE NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	 "IdTipoDesbloqueo" 						BIGINT CS_FIXED,
	 "ComentarioDesbloqueo" 					NVARCHAR(520) 			 NOT NULL 	DEFAULT '',
	 "FechaDesbloqueo" 							SECONDDATE CS_SECONDDATE NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	 /* USO */
	 "IdDireccionEnvioCorrespondencia"			BIGINT CS_FIXED			 NOT NULL DEFAULT 0,
	 "IdTelefonoContacto"						BIGINT CS_FIXED 		 NOT NULL DEFAULT 0,
	/* MODELO PERFIL COMERCIAL */
	 "IdPerfilComercialBaseOrigen"				NVARCHAR(40) 			 NOT NULL 	DEFAULT '0', -- Sólo Referencia Inicial
	 "IdPerfilProductos"						NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPerfilProductosExtra"					NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPerfilCobros"							NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
 	 "IdPerfilCobrosExtra"						NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPerfilLimites"							NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPerfilLimitesExtra"						NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPerfilCondicionesOperativas"			NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPerfilCondicionesOperativasExtra"		NVARCHAR(40)			 NOT NULL 	DEFAULT '0',
	 "IdPersonaBrokerExtranjero" 				BIGINT 					NOT NULL 	DEFAULT 0
	 
	 
	/* MODULO AUTORIZACION */
	 "IdEstadoAutorizacion"						NVARCHAR(3)				 NOT NULL 	DEFAULT 'PEN',
 	/* ------------------------------ */
	 "FechaIngreso" 							SECONDDATE CS_SECONDDATE NOT NULL	DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 							SECONDDATE CS_SECONDDATE NOT NULL	DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 						SECONDDATE CS_SECONDDATE NOT NULL	DEFAULT '0001-01-01 00:00:00',
	/* CARGA */
	 "IdCarga" 									BIGINT CS_FIXED 					DEFAULT 0,
	 "Alias" nvarchar(255) not null default '',
	/* */
	
PRIMARY KEY ("IdPortafolio"));



-- 23-11-2015
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.PropositoPortafolio" (
	 "IdPropositoPortafolio" 					NVARCHAR(40)			 NOT NULL,
	 "Descripcion" 								NVARCHAR(100) 			 NOT NULL 	DEFAULT '',
	 "IdEstado" 								NVARCHAR(3) 			 NOT NULL 	DEFAULT 'INA',
PRIMARY KEY ("IdPropositoPortafolio"));


CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.EstadoPortafolio" (
	 "IdEstadoPortafolio" 						BIGINT CS_FIXED 		 NOT NULL,
	 "IdEstado" 								NVARCHAR(3),
	 "NombreEntidad" 							NVARCHAR(100), 
PRIMARY KEY ("IdEstadoPortafolio"));

CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.TipoBloqueoPortafolio" (
	 "IdTipoBloqueoPortafolio" 					BIGINT CS_FIXED 		 NOT NULL,
	 "NombreTipoBloqueoPortafolio" 				NVARCHAR(100)  			 NOT NULL	DEFAULT '', 
PRIMARY KEY ("IdTipoBloqueoPortafolio"));
 

CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.Autorizado" (
   	"IdAutorizado"					BIGINT			NOT NULL, 
   	"IdPortafolio"					BIGINT			NOT NULL 	DEFAULT 0,
   	"IdPersona" 					BIGINT   		NOT NULL ,
   	"IdTipoAutorizado"				NVARCHAR(40) 	NOT NULL	DEFAULT 'ORDEN-CREAR',
   	"IdEstado"						NVARCHAR(3) 	NOT NULL 	DEFAULT 'ACT',
   	"IdEstadoAutorizacion"			NVARCHAR(3)		NOT NULL	DEFAULT 'PEN',
   	"FechaIngreso"       			SECONDDATE      NOT NULL    DEFAULT '0001-01-01 00:00:00',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  		DEFAULT 0,
	"NumeroRegistroPublico" 		NVARCHAR(40) NOT NULL DEFAULT '',
	"Asiento" 						NVARCHAR(40) NOT NULL DEFAULT '',
	"FichaPartida" 					NVARCHAR(40) NOT NULL DEFAULT '',
	"TipoFirma" 					NVARCHAR(40) NOT NULL DEFAULT '',
	
  	PRIMARY KEY ("IdAutorizado")
);


CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.Anotacion" (
   	"IdAnotacion"					BIGINT			NOT NULL, 
   	"IdPortafolio"					BIGINT			NOT NULL,
   	"IdTipoAnotacion"				NVARCHAR(6)     NOT NULL,
   	"FechaAnotacion"				SECONDDATE		NOT NULL 	DEFAULT 0,
   	"Titulo"						NVARCHAR(100)	NOT NULL,
   	"Detalle"	 					NVARCHAR(500)   NOT NULL,
   	"FechaIngreso"       			SECONDDATE      NOT NULL    DEFAULT '0001-01-01 00:00:00',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  		DEFAULT 0,
	"IdMotivoAnotacion" 			NVARCHAR(6) 	not null,
	"IdRelevanciaAnotacion" 		NVARCHAR(6) 	not null,
	"IdAmbitoAnotacion" 			NVARCHAR(6) 	not null,
	"Borrado"						NVARCHAR(1)     NOT NULL 	DEFAULT 'N',
	"Archivo"						BLOB			NULL,
	"NombreArchivo"					NVARCHAR(100)	NULL,
	"ValidoDesde" 					DATE 			NULL,
	"ValidoHasta" 					DATE 			NULL,
  	PRIMARY KEY ("IdAnotacion")
);

CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.TipoAnotacion" (
   	"IdTipoAnotacion"					NVARCHAR(6)		NOT NULL, 
   	"NombreTipoAnotacion"					NVARCHAR(100)	NOT NULL,
   	"IdEmpresa"							BIGINT          NOT NULL,
  	PRIMARY KEY ("IdTipoAnotacion")
);

/* POR.portafolio -< POR.PreferenciaPagoPortafolio >- PAG.PreferenciaPago */
create column table "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
	"IdPreferenciaPagoPortafolio"	BIGINT			not null, 
	"IdPortafolio" 					BIGINT 			not null default 0,
	"IdPreferenciaPago"				NVARCHAR(40)	not null default '', 
	"IdPersonaRelacionada"			BIGINT			not null default 0,   -- cuando PreferenciaPago.UsaPersonaRelacionada = 'S'
	"IdCuentaBancaria"				BIGINT			not null default 0, -- cuando PreferenciaPago.UsaCuentaBancaria = 'S'
	"IdTarjetaBancaria"				BIGINT			not null default 0, -- cuando PreferenciaPago.UsaTarjetaBancaria = 'S'
   	"FechaCreacion" 				SECONDDATE  	not null default '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		not null default '0001-01-01 00:00:00',
	"FechaIngreso" 					SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL,
	"IdUsuarioCreador"  			BIGINT  		not null default 0,
	"IdEstado"						NVARCHAR(3)   	not null default 'ACT', -- 'ACT','INA'
	"IdEmpresa"						BIGINT         	not null default 0,
	"PorDefecto"					NVARCHAR(1)		not null default 'N'
	primary key ("IdPreferenciaPagoPortafolio")
);



CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.TipoAutorizado" (
   "IdEmpresa"                  BIGINT       	   NOT NULL,
   "IdTipoAutorizado"           NVARCHAR(40)       NOT NULL, 
   "NombreTipoAutorizado"       NVARCHAR(100)      NOT NULL,
   "IdTipoRelacion" 			NVARCHAR(6)		   NOT NULL DEFAULT '', 
   PRIMARY KEY ("IdEmpresa","IdTipoAutorizado")
);

 
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"(
	"IdCuentaCustodioPortafolio"  	BIGINT    		NOT NULL,
  	"IdPortafolio"           		BIGINT  		NOT NULL 	DEFAULT 0, 
   	"IdBoveda"       				NVARCHAR(3) 	NOT NULL 	DEFAULT '',
   	"Codigo" 						NVARCHAR(40)	NOT NULL 	DEFAULT '', 
   	"FechaIngreso"       			SECONDDATE      NOT NULL   	DEFAULT '0001-01-01 00:00:00',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  		NOT NULL 	DEFAULT 0,
	"IdEstado"						NVARCHAR(3)   	NOT NULL 	DEFAULT 'ACT', -- 'ACT','INA'
	"IdEstadoAutorizacion"			NVARCHAR(3) 	NOT NULL	DEFAULT '',
   	PRIMARY KEY ("IdCuentaCustodioPortafolio")
);




/*==============================================================*/
/* Secuences:                                                   */
/*==============================================================*/
create sequence "BSCL.Portafolio.BD.Secuencias::Portafolio" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Portafolio.BD.Secuencias::PreferenciaFacturacion" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Portafolio.BD.Secuencias::Autorizado" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Portafolio.BD.Secuencias::Anotacion" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;


create sequence "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;


/*==============================================================*/
/* Tipos:                                                       */
/*==============================================================*/
DROP TYPE "BSCL.Portafolio.BD::POR.Tipos.ConsultarPortafolios";
CREATE TYPE "BSCL.Portafolio.BD::POR.Tipos.ConsultarPortafolios" AS TABLE ("IdPortafolio" BIGINT,"CodigoPortafolio" NVARCHAR(100),"Proposito" NVARCHAR(520),"IdCliente" BIGINT,"IdPerfilComercial" BIGINT,"IdEstado" NVARCHAR(3),"FechaBloqueo" SECONDDATE,"ComentarioBloqueo" NVARCHAR(520),"IdTipoBloqueo" BIGINT,"FechaIngreso" SECONDDATE,"FechaCreacion" SECONDDATE,"FechaModificacion" SECONDDATE );

DROP TYPE "BSCL.Portafolio.BD::POR.Tipos.ConsultarCobrosPerfilComercial";
CREATE TYPE "BSCL.Portafolio.BD::POR.Tipos.ConsultarCobrosPerfilComercial" AS TABLE ("IdPerfilComercial" BIGINT,"IdPortafolio" BIGINT,"FechaCreacion" TIMESTAMP,"IdPlantillaComercial" BIGINT,"IdEmpresa" BIGINT,"NombrePlantillaComercial" NVARCHAR(100),"EsPlantillaPersonalizada" NVARCHAR(1),"IdTipoProducto" NVARCHAR(4),"IdTipoOperacion" NVARCHAR(6),"IdCanalIngreso" NVARCHAR(3),"IdCobro" NVARCHAR(6),"NombreCobro" NVARCHAR(100),"PermiteCambiarValorMaestro" NVARCHAR(1),"UsarValorPlantilla" NVARCHAR(1),"PermiteCambiarValorUsuario" NVARCHAR(1),"ValorCobroPlantilla" DECIMAL(26,8),"ValorCobroMaestro" DECIMAL(26,8),"ValorCobroMinimo" DECIMAL(26,8),"ValorCobro" DECIMAL(26,8),"FormaCobro" NVARCHAR(1),"IdCobroClasificacion" NVARCHAR(3),"DescripcionCobroClasificacion" NVARCHAR(100) );

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
alter table "BSCL.Portafolio.BD::POR.Anotacion"  drop constraint "FK_Portafolio_Anotacion";  
alter table "BSCL.Portafolio.BD::POR.Anotacion"
   add constraint "FK_Portafolio_Anotacion" foreign key ("IdPortafolio")
      references "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio");
      
alter table "BSCL.Portafolio.BD::POR.Anotacion"  drop constraint "FK_TipoAnotacion_Anotacion";  
alter table "BSCL.Portafolio.BD::POR.Anotacion"
   add constraint "FK_TipoAnotacion_Anotacion" foreign key ("IdTipoAnotacion")
      references "BSCL.Portafolio.BD::POR.TipoAnotacion" ("IdTipoAnotacion");
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
