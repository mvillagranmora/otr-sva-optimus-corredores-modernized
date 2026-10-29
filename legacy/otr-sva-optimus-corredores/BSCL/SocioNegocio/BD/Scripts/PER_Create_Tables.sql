SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.SocioNegocio.BD::PER.DireccionFisica" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.Persona" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.Telefono" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.TipoTelefono" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.CuentaBancaria" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.NombreFantasia" cascade;


DROP TABLE "BSCL.SocioNegocio.BD::PER.TipoGenero" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.TipoPersona" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.GrupoDireccion" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" cascade;


DROP TABLE "BSCL.SocioNegocio.BD::PER.ClasificacionFatca" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca" cascade;
DROP TABLE "BSCL.SocioNegocio.BD::PER.Fatca" cascade;

DROP TABLE "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" cascade;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" (
	"IdTipoCargoPersona" 		BIGINT NOT NULL ,
	"CodigoTipoCargoPersona" 	NVARCHAR(6),
	"NombreTipoCargoPersona" 	NVARCHAR(520), 
	"IdEmpresa" 				BIGINT,
	"Borrado" 				   	NVARCHAR(1) DEFAULT 'N',
	"UtilizaDetalle"		   	NVARCHAR(1) DEFAULT 'N'  NOT NULL,
	PRIMARY KEY ("IdTipoCargoPersona")
);
 

create column table "BSCL.SocioNegocio.BD::PER.CuentaBancaria" (
   "IdCuentaBancaria"  			BIGINT     			not null,
   "IdBanco"  					BIGINT     			not null,	--//[FK];CuentaBancaria;IdBanco;Banco;IdBanco
   "IdTipoCuentaBancaria"  		NVARCHAR(4)     	not null,	--//[FK];CuentaBancaria;IdTipoCuentaBancaria;TipoCuentaBancaria;IdTipoCuentaBancaria
   "IdMoneda"  					NVARCHAR(3)			not null,	--//[FK];CuentaBancaria;IdMoneda;TipoMonedaCuenta;IdTipoMonedaCuenta
   "NumeroCuentaBancaria"   	NVARCHAR(100)  		not null default '',    
   "IdPersona"					BIGINT     			not null,
   "FechaIngreso"       		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaCreacion"     			SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "IdEstado" 				 	NVARCHAR(3) 		DEFAULT 'ACT' NOT NULL ,
   "IdEstadoAutorizacion" 		NVARCHAR(3)         default 'PEN' not null,
   "Borrado" 					NVARCHAR(1) DEFAULT 'N',
   "IdUsuarioCreador"   		BIGINT  DEFAULT 0,
   "IdTipoIdentificadorCuenta"	NVARCHAR(3)			DEFAULT '',
   "IdentificadorCuenta"		BIGINT				DEFAULT 0
   primary key ("IdCuentaBancaria")
);

create column table "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" (
   	"IdTarjetaBancaria"  		BIGINT     		not null,
   	"IdBanco"  					BIGINT     		not null,	--//[FK];CuentaBancaria;IdBanco;Banco;IdBanco
   	"IdTipoTarjetaBancaria"  	NVARCHAR(4)		not null,	--//[FK];TarjetaBancaria;IdTipoTarjetaBancaria;TipoTarjetaBancaria;IdTipoTarjetaBancaria
	"NumeroTarjetaBancaria"  	NVARCHAR(100)   not null default '',	--//[FK];CuentaBancaria;IdMoneda;TipoMonedaCuenta;IdTipoMonedaCuenta
    "IdPersona"                 BIGINT          not null default 0,
   	--"Vencimiento"   			NVARCHAR(7)     not null default '',
   	"FechaVencimiento"			DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
   	"CodigoVerificacion"   		INTEGER 		not null default 0,
    "FechaIngreso"       		SECONDDATE      default '0001-01-01 00:00:00' not null,
    "FechaCreacion"     		SECONDDATE      default '0001-01-01 00:00:00' not null,
    "FechaModificacion"  		SECONDDATE      default '0001-01-01 00:00:00' not null,
    "IdEstado" 				 	NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL ,
   	"IdEstadoAutorizacion" 		NVARCHAR(3)     default 'PEN' not null,
   	"Borrado" 					NVARCHAR(1) DEFAULT 'N',
    "IdUsuarioCreador"   		BIGINT  DEFAULT 0,   	
   primary key ("IdTarjetaBancaria")
);

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.DireccionFisica" (
 "IdDireccionFisica"		 	BIGINT CS_FIXED NOT NULL ,
 "Calle"					 	NVARCHAR(100) DEFAULT '' NOT NULL ,
 "Numero" 					 	NVARCHAR(40) DEFAULT '' NOT NULL ,
 "Departamento"				 	NVARCHAR(40) DEFAULT '' NOT NULL ,
 "Sector" 					 	NVARCHAR(100) DEFAULT '' NOT NULL ,
 "CodigoPostal" 			 	NVARCHAR(100) DEFAULT '' NOT NULL ,
 "IdCiudad" 				 	BIGINT CS_FIXED,
 "IdDivisionPais" 				BIGINT CS_FIXED, -- valido en caso Chile.
 "CodigoProposito"			 	NVARCHAR(40),
 "Comentario" 					NVARCHAR(100),
 "IdEstado" 				 	NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 			SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL , 
 "IdPersona" 					BIGINT CS_FIXED NOT NULL ,
 "IdEstadoAutorizacion"			NVARCHAR(3) DEFAULT '' NOT NULL ,
 "IdCarga" 						BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen" 					BIGINT CS_FIXED DEFAULT 0,
 "Borrado" 						NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"   			BIGINT  DEFAULT 0,
 "Predeterminado"				NVARCHAR(1) DEFAULT 'N' NOT NULL, 
 "CodigoCompuesto"				NVARCHAR(100)DEFAULT '' NOT NULL ,
  PRIMARY KEY ("IdDireccionFisica"))  ;
  
  

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" (
 "IdDocumentoIdentidad" 		BIGINT CS_FIXED NOT NULL ,
 "IdPersona" 					BIGINT CS_FIXED NOT NULL ,--*
 "IdTipoDocumentoIdentidad"		NVARCHAR(3) DEFAULT '0' NOT NULL ,
 "DocumentoIdentidad" 			NVARCHAR(40) DEFAULT '' NOT NULL ,
 "NumeroSerie" 					NVARCHAR(40) DEFAULT '' NOT NULL ,
 "FechaEmision"					DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
 "FechaVencimiento"				DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
 "IdEstado" 					NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 			SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdCarga" 						BIGINT CS_FIXED DEFAULT 0,
 "IdEstadoAutorizacion"			NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdOrigen" 					BIGINT CS_FIXED DEFAULT 0,
 "Borrado" 						NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"   			BIGINT  DEFAULT 0,
 "Predeterminado"				NVARCHAR(1) DEFAULT 'N' NOT NULL,
 "IdTipoDocumentoIdentidadAsociado"		NVARCHAR(3) NULL , 
  PRIMARY KEY ("IdDocumentoIdentidad"))  ;
  
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.Persona" (
 "IdPersona" 						BIGINT CS_FIXED NOT NULL ,
 "IdEstado"  						NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdDocumentoIdentidad" 			BIGINT CS_FIXED,
 "IdDocumentoIdentidadAdicional"	BIGINT ,
 "Tipo"								NVARCHAR(2) DEFAULT 'N' NOT NULL ,
 "Alias" 							NVARCHAR(100) DEFAULT '' NOT NULL ,
 "Nombres" 							NVARCHAR(100) DEFAULT '' NOT NULL ,
 "ApellidoPaterno"					NVARCHAR(100) DEFAULT '' NOT NULL ,
 "ApellidoMaterno" 					NVARCHAR(100) DEFAULT '' NOT NULL ,
 "RazonSocial" 						NVARCHAR(100) DEFAULT '' NOT NULL ,
 "IdGiroPrincipal" 					BIGINT CS_FIXED,--*
 "IdPaisNacimiento" 				BIGINT CS_FIXED,
 "IdPaisResidencia" 				BIGINT CS_FIXED,
 "IdPaisResidenciaSecundaria"		BIGINT CS_FIXED, 
 "IdPaisNacionalidadPrincipal"		BIGINT CS_FIXED, -- N380
 "IdPaisNacionalidadSecundaria"		BIGINT CS_FIXED, 
 "FechaNacimiento" 					DAYDATE CS_DAYDATE DEFAULT '0001-01-01',
 "FechaDefuncion" 					DAYDATE CS_DAYDATE DEFAULT '0001-01-01' NOT NULL ,
 "Sexo" 							NVARCHAR(2) NOT NULL ,
 "EsExtranjero" 					NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "EstadoCivil" 						NVARCHAR(2),
 "RegimenConyugal"					NVARCHAR(2),
 --"Cargo" 					NVARCHAR(100) DEFAULT '' NOT NULL ,--*
 --"Empleador" 				NVARCHAR(100) DEFAULT '' NOT NULL ,--*
 "IdDireccionFisicaPrincipal" 		BIGINT CS_FIXED,--*
 "IdDireccionElectronicaPrincipal" 	BIGINT CS_FIXED,--*
 "IdTelefonoPrincipal" 				BIGINT CS_FIXED,--*
 "IdProfesion" 						BIGINT CS_FIXED,
 "FotoPersona" 						BLOB MEMORY THRESHOLD 1000,
 "FotoFirmaPersona" 				BLOB MEMORY THRESHOLD 1000,
 "IdPersonaConyuge" 				BIGINT CS_FIXED  DEFAULT 0,
 "IdPersonaRepresentante"			BIGINT CS_FIXED  DEFAULT 0,
 "IdPersonaEmpleador" 				BIGINT CS_FIXED  DEFAULT 0,
 "CodigoTipoCargoEmpleado" 			NVARCHAR(6) DEFAULT '',
 "DetalleCargo"						NVARCHAR(40) DEFAULT ''  NOT NULL,
 "GlobalLegalEntity"				NVARCHAR(520) NOT NULL DEFAULT '', -- N380 para Jurídicos.
 "FechaInicioConyuge" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaInicioRepresentante" 		SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaInicioActividad" 			SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaIngreso" 					SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 					SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "EsEnrolado" 						NVARCHAR(1) DEFAULT 'N' NOT NULL , 
 "IdCarga" 							BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen" 						BIGINT CS_FIXED DEFAULT 0,
 "Borrado" 							NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"  				BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 					NVARCHAR(40),
 "FechaBloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 				NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo"					NVARCHAR(40),
 "FechaDesbloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo"				NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdNombreFantasiaPrincipal" 		BIGINT DEFAULT 0 NOT NULL ,
 --peru--
