SET SCHEMA "BSCL";


/*****************************************************************/
---	"BSCL.Contabilidad.BD.Servicios::ITZ.CentralizaGenerico"
/*****************************************************************/
DROP TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_PROC_Movimiento";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_PROC_Movimiento"(
	 "IdComprobante"	BIGINT
	,"IdMovimiento"		BIGINT 
) ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_PROC_DatosMovimiento";
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_PROC_DatosMovimiento"(
	 "IdMovimiento"		BIGINT
	,"IdTipoDatoEvento"	NVARCHAR(40)
	,"ValorVarChar"		NVARCHAR(60)
	,"ValorDecimal"		DECIMAL(26,8)
	,"Estado"			NVARCHAR(1)
	,"DatoValido"		NVARCHAR(1)	 
) ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_OUT_TablaCodigosMensajes";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_OUT_TablaCodigosMensajes"(
	 "IdInterno" 			BIGINT
	,"IdSesion"				BIGINT
	,"Codigo" 				NVARCHAR(100)
	,"CodigoDato1" 			NVARCHAR(100)
	,"ValorDato1" 			NVARCHAR(520)
	,"CodigoDato2" 			NVARCHAR(100)
	,"ValorDato2" 			NVARCHAR(520)
	,"Resultado" 			NVARCHAR(4) 
) ON COMMIT DELETE ROWS;


/*****************************************************************/
---	"BSCL.Contabilidad.BD.Servicios::ITZ.CentralizaMultiSeleccion"
/*****************************************************************/
DROP TABLE "BSCL.Contabilidad.BD::GBL.CentralizaMultiSeleccion_SRV_TablaCodigosMensajes";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Contabilidad.BD::GBL.CentralizaMultiSeleccion_SRV_TablaCodigosMensajes"(
	 "IdInterno" 			BIGINT
	,"IdSesion"				BIGINT
	,"Codigo" 				NVARCHAR(100)
	,"CodigoDato1" 			NVARCHAR(100)
	,"ValorDato1" 			NVARCHAR(520)
	,"CodigoDato2" 			NVARCHAR(100)
	,"ValorDato2" 			NVARCHAR(520)
	,"Resultado" 			NVARCHAR(4)	
) ON COMMIT DELETE ROWS;


/*****************************************************************/
---	"BSCL.Contabilidad.BD.Servicios::ITZ.GeneraPreComprobante"
/*****************************************************************/
DROP TABLE "BSCL.Contabilidad.BD::GBL.GeneraPreComprobante_SRV_TablaCodigosMensajes";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Contabilidad.BD::GBL.GeneraPreComprobante_SRV_TablaCodigosMensajes"(
	 "IdInterno" 			BIGINT
	,"Codigo" 				NVARCHAR(100)
	,"CodigoDato1" 			NVARCHAR(100)
	,"ValorDato1" 			NVARCHAR(520)
	,"CodigoDato2" 			NVARCHAR(100)
	,"ValorDato2" 			NVARCHAR(520)
	,"Resultado" 			NVARCHAR(4)
	,"GuardaLog"			NVARCHAR(1)
	,"GlosaLog"				NVARCHAR(5000)
) ON COMMIT DELETE ROWS;


/*****************************************************************/
---	"BSCL.Contabilidad.BD.Servicios::ITZ.GeneraPreComprobanteMultiSeleccion"
/*****************************************************************/
DROP TABLE "BSCL.Contabilidad.BD::GBL.GeneraPreComprobanteMultiSeleccion_SRV_ResultadoProceso";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Contabilidad.BD::GBL.GeneraPreComprobanteMultiSeleccion_SRV_ResultadoProceso"(
	"IdInterno" 			BIGINT, 
	"Codigo" 				NVARCHAR(100),
	"RequiereAutorizacion" 	NVARCHAR(40),
	"Descripcion"			NVARCHAR(5000),
	"Resultado" 			NVARCHAR(40)
) ON COMMIT DELETE ROWS;
												

COMMIT;
