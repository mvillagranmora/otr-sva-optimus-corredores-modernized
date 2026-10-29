SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" cascade;
drop table "BSCL.Plataforma.BD::INS.EmisorYDocumentoIdentidad" cascade; 
drop table "BSCL.Plataforma.BD::INS.Instrumento" cascade; 
drop table "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" cascade; 
drop table "BSCL.Plataforma.BD::INS.TipoInstrumento" cascade; 
drop table "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" cascade; 
drop table "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" cascade; 
drop table "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" cascade;
drop table "BSCL.Plataforma.BD::INS.TipoInstrumentoFlujoCustodia" cascade; 
drop table "BSCL.Plataforma.BD::INS.ItemFlujoCustodia" cascade; 
drop table "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cascade; 
drop table "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" cascade;
drop table "BSCL.Plataforma.BD::INS.Bolsa" cascade; 
drop table "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" cascade; 
drop table "BSCL.Plataforma.BD::INS.MercadoYBolsa" cascade;
drop table "BSCL.Plataforma.BD::INS.Mercado" cascade; 
drop table "BSCL.Plataforma.BD::INS.ProductoEmpresa" cascade;
drop table "BSCL.Plataforma.BD::INS.TipoProducto" cascade;
drop table "BSCL.Plataforma.BD::INS.TipoOperacion" cascade;
drop table "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" cascade;
drop table "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" cascade;
drop table "BSCL.Plataforma.BD::INS.CoberturaProducto" cascade;
drop table "BSCL.Plataforma.BD::INS.DiversificacionProducto" cascade;
drop table "BSCL.Plataforma.BD::INS.ExigenciaProducto" cascade;
drop table "BSCL.Plataforma.BD::INS.ProductoLineaRiesgo" cascade;
drop type "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosActual" cascade; 
 
drop table "BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" cascade; 
drop table "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" cascade;
drop table "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" cascade;
drop table "BSCL.Plataforma.BD::INS.EquivalenciaClasificacion" cascade;
drop table "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" cascade;

drop table "BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" cascade;
drop table "BSCL.Plataforma.BD::INS.TareaValorizacion"  cascade;
DROP TABLE "BSCL.Plataforma.BD::INS.RelacionInstrumento" cascade;
DROP TABLE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" cascade;

/*==============================================================*/
/* Create Tables:                                               */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.EmisorYDocumentoIdentidad" (
	 "IdEmisorYDocumentoIdentidad" BIGINT 	not null,
	 "IdEmisor" 			NVARCHAR(40) 	default '' 	not null,
	 "IdDocumentoIdentidad" BIGINT			default '0' not null,
	 PRIMARY KEY ("IdEmisorYDocumentoIdentidad")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.VectorPrecios" (
	 "IdInstrumento"		 			BIGINT 					NOT NULL,
	 "FechaVector" 						SECONDDATE			NOT NULL,
	 "SecuenciaLista"		 			INT						NOT NULL,
	 "Tipo"		 						NVARCHAR(1)				NOT NULL,
	 "ValorPrecioAlgorithmics100"		DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorPrecioAlgorithmics95"		DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorPrecioAlgorithmics98"		DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorPrecioOperacionesPlazo01"	DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorPrecioVentaCorta02"			DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorOtrosPrecios03"	 			DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorOtrosPrecios04"	 			DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorOtrosPrecios05"	 			DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 "ValorPrecioBase"			 		DECIMAL(26,8)			NOT NULL DEFAULT 0,
	 PRIMARY KEY ("IdInstrumento","FechaVector")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.Instrumento" (
	 "IdInstrumento" 		BIGINT 			not null,
	 "Nemotecnico" 			NVARCHAR(40) 	default '' 	not null,
	 "ISIN" 				NVARCHAR(40) 	default '' 	not null,
	 "CorteMinimo" 			DECIMAL(26,8) 	default '0' not null,
	 "EstaInscrito"			NVARCHAR(1) 	default 'N'	not null,
	 "EsDivisible"			NVARCHAR(1) 	default 'N'	not null,
	 "RazonSocial" 			NVARCHAR(100) 	default '' 	not null,
	 "Lista" 				NVARCHAR(520) 	default '' 	not null,
	 "FechaEmision" 		DATE            default '0001-01-01' not null,
	 "FechaVencimiento" 	DATE            default '0001-01-01' not null,
	 "IdInstrumentoRelacionado" BIGINT		default '0' not null,
	 "IdEmisor" 			NVARCHAR(40)	default '' 	not null,
	 "IdTipoReajuste" 		NVARCHAR(3) 	default '' 	not null,
	 "Reajustable" 			NVARCHAR(1) 	default 'N' not null,
	 "IdMoneda"				NVARCHAR(3) 	default '' 	not null,
	 "IdTipoInstrumento" 	NVARCHAR(3) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3)		default '' 	not null,
	 "DecimalesPrecio" 		INTEGER 		default '0' not null,
	 "Tamano" 				INTEGER 		default '1' not null,
	 "FechaInicioVigencia"	DATE    		default '0001-01-01' not null,
	 "TasaFiscal"			DECIMAL(26,8)   default '0' not null, 
	 "IndArt104"   			NVARCHAR(1) 	default 'N' not null,	 
     "IdEntidadClasificadoraRiesgo"       	NVARCHAR(10) default ''  not null, 	    
	 "ValorClasificacionRiesgo"     		NVARCHAR(20) not null default '', -- puede ser cualquier nomenclatura, depende de la entidad
	 "TipoValorizacion" 					NVARCHAR(10), -- requerido para Valorizacion.	 
	 "Tasa"									DECIMAL(26,8),
	 "Precio"								DECIMAL(26,8), 
 	 "ValorizaPor" 							NVARCHAR(1) not null default 'T', 	-- P: Precio, T: Tasa.	 
	 "TieneCupon"							NVARCHAR(1)	not null default 'N',
	 "NumeroAccionesCirculacion" 			BIGINT NOT NULL DEFAULT 0,
	 "NumeroCuotasCirculacionMesActual" 	BIGINT NOT NULL DEFAULT 0,
	 "IdPaisEmisor" 						BIGINT NOT NULL DEFAULT 0,
	 "IndicadorLiquidaDCV" 					NVARCHAR(1) NOT NULL DEFAULT 'N',
	 "Serie"								NVARCHAR(40) NOT NULL DEFAULT '',
	 "IndicadorBolson"						NVARCHAR(1) NOT NULL DEFAULT 'N',
	 "FrecuenciaCupon" 						DECIMAL(26,8) NOT NULL DEFAULT 0,
	 "PrecioReferencia" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
	 "FechaReferencia" 						DATE NOT NULL DEFAULT '0001-01-01'
	 PRIMARY KEY ("IdInstrumento")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" (
	"IdInstrumento" 		BIGINT 			NOT NULL,
	"IdTipoInstrumento" 	NVARCHAR(3) 	NOT NULL,
	"IdTipoReajuste" 		NVARCHAR(3) 	NOT NULL,
	"CorteMinimo" 			DECIMAL(26,8) 	DEFAULT '0' NOT NULL,
	PRIMARY KEY ("IdInstrumento","IdTipoReajuste")
);
	
CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoInstrumento" (
	 "IdTipoInstrumento" 	NVARCHAR(3) 	not null,
	 "Codigo" 				NVARCHAR(40) 	default '' 	not null,
	 "Nombre" 				NVARCHAR(100) 	default '' 	not null,
	 "Descripcion"			NVARCHAR(520) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 "IdClasificacionTipoInstrumento" 	NVARCHAR(3)		default '' 	not null,
	 PRIMARY KEY ("IdTipoInstrumento")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" (
	"IdTipoInstrumentoMonedaLiquidacion"		BIGINT 			not null,
	"IdTipoInstrumento" 						NVARCHAR(3) 	not null default '',
	"IdMoneda"									NVARCHAR(3) 	not null default '',
	"IdMonedaLiquidacion"						NVARCHAR(3) 	not null default '',
	PRIMARY KEY ("IdTipoInstrumentoMonedaLiquidacion")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" (
"IdGrupoInstrumento" 					BIGINT 			not null,
"CodigoClasificacion" 					NVARCHAR(40) 	not null default '',
"GlosaCodigoClasificacion" 				NVARCHAR(100) 	not null default '',
"IdClasificacionTipoInstrumento"		NVARCHAR(3) 	not null default '',
"IdTipoInstrumento" 					NVARCHAR(6) 	not null default '',
PRIMARY KEY ("IdGrupoInstrumento")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" (
	"IdInstrumentoSubyacente"			BIGINT 		not null,--1
	"IdTipoInstrumento" 				NVARCHAR(3)	not null,--SIV
	"IdBolsa"				 			NVARCHAR(3)	not null,--BCS
	--"IdBolsaTipoInstrumento" 			BIGINT 		not null,
	"IdTipoInstrumentoSubyacente"		NVARCHAR(3)	not null,--ACN
	--"IdBolsaInstrumentoSubyacente"		NVARCHAR(3)	not null,
	--"IdBolsaTipoInstrumentoTransable"	BIGINT		not null,
	"IdEstado" 							NVARCHAR(3) default '' 	not null,--ACT
    "IdInstrumento"                     BIGINT default 0 not null,
    "Nemotecnico" 			    		NVARCHAR(40) 	default '' 	not null,                       
	PRIMARY KEY ("IdInstrumentoSubyacente")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" (
	"IdInstrumentoSubyacente"			BIGINT 		not null,--1
	"IdTipoInstrumento" 				NVARCHAR(3)	not null,--SIV
	"IdBolsa"				 			NVARCHAR(3)	not null,--BCS
	"IdTipoInstrumentoSubyacente"		NVARCHAR(3)	not null,--ACN
	"IdInstrumento"						BIGINT		not null,--COPEC (Id)
	PRIMARY KEY ("IdInstrumentoSubyacente")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoInstrumentoFlujoCustodia" (
	"IdTipoInstrumentoFlujoCustodia"	BIGINT 		not null,--1
	"IdTipoInstrumento" 				NVARCHAR(3)	not null,--SIV
	"IdBolsa"				 			NVARCHAR(3)	not null,--BCS
	"IdFlujoCustodia"					BIGINT		not null,----1
	"IdEstado"							NVARCHAR(3) not null,--ACT
	PRIMARY KEY ("IdTipoInstrumentoFlujoCustodia")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.ItemFlujoCustodia" (
	"IdItemFlujoCustodia"				BIGINT 			not null,--1
	"IdFlujoCustodia"					BIGINT			not null,--1
	"NombreFlujoCustodia" 				NVARCHAR(100)	not null,--Flujo Simul
	"ItemFlujoCustodia"					NVARCHAR(40)	not null,--Cust, SimV,SimC,AccN, etc
	"IdEstado"							NVARCHAR(3) 	not null,--ACT
	PRIMARY KEY ("IdItemFlujoCustodia")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" (
	 "IdClasificacionTipoInstrumento" 		NVARCHAR(3) 	not null,
	 "Codigo" 				NVARCHAR(40) 	default '' 	not null,
	 "Nombre" 				NVARCHAR(100) 	default '' 	not null,
	 "Descripcion" 			NVARCHAR(520) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdClasificacionTipoInstrumento")
);		
		


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" (
	 "IdBolsaTipoInstrumento" 	BIGINT 			not null,
	 "IdTipoInstrumento"		NVARCHAR(3) 	default '' 	not null,
	 "IdBolsa" 					NVARCHAR(3) 	default '' 	not null,
	 "IdBoveda" 				NVARCHAR(3) 	default '' 	not null,
	 "ExigeGarantia" 			NVARCHAR(1) 	default 'N'	not null,	 
	 "IdContraparteGarantia"	BIGINT			default 0   not null,
	 "IdEstado" 				NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdBolsaTipoInstrumento")
);



CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.Bolsa" (
	 "IdBolsa"				NVARCHAR(3) 	not null,
	 "Codigo" 				NVARCHAR(40) 	default '' 	not null,
	 "Nombre" 				NVARCHAR(100) 	default '' 	not null,
	 "IdPais" 				BIGINT			default '0' not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdBolsa")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.Corredora" (
	 "IdCorredora" 					BIGINT,
	 "IdPersona"					BIGINT,
	 "CodigoInscripcionRegulador"	NVARCHAR(40)	NOT NULL DEFAULT '',
	 "IdEstado"						NVARCHAR(3) 	DEFAULT 'ACT',
	 "Borrado" 						NVARCHAR(1) 	DEFAULT 'N',
	 PRIMARY KEY ("IdCorredora")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.CorredoraCodigos" (
	 "IdCorredora" 	BIGINT,
	 "IdBolsa" 				NVARCHAR(3)		NOT NULL,
	 "CodigoCorredora"		NVARCHAR(40) 	NOT NULL,
	 PRIMARY KEY ("IdCorredora","IdBolsa")
);

/*INSERT INTO "BSCL.Plataforma.BD::INS.Corredora"
values (2,1741534,'XX','ACT','N')

INSERT INTO "BSCL.Plataforma.BD::INS.CorredoraCodigos"
VALUES (2,'BCS',35)*/

--EJECUTADAS
--ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ALTER ("IdInstrumento"	BIGINT	not null)
--ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ALTER ("IdBolsa"			NVARCHAR(3)	not null)
--ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ALTER ("FechaCierre"		DATE	not null)
--ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("ValorContratoUnitario")
--ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("ValorContratoEntidad")

--PENDIENTES
/*
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("PrecioMayor")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("PrecioMenor")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("UltimoPrecioCompra")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("UltimoPrecioVenta")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("VolumenTransado")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("MontoTransado")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("PrecioMedio")
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" DROP ("PrecioMercadoAnterior")
*/

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosHistoricos" (
	"IdInstrumento"					BIGINT			NOT NULL,
	"IdBolsa" 						NVARCHAR(3) 	NOT NULL,
	"FechaIngresoRegistro"			TIMESTAMP		NOT NULL DEFAULT CURRENT_TIMESTAMP,
 	
 	"MontoTransadoAgnoActual"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"MontoTransadoDiaActual"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"MontoTransadoDiaAnterior"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"MontoTransadoMesActual"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
					
	"PrecioCierreDiaActual"			DECIMAL(26,8) 	DEFAULT '0' NOT NULL,
	"PrecioCierreDiaAnterior"		DECIMAL(26,8) 	DEFAULT '0' NOT NULL,
	 
	"PrecioMayorAgnoActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMayorDiaActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMayorDiaAnterior"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMayorMesActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,

	"PrecioMedioAgnoActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMedioDiaActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMedioDiaAnterior"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMedioMesActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
										
	"PrecioMenorAgnoActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMenorDiaActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMenorDiaAnterior"		DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"PrecioMenorMesActual"			DECIMAL(26,8)	DEFAULT '0' NOT NULL,
									
	"UltimoPrecioDiaActual" 		DECIMAL(26,8) 	DEFAULT '0' NOT NULL,

	"UnidadesTransadasDiaActual"	DECIMAL(26,8)	DEFAULT '0' NOT NULL,
	"UnidadesTransadasDiaAnterior"	DECIMAL(26,8)	DEFAULT '0' NOT NULL,
									
	"VariacionDiaria"		 		DECIMAL(26,8)	DEFAULT '0' NOT NULL
);

CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" (
	"IdInstrumento"					BIGINT			NOT NULL,
	"IdBolsa" 						NVARCHAR(3) 	NOT NULL,
	"FechaActualizacionArchivo"		SECONDDATE	NOT NULL DEFAULT '0001-01-01',
	"FechaActualizacionMD"			SECONDDATE	NOT NULL DEFAULT '0001-01-01',
	"PrecioMayor"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PrecioMenor"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PrecioMedio"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PrecioApertura"				DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PrecioCierre"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PuntaCompra"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PuntaVenta"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"PrecioUltimaTransaccion"		DECIMAL(26,8)	NOT NULL DEFAULT 0,
 	"UnidadesTransadas"				DECIMAL(26,8)	NOT NULL DEFAULT 0,
 	"MontoTransado"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
 	PRIMARY KEY("IdInstrumento","IdBolsa")
 	);








CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" (
	 "IdBolsaInstrumentoPrecios" 	BIGINT 	not null,
	 "IdInstrumento"				BIGINT			not null,
	 "IdBolsa" 						NVARCHAR(3) 	not null,
	 "PrecioCierre"					DECIMAL(26,8) 	default '0' not null,
	 "PrecioMayor" 					DECIMAL(26,8) 	default '0' not null,
	 "PrecioMenor" 					DECIMAL(26,8) 	default '0' not null,
	 "UltimoPrecioTransado" 		DECIMAL(26,8) 	default '0' not null,
	 "UltimoPrecioCompra" 			DECIMAL(26,8) 	default '0' not null,
	 "UltimoPrecioVenta" 			DECIMAL(26,8) 	default '0' not null,
	 "FechaCierre" 					DATE 			not null,
	 "VariacionDiaria"		 		DECIMAL(26,8)  default 0 not null,
	 "VolumenTransado"		 		DECIMAL(26,8)  default 0 not null,
	 "MontoTransado"		 		DECIMAL(26,8)  default 0 not null,
	 "PrecioMedio" 					DECIMAL(26,8)  default 0 not null,
	 "PrecioMercadoAnterior" 		DECIMAL(26,8)  default 0 not null,
	 PRIMARY KEY ("IdBolsaInstrumentoPrecios")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.MercadoYBolsa" (
	 "IdMercadoYBolsa" 		BIGINT 			not null,
	 "IdMercado" 			NVARCHAR(4) 	default '' 	not null,
	 "IdBolsa" 				NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdMercadoYBolsa")
);
	


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.Mercado" (
	 "IdMercado" 			NVARCHAR(4) 	not null,
	 "Codigo" 				NVARCHAR(40) 	default '' 	not null,
	 "Nombre" 				NVARCHAR(100) 	default '' 	not null,
	 "Descripcion" 			NVARCHAR(100) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdMercado")
);
	


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.ProductoEmpresa" (
	 "IdProductoEmpresa" 	BIGINT 			not null,
	 "IdTipoProducto" 		NVARCHAR(4) 	default '' 	not null,
	 "IdEmpresa" 			BIGINT 			default '0' not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdProductoEmpresa")
);
		

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoProducto" (
	 "IdTipoProducto" 		NVARCHAR(4) 	not null,
	 "Codigo" 				NVARCHAR(40) 	default '' 	not null,
	 "Nombre" 				NVARCHAR(100) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdTipoProducto")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" (
	 "IdTipoOperacion" 				NVARCHAR(6)  	not null,
	 "Codigo" 						NVARCHAR(40) 	default '' 	not null,
	 "IdTipoOperacionRelacionada"	NVARCHAR(6)  	not null DEFAULT '',
	 "Nombre" 					NVARCHAR(100) 	default '' 	not null,
	 "Accion"					NVARCHAR(40) 	default '' 	not null,
	 "AccionOperacion"			NVARCHAR(40) 	default '' 	not null,
	 "IdTipoProducto" 			NVARCHAR(4) 	default '' 	not null,
	 "Comprobante" 				NVARCHAR(1) 	default 'N' 	not null,
	 "EsOrden" 					NVARCHAR(1) 	default 'N' 	not null,
	 "EsSimultanea"				NVARCHAR(1) 	default 'N' 	not null,
 	 "EsVentaCorta"				NVARCHAR(1) 	default 'N' 	not null,
	 "EsTransaccion" 			NVARCHAR(1) 	default 'N' 	not null,
	 "EsIngresoEgresoTitulo" 	NVARCHAR(1)		default 'N' 	not null,
	 "RecibePagaPortafolio"		NVARCHAR(1)		default ''		not null, -- 'R': recibe, 'P': paga
	 PRIMARY KEY ("IdTipoOperacion")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" (
	 "IdTipoOperacionTipoInstrumento" BIGINT not null,
	 "IdTipoOperacion" 		NVARCHAR(6) 	default '' 	not null,
	 "IdTipoInstrumento" 	NVARCHAR(3) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 "IdInstrumento"		BIGINT			default '0'	not null, 
	 "Nemotecnico"			NVARCHAR(40)	default ''	not null,
	 PRIMARY KEY ("IdTipoOperacionTipoInstrumento")
);
	
	

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" (
	 "IdTipoOperacionYMoneda" BIGINT 		not null,
	 "IdTipoOperacion" 		NVARCHAR(6) 	default '' 	not null,
	 "IdMoneda" 			NVARCHAR(3) 	default '' 	not null,
	 "IdEstado" 			NVARCHAR(3) 	default '' 	not null,
	 PRIMARY KEY ("IdTipoOperacionYMoneda")
);
	


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.CoberturaProducto" (
	 "IdCoberturaProducto" 			BIGINT 		not null,
	 "PorcentajeCoberturaBolsa" 	DECIMAL(26,8) 	default '0' not null,
	 "PorcentajeCoberturaCorredora" DECIMAL(26,8) 	default '0' not null,
	 PRIMARY KEY ("IdCoberturaProducto")
);
	
		 

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.DiversificacionProducto" (
	 "IdDiversificacionProducto" 			BIGINT 		not null,
	 "PorcentajeDiversificacionBolsa" 		DECIMAL(26,8) 	default '0' not null,
	 "PorcentajeDiversificacionCorredora" 	DECIMAL(26,8) 	default '0' not null,
	 PRIMARY KEY ("IdDiversificacionProducto")
);
	
		 

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.ExigenciaProducto" (
	 "IdExigenciaProducto" 			BIGINT 		not null,
	 "PorcentajeExigenciaBolsa" 	DECIMAL(26,8) 	default '0' not null,
	 "PorcentajeExigenciaCorredora" DECIMAL(26,8) 	default '0' not null,
	 PRIMARY KEY ("IdExigenciaProducto")
);
		 



CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.ProductoLineaRiesgo" (
	 "IdProductoLineaRiesgo" 		BIGINT 		not null,
	 "Descripcion" 					NVARCHAR(520) 	default '' 	not null,
	 "MontoMaximoLinea" 			DECIMAL(26,8) 	default '0' not null,
	 PRIMARY KEY ("IdProductoLineaRiesgo")
);
	


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento"(
	"IdTipoRelacionInstrumento"	NVARCHAR(6)	NOT NULL,
	"TipoRelacionInstrumento"	NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdEstado"					NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
  	PRIMARY KEY ("IdTipoRelacionInstrumento")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.RelacionInstrumento" (
	"IdRelacionInstrumento" 					BIGINT 			not null,
	"IdTipoRelacionInstrumento"					NVARCHAR(6) 	default '' 				not null,
	"IdClasificacionTipoInstrumento"			NVARCHAR(3) 	default ''				not null,
	"IdTipoInstrumento"  		 				NVARCHAR(3)   	default ''				not null ,
	"IdInstrumento"								BIGINT			default '0' 			not null,
	"Nemo" 										NVARCHAR(40)	default '' 				not null,
	"IdClasificacionTipoInstrumentoRelacionado"	NVARCHAR(3) 	default ''				not null,	
	"IdTipoInstrumentoRelacionado"  			NVARCHAR(3)   	default ''				not null ,
	"IdInstrumentoRelacionado"					BIGINT			default '0' 			not null,
	"NemoRelacionado"							NVARCHAR(40)	default '' 				not null,
	"Descripcion"								NVARCHAR(100)	default '' 				not null,
	"FechaInicioRelacion"						DATE            default '0001-01-01' 	not null,
	"FechaFinRelacion"							DATE            default '0001-01-01' 	not null,
	"IdUsuario" 								BIGINT			default '0' 			not null,
	"IdEstado" 									NVARCHAR(3) 	default '' 				not null,
	 PRIMARY KEY ("IdRelacionInstrumento")
);
--
	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_IdTipoRelacionInstrumento_RelacionInstrumento" foreign key ("IdTipoRelacionInstrumento")
	        references "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_ClasificacionTipoInstrumento_RelacionInstrumento" foreign key ("IdClasificacionTipoInstrumento")
	        references "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ("IdClasificacionTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_TipoInstrumento_RelacionInstrumento" foreign key ("IdTipoInstrumento")
	        references "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_ClasificacionTipoInstrumentoRelacionado_RelacionInstrumento" foreign key ("IdClasificacionTipoInstrumentoRelacionado")
	        references "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ("IdClasificacionTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_TipoInstrumentoRelacionado_RelacionInstrumento" foreign key ("IdTipoInstrumentoRelacionado")
	        references "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_Estado_RelacionInstrumento" foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;		     
	  
	  
--CREATE TYPE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosActual" AS TABLE  ( "IdInstrumento" BIGINT CS_FIXED,


