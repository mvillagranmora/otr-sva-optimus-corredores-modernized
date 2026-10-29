SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::TAS.Fuente";
DROP TABLE "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" cascade;
DROP TABLE "BSCL.Plataforma.BD::TAS.Tasa" cascade;
DROP TABLE "BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento";
DROP TABLE "BSCL.Plataforma.BD::TAS.PrecioTasa"
DROP TABLE "BSCL.Plataforma.BD::TAS.TipoTasa";
DROP TABLE "BSCL.Plataforma.BD::TAS.EstadoTasa";
DROP TABLE "BSCL.Plataforma.BD::TAS.TipoCriterioTasa";
DROP TABLE "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas";
DROP TABLE "BSCL.Plataforma.BD::TAS.MargenTasa";
DROP TABLE "BSCL.Plataforma.BD::TAS.Uso";


/*==============================================================*/
/* Create Tables                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.Fuente" (
	"IdFuente"		BIGINT  		not null,
	"Codigo"		NVARCHAR(40)	default '' 	not null,
	"Descripcion"	NVARCHAR(250)	default '' 	not null,
	"IdEmpresa" 	BIGINT			default 0	not null,
	"IdEstado"		NVARCHAR(3)		default '' 	not null,
	"NombreEntidad"	NVARCHAR(100)	not null,
	PRIMARY KEY ("IdFuente")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" (
	"IdConfiguracionTasas"	BIGINT  		not null,
	"IdEmpresa"				BIGINT  		not null,
	"NombreEntidad"			NVARCHAR(40)	not null,
	"Valor"					NVARCHAR(250)	not null,
	"Descripcion"			NVARCHAR(520)	not null,
	PRIMARY KEY ("IdConfiguracionTasas")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.Tasa" (
	"IdTasa"			BIGINT  		not null,
	"IdFuente"			BIGINT  		not null,
	"IdInstrumento"		BIGINT  		not null,
	"Nemotecnico"		NVARCHAR(40)	default ''	not null,
	"Tasa"				DECIMAL(26,8)	default 0	not null,
	"TasaEstimada" 		DECIMAL(26,8)	default 0	not null,
	"IdEstado"			NVARCHAR(3)		default ''	not null,
	"IdEmpresa"			BIGINT  		not null,
	"IdUsuarioCreador"	BIGINT 			default 0	not null,
	"FechaArchivo"		SECONDDATE		default '0001-01-01 00:00:00' not null,
	"FechaIngreso"		SECONDDATE		default '0001-01-01 00:00:00' not null,
	"FechaCreacion"		SECONDDATE		default '0001-01-01 00:00:00' not null,
	"FechaModificacion"	SECONDDATE		default '0001-01-01 00:00:00' not null,
	"RutaArchivo"		NVARCHAR(520)	default ''	not null,
	"NombreArchivo"		NVARCHAR(520)	default ''	not null,
	"Borrado"			NVARCHAR(1)		default 'N'	not null,
	PRIMARY KEY ("IdTasa")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento"(
	"IdPrecioTasaTipoInstrumento"	BIGINT,
	"IdTipoInstrumento"				NVARCHAR(3),
	"Tasa"							DECIMAL(26,8),
	"Precio"						DECIMAL(26,8),
	"PlazoDesde"					BIGINT,
	"PlazoHasta"					BIGINT,
	"IdEstado"						NVARCHAR(3),
	PRIMARY KEY ("IdPrecioTasaTipoInstrumento")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.PrecioTasa" (
	"IdPrecioTasa"		BIGINT  		NOT NULL,
	"IdFuente"			BIGINT  		not null,
	"IdTipoInstrumento"	NVARCHAR(3)		default ''	NOT NULL,
	"IdInstrumento"		BIGINT  		not null,
	"Nemotecnico"		NVARCHAR(40)	default ''	not null,
	"IdTipoTasa"		NVARCHAR(40),		-- valor : CURVA, PUNTO, TASA
	"Tasa"				DECIMAL(26,8),
	"TasaEstimada" 		DECIMAL(26,8),
	"IdTipoPrecio"		NVARCHAR(40),
	"Precio"			DECIMAL(26,8),
	"Plazo"				BIGINT 			default 0	not null,
	--"PlazoDesde"		BIGINT 			default 0	not null,
	--"PlazoHasta"		BIGINT 			default 0	not null,
	"IdEstado"			NVARCHAR(3)		default ''	not null,
	"IdEmpresa"			BIGINT  		not null,
	"IdUsuarioCreador"	BIGINT 			default 0	not null,
	"IdUsuarioModificacion"	BIGINT 		default 0	not null,
	"FechaSolicitud"	SECONDDATE		default '0001-01-01 00:00:00' not null, -- Fecha solicitud registro es creado
	"FechaIngreso"		SECONDDATE		default '0001-01-01 00:00:00' not null, -- Fecha sistema registro es creado
	"FechaCreacion"		SECONDDATE		default '0001-01-01 00:00:00' not null, -- Fecha utc registro es creado
	"FechaModificacion"	SECONDDATE		default '0001-01-01 00:00:00' not null, -- Fecha registro es modificado
	"FechaPublicacion"	SECONDDATE		default '0001-01-01 00:00:00' not null, -- Fecha registro es publicado
	"RutaArchivo"		NVARCHAR(520)	default ''	not null,
	"NombreArchivo"		NVARCHAR(520)	default ''	not null,
	"Borrado"			NVARCHAR(1)		default 'N'	not null,
	"TipoRegistro"		NVARCHAR(10)	default ''	not null,	-- TASA o PRECIO
	PRIMARY KEY ("IdPrecioTasa")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.TipoTasa" (
	"IdTipoTasa"	NVARCHAR(40)	not null,
	"Codigo"		NVARCHAR(40)	default '' 	not null,
	"Descripcion"	NVARCHAR(250)	default '' 	not null,
	"IdEmpresa" 	BIGINT			default '0' not null,
	"IdEstado"		NVARCHAR(3)		default '' 	not null,
	PRIMARY KEY ("IdTipoTasa")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.EstadoTasa" (
	"IdEstadoTasa"	BIGINT			NOT NULL,
	"IdEstado"		NVARCHAR(3)		NOT NULL,
	"NombreEntidad"	NVARCHAR(100)	NOT NULL,
	"Descripcion"	NVARCHAR(520)	NOT NULL,
	PRIMARY KEY ("IdEstadoTasa")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.TipoCriterioTasa" (
	"IdTipoCriterioTasa"	NVARCHAR(40)	NOT NULL,
	"Codigo"				NVARCHAR(40)	DEFAULT '' 	NOT NULL,
	"Descripcion"			NVARCHAR(250)	DEFAULT '' 	NOT NULL,
	"IdEmpresa" 			BIGINT			DEFAULT '0' NOT NULL,
	"IdEstado"				NVARCHAR(3)		DEFAULT '' 	NOT NULL,
	PRIMARY KEY ("IdTipoCriterioTasa")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" (
	"IdCriterioSeleccionTasas"			BIGINT  		NOT NULL,
	"Prioridad"							BIGINT  		DEFAULT 0	NOT NULL,
	"TipoCartera"						NVARCHAR(250)	DEFAULT '0'	NOT NULL,	-- valor : 0 (TODOS), PROPIA, TERCEROS
	"IdClasificacionTipoInstrumento"	NVARCHAR(3)		DEFAULT ''	NOT NULL,
	"IdTipoInstrumento"					NVARCHAR(3)		DEFAULT ''	NOT NULL,
	"IdTipoOperacion"					NVARCHAR(6)		DEFAULT ''	NOT NULL,
	"IdEmisor"							NVARCHAR(40)	DEFAULT ''	NOT NULL,	-- valor : TODOS
	"IdMoneda"							NVARCHAR(3)		DEFAULT ''	NOT NULL,	-- valor : TODOS
	"IdInstrumento"						BIGINT  		DEFAULT 0	NOT NULL,	-- valor : 0 (TODOS)
	"Nemotecnico"						NVARCHAR(40)	DEFAULT ''	NOT NULL,	-- valor : TODOS
	"DiasAntiguedad"					BIGINT  		DEFAULT 0	NOT NULL,
	"IdTipoCriterioTasa"				NVARCHAR(40)	DEFAULT '0' NOT NULL,	-- valor : 0 (TODOS), COMERCIAL, NORMATIVO
	"CodigoFuente"						NVARCHAR(40)	DEFAULT ''	NOT NULL,
	"IdTipoTasa"						NVARCHAR(40)	DEFAULT ''	NOT NULL,	-- valor : CURVA, PUNTO, TASA
	"IdTipoPrecio"						NVARCHAR(40)	DEFAULT ''	NOT NULL,
	"Interpolar"						NVARCHAR(1)		DEFAULT 'N'	NOT NULL,
	"IdEstado"							NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
	"Borrado"							NVARCHAR(1)		DEFAULT 'N'	NOT NULL,
	"IdEmpresa"							BIGINT  		NOT NULL,
	"IdUsuarioModificacion"				BIGINT 			DEFAULT 0	NOT NULL,
	"FechaCreacion"						SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"FechaModificacion"					SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdUso"								NVARCHAR(40)	DEFAULT ''	NOT NULL,
	PRIMARY KEY ("IdCriterioSeleccionTasas")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.MargenTasa" (
	"IdMargenTasa"		BIGINT			NOT NULL,
	"TipoEntidad"		NVARCHAR(40)	NOT NULL, 
	"IdMoneda"			NVARCHAR(3)		NOT NULL DEFAULT '',
	"MontoDesde"		DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"MontoHasta"		DECIMAL(26,8)	NOT NULL DEFAULT 0,	
	"PlazoDesde"		INTEGER			NOT NULL DEFAULT 0,
    "PlazoHasta"		INTEGER			NOT NULL DEFAULT 0,
	"Tasa"				DECIMAL(26,8)	NOT NULL DEFAULT 0,
    "IdEstado"			NVARCHAR(3)		NOT NULL DEFAULT 'ACT',
	"IdEmpresa"			BIGINT			NOT NULL DEFAULT 0,
    "FechaIngreso"		SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
    "FechaCreacion"		SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
    "FechaModificacion"	SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioCreador"	BIGINT			NOT NULL DEFAULT 0,
    "Borrado"			NVARCHAR(1)		NOT NULL DEFAULT 'N',
    PRIMARY KEY ("IdMargenTasa")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::TAS.Uso" (
	"IdUso"			NVARCHAR(40)	NOT NULL,
	"Codigo"		NVARCHAR(40)	DEFAULT '' 	NOT NULL,
	"Descripcion"	NVARCHAR(250)	DEFAULT '' 	NOT NULL,
	"IdEmpresa" 	BIGINT			DEFAULT 0	NOT NULL,
	"IdEstado"		NVARCHAR(3)		DEFAULT '' 	NOT NULL,
	PRIMARY KEY ("IdUso")
);


/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdTasa"					CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa"				CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdCriterioSeleccionTasas"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdMargenTasa"				CASCADE;


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdTasa"
	START     WITH 1
	INCREMENT BY 1
	MINVALUE  1;


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa"
	START     WITH 1
	INCREMENT BY 1
	MINVALUE  1;


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdCriterioSeleccionTasas"
	START     WITH 1
	INCREMENT BY 1
	MINVALUE  1;


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdMargenTasa"
	START     WITH 1
	INCREMENT BY 1
	MINVALUE  1;


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