"IdGradoInstruccion"				bigint default 0, --: Primaria, Secundaria, Superior, Técnico
"AgnosServicio"						int default 0,
"IngresoPromedioMensual"  			decimal(26,8) default 0,
"IdGiroCIIU"						BIGINT CS_FIXED,
 --llave--
  PRIMARY KEY ("IdPersona")
);

  
  
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.Telefono" (
 "IdTelefono"					BIGINT CS_FIXED NOT NULL ,
 "IdPais" 						BIGINT CS_FIXED,
 "IdTipoTelefono"				NVARCHAR(40) DEFAULT 'OTRO' NOT NULL,
 "Numero"						NVARCHAR(40) DEFAULT '' NOT NULL ,
 "Anexo" 						NVARCHAR(40) DEFAULT '' NOT NULL ,
 "CodigoProposito" 				NVARCHAR(40),
-- "CodigoTipoMedioContacto" NVARCHAR(40),
 "Comentario" 					NVARCHAR(100),
 "IdEstado" 					NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 			SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdPersona" 					BIGINT CS_FIXED NOT NULL ,--*
 "IdEstadoAutorizacion"			NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdCarga" 						BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen" 					BIGINT CS_FIXED DEFAULT 0,
 "Borrado" 						NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"  			BIGINT  DEFAULT 0,
 "Predeterminado"				NVARCHAR(1) DEFAULT 'N' NOT NULL, 
  PRIMARY KEY ("IdTelefono"))  ;

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoTelefono" (
 "IdTipoTelefono"				NVARCHAR(40) 	NOT NULL ,
 "Nombre"						NVARCHAR(100) 	NOT NULL DEFAULT '',
  PRIMARY KEY ("IdTipoTelefono")
);
  

  CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" (
 "IdDireccionElectronica"		 BIGINT CS_FIXED NOT NULL ,
 "Direccion"					 NVARCHAR(100) DEFAULT '' NOT NULL ,
 "CodigoProposito" 				 NVARCHAR(40),
 --"CodigoTipoMedioContacto" 	NVARCHAR(40),
 "Comentario" 					NVARCHAR(100),
 "IdEstado" 					NVARCHAR(3) DEFAULT 'ACT' NOT NULL ,
 "FechaIngreso" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion"				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 			SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdPersona" 					BIGINT CS_FIXED NOT NULL ,--*
 "IdEstadoAutorizacion"			NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 "IdCarga" 						BIGINT CS_FIXED DEFAULT 0,
 "IdOrigen" 					BIGINT CS_FIXED DEFAULT 0,
 "Borrado" 						NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"  			BIGINT  DEFAULT 0,
 "Predeterminado"				NVARCHAR(1) DEFAULT 'N' NOT NULL, 
  PRIMARY KEY ("IdDireccionElectronica"))  ;
  
  create column table "BSCL.SocioNegocio.BD::PER.NombreFantasia" (
   "IdNombreFantasia"  			BIGINT     			not null,
   "DescripcionNombreFantasia" NVARCHAR(200)  		not null,	
   "IdPersona"			  		BIGINT     			not null,	
   "FechaCreacion"     			SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaIngreso"       		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "IdEstado" 				 	NVARCHAR(3) 		DEFAULT 'ACT' NOT NULL ,
   "IdEstadoAutorizacion" 		NVARCHAR(3)         default 'PEN' not null,
   "Borrado" 					NVARCHAR(1) 		DEFAULT 'N',
   "IdUsuarioCreador"   		BIGINT  			DEFAULT 0,
   primary key ("IdNombreFantasia")
);
  


	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoGenero" (
		 "IdTipoGenero" 	 NVARCHAR(2) 	 not null,
		 "Descripcion"	 	 NVARCHAR(100)   not null default '',
		 PRIMARY KEY ("IdTipoGenero")
	);
	
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoPersona" (
		 "IdTipoPersona" 	 NVARCHAR(2) 	 not null,
		 "Descripcion" 		 NVARCHAR(100)   not null default '',
		 PRIMARY KEY ("IdTipoPersona")
	);	

	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" (
		 "IdTipoMedioContacto" 	 	 NVARCHAR(8) 	 not null,
		 "Descripcion" 	 			 NVARCHAR(100)   not null default '',
		 "SubTipo"					 NVARCHAR(8)    not null default '',
		 PRIMARY KEY ("IdTipoMedioContacto")
	);

	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" (
		 "IdPropositoMedioContacto"	 		 NVARCHAR(8) 	 not null,
		 "Descripcion" 						 NVARCHAR(100)   not null default '',
		 PRIMARY KEY ("IdPropositoMedioContacto")
	);
	
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GrupoDireccion" (
		 "IdGrupoDireccion" 	 BIGINT CS_FIXED NOT NULL ,
		 "Grupo" 				 NVARCHAR(100),
		 "Comentario" 			 NVARCHAR(100),
	     "IdPersona" 			 BIGINT CS_FIXED NOT NULL ,--*	 
		 "FechaCreacion"     	 SECONDDATE          default '0001-01-01 00:00:00' not null,
		 "FechaModificacion"  	 SECONDDATE          default '0001-01-01 00:00:00' not null,
		 "FechaIngreso"       	 SECONDDATE          default '0001-01-01 00:00:00' not null,	 
		 "IdEstado" 	 		 NVARCHAR(3) 		 default 'ACT' NOT NULL ,
	     "IdEstadoAutorizacion"  NVARCHAR(3)         default 'PEN' not null,
	     "Borrado" 				 NVARCHAR(1) 		 default 'N',
	     "IdUsuarioCreador"   	 BIGINT  			 default 0,
		 PRIMARY KEY ("IdGrupoDireccion")
	);
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" (
		 "IdGrupoDireccionDetalle" 	 BIGINT CS_FIXED NOT NULL ,
		 "IdDireccionElectronica" 	 BIGINT CS_FIXED NOT NULL ,
		 "IdTipoDestinatario" 		 NVARCHAR(6),
	     "IdGrupoDireccion" 		 BIGINT CS_FIXED NOT NULL ,
		 "FechaCreacion"     		 SECONDDATE          default '0001-01-01 00:00:00' not null,
		 "FechaModificacion"  	 	 SECONDDATE          default '0001-01-01 00:00:00' not null,
		 "FechaIngreso"       	 	 SECONDDATE          default '0001-01-01 00:00:00' not null,	 
		 "IdEstado" 	 		 	 NVARCHAR(3) 		 default 'ACT' NOT NULL ,
	     "IdEstadoAutorizacion"  	 NVARCHAR(3)         default 'PEN' not null,	 
	     "Borrado" 				 	 NVARCHAR(1) 		 default 'N',
	     "IdUsuarioCreador"   	 	 BIGINT  			 default 0,
		 PRIMARY KEY ("IdGrupoDireccionDetalle")
	);
	
	
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GradoInstruccion" (
		 "IdGradoInstruccion" 	BIGINT,
		 "Codigo" 	 			NVARCHAR(40) NOT NULL DEFAULT '' ,
		 "Nombre" 	 			NVARCHAR(100) NOT NULL DEFAULT '' ,
		 "IdPais"				BIGINT NOT NULL DEFAULT 0,
		 PRIMARY KEY ("IdGradoInstruccion")
	);
	
