SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Plataforma.BD::BAN.TipoBanco"           cascade;
DROP TABLE "BSCL.Plataforma.BD::BAN.Banco"               cascade;
DROP TABLE "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria"  cascade;
DROP TABLE "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" cascade;
DROP TABLE "BSCL.Plataforma.BD::BAN.Plaza"               cascade;
--DROP TABLE "BSCL.Plataforma.BD::BAN.PlazaCiudad"         cascade;   -- Eliminada(la ciudad se agregó en tabla plaza)
DROP TABLE "BSCL.Plataforma.BD::BAN.BancoPlaza"           cascade;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

-- Tipo Banco: Nacional / Extranjero
create column table BSCL."BSCL.Plataforma.BD::BAN.TipoBanco" ( 
   "IdTipoBanco"  			NVARCHAR(3)           not null default '' ,
   "Nombre"                 NVARCHAR(100)         not null default '' ,
   "IdEstado"               NVARCHAR(3)           not null default '' ,  
   primary key ("IdTipoBanco")
);



create column table "BSCL.Plataforma.BD::BAN.Banco" ( 
   "IdBanco"  			BIGINT              not null,
   "NombreBanco"        NVARCHAR(100)       not null default '' ,
   "CodigoBanco"        NVARCHAR(40)  		not null default '' ,   -- código del banco a nivel nacional
   "IdPaisOperacion"    BIGINT              not null default 0,     -- Ej.: Santander(Chile)  Santander(Brasil)  > Pais operación Chile y Brazil (1 registro por cada uno)
   "RutBanco"           NVARCHAR(25)        not null default '',
   "CodigoAba"          NVARCHAR(40)  		not null default '' ,
   "CodigoIban"         NVARCHAR(40)  		not null default '' ,
   "IdPersona"			BIGINT				not null default 0 ,   
   "IdTipoBanco"        NVARCHAR(3)         not null default '',    -- NAC: nacional   EXT:extranjero
   "IdEstado"           NVARCHAR(3)         not null default '',   
   "IdEmpresa"          BIGINT              not null default 0
   primary key ("IdBanco")
);


/* Mantiene datos de los tipos de cuentas bancarias por pais. Ej: Cuenta Corriente / Cuenta Ahorro / Cuenta Vista) */
create column table "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" (
   "IdTipoCuentaBancaria"  		NVARCHAR(4)     not null,
   "NombreTipoCuentaBancaria"   NVARCHAR(100)   not null default '' ,
   "IdPais"          			BIGINT        	not null, --   APais : Association[1,1..1] to ConfigRegional.Pais on APais.IdPais = IdPais;			
   primary key ("IdTipoCuentaBancaria")
);

create column table "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" (
   "IdTipoTarjetaBancaria"  	NVARCHAR(4)  	not null,
   "NombreTipoTarjetaBancaria" 	NVARCHAR(40)   	not null default '',
   primary key ("IdTipoTarjetaBancaria")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::BAN.Plaza" (
	"IdPlaza"						BIGINT,
	"Codigo"						NVARCHAR(10)	NOT NULL DEFAULT '',
	"NombrePlaza"					NVARCHAR(100)	NOT NULL,	
	"PlazoRetencionMismoBanco"		BIGINT			NOT NULL DEFAULT 0, 	--Días de retención forma de pago
	"PlazoRetencionOtroBanco"		BIGINT			NOT NULL DEFAULT 1,		--Días de retención forma de pago
	"PlazoMaximoCobroMismoBanco"	BIGINT			NOT NULL DEFAULT 60, 	--Días maximo de cobro forma de pago
	"PlazoMaximoCobroOtroBanco"		BIGINT			NOT NULL DEFAULT 90, 	--Días maximo de cobro forma de pago	
	"IdPais"						BIGINT,
	"IdCiudad"						BIGINT          NOT NULL DEFAULT 0,	    -- Solo referencial ya que plaza depende de pais
	"IdEstado"						NVARCHAR(3)		NOT NULL,
	PRIMARY KEY ("IdPlaza")
);


/*-- eliminada, la ciudad se agregó en tabla plaza
CREATE COLUMN TABLE "BSCL.Plataforma.BD::BAN.PlazaCiudad" (
	"IdPlazaCiudad"					BIGINT,
	"IdPlaza"						BIGINT	NOT NULL,
	"IdCiudad"						BIGINT	NOT NULL,
	PRIMARY KEY ("IdPlazaCiudad")
);
*/


CREATE COLUMN TABLE "BSCL.Plataforma.BD::BAN.BancoPlaza" (
	"IdBancoPlaza"					BIGINT,
	"IdBanco"						BIGINT	     NOT NULL  DEFAULT 0,
	"IdPlaza"						BIGINT	     NOT NULL  DEFAULT 0,
	"CodigoSwift"					NVARCHAR(60) NOT NULL  DEFAULT '',
	PRIMARY KEY ("IdPlazaCiudad")
);


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
