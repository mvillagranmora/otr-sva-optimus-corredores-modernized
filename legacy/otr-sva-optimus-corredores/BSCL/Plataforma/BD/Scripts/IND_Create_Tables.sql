SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Plataforma.BD::IND.Indicador" 					CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" 				CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador" 			CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.EstadoTipoIndicador"		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.TipoDatoTipoIndicador"		CASCADE;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoIndicador"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Indicador"		CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleIndicador"	CASCADE;

 
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

create column table "BSCL.Plataforma.BD::IND.Indicador" (
   "IdIndicador"        BIGINT		         not null,
   "IdEntidad"          BIGINT               not null,
   "IdTipoEntidad"      NVARCHAR(40)         not null,
   "IdTipoIndicador"  	BIGINT  			 DEFAULT 0  NOT NULL,
   "ModeloCliente"		NVARCHAR(40)		 not null, -- <TipoCliente>
   "IdEmpresa"			BIGINT				 not null,
   "ValorIndicador"     NVARCHAR(255)        null, -- 13/02/2017 se deja null para permitir no guardar valor y utilizar jerarqu�a
   "FechaIngreso"       SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaCreacion"      SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  SECONDDATE           default '0001-01-01 00:00:00' not null,
   "IdEstadoAutorizacion" NVARCHAR(3)        not null,
   "FechaVencimiento"   SECONDDATE           default '0001-01-01 00:00:00' not null,
   "IdCarga"            BIGINT               not null default 0, 
   primary key  ("IdEntidad", "IdTipoEntidad", "IdIndicador", "IdEmpresa", "ModeloCliente")
);

set schema bscl;
alter table "BSCL.Plataforma.BD::IND.Indicador" 
alter("ValorIndicador"  NVARCHAR(255) null);


create column table "BSCL.Plataforma.BD::IND.TipoIndicador" (
   "IdTipoIndicador"        BIGINT      	NOT NULL,
   "IdTipoEntidad"      	NVARCHAR(40) 	NOT NULL,
   "IdEmpresa"				BIGINT			NOT NULL,
   "ValorIndicadorDefecto"  NVARCHAR(255)  	NOT NULL, -- <TipoCliente>
   "ClaseDato"         		NVARCHAR(40)  	NOT NULL,
   "CodigoTipoIndicador"   	NVARCHAR(40) 	NOT NULL,----COdigo de busqueda
   "NombreTipoIndicador"    NVARCHAR(255)  	NOT NULL,   
   "GrupoIndicador"       	NVARCHAR(40)    		,
   "CantidadDecimales"		BIGINT			NOT NULL,
   "LongitudMinima"			BIGINT			NOT NULL,
   "LongitudMaxima"			BIGINT			NOT NULL,
   "IdUso"					NVARCHAR(3) 	DEFAULT 'IND' 	NOT NULL, -- IND: Indicador, PRE: Preferencia, REQ: Requisito
   "IdTipoPersona"			NVARCHAR(1)		DEFAULT 'A'		NOT NULL, -- N: Natural, J:Juridica, A:Ambas
   "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' 	NOT NULL,
   "RequiereAutorizacion" 	NVARCHAR(1)		DEFAULT 'N' 	NOT NULL,
   "RequiereFechaVencimiento" 	NVARCHAR(1)		DEFAULT 'N' 	NOT NULL,
   "IdCarga"                BIGINT          DEFAULT  0 NOT NULL,
   "IdIndicadorMigracion"   BIGINT          DEFAULT  0 NOT NULL,
   primary key  ("IdTipoIndicador", "IdTipoEntidad", "IdEmpresa")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador" (
   "IdDetalleIndicador"         BIGINT      	NOT NULL,
   "IdTipoIndicador"      	    BIGINT		  	NOT NULL,
   "Codigo"						NVARCHAR(40)	NOT NULL DEFAULT '', -- TODO: IMPLICA MODIFICAR UI. Pend.
   "DetalleIndicador"           NVARCHAR(255)  	NOT NULL,
   "IdCarga"  		            BIGINT          DEFAULT  0 NOT NULL,
   primary key  ("IdDetalleIndicador", "IdTipoIndicador")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::IND.EstadoTipoIndicador" (
 	"IdEstadoTipoIndicador" 	BIGINT CS_FIXED NOT NULL ,
 	"IdEstado" 					NVARCHAR(3) 	DEFAULT '' NOT NULL ,
	"NombreEntidad" 			NVARCHAR(100) 	DEFAULT '' NOT NULL , 
	PRIMARY KEY ("IdEstadoTipoIndicador")
);
 
CREATE COLUMN TABLE "BSCL.Plataforma.BD::IND.TipoDatoTipoIndicador" (
 	"IdTipoDatoTipoIndicador"	BIGINT CS_FIXED NOT NULL ,
 	"TipoDato" 					NVARCHAR(20) 	NOT NULL,
 	"NombreTipoDato" 			NVARCHAR(20) 	NOT NULL,
PRIMARY KEY ("IdTipoDatoTipoIndicador")) ;

/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoIndicador"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Indicador"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
              
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleIndicador"
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
commit
