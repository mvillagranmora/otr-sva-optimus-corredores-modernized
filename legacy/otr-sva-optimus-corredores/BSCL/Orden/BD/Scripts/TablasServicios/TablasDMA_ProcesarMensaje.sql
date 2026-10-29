SET SCHEMA "BSCL"; 

drop table "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada";
DROP TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Proceso_Bitacora";
drop table "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_InformacionDetalle";
drop table "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_AdvertenciasDetalle";
drop table "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_ErroresDetalle";
drop table "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_Resultado";
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::DMA_IdInterno";

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::DMA_IdInterno" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Proceso_Bitacora"(
	"NombreEntidad"				NVARCHAR(100),
	"IdOrden"					BIGINT,
	"IdAsignacion"				BIGINT,
	"IdTransaccion"				BIGINT,
	"MensajeEventoBitacora"		NVARCHAR(5000),
	"IdTipoEventoBitacora"		NVARCHAR(40)
) ON COMMIT DELETE ROWS;


      
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_Resultado"(
	"IdInterno"		BIGINT,
	"Resultado"		NVARCHAR
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada"(
	"IdInterno"				BIGINT,
	"NRO_ENTRADA" 			DECIMAL(26,8), 
	"FOLIO_TRANS" 			NVARCHAR(20), 
	"FECHA" 				NVARCHAR(10), 
	"NEMO" 					NVARCHAR(10), 
	"SEC_ORDEN" 			DECIMAL(26,8), 
	"CANTIDAD" 				DECIMAL(26,8), 
	"PRECIO" 				DECIMAL(26,8), 
	"CAN_ASIG_ACUM" 		DECIMAL(26,8), 
	"PRECIO_PROM" 			DECIMAL(26,8), 
	"MENSAJE" 				NVARCHAR(255), 
	"ESTADO" 				NVARCHAR(2), 
	"PROCESADO" 			NVARCHAR(1), 
	"IN_SGC" 				NVARCHAR(23), 
	"MONEDA" 				NVARCHAR(3), 
	"BOLSA" 				NVARCHAR(10), 
	"CONDICION_LIQUIDACION" NVARCHAR(10), 
	"SPONSORING_FIRM" 		NVARCHAR(3), 
	"EXEC_ID" 				NVARCHAR(50), 
	"ORDER_ID"				NVARCHAR(32), 
	"MERCADO" 				NVARCHAR(2), 
	"SUB_MERCADO" 			NVARCHAR(2), 
	"PREPAGO" 				NVARCHAR(1), 
	"TASA_MAXIMA" 			DECIMAL(26,8), 
	"TASA_PROPIA" 			DECIMAL(26,8), 
	"TASA_ADJUDICACION" 	DECIMAL(26,8), 
	"MEJOR_POSTURA" 		NVARCHAR(1), 
	"ID_REFERENCIA" 		NVARCHAR(32),
	"FECHA_VENCIMIENTO" 	NVARCHAR(32), 
	"DIVISIBLE" 			NVARCHAR(1), 
	"OD" 					NVARCHAR(1), 
	"HORA_ADJUDICACION" 	NVARCHAR(32), 
	"SEC_ORDER_OD" 			DECIMAL(26,8), 
	"TIPO_OPERAC" 			NVARCHAR(1), 
	"OP_INTERNO" 			NVARCHAR(3), 
	"SEC_ORDEN_2" 			NVARCHAR(32), 
	"ID_CLIENTE" 			NVARCHAR(32) 
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_InformacionDetalle"(
	"IdInterno" BIGINT,
	"IdOperacion" BIGINT,
	"Codigo" nvarchar(40),
	"Descripcion" nvarchar(520)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_AdvertenciasDetalle"(
	"IdInterno" BIGINT,
	"IdOperacion" BIGINT,
	"Codigo" nvarchar(40),
	"RequiereAutorizacion" nvarchar(40),
	"Descripcion" nvarchar(520)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_ErroresDetalle"(
	"IdInterno" BIGINT,
	"IdOperacion" BIGINT,
	"Codigo" nvarchar(40),
	"Descripcion" nvarchar(520)

) ON COMMIT DELETE ROWS;

commit

 
 

 

 

 





 






 

 
 

