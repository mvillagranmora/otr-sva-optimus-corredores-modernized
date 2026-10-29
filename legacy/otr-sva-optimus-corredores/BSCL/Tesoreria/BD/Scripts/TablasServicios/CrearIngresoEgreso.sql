SET SCHEMA "BSCL";

-- Entrada
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"     ;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"     			;

-- Proceso
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"		;

-- Salida
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado"     ;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso"      	;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle"      	;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle"      	;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"      	   	;



CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
   LIKE "BSCL.Tesoreria.BD.Tipos::TES.IngresoEgresoEditar"  ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
   LIKE "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago"  ON COMMIT DELETE ROWS;
  
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"
	LIKE "BSCL.Tesoreria.BD.Tipos::TES.TablaCodigosMensajes" ON COMMIT DELETE ROWS;

  

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado"
(
	"NumeroReferencia"		BIGINT,
	"IdIngresoEgreso" 		BIGINT,
	"IdTipoMovimiento" 		NVARCHAR(6)
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso"
(
	"Campo" 				VARCHAR(40),
	"Id" 					BIGINT, 
	"Servicio" 				VARCHAR(250), 
	"Resultado"				VARCHAR(40)
)  ON COMMIT DELETE ROWS;



CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle"
(
	"NumeroReferencia" 		BIGINT, 
	"Id" 					BIGINT,
	"Codigo" 				NVARCHAR(100),
	"Descripcion" 			NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle"
(
	"NumeroReferencia" 		BIGINT, 
	"Id" 					BIGINT, 
	"Codigo" 				NVARCHAR(100),
	"RequiereAutorizacion" 	NVARCHAR(40),
	"Descripcion" 			NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"
(
	"NumeroReferencia" 		BIGINT, 
	"Id" 					BIGINT, 
	"Codigo" 				NVARCHAR(100),
	"Descripcion" 			NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;



commit

 
	

