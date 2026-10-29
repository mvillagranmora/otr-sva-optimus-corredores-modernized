SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::REG.Ciudad" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.DiaFeriadoCiudad" cascade;
DROP TABLE "BSCL.Plataforma.BD::REG.Giro" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.Profesion" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.Region" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.TipoEstadoCivil" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.ZonaHoraria" cascade; 
DROP TABLE "BSCL.Plataforma.BD::REG.Pais" cascade;
DROP TABLE "BSCL.Plataforma.BD::REG.TipoDiaFeriado" cascade;
DROP TABLE "BSCL.Plataforma.BD::REG.DivisionPais" cascade;
DROP TABLE "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" (
		 "IdTipoDocumentoIdentidad" 	NVARCHAR(1)     not null,
		 "NombreTipoDocumentoIdentidad" NVARCHAR(100)   not null default '',
		 "NombreAbrTipoDocumentoIdentidad" NVARCHAR(5)   	not null default '',
		 "IdPais" 						BIGINT CS_FIXED not null,
		 "PorDefecto" 					NVARCHAR(1)     not null default 'N',
		 "IdEstado" 					NVARCHAR(3)     not null default 'ACT',
		 PRIMARY KEY ("IdTipoDocumentoIdentidad")
	 );


	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" (
		 "IdFeriado" 	  BIGINT CS_FIXED  		not null,
		 "Tipo" 		  NVARCHAR(40) 			not null default '', 
		 "Fecha" 		  DAYDATE CS_DAYDATE 	not null default '0001-01-01',
		 "Dia" 			  INTEGER CS_INT 		not null default '0',
		 "DescripcionDia" NVARCHAR(100) 		not null default '', 
		 "AgnoCarga" 	  INTEGER CS_INT 		not null default '0',
		 "IdPais" 		  BIGINT CS_FIXED 		not null default '0', 
	 	 PRIMARY KEY ("IdFeriado")
	);
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.DiaFeriadoCiudad" (
		 "IdFeriadoCiudad" 	BIGINT CS_FIXED  		not null,
		 "Tipo" 		  	NVARCHAR(40) 			not null default '', 
		 "Fecha" 		  	DAYDATE CS_DAYDATE 	not null default '0001-01-01',
		 "Dia" 			  	INTEGER CS_INT 		not null default '0',
		 "DescripcionDia" 	NVARCHAR(100) 		not null default '', 
		 "AgnoCarga" 	  	INTEGER CS_INT 		not null default '0',
		 "IdCiudad" 	  	BIGINT CS_FIXED 		not null default '0', 
	 	 PRIMARY KEY ("IdFeriadoCiudad")
	);
 	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.ZonaHoraria" (
		 "IdPais" 			 BIGINT CS_FIXED    not null,
		 "Zona" 			 NVARCHAR(40)       not null,
		 "FechaRegimen" 	 DAYDATE CS_DAYDATE not null,
		 "MinutosDiferencia" INTEGER CS_INT     not null,
		 PRIMARY KEY ("IdPais", "Zona", "FechaRegimen")
	 );
 	 
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.Pais" (
		 "IdPais" 						BIGINT CS_FIXED not null,
		 "NombrePais" 					NVARCHAR(100) 	not null default '' ,
		 "CodigoPais" 					NVARCHAR(40) 	not null default '' ,
		 "UsaDivisionPais"				VARCHAR(1)		not null default 'N',
		 "NombreDivisionPais"			NVARCHAR(100) 	not null default '' ,
		 "IdEntidadClasificadoraRiesgo" NVARCHAR(10) 	not null default '' ,
		 "ValorClasificacionRiesgo"     NVARCHAR(20) 	not null default '' ,
		 PRIMARY KEY ("IdPais")
	 );
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.DivisionPais" (
		 "IdDivisionPais" 		BIGINT CS_FIXED not null,
		 "IdPais"				BIGINT CS_FIXED	not null default 0,
		 "IdCiudad"				BIGINT CS_FIXED, -- permite null.
		 "NombreDivisionPais" 	NVARCHAR(100) 	not null default '',
		 "IdClasificacion1" 	BIGINT CS_FIXED not null default 0,
		 "IdClasificacion2" 	BIGINT CS_FIXED not null default 0,
		 PRIMARY KEY ("IdDivisionPais")
	 );
	 
	 CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" (
		 "IdClasificacionDivisionPais" 		BIGINT CS_FIXED not null,
		 "NombreClasificacionDivisionPais" 	NVARCHAR(100) 	not null default '', --	LOS RIOS, VALDIVIA. 
		 "TipoClasificacionDivisionPais"	NVARCHAR(40)	not null default '', -- REGION	, PROVINCIA.
		 "IdPais"							BIGINT CS_FIXED	not null default 0,
		 PRIMARY KEY ("IdClasificacionDivisionPais")
	 );
	 
	/*
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.Region" (
		 "IdRegion" 			BIGINT CS_FIXED not null,
		 "NombreRegion" 		NVARCHAR(100),
		 "IdPais" 				BIGINT CS_FIXED,
		 PRIMARY KEY ("IdRegion")
	 );
	 DROP TABLE "BSCL.Plataforma.BD::REG.Region"; */
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.Ciudad" (
		 "IdCiudad" 			BIGINT CS_FIXED  NOT NULL ,
		 "IdPais"				BIGINT CS_FIXED	 NOT NULL default 0,
		 "NombreCiudad" 		NVARCHAR(100)    NOT NULL default '',
		 --"IdRegion" 			BIGINT CS_FIXED  NOT NULL default '0',
		 PRIMARY KEY ("IdCiudad")
	 );
 
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.TipoEstadoCivil" (
		 "IdTipoEstadoCivil" NVARCHAR(2) 	 not null default '',
		 "Nombre" 			 NVARCHAR(100)   not null default '',
		 "IdPais" 			 BIGINT CS_FIXED not null default '0',
		 "IdEstado" 		 NVARCHAR(3)  	 not null default 'ACT', 
		 PRIMARY KEY ("IdTipoEstadoCivil")
	);

	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" (
		 "IdTipoRegimenConyugal" NVARCHAR(2)     not null,
		 "Nombre"				 NVARCHAR(100) 	 not null default '',
		 "IdPais" 				 BIGINT CS_FIXED not null default '0',
		 PRIMARY KEY ("IdTipoRegimenConyugal")
	 );

	 CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.Giro" (
		 "IdGiro" BIGINT CS_FIXED 	not null,
		 "Codigo" NVARCHAR(40) 		not null default '',
		 "Nombre" NVARCHAR(100) 	not null default '',
		 "IdPais" BIGINT CS_FIXED   not null default '0', 
	 	PRIMARY KEY ("IdGiro")
	 );
 
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.Profesion" (
		 "IdProfesion" 		 BIGINT CS_FIXED 		  not null,
		 "Codigo" 			 NVARCHAR(40) 			  not null default '',
		 "Nombre" 			 NVARCHAR(100) 			  not null default '',
		 "IdPais" 			 BIGINT CS_FIXED 		  not null default '0', 	 
		 "IdEstado" 		 NVARCHAR(3) 			  not null DEFAULT 'ACT',
		 "FechaIngreso" 	 SECONDDATE CS_SECONDDATE not null DEFAULT '0001-01-01 00:00:00',
		 "FechaCreacion" 	 SECONDDATE CS_SECONDDATE not null DEFAULT '0001-01-01 00:00:00',
		 "FechaModificacion" SECONDDATE CS_SECONDDATE not null DEFAULT '0001-01-01 00:00:00',
		 PRIMARY KEY ("IdProfesion")
	 );
	 
	 
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::REG.TipoDiaFeriado" (
		"IdTipoDiaFeriado"	NVARCHAR(40)	not null,
		"Nombre"			NVARCHAR(100)	not null default '',
		"IdPais"			BIGINT CS_FIXED	not null default 0,
		"IdEstado"			NVARCHAR(3)		not null default 'ACT',
		PRIMARY KEY ("IdTipoDiaFeriado")
	);


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */
	/*alter table "BSCL.Plataforma.BD::REG.Region"
	   add constraint "FK_Pais_Region" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais"); */
	
	alter table "BSCL.Plataforma.BD::REG.Ciudad"
	   add constraint "FK_Region_Ciudad" foreign key ("IdRegion")
	      references "BSCL.Plataforma.BD::REG.Region" ("IdRegion");	
	      
	alter table "BSCL.Plataforma.BD::REG.DiaFeriado"
	   add constraint "FK_Pais_DiaFeriado" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");	
	      
	alter table "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad"
	   add constraint "FK_Pais_TipoDocumentoIdentidad" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");		      	      
      	
	alter table "BSCL.Plataforma.BD::REG.TipoEstadoCivil"
	   add constraint "FK_Pais_TipoEstadoCivil" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");		      	      
	
	alter table "BSCL.Plataforma.BD::REG.TipoRegimenConyugal"
	   add constraint "FK_Pais_TipoRegimenConyugal" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");		      	      	

	alter table "BSCL.Plataforma.BD::REG.Giro"
	   add constraint "FK_Pais_Giro" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");

	alter table "BSCL.Plataforma.BD::REG.Profesion"
	   add constraint "FK_Pais_Profesion" foreign key ("IdPais")
	      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais");
		
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/


	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit



