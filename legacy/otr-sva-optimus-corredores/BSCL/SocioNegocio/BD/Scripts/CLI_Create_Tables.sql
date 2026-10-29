SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.SocioNegocio.BD::CLI.Cliente"; 
DROP TABLE "BSCL.SocioNegocio.BD::CLI.TipoSegmento"; 

DROP TABLE "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"; 

DROP TABLE "BSCL.SocioNegocio.BD::CLI.Anotacion";
DROP TABLE "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion";

DROP TABLE  "BSCL.SocioNegocio.BD::CLI.Grupo"
DROP TABLE  "BSCL.SocioNegocio.BD::CLI.GrupoClientes"
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" (
 "IdCliente" 					BIGINT 		NOT NULL ,
 "IdPersona" 					BIGINT 		NOT NULL,
 "IdEmpresa" 					BIGINT 		NOT NULL,
 "IdEstado" 					NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdSegmentoCliente" 			BIGINT,
 "IdCategoriaInversionista" 	NVARCHAR(40),
 "IdTipoBloqueo" 				NVARCHAR(40),
 "FechaBloqueo" 				SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 			NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdUsuarioCreador"		 		BIGINT,  
 "FechaIngreso" 				SECONDDATE 	NOT NULL ,
 "FechaCreacion" 				SECONDDATE  NOT NULL ,
 "FechaModificacion" 			SECONDDATE  NOT NULL , 
 "EsEnrolado" 					NVARCHAR(1) DEFAULT 'N' NOT NULL,
 "IdPortafolioDefecto"			BIGINT		DEFAULT 0	NOT NULL,
 "IdCarga" 						BIGINT  	DEFAULT 0, 
 "Borrado" 						NVARCHAR(1) DEFAULT 'N',
 "IdTipoDesbloqueo" 			NVARCHAR(40),
 "FechaDesbloqueo" 				SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 		NVARCHAR(520) DEFAULT '' NOT NULL,
 "CodigoEjecutivoExterno" 		NVARCHAR(40) DEFAULT '' NOT NULL,
 "IdTipoCliente"				NVARCHAR(40) NOT NULL DEFAULT '',
 "IdPlantillaLimiteExtra"		NVARCHAR(40) NOT NULL 	DEFAULT '0',
 "CodigoAdicionalHolding"		NVARCHAR(40) NOT NULL 	DEFAULT '', --PERU
 "IdSucursalPreferencia"		BIGINT 		 null, --PERU
 "IdPersonaBrokerExtranjero"	BIGINT 		 null, --PERU
 "IdClaseCliente"				NVARCHAR(40) NOT NULL DEFAULT '', --PERU
 "FechaBloqueoTemporal"			SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL , --PERU
 "FechaDesbloqueoTemporal"		SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL , --PERU
	--falta correccion, usar IdEmpleadoEjecutivo en vez de IdPersonaEjecutivo!.
 PRIMARY KEY ("IdCliente")) ;
 
 
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoSegmento" (
 "IdSegmentoCliente" BIGINT  NOT NULL ,
 "NombreSegmentoCliente" NVARCHAR(100) NOT NULL ,
 "DescripcionSegmento" NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdEmpresa" BIGINT  NOT NULL,
 "IdEstado" NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 PRIMARY KEY ("IdSegmentoCliente"))  ;
 

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
 "IdTipoCliente" 			NVARCHAR(40)	NOT NULL ,
 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
 "IdEmpresa" 				BIGINT  		NOT NULL,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdTipoCliente","IdEmpresa"));
 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.ClaseCliente" (
 "IdClaseCliente" 			NVARCHAR(40)	NOT NULL ,
 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
 "IdEmpresa" 				BIGINT  		NOT NULL,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdClaseCliente","IdEmpresa"));
 
 -- DECLARACION DE ORIGEN DE FONDOS.

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" (
	 "IdTipoOperacionOrigenFondos" 	NVARCHAR(6)		NOT NULL , -- Corresponde al "IdTipoOperacion"
	 "Descripcion" 					NVARCHAR(100)	NOT NULL ,
	 "IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEmpresa" 					BIGINT  		NOT NULL,
	 "IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
PRIMARY KEY ("IdTipoOperacionOrigenFondos"));

 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" (
	 "IdDeclaracionOrigenFondos" 	BIGINT			NOT NULL ,
	 "IdCliente"				 	BIGINT			NOT NULL DEFAULT 0,
	 "IdTipoOperacionOrigenFondos"	NVARCHAR(40)	NOT NULL DEFAULT '',
	 "FechaDeclaracion"				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "Comentario"					NVARCHAR(500)	NOT NULL DEFAULT '',
	 "IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEmpresa" 					BIGINT  		NOT NULL,
	 "IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	 "IdEstadoAutorizacion"			NVARCHAR(3) 	DEFAULT '' NOT NULL ,
	 "OperacionInscripcion"			NVARCHAR(40)	NOT NULL DEFAULT '',
 PRIMARY KEY ("IdDeclaracionOrigenFondos"));
  
 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" (
	 "IdTipoOrigenFondo" 				NVARCHAR(40)	NOT NULL ,
	 "IdDeclaracionOrigenFondos" 		BIGINT			NOT NULL DEFAULT 0,
	 "Valor"				 			DECIMAL(26,8) 	NOT NULL DEFAULT 0,
	 "IdUsuarioCreador"					BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 					SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 					SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdTipoOrigenFondo","IdDeclaracionOrigenFondos"));
 
 -- Posibilidad: ConceptoOrigenDeclaracion, en caso de querer customizar.

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" (
	 "IdTipoOrigenFondo" 			NVARCHAR(40)	NOT NULL ,
	 "Nombre"					 	NVARCHAR(100)	NOT NULL DEFAULT '',
	 "ValorDefecto"				 	DECIMAL(26,8) 	NOT NULL DEFAULT 0,
	 "IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	 "FechaIngreso" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	 "FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	 "IdEmpresa" 					BIGINT  		NOT NULL,
	 "IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
