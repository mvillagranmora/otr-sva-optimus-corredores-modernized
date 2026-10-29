SET SCHEMA "BSCL"; 

drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_Resultado";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_InformacionDetalle";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_AdvertenciasDetalle";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_ErroresDetalle";
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::MarketData_IdInterno";

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::MarketData_IdInterno" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;



DROP TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada" (
	"IdInterno"				BIGINT,
	"action" 				VARCHAR(2),
	"timesTamp"				VARCHAR(30),
	"sellExecutingFirm" 	VARCHAR(10), 
	"sellEnteringFirm" 		VARCHAR(10), 
	"sellEnteringTrader"	VARCHAR(20),
	"buyExecutingFirm" 		VARCHAR(10), 
	"buyEnteringFirm" 		VARCHAR(10), 
	"buyEnteringTrader" 	VARCHAR(20),
	"qty" 					VARCHAR(10), 
	"symbol" 				VARCHAR(50), 
	"spotPrice" 			DECIMAL(16,4) CS_FIXED, 
	"forwardPrice" 			DECIMAL(16,4) CS_FIXED, 
	"maxRate" 				DECIMAL(16,4) CS_FIXED, --TASA INICIAL DE OFERTA
	"pubRate" 				DECIMAL(16,4) CS_FIXED, --TASA DEL PALO
	"settlementType" 		VARCHAR(10), 
	"expirationDate" 		VARCHAR(30), 
	"amount" 				INTEGER CS_INT, 
	"transactionTime" 		VARCHAR(50), 
	"transactionDay" 		VARCHAR(50), 
	"prepaid" 				VARCHAR(5), 
	"folio" 				VARCHAR(10), 
	"bestStance" 			VARCHAR(20), 
	"divisible" 			VARCHAR(15), 
	"modified" 				VARCHAR(20),
	"codIsin" 				VARCHAR(20),
	"securityType" 			VARCHAR(20),
	"securityExchange" 		VARCHAR(20),
	"orderID" 				VARCHAR(20)
	) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada"(
	"IdInterno"			BIGINT,
	"codTicker" varchar(30) NULL,					--Codigo Ticker
	"timeStamp" varchar(30) NULL,					--Hora Ticker
	"action" varchar(5) NULL,						--Accion
	"tradeTime" varchar(30) NULL,					--Hora de la Transaccion
	"tradeID" varchar(20) NULL,						--Folio
	"instrument" varchar(20) NULL,					--Instrumento
	"market" varchar(50) NULL,						--Mercado
	"settlementCondition" varchar(20) NULL,			--Condicion de Liquidacion
	"orderType" varchar(20) NULL,					--Tipo de Orden
	"indivisible" varchar(20) NULL,					--Invisible
	"shortSell" varchar(5) NULL,					--Transaccion de Venta Corta
	"quantity" decimal(20, 4) NULL,					--Cantidad
	"price" decimal(20, 4) NULL,					--Precio
	"amount" decimal(20, 4) NULL,					--Monto
	"closingPrice" decimal(20, 4) NULL,				--precio de Cierre
	"buyerBroker" varchar(20) NULL,					--Corredor Comprador
	"buyEnteringFirm" varchar(20) NULL,
	"buyEnteringTrader" varchar(20) NULL,
	"sellerBroker" varchar(20) NULL,				--Corredor Vendedor
	"sellEnteringFirm" varchar(20) NULL,
	"sellEnteringTrader" varchar(20) NULL,
	"priceBuyingQTY" decimal(20, 4) NULL,			--Compra Cantidad
	"priceBuyingPrice" decimal(20, 4) NULL,			--Mejor Precio Compra 
	"priceSellingQty" decimal(20, 4) NULL,			--Venta Cantidad
	"priceSellingPrice" decimal(20, 4) NULL,		--Mejor Precio Venta
	"codIsin" varchar(20) NULL,
	"securityType" varchar(20) NULL,
	"timeInForce" varchar(20) NULL,
	"tradingSessionID" varchar(20) NULL,
	"entryType" varchar(20) NULL,
	"priceExchange" varchar(20) NULL,
	"priceTime" varchar(20) NULL
) ON COMMIT DELETE ROWS
;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada"(
	"IdInterno"					BIGINT,
	"codTicker"					NVARCHAR(2),
	"timeTicker"				NVARCHAR(16),
	"action"					NVARCHAR(1),
	"tradeTime"					NVARCHAR(20),
	"tradeID"					NVARCHAR(10),
	"symbol"					NVARCHAR(10),
	"condIsin"					NVARCHAR(15),
	"quantity"					DECIMAL(15,2),
	"buyExecutingFirm"			NVARCHAR(3),
	"sellExecutingFirm"			NVARCHAR(3),
	"buyEnteringFirm"			NVARCHAR(3),
	"sellEnteringFirm"			NVARCHAR(3),
	"tradeCondition"			NVARCHAR(15),
	"condition"					NVARCHAR(15),
	"amount"					DECIMAL(19,4),
	"term"						NVARCHAR(10),
	"coraMaturity"				NVARCHAR(9),
	"price"						DECIMAL(19,4),
	"auctionId"					NVARCHAR(3),
	"tir"						DECIMAL(9,4),
	"signTir"					NVARCHAR(1),
	"calculationType"			NVARCHAR(1),
	"traderBuy"					NVARCHAR(3),
	"traderSell"				NVARCHAR(3),
	"dcvTitle"					NVARCHAR(1),
	"settlDate"					NVARCHAR(10),
	"valAdjustmentCurrency"		NVARCHAR(3),
	"currency"					NVARCHAR(3),
	"currencySettlementAmount"	DECIMAL(19,4),
	"duration"					DECIMAL(19,4),
	"family"					NVARCHAR(10),
	"buyerFund"					NVARCHAR(3),
	"sellerFund"				NVARCHAR(3)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada"(
	"IdInterno"					BIGINT,
	"CODTICKER"					NVARCHAR(2),
	"TIMETICKER"				NVARCHAR(16),
	"ACTION"					NVARCHAR(1),
	"TRADETIME"					NVARCHAR(20),
	"TRADEID"					NVARCHAR(10),
	"SYMBOL"					NVARCHAR(10),
	"CONDISIN"					NVARCHAR(15),
	"BUYEXECUTINGFIRM"			NVARCHAR(3),
	"SELLEXECUTINGFIRM"			NVARCHAR(3),
	"BUYENTERINGFIRM"			NVARCHAR(3),
	"SELLENTERINGFIRM"			NVARCHAR(3),
	"TRADECONDITION"			NVARCHAR(15),
	"CONDITION"					NVARCHAR(15),
	"ISSUER"					NVARCHAR(10),
	"FAMILY"					NVARCHAR(2),
	"ENTERINGDATE"				NVARCHAR(8),
	"EXPIRETIME"				NVARCHAR(8),
	"LAMINA"					NVARCHAR(1),
	"AMOUNT"					DECIMAL(19,4),
	"TERM"						NVARCHAR(10),
	"AUCTIONID"					NVARCHAR(3),
	"RATE"						DECIMAL(9,4),
	"SIGNRATE"					NVARCHAR(1),
	"RESETTYPE"					VARCHAR(2),
	"RESCUEVALUE"				DECIMAL(19,4),
	"TRADERBUY"					NVARCHAR(3),
	"TRADERSELL"				NVARCHAR(3),
	"DCVTITLE"					NVARCHAR(1),
	"SETTLDATE"					NVARCHAR(10),
	"SYMBOLSVS"					NVARCHAR(15),
	"LOCALAMOUNT"				DECIMAL(19,4),
	"BUYERFUND"					NVARCHAR(3),
	"SELLERFUND"				NVARCHAR(3)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_Resultado"(
	"IdInterno"				BIGINT,
    "IdTransaccion"			BIGINT
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_InformacionDetalle"(
	"IdInterno" BIGINT,
	"IdOperacion" BIGINT,
	"Codigo" nvarchar(40),
	"Descripcion" nvarchar(520)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_AdvertenciasDetalle"(
	"IdInterno" BIGINT,
	"IdOperacion" BIGINT,
	"Codigo" nvarchar(40),
	"RequiereAutorizacion" nvarchar(40),
	"Descripcion" nvarchar(520)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_ErroresDetalle"(
	"IdInterno" BIGINT,
	"IdOperacion" BIGINT,
	"Codigo" nvarchar(40),
	"Descripcion" nvarchar(520)

) ON COMMIT DELETE ROWS;

commit

 
 

 

 

 





 






 

 
 

