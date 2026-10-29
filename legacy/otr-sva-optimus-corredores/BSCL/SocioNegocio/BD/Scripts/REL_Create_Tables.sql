SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.SocioNegocio.BD::REL.RelacionPersona" cascade; 
DROP TABLE "BSCL.SocioNegocio.BD::REL.TipoRelacion" cascade; 
DROP TABLE "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" cascade; 
DROP TABLE "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::REL.NormativaRelacion" cascade;
drop table "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental" cascade;
drop type "BSCL.SocioNegocio.BD::REL.PersonasRelacionadas" cascade; 


drop sequence "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.RelacionPersona" (
 "IdRelacionPersona"		BIGINT 			not null,
 "IdTipoRelacion"			NVARCHAR(6)		default '' 	not null,
 "IdPersonaOrigen" 			BIGINT 			default '0' not null,
 --"IdTipoEntidadOrigen"	BIGINT 			default '0' not null,
 --"IdEntidadOrigen" 		BIGINT 			default '0' not null,
 "IdPersonaDestino" 		BIGINT 			default '0' not null,
 --"IdTipoEntidadDestino" 	BIGINT 			default '0' not null,
 --"IdEntidadDestino" 		BIGINT 			default '0' not null,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' not null,
 "PorcentajeParticipacion" 	DECIMAL(26,8)   DEFAULT 0 not null, -- JG:26-05-2016
 "FechaInicioRelacion" 		SECONDDATE  	DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaFinRelacion" 		SECONDDATE  	DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaIngreso" 			SECONDDATE  	DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 			SECONDDATE  	DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 		SECONDDATE  	DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdUsuarioCreador"		 	BIGINT  		DEFAULT 0,
 "IdEstadoAutorizacion" 	NVARCHAR(3)     default 'PEN' not null,
 --PERU
 "FinalidadRelacion"			NVARCHAR(255)	DEFAULT '' not null,
 "CondicionesRelacion"			NVARCHAR(255)	DEFAULT '' not null,
 "IdGradoVinculacionParental"	BIGINT	DEFAULT 0 not null,
  PRIMARY KEY ("IdRelacionPersona"))  ;

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental" (
	"IdGradoVinculacionParental" 	BIGINT not null,
	"Nombre"						NVARCHAR(255) not null,
	 PRIMARY KEY ("IdGradoVinculacionParental"));
)


CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.TipoRelacion" (
 "IdTipoRelacion" 				NVARCHAR(6) 	not null,
 "IdEmpresa"					BIGINT 			default '0' 	not null,
 "EsPersonaNatural" 			NVARCHAR(1) 	default 'S'		not null,
 "IdTipoEntidadOrigen" 			NVARCHAR(100)	default '' 		not null,
 "NombreTipoRelacion" 			NVARCHAR(100) 	default '' 		not null,
 "IdTipoRelacionDestino" 		NVARCHAR(6)		default '' 		not null,
 "EsIndefinida" 				NVARCHAR(1) 	default 'N'		not null,
 "IdEstado" 					NVARCHAR(3) 	default 'ACT'	not null,
 "Administracion" 				NVARCHAR(40)	default '' 		not null,
 "UsaPorcentajeParticipacion" 	NVARCHAR(1)		default 'N' 	not null,
  "RetiraDocumentos"			NVARCHAR(1)		default 'N' 	not null,
  --PERU:
 "UsaFinalidadRelacion"			NVARCHAR(1)		default 'N' 	not null,
 "UsaCondicionesRelacion"		NVARCHAR(1)		default 'N' 	not null,
 "UsaGradoVinculacionParental"	NVARCHAR(1)		default 'N' 	not null,
  PRIMARY KEY ("IdTipoRelacion"))  ;


CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" (
 "IdTipoRelacionDestino" NVARCHAR(6) NOT NULL ,
 "IdTipoRelacion" NVARCHAR(6),
 "IdTipoEntidad" NVARCHAR(100),
 "IdEstado" NVARCHAR(3) DEFAULT 'ACT', PRIMARY KEY ("IdTipoRelacionDestino"))  ;
 
 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" (
 "IdClasificacionRelacion"	 NVARCHAR(20) 	NOT NULL ,
 "NombreClasificacion" 		 NVARCHAR(100)	default '' not null,
 "IdNormativa" 				 NVARCHAR(15)	default '' not null,
 "IdEstado" 				 NVARCHAR(3) DEFAULT 'ACT',
 PRIMARY KEY ("IdClasificacionRelacion"))  ;
 
 
  CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" (
 "IdNormativaRelacionClasificacion"		BIGINT 			not null,
 "IdClasificacionRelacion" 				NVARCHAR(20)	default '' not null,
 "IdTipoRelacion" 				 		NVARCHAR(6)		default '' not null,
  PRIMARY KEY ("IdNormativaRelacionClasificacion"))  ;
 
 
  CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::REL.NormativaRelacion" (
 "IdNormativaRelacion"					BIGINT 			not null,
 "IdNormativa"				 			NVARCHAR(15) 	NOT NULL ,
 "IdTipoRelacion" 				 		NVARCHAR(6)		default '' not null,
 "IdEstado" 							NVARCHAR(3) 	default 'ACT',
 "DescripcionRelacion" 					NVARCHAR(100)	default '' not null,
  PRIMARY KEY ("IdNormativaRelacion"))  ;
 
 
 
  CREATE TYPE "BSCL.SocioNegocio.BD::REL.PersonasRelacionadas" AS TABLE  
 
									 ("IdPersonaOrigen"			BIGINT,
										 "IdRelacionPersona"		BIGINT, 
										 "Administracion" 			NVARCHAR(40),
   										 "IdTipoRelacion" 			NVARCHAR(6),
    									 "NombreTipoRelacion"		NVARCHAR(520),
    									 "FechaInicioRelacion" 		SECONDDATE,
    									 "FechaFinRelacion" 		SECONDDATE,
    									 "IdEstado" 				NVARCHAR(3),
    									 "NombreEstado" 			NVARCHAR(100),
    								     "IdSocioDeNegocio" 		BIGINT,
										 "IdPersona" 				BIGINT, 
										 "IdEmpresa" 				BIGINT,
										 "IdCliente" 				BIGINT,
										 "IdEmpleado" 				BIGINT,
										 "IdProveedor" 				BIGINT,
										 "IdContraparte" 			BIGINT,
										 "FechaIngreso" 			SECONDDATE,
										 "FechaCreacion" 			SECONDDATE,
										 "FechaModificacion" 		SECONDDATE,
										 "DocumentoIdentidadDefecto" NVARCHAR(40),
										 "CodigoTipoPersona"						NVARCHAR(2),
										 "NombreCompleto"			NVARCHAR(400),
										 "RazonSocial" 				NVARCHAR(100),
										 "Alias" 					NVARCHAR(100),    
										 "Nombres" 					NVARCHAR(100),
										 "ApellidoPaterno"			NVARCHAR(100),
										 "ApellidoMaterno" 			NVARCHAR(100),    
										 "FechaNacimiento" 			DAYDATE,
										 "IdPaisResidencia" 		BIGINT,
										 "IdTipoDocumentoIdentidadDefecto"				NVARCHAR(1),        
									     "NombreTipoDocumentoIdentidadDefecto" 			NVARCHAR(100),      
									     "NombreAbrTipoDocumentoIdentidadDefecto"		NVARCHAR(5),   
									     "TipoPersona" 				NVARCHAR(300),--desde constante
										 "CodigoPaisResidencia" 	NVARCHAR(40),
										 "NombrePaisResidencia" 	NVARCHAR(100), 
										 "EsCliente" 				NVARCHAR(6),
										 "EsEmpleado"				NVARCHAR(6),
										 "EsProveedor"				NVARCHAR(6),
										 "EsContraparte"			NVARCHAR(6),
										 "EstadoPersona"   			NVARCHAR(3), 
										 "EstadoCliente"   			NVARCHAR(3), 
										 "EstadoEmpleado"   		NVARCHAR(3), 
										 "EstadoProveedor"   		NVARCHAR(3), 
										 "EstadoContraparte"	    NVARCHAR(3),
										 "NombreEstadoPersona"   	NVARCHAR(100), 
										 "NombreEstadoCliente"   	NVARCHAR(100), 
										 "NombreEstadoEmpleado"   	NVARCHAR(100), 
										 "NombreEstadoProveedor"   	NVARCHAR(100), 
										 "NombreEstadoContraparte"	NVARCHAR(100) 	 	
									    )
 
 
 
 
 /*==============================================================*/
/* sequences:                                                  */
/*==============================================================*/

create sequence "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;

 

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

/*alter table "BSCL.Plataforma.BD::AUT.AutorizadoEtapa"
   add constraint "FK_Etapa_AutorizadoEtapa" foreign key ("IdEtapa")
      references "BSCL.Plataforma.BD::AUT.Etapa" ("IdEtapa") on delete restrict on update restrict;*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

/*create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