PRIMARY KEY ("IdTipoOrigenFondo"));

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.Anotacion" (
   	"IdAnotacion"					BIGINT			NOT NULL, 
   	"IdCliente"						BIGINT			NOT NULL,
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

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion"(
	"IdEmpresa"						BIGINT 			NOT NULL, 
	"IdAtributoAnotacion"			NVARCHAR(6)		NOT NULL, 
	"TipoAtributoAnotacion"			NVARCHAR(10)	NOT NULL, 
	"NombreAtributoAnotacion"		NVARCHAR(100)	NOT NULL, 
	PRIMARY KEY INVERTED VALUE ("IdEmpresa", "IdAtributoAnotacion")
);


CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.Grupo" (
	"IdGrupo" BIGINT NOT NULL,
	"IdEmpresa" BIGINT NOT NULL,
	"Nombre" NVARCHAR(50) NOT NULL,
	"IdEstado" NVARCHAR(3) NOT NULL,
	"Codigo" NVARCHAR(40) NOT NULL,
	PRIMARY KEY ("IdGrupo")
);

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.GrupoClientes" (
	"IdEmpresa" BIGINT NOT NULL,
	"IdGrupo" BIGINT NOT NULL,
	"IdPortafolio" BIGINT NOT NULL,
	PRIMARY KEY ("IdGrupo", "IdPortafolio")
);


/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
drop sequence "BSCL.SocioNegocio.BD.Secuencias::Cliente";
create sequence "BSCL.SocioNegocio.BD.Secuencias::Cliente" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

drop sequence "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos";
create sequence "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

drop sequence "BSCL.SocioNegocio.BD.Secuencias::Anotacion";
create sequence "BSCL.SocioNegocio.BD.Secuencias::Anotacion"  increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

drop sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoClientes";
CREATE SEQUENCE "BSCL.SocioNegocio.BD.Secuencias::GrupoClientes" START WITH 1 INCREMENT BY 1 MINVALUE 1;

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"  drop constraint "FK_TipoSegmento_Cliente";
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"  drop constraint "FK_CategoriaInversionista_Cliente";
--alter table "BSCL.SocioNegocio.BD::CLI.Cliente"  drop constraint "FK_Persona_Cliente";
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"  drop constraint "FK_TipoBloqueoSocioDeNegocio_Cliente";

alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
   add constraint "FK_TipoSegmento_Cliente" foreign key ("IdSegmentoCliente")
      references "BSCL.SocioNegocio.BD::CLI.TipoSegmento" ("IdSegmentoCliente"); 
   
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
   add constraint "FK_CategoriaInversionista_Cliente" foreign key ("IdCategoriaInversionista")
      references "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista");
 /*     
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
   add constraint "FK_Persona_Cliente" foreign key ("IdPersona")
      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");
   */         
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
   add constraint "FK_TipoBloqueoSocioDeNegocio_Cliente" foreign key ("IdTipoBloqueo")
      references "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" ("IdTipoBloqueo");
      
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.GrupoClientes"
	ADD CONSTRAINT "FK_GrupoClientes_EventoGrupo" FOREIGN KEY ("IdGrupo")
	REFERENCES "BSCL.SocioNegocio.BD::CLI.Grupo" ("IdGrupo") ON DELETE RESTRICT ON UPDATE RESTRICT;
      
                             
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/
DROP INDEX "INX01_Cliente";

create unique index "INX01_Cliente" on "BSCL.SocioNegocio.BD::CLI.Cliente"(
											 	"IdCliente", "Borrado");

DROP INDEX "BSCL_SOCIONEGOCIO_BD_CLI_GRUPO_CODIGO_IDX";											 	
CREATE UNIQUE INDEX "BSCL_SOCIONEGOCIO_BD_CLI_GRUPO_CODIGO_IDX" ON "BSCL.SocioNegocio.BD::CLI.Grupo" ("Codigo");

DROP INDEX "BSCL_SOCIONEGOCIO_BD_CLI_GRUPO_NOMBRE_IDX";
CREATE UNIQUE INDEX "BSCL_SOCIONEGOCIO_BD_CLI_GRUPO_NOMBRE_IDX" ON "BSCL.SocioNegocio.BD::CLI.Grupo" ("Nombre");


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
