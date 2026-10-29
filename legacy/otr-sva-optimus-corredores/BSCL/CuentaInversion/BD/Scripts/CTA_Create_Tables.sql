SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" 		        CASCADE;
DROP TABLE "BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle" 		        CASCADE;
DROP TABLE "BSCL.CuentaInversion.BD::CAR.CargaMasiva" 	            		CASCADE;--CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos



/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
--CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
-- Maestro historico de cargas CuentaInversion
CREATE COLUMN TABLE "BSCL.CuentaInversion.BD::CAR.CargaMasiva" (
	"IdCargaMasiva"			BIGINT     		NOT NULL,
	"CodigoTipoCargaMasiva" NVARCHAR(40)    NOT NULL DEFAULT '',
	"IdEmpresa"  			BIGINT 			NOT NULL DEFAULT 0,
	"IdUsuario" 			BIGINT 			NOT NULL DEFAULT 0,
	"CantidadRegistros"     INTEGER         NOT NULL DEFAULT 0,	
	"MontoTotal"			DECIMAL(26,8)   NOT NULL DEFAULT 0,	
	"NombreArchivo"         NVARCHAR(100)   NOT NULL DEFAULT '',
	"FechaCarga" 			SECONDDATE      NOT NULL DEFAULT '0001-01-01',        -- Fecha de sistema
    "FechaProceso" 			SECONDDATE      NOT NULL DEFAULT '0001-01-01',        -- Fecha en que se proceso la CM
  	"FechaIngresoRegistro"	SECONDDATE      NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- Fecha de servidor BD
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT '',
	"IdMoneda" 				NVARCHAR(3) 	NOT NULL DEFAULT '',
	"IdConcepto" 			NVARCHAR(6) 	NOT NULL DEFAULT '',	
PRIMARY KEY ("IdCargaMasiva"));

-- Maestro historico de detalle de cargas CuentaInversion
CREATE COLUMN TABLE BSCL."BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle" (
	"IdCargaMasivaDetalle" BIGINT NOT NULL,
	"IdCargaMasiva" BIGINT DEFAULT 0 NOT NULL,
	"DocumentoIdentidad" NVARCHAR(40) NOT NULL,
	"IdPortafolio" BIGINT DEFAULT 0 NOT NULL,
	"IdMoneda" NVARCHAR(3) NOT NULL,	
	"IdTipoMovimiento" NVARCHAR(6) NOT NULL,
	"IdConcepto" NVARCHAR(6) NOT NULL,	
	"Monto" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"Descripcion" NVARCHAR(520) NOT NULL,	
	"IdEstado" NVARCHAR(3) NOT NULL,
	"ObservacionValidacion" NVARCHAR(520) NOT NULL,	
	PRIMARY KEY ("IdCargaMasivaDetalle")
);

CREATE TABLE "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" (
	"IdTipoCargaMasiva" BIGINT NOT NULL,
	"CodigoTipoCargaMasiva" NVARCHAR(40) NOT NULL,
	"Nombre" NVARCHAR(100) NOT NULL,
	"IdEmpresa" BIGINT DEFAULT 0 NOT NULL,
	"IdEstado" NVARCHAR(3) NOT NULL,
	"IdConcepto" NVARCHAR(6) NOT NULL,
	PRIMARY KEY ("IdTipoCargaMasiva"));

--CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos

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
COMMIT;