CREATE TYPE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosActual" AS TABLE  ( "IdInstrumento" BIGINT CS_FIXED,
 "Nemotecnico" NVARCHAR(40)  CS_STRING,
 "ISIN" NVARCHAR(40)  CS_STRING,
 "CorteMinimo" DECIMAL(26,8) CS_FIXED,
 "EstaInscrito" NVARCHAR  CS_STRING,
 "EsDivisible" NVARCHAR  CS_STRING,
 "RazonSocial" NVARCHAR(100)  CS_STRING,
 "Lista" NVARCHAR(520)  CS_STRING,
 "FechaEmision" DAYDATE CS_DAYDATE,
 "FechaVencimiento" DAYDATE CS_DAYDATE,
 "IdInstrumentoRelacionado" BIGINT CS_FIXED,
 "IdEmisor" NVARCHAR(40)  CS_STRING,
 "IdTipoReajuste" NVARCHAR(3)  CS_STRING,
 "Reajustable" NVARCHAR  CS_STRING,
 "IdMoneda" NVARCHAR(3)  CS_STRING,
 "IdTipoInstrumento" NVARCHAR(3)  CS_STRING,
 "IdEstado" NVARCHAR(3)  CS_STRING,
 "DecimalesPrecio" INT CS_INT,
 "NombreTipoInstrumento" NVARCHAR(100)  CS_STRING,
 "IdClasificacionTipoInstrumento" NVARCHAR(3)  CS_STRING,
 "NombreClasificacionTipoInstrumento" NVARCHAR(100)  CS_STRING,
 "RazonSocialEmisor" NVARCHAR(100)  CS_STRING,
 "IdBolsa" NVARCHAR(3)  CS_STRING,
 "IdBolsaInstrumentoPrecios" BIGINT CS_FIXED,
 "UltimoPrecioTransado" DECIMAL(26,8) CS_FIXED,
 "UltimoPrecioCompra" DECIMAL(26,8) CS_FIXED,
 "UltimoPrecioVenta" DECIMAL(26,8) CS_FIXED,
 "FechaCierre" DAYDATE CS_DAYDATE ,
 "Tamano" BIGINT CS_FIXED);


/*==============================================================*/
/* Modelo de Clasificacion de Riesgo
*/
/*==============================================================*/
create column table "BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" (
   "IdEntidadClasificadoraRiesgo"       NVARCHAR(10)   	not null,   
   "Nombre"                             NVARCHAR(100) 	default '' 	not null,
   primary key ("IdEntidadClasificadoraRiesgo")
);

create column table "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" (
   "IdTipoClasificacionRiesgo"          NVARCHAR(4)   	not null,   
   "IdClasificacionTipoInstrumento" 	NVARCHAR(3)		default '' 	not null,
   "Codigo"                             NVARCHAR(40) 	default '' 	not null,
   "Nombre"                             NVARCHAR(100) 	default '' 	not null,
   "IdEstado"                           NVARCHAR(3) 		default '' 	not null,
   primary key ("IdTipoClasificacionRiesgo")
);

create column table "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" (
   "IdClasificacionRiesgo"        	BIGINT      		not null,
   "IdTipoClasificacionRiesgo"     	NVARCHAR(4)  		not null,
   "IdEntidadClasificadoraRiesgo"  	NVARCHAR(10)   		not null,   
   "ClasificacionRiesgo"     		NVARCHAR(20) 		default '' 	not null,   
   "Nivel"                         	INTEGER 			default '0' not null,
   "IdEstado"                      	NVARCHAR(3) 		default '' 	not null,
   primary key ("IdClasificacionRiesgo")
);

-- CONFIGURACION PARA EXIGIBILIDAD, COBERTURA Y PRESTAMOS: 
-- LISTA DE TIPOS DE INSTRUMENTOS agrupados por TIPO DE CLASIFICACION de Riesgo, los cuales deteminan papeles aconsiderar como exigibilidad de una simultanea
create column table "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" (
   "IdTipoInstrumentoClasificacionRiesgo" BIGINT      not null,
   "IdTipoClasificacionRiesgo"            NVARCHAR(4) default '' 	not null,
   "IdTipoInstrumento"                    NVARCHAR(3) default '' 	not null,
   "Nivel"                                INTEGER  	  default '0' not null, -- ELIMINAR
   "IdEstado"                             NVARCHAR(3) default '' 	not null,
   primary key ("IdTipoInstrumentoClasificacionRiesgo")
);

create column table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion" (
   "IdEquivalenciaClasificacion"    BIGINT      	not null,
   "IdClasificacionRiesgoOrigen"  	BIGINT      	not null,
   "IdClasificacionRiesgoDestino"  	BIGINT      	not null,
   primary key ("IdEquivalenciaClasificacion")
);

/*==============================================================*/
/* Table: "BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio"
Define tipos de precios a utilizar segun uso o concepto.
El Servicio que entrega precios, consulta esta configuración por defecto
*/
/*==============================================================*/
create column table "BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" (
	"IdConfiguracionUsoPrecio"			BIGINT        not null,
	"CodigoUsoPrecio"					NVARCHAR(40)  not null, -- EJ: INGORD, VALCAR, VALGTA
	"IdModulo"							NVARCHAR(3)   not null, -- EJ: ORD, PLA 
	"IdTipoOperacion"					NVARCHAR(6)   not null, -- EJ: COMSIM
	"IdConcepto"						NVARCHAR(6)   not null, -- GAINRF, GAINFU, GARTFU ConceptoGarantia
    "Descripcion"						NVARCHAR(100) not null, --
	"TipoPrecioDefault" 				NVARCHAR(10)  not null, -- EJ: PROMEDIO, MERCADO, CARTERA, BASE, NOINSCRITO
	"FuentePrecioDefault"				NVARCHAR(40)  not null, -- EJ: BCS, RISK , BEC, CCLV,
	"IdClasificacionTipoInstrumento"	NVARCHAR(03)  default '' not null, -- EJ: RV
   primary key ("IdConfiguracionUsoPrecio")
);


/*==============================================================*/
/* Table: "BSCL.Plataforma.BD::INS.TareaValorizacion"
Contiene resultado de valorizacion masiva para RF
*/
/*==============================================================*/

drop TABLE BSCL."BSCL.Plataforma.BD::INS.TareaValorizacion";
CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::INS.TareaValorizacion" (
	 --- DATOS TAREA
	 "IdTarea" 						BIGINT 			not null,
	 "GrupoValorizacion"			BIGINT			not null,	 
	 "FechaSolicitud" 				SECONDDATE		not null,
 	 "IdEstado" 					NVARCHAR(3) 	not null,
	 "SoloTipificar"				NVARCHAR(1), 	 --	INDICA SI TAREA ES PARA TIPIFICAR si no es S entonces Valoriza.
	 "TipoTarea"					NVARCHAR(3), -- VAL, TIP, CUP
 	 -- DATOS VALORIZADOR
	 "RutinaValorizacion" 			NVARCHAR(4) 	not null, ----BCS  
	 "OpcionValorizacion" 			NVARCHAR(4) 	, -- COLUMNA LEGACY --Las mismas que el valorizador  
	 "TipoValorizacion"				NVARCHAR(4) 	, -- COLUMNA LEGACY --Tipo de valorizacion a usar x valorizador 

	 -- DATOS ENTRADA|SALIDA
	"TipoVal" 				BIGINT,						-- E|S	- Tipo de valorización
	"Nemo"					NVARCHAR(100) not null,		-- E|S 	- Nemotécnico
	"FechaVal"				DATE not null,				-- E|S 	- Fecha de Valorización (AAAAMMDD)
	"FechaEmi"				DATE,						-- E|S 	- Fecha de Emisión del instrumento (AAAAMMDD)
	"FechaVcto"				DATE,						-- E|S 	- Fecha de Vencimiento del instrumento (AAAAMMDD)
	"NumeroDias"			BIGINT,						-- E|S 	- N° Días al Vencimiento del instrumento
	"TasaTir"				DECIMAL(26,8),				-- E|S 	- Single Tasa Interna Retorno, Real Anual (IIF, IRF) Single => BIGDECIMAL
	"TasaTip"				DECIMAL(26,8),				-- E|S 	- Single Tasa Flotante Estimada (IIF, IRF)
	"TasaInteres"			DECIMAL(26,8),				-- E|S 	- Single Tasa Interés (IIF)
	"ValRescateP"			DECIMAL(26,8),				-- E|S 	- Valor Rescate Pesos a fecha valorización
	"ValRescateR"			DECIMAL(26,8),				-- E|S 	- Valor Rescate en U.F. a fecha valorización
	"ValCaptacionP"			DECIMAL(26,8),				-- E|S 	- Valor Captación Pesos a fecha valorización
	"ValCaptacionR"			DECIMAL(26,8),				-- E|S 	- Valor Captación U.F. a fecha valorización
	"GlosaEmisor"			NVARCHAR(100),				-- E|S 	- Descripción Emisor
	"GlosaMensaje"			NVARCHAR(100),				-- E|S 	- Glosa mensaje valorización
	"Cantidad"				DECIMAL(26,8),				-- E|S 	- Cantidad (unidades)
	"UnidadMon"				NVARCHAR(40),				-- E|S 	- Descripción Unidad Monetaria
	"Precio"				DECIMAL(26,8),				-- E|S 	- Precio del instrumento
	"MontoPeso"				DECIMAL(26,8),				-- E|S 	- Monto en Pesos a fecha valorización
	"MontoUF"				DECIMAL(26,8),				-- E|S 	- Monto en U.F. a fecha valorización
	"MontoUS"	 			DECIMAL(26,8),				-- E|S 	- Monto en US$ a fecha valorización
	"AnoVcto"				BIGINT,						-- E|S 	- Año vencimiento del instrumento
	"MesVcto"				BIGINT,						-- E|S 	- Mes vencimiento del instrumento
	"Duration"				DECIMAL(26,8),				-- E|S 	- Valor Duration Macaulay
	"ValorBase"				DECIMAL(26,8),				-- E|S 	- Valor Base
	"Convexidad"			DECIMAL(26,8),				-- E|S 	- Valor Convexividad
	"ValorPar2"				DECIMAL(26,8),				-- E|S 	- Valor Par en Pesos
	"ValorPar"				DECIMAL(26,8),				-- E|S 	- Valor Par
	"TipoTasa"				NVARCHAR(40),				-- E|S 	- Descripción Tipo Tasa
	"CalculoEst"			NVARCHAR(40),				-- E|S 	- Índice Cálculo Estimado
	"APlazo"				BIGINT,						-- E|S 	- Plazo en años del instrumento
	"ValorRM"				DECIMAL(26,8),				-- E|S 	- Valor Inflación Promedio Anual
	"ValorNom"				DECIMAL(26,8),				-- E|S 	- Valor Nominal
	"TipoMoneda"			BIGINT,						-- E|S 	- Código Tipo Moneda: 1=UF, 2=US$
	"CLiq"					NVARCHAR(40),				-- E|  	- Condición de Liquidación (PH, PM, CN)
	"Gestion"				NVARCHAR(1) default 'S',	-- E|  	- Pendiente de inyectar 
	"Estado"				NVARCHAR(40),				--  |S 	- Resultado de la consulta (0, 1, 2)
	"Glosa"					NVARCHAR(100),				--  |S 	- Descripcion
	"Hora"					NVARCHAR(40),				--  |S 	- FechaHora de la información (AAAAMMDDHHMMSSCC)
	-- DATOS CUPON
	"FechaIni"				DATE,						-- E|S 	- Pendiente documento definicion
	"FechaFin"				DATE,						-- E|S 	- Pendiente documento definicion
	"CodTipoMod"			NVARCHAR(1),				-- E| 	- Pendiente documento definicion
	"Plazo"					BIGINT,						-- E|S 	- Pendiente documento definicion		
	"CodigoEmisor"			NVARCHAR(40),				--  |S 	- Pendiente documento definicion
	"FechaEmision"			DATE,						--  |S 	- Pendiente documento definicion
	"FechaVencimiento"		DATE,						--  |S 	- Pendiente documento definicion
	"TipoMod"				NVARCHAR(40),				--  |S 	- Pendiente documento definicion
	"TipoUnidad"			NVARCHAR(40),				--  |S 	- Pendiente documento definicion -- equivale a tipo moneda en valoriz.
	"ValorReferencial"		NVARCHAR(100),				--  |S 	- Pendiente documento definicion
	"CuponAmortizacion"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponFactorAjuste"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponFechaEfectiva"	DATE,						--  |S 	- Pendiente documento definicion
	"CuponFechaHabil"		DATE,						--  |S 	- Pendiente documento definicion
	"CuponFlujo"			DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponIntereses"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponSecuencia"		BIGINT,						--  |S 	- Pendiente documento definicion
	-- DATOS SALIDA WS
	"WsCallerCodSalida"		NVARCHAR(100),				--  |S 	- WRAPPER - Salida producto invocación del WS.
	"WsCallerDescSalida" 	NVARCHAR(100),				--	|S 	- WRAPPER - Salida producto invocación del WS.
	 PRIMARY KEY ("IdTarea")
);



/*==============================================================*/
/* Constraint:                                                  */
/*==============================================================*/

/* Table: */
alter table "BSCL.Plataforma.BD::INS.Instrumento"
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

alter table "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

alter table "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
       add ("IdMoneda" NVARCHAR(3) not null default '');


alter table "BSCL.Plataforma.BD::INS.TipoInstrumento"
   add constraint "FK_ClasificacionTipoInstrumento_TipoInstrumento" foreign key ("IdClasificacionTipoInstrumento")
      references "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ("IdClasificacionTipoInstrumento") on delete restrict on update restrict;


/* Table: */
alter table "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"
   add constraint "FK_Estado_TipoClasificacionRiesgo" foreign key ("IdEstado")
      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
   add constraint "FK_Estado_TipoInstrumentoClasificacionRiesgo" foreign key ("IdEstado")
      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::INS.ClasificacionRiesgo"
   add constraint "FK_Estado_ClasificacionRiesgo" foreign key ("IdEstado")
      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
   add constraint "FK_TipoInstrumentoClasificacionRiesgo_TipoClasificacionRiesgo" foreign key ("IdTipoClasificacionRiesgo")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"  ("IdTipoClasificacionRiesgo") on delete restrict on update restrict;
      
alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
   add constraint "FK_TipoInstrumentoClasificacionRiesgo_TipoInstrumento" foreign key ("IdTipoInstrumento")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"  ("IdTipoInstrumento") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"
   add constraint "FK_ClasificacionRiesgo_TipoClasificacionRiesgo" foreign key ("IdTipoClasificacionRiesgo")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"  ("IdTipoClasificacionRiesgo") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion"
   add constraint "FK_EquivalenciaClasificacion_ClasificacionOrigen" foreign key ("IdClasificacionRiesgoOrigen")
      references "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"  ("IdClasificacionRiesgo") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion"
   add constraint "FK_EquivalenciaClasificacion_ClasificacionDestino" foreign key ("IdClasificacionRiesgoDestino")
      references "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"  ("IdClasificacionRiesgo") on delete restrict on update restrict;


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
--create index "INX01_" on "BSCL.Plataforma.BD::"("");


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit

