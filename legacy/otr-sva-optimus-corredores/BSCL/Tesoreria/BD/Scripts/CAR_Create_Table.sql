-- MODELO CARGA MASIVA DE DATOS (Ej.: POR RECAUDACION)

SET SCHEMA "BSCL";


/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::CAR.CargaMasiva" CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::CAR.ConfiguracionCargaMasiva" CASCADE;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" (
	"IdTipoCargaMasiva"		BIGINT     		NOT NULL,
	"Codigo"				NVARCHAR(40)    NOT NULL DEFAULT '',
	"Nombre"				NVARCHAR(100)   NOT NULL DEFAULT '',
	"IdEmpresa"  			BIGINT 			NOT NULL DEFAULT 0,
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdTipoCargaMasiva"));


-- Maestro historico de cargas
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::CAR.CargaMasiva" (
	"IdCargaMasiva"			BIGINT     		NOT NULL,
	"IdTipoCargaMasiva"     BIGINT     		NOT NULL DEFAULT 0,
	"IdEmpresa"  			BIGINT 			NOT NULL DEFAULT 0,
	"IdUsuario" 			BIGINT 			NOT NULL DEFAULT 0,
	"CantidadRegistros"     INTEGER         NOT NULL DEFAULT 0,	
	"MontoTotal"			DECIMAL(26,8)   NOT NULL DEFAULT 0,	
	"NombreArchivo"         NVARCHAR(100)   NOT NULL DEFAULT '',
	"FechaCarga" 			SECONDDATE      NOT NULL DEFAULT '0001-01-01',        -- Fecha de sistema
    "FechaProceso" 			SECONDDATE      NOT NULL DEFAULT '0001-01-01',        -- Fecha en que se proceso la CM
  	"FechaIngresoRegistro"	SECONDDATE      NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- Fecha de servidor BD
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdCargaMasiva"));


-- Detalle historico de cargas
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" (
	"IdCargaMasivaDetalle"				BIGINT     		NOT NULL,
	"IdCargaMasiva"						BIGINT     		NOT NULL DEFAULT 0,
    "IdTipoMovimiento"					NVARCHAR(6)		NOT NULL DEFAULT '', -- MOVING: Ingreso   MOVEGR: Egreso
	"DocumentoIdentidad" 				NVARCHAR(40)	NOT NULL DEFAULT '',
	"Monto" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
	"IdMoneda" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
	"ObservacionValidacion"             NVARCHAR(520)   NOT NULL DEFAULT '',
	"IdEstado"							NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdCargaMasivaDetalle"));



CREATE COLUMN TABLE "BSCL.Tesoreria.BD::CAR.ConfiguracionCargaMasiva" (
	"IdConfiguracionCargaMasiva"		BIGINT     		NOT NULL,
	"CodigoConfiguracionCargaMasiva"	NVARCHAR(40)    NOT NULL DEFAULT '',		
	"IdTipoCargaMasiva"					BIGINT     		NOT NULL DEFAULT 0,
	"IdPersonaEntidadRecaudadora"		BIGINT 			NOT NULL DEFAULT 0,
	"IdConcepto" 						NVARCHAR(6) 	NOT NULL DEFAULT '',
    "IdCuentaBanco"						BIGINT     		NOT NULL DEFAULT 0, -- Cuenta en la que la entidad recaudadora hace el deposito
 	"TieneMovimientoReferencia"  		NVARCHAR(1)     NOT NULL DEFAULT 'N',
 	"IdMedioDePago" 					NVARCHAR(3) 	NOT NULL DEFAULT '',
 	"CodigoArchivoCarga"    			NVARCHAR(50)    NOT NULL DEFAULT '',   -- "BSCL.Plataforma.BD::PLA.Archivo"
	"ProfundidadProcesoCarga"          	NVARCHAR(3)     NOT NULL DEFAULT '',  -- MOV / COM / LIQ -- hasta donde llega el proceso de carga
	"IdEmpresa"  						BIGINT 			NOT NULL DEFAULT 0,
	"IdEstado"							NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdConfiguracionCargaMasiva"));


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/


/* Table: */
	


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
--create index "INX01_" on "BSCL.Plataforma.BD::GTA.("");


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
Commit
--Rollback