/*============================================================
						FATCA                                               
=============================================================*/	
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"(
	"IdClasificacionFatca"			NVARCHAR(10) 				NOT NULL,
	"Nombre"						NVARCHAR(100)				NOT NULL DEFAULT '',
	"PorDefecto"					NVARCHAR(1)					NOT NULL DEFAULT 'N',
PRIMARY KEY ("IdClasificacionFatca"));	

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"(
	"IdClasificacionEmpresaFatca"	NVARCHAR(10) 				NOT NULL,
	"Nombre"						NVARCHAR(100)				NOT NULL DEFAULT '',
	"PorDefecto"					NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"UsaNumeroGIIN"					NVARCHAR(1)					NOT NULL DEFAULT 'N',
PRIMARY KEY ("IdClasificacionEmpresaFatca"));	

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.Fatca" (
 	"IdPersona" 					BIGINT 						NOT NULL,
 	"IdEstado"  					NVARCHAR(3) 				NOT NULL DEFAULT '',
 	"IdClasificacionFatca"			NVARCHAR(10)				NOT NULL DEFAULT '',
 	"IdClasificacionEmpresaFatca"	NVARCHAR(10)				NOT NULL DEFAULT '',
 	"NumeroGIIN"					NVARCHAR(40)				NOT NULL DEFAULT '', -- depende de clasificacion empresa
	"FechaFirmaFormulario"			SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 	"AutorizacionInformarOrganismo"	NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	--INDICIOS
	--IdPaisNacionalidad1, la de ficha persona!
	"TIN1"							NVARCHAR(40)				NOT NULL DEFAULT '', -- ANALIZAR SI MANTENER!
 	"IdPaisNacionalidad2"			BIGINT						NOT NULL DEFAULT 0,
	"TIN2"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	"IdPaisNacionalidad3"			BIGINT						NOT NULL DEFAULT 0,
 	"TIN3"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	"IdPaisNacionalidad4"			BIGINT						NOT NULL DEFAULT 0,
 	"TIN4"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	"IdPaisNacionalidad5"			BIGINT						NOT NULL DEFAULT 0,
 	"TIN5"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	--EXCEPCIONES
 	"ExcepcionResidente"			NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionNacimiento"			NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionDomicilio"			NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionTelefono"				NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionOrdenPermanente"		NVARCHAR(1)					NOT NULL DEFAULT 'N',	
 	"ExcepcionFirma"				NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	--
 	"FechaIngreso" 					SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 	"FechaCreacion" 				SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 	"FechaModificacion" 			SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00', 
 	"Borrado" 						NVARCHAR(1) 				DEFAULT 'N',
 	"IdUsuarioCreador"  			BIGINT  					DEFAULT 0,
 	"IdUsuarioModificador"  		BIGINT  					DEFAULT 0,
PRIMARY KEY ("IdPersona")) ;



/*==============================================================*/
/* sequences:                                                  */
/*==============================================================*/
drop sequence "BSCL.SocioNegocio.BD.Secuencias::Persona";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::Telefono"; 
drop sequence "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::TarjetaBancaria";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::NombreFantasia";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccion";
drop sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccionDetalle";



create sequence "BSCL.SocioNegocio.BD.Secuencias::Persona" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::Telefono" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::TarjetaBancaria" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::NombreFantasia" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccion" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;
create sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccionDetalle" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;

/*==============================================================*/
/* Types:                                                  */
/*==============================================================*/


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

alter table "BSCL.SocioNegocio.BD::PER.Persona"  drop constraint "FK_EstadoCivil_Persona";
alter table "BSCL.SocioNegocio.BD::PER.Persona"
   add constraint "FK_EstadoCivil_Persona" foreign key ("EstadoCivil")
      references "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ("IdTipoEstadoCivil");
 
alter table "BSCL.SocioNegocio.BD::PER.Persona"  drop constraint "FK_Profesion_Persona";      
alter table "BSCL.SocioNegocio.BD::PER.Persona"
   add constraint "FK_Profesion_Persona" foreign key ("IdProfesion")
      references "BSCL.Plataforma.BD::REG.Profesion" ("IdProfesion");
      
alter table "BSCL.SocioNegocio.BD::PER.Persona"  drop constraint "FK_PaisNacimiento_Persona";  
alter table "BSCL.SocioNegocio.BD::PER.Persona"
   add constraint "FK_PaisNacimiento_Persona" foreign key ("IdPaisNacimiento")
      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");

alter table "BSCL.SocioNegocio.BD::PER.Persona"  drop constraint "FK_PaisResidencia_Persona";  
alter table "BSCL.SocioNegocio.BD::PER.Persona"
   add constraint "FK_PaisResidencia_Persona" foreign key ("IdPaisResidencia")
      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");
      
alter table "BSCL.SocioNegocio.BD::PER.DireccionElectronica"  drop constraint "FK_Persona_DireccionElectronica";
alter table "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
   add constraint "FK_Persona_DireccionElectronica" foreign key ("IdPersona")
	 references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");
 
alter table "BSCL.SocioNegocio.BD::PER.Telefono"  drop constraint "FK_Persona_Telefono";
alter table "BSCL.SocioNegocio.BD::PER.Telefono"
   add constraint "FK_Persona_Telefono" foreign key ("IdPersona")
      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");
  
alter table "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  drop constraint "FK_Persona_DocumentoIdentidad";
alter table "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
   add constraint "FK_Persona_DocumentoIdentidad" foreign key ("IdPersona")
      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");
 
alter table "BSCL.SocioNegocio.BD::REL.RelacionPersona"  drop constraint "FK_Persona_RelacionPersona";
alter table "BSCL.SocioNegocio.BD::REL.RelacionPersona"
   add constraint "FK_Persona_RelacionPersona" foreign key ("IdPersonaOrigen")
      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");

alter table "BSCL.SocioNegocio.BD::PER.NombreFantasia"  drop constraint "FK_Persona_NombreFantasia";
alter table "BSCL.SocioNegocio.BD::PER.NombreFantasia"
   add constraint "FK_Persona_NombreFantasia" foreign key ("IdPersona")
	 references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/
DROP INDEX "INX01_Persona";

DROP INDEX "INX01_DocumentoIdentidad";

/* Table: */
create unique index "INX01_Persona" on "BSCL.SocioNegocio.BD::PER.Persona"(
											 "IdPersona", "Borrado");
											 
create unique index "INX01_DocumentoIdentidad" on "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
											 "IdDocumentoIdentidad", "Borrado");
											 
											 
/*create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
