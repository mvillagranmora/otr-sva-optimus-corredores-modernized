SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento";
DROP TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio";
DROP TABLE "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio";
DROP TABLE "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"; 


drop sequence "BSCL.SocioNegocio.BD.Secuencias::Enrolamiento";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio";

drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.SocioNegocio.BD::SOC.Enrolamiento" (
   "IdEnrolamiento"     BIGINT               not null,
   "IdEmpresa"          BIGINT               not null,
   "IdUsuario"			BIGINT				 not null,
   "IdTipoEntidad"      NVARCHAR(100)        not null,
   "IdEntidadEnrolada"  BIGINT               not null,
   "IdTipoDocumentoIdentidad" NVARCHAR(3)    not null,
   "DocumentoIdentidad" NVARCHAR(40)         not null,
   "IdTipoDocumentoIdentidadAdicional" NVARCHAR(3) not null
   "DocumentoIdentidadAdicional" NVARCHAR(40) not null
   "Tipo"               NVARCHAR(2)          default 'N' not null,
   "Nombres"            NVARCHAR(100)        not null,
   "ApellidoPaterno"    NVARCHAR(100)        not null,
   "ApellidoMaterno"    NVARCHAR(100)        not null,
   "RazonSocial"        NVARCHAR(100)        not null,
   "IdPaisNacimiento"   BIGINT				 not null,
   "FechaNacimiento"    DATE                 default '0001-01-01' null,
   "Sexo"               NVARCHAR(2)          not null,
   "EstadoCivil"        NVARCHAR(2)          not null,
   "IdCiudad"           BIGINT               not null,
   "IdDivisionPais"     BIGINT               DEFAULT 0,
   "IdTipoTelefono"     NVARCHAR(40)         not null,
   "Telefono"           NVARCHAR(100)        not null,
   "Email"              NVARCHAR(100)        not null,
   "DireccionCalle"     NVARCHAR(100)        not null,
   "DireccionNumero"    NVARCHAR(40)         not null,
   "DireccionDepartamento" NVARCHAR(40)      not null,
   "FechaCreacion"      SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  SECONDDATE           default '0001-01-01 00:00:00' not null,
   "IdPersonaConyuge"   BIGINT CS_FIXED  NULL DEFAULT 0,
   "RegimenConyugal"    NVARCHAR(2) NULL DEFAULT '',
   "CLI_IdCategoriaInversionista"	NVARCHAR(40)	not null,
   "CLI_IdTipoCliente"				NVARCHAR(40)	DEFAULT '' NOT NULL,
   "CLI_IdEmpleadoEjecutivo"   		BIGINT DEFAULT 0 NOT NULL,
   "CLI_CodigoAdicionalHolding"		NVARCHAR(40)	not null,
   "CLI_IdClaseCliente"				NVARCHAR(40)	DEFAULT '' NOT NULL,
   "POR_IdPerfilComercial"			NVARCHAR(40)	not null,
   "EMP_CodigoEmpleado"				NVARCHAR(40)	DEFAULT '',
   "EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6)	 	DEFAULT '',
   "EMP_IdEmpleadoSupervisor"		BIGINT 			DEFAULT 0,
   "EMP_IdSucursal"					BIGINT			DEFAULT 0,
   "EMP_FechaInicioCargo"			DATE  			DEFAULT '0001-01-01',
   "EMP_FechaTerminoCargo" 			DATE			DEFAULT '0001-01-01',
   "CNT_CodigoInscripcion"			NVARCHAR(40)	DEFAULT '',
   "CNT_Emisor" 					NVARCHAR(1)		DEFAULT 'N',
   "CNT_Depositos"					NVARCHAR(1)		DEFAULT 'N',
   "CNT_Bolsa" 						NVARCHAR(1)		DEFAULT 'N',
   "CNT_Boveda" 					NVARCHAR(1)		DEFAULT 'N',
   "CNT_Corredora" 					NVARCHAR(1)		DEFAULT 'N',
   "CNT_CamaraDeCompensacion" 		NVARCHAR(1)		DEFAULT 'N',
   "CNT_Banco" 						NVARCHAR(1)		DEFAULT 'N',
   "PRO_IdAreaProveedor"			BIGINT			DEFAULT 0,
   "EMP_CargoIndefinido"			NVARCHAR(1)		NULL,
   primary key  ("IdEnrolamiento")
);

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" (
 "IdSocioDeNegocio" BIGINT  NOT NULL,
 "IdPersona" 		BIGINT  NOT NULL, 
 "IdEmpresa" 		BIGINT  NOT NULL,
 "IdCliente" 		BIGINT ,
 "IdEmpleado" 		BIGINT ,
 "IdOperador" 		BIGINT ,
 "IdProveedor" 		BIGINT ,
 "IdContraparte" 	BIGINT ,
 "Borrado" 			NVARCHAR(1) DEFAULT 'N',
 PRIMARY KEY ("IdSocioDeNegocio"))  ;
 
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" (
 "IdEstadoSocioDeNegocio" 	BIGINT  	 	NOT NULL ,
 "IdEstado" 				NVARCHAR(40) 	NOT NULL ,
 "NombreEntidad" 			NVARCHAR(100) 	NOT NULL , 
 PRIMARY KEY ("IdEstadoSocioDeNegocio"));
 
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" (
 "IdEmpresa"		 BIGINT NOT NULL,
 "IdTipoBloqueo" 	 NVARCHAR(40)  NOT NULL ,
 "IdTipoEntidad"	 NVARCHAR(40)  NOT NULL ,
 "NombreTipoBloqueo" NVARCHAR(100) NOT NULL ,
 "IdAccion"	         NVARCHAR(3)   NOT NULL ,			 
 "IdEstado" 		 NVARCHAR(3)  DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdEmpresa","IdTipoBloqueo","IdTipoEntidad")); 
 
 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" (
 "IdTipoSocioDeNegocio" 	NVARCHAR(3),
 "NombreTipoSocioDeNegocio" NVARCHAR(100), 
 PRIMARY KEY ("IdTipoSocioDeNegocio"));


/*==============================================================*/
/* Tipos:	                                                    */
/*==============================================================*/
CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
"IdEnrolamiento"			BIGINT,
"IdEmpresa" 				BIGINT,
"IdUsuario" 				BIGINT,
"IdTipoEntidad" 			NVARCHAR(100),
"IdEntidadEnrolada" 		BIGINT,
"IdTipoDocumentoIdentidad"  NVARCHAR(3),
"IdTipoDocumentoIdentidadAsociado"  NVARCHAR(3) null,
"DocumentoIdentidad" 		NVARCHAR(40),
"IdTipoDocumentoIdentidadSecundario" NVARCHAR(3) null default '',
"DocumentoIdentidadSecundario" NVARCHAR(40) null default '',
"Tipo" 						NVARCHAR(2),
"Nombres" 					NVARCHAR(100),
"ApellidoPaterno" 			NVARCHAR(100),
"ApellidoMaterno" 			NVARCHAR(100),
"RazonSocial" 				NVARCHAR(100),
"IdPaisNacimiento" 			BIGINT,
"FechaNacimiento" 			DATE,
"Sexo" 						NVARCHAR(2),
"EstadoCivil" 				NVARCHAR(2),
"IdCiudad" 					BIGINT,
"IdDivisionPais" 			BIGINT,
"IdTipoTelefono" 			NVARCHAR(40),
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"DireccionCalle" 			NVARCHAR(100),
"DireccionNumero" 			NVARCHAR(40),
"DireccionDepartamento" 	NVARCHAR(40),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista" 	NVARCHAR(40),
"CLI_IdTipoCliente"				NVARCHAR(40),
"CLI_IdEmpleadoEjecutivo"   	BIGINT,
"CLI_CodigoAdicionalHolding"	NVARCHAR(40) null default '',
"CLI_IdClaseCliente" NVARCHAR(40) CS_STRING,
"POR_IdPerfilComercial" 		NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE NULL DEFAULT '',
"CNT_CodigoInscripcion" 		NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"CNT_Boveda" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT,
"EMP_CargoIndefinido"			NVARCHAR(1) NULL DEFAULT ''
--,"CLI_IdTipoEnrolamiento"		NVARCHAR(40)
 );



/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
create sequence "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::Enrolamiento" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
