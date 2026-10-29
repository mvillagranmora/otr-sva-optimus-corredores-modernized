SET SCHEMA "BSCL";


-- Entrada
DROP TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes"		cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Proceso_TablaCodigosMensajes"
	LIKE "BSCL.Tesoreria.BD.Tipos::TES.TablaCodigosMensajes" ON COMMIT DELETE ROWS;


-- Salida
DROP TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_InformacionDetalle"		cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_InformacionDetalle"
(
	"Codigo" 					NVARCHAR(100),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_AdvertenciasDetalle"     	cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_AdvertenciasDetalle"
(	
	"Codigo" 					NVARCHAR(100),
	"RequiereAutorizacion" 		NVARCHAR(40),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle"     		cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle"
(	
	"Codigo" 					NVARCHAR(100),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;
	

COMMIT;