SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Orden.BD::MRV.INDICES_FUTUROS"; 		--MDC_TX_INDICES_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_FUTUROS"; 	--MDC_TX_INSTRUMENTOS_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.PALOS_FUTUROS"; 			--MDC_TX_PALOS_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.PUNTAS_FUTUROS"; 			--MDC_TX_PUNTAS_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.RESUMEN_FUTUROS"; 		--MDC_TX_RESUMEN_FUTUROS

DROP TABLE "BSCL.Orden.BD::MRV.PUNTAS_MONETARIOS_RV"; 	--MDC_TX_PUNTAS_MONETARIOS_RV

DROP TABLE "BSCL.Orden.BD::MRV.RESUMEN_RV"; 				--MDC_TX_RESUMEN_RV

DROP TABLE "BSCL.Orden.BD::MRV.OFERTAS_SERIALIZADOS"; 	--MDC_TX_OFERTAS_SERIALIZADOS
DROP TABLE "BSCL.Orden.BD::MRV.POSTURAS_SERIALIZADOS"; 	--MDC_TX_POSTURAS_SERIALIZADOS

DROP TABLE "BSCL.Orden.BD::MRV.INDICES_RV";
CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.INDICES_RV" (
	"CODTICKER" VARCHAR(10), 
	"TIMESTAMP" VARCHAR(30), 
	"ACTION" VARCHAR(2), 
	"TIMESTRADE" VARCHAR(30), 
	"NEMO" VARCHAR(50), 
	"INDICE" VARCHAR(50), 
	"VOLUME" DECIMAL(20,3) CS_FIXED, 
	"PRICE" DECIMAL(20,10) CS_FIXED, 
	"VARIATION" DECIMAL(10,2) CS_FIXED) 
UNLOAD PRIORITY 5  AUTO MERGE;

DROP TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV";
CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV" (
	"CODTICKER" varchar(15) NULL,			--Codigo Ticker
	"TIMESTAMP"  varchar(30) NULL,			--Hora Ticker
	"ACTION"  varchar(10) NULL,				--Accion
	"INSTRUMENTS"  varchar(30) NULL,		--Instrumento
	"CODISIN"  varchar(20) NULL,				--Mercado
	"MARKET"  varchar(30) NULL,				--Mercado
	"MINPRICE"  decimal(20, 4) NULL,		--Precio Minimo 
	"OPENPRICE"  decimal(20, 4) NULL,		--Precio de Apertura
	"HIGHPRICE"  decimal(20, 4) NULL,		--Precio Maximo
	"IMBALANCE"  decimal(15, 2) NULL,		--Imbalance
	"TRADEVOLUME"  decimal(20, 4) NULL,		--Cantidad Ultimo Calse.
	"AVGPRICE"  decimal(20, 4) NULL,		--Precio Medio
	"TRADEAMOUNT"  decimal(30, 4) NULL,		--Monto del Comercio
	"CLOSINGPRICE"  decimal(20, 4) NULL,	--Precio de Cierre
	"TREND"  varchar(10) NULL,				--Tendencia
	"DCV" varchar(10) NULL					--Estado en la DCV
);


DROP TABLE "BSCL.Orden.BD::MRV.PALOS_RV";
CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PALOS_RV" (

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
);

DROP TABLE "BSCL.Orden.BD::MRV.PUNTAS_RV"; 				
CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PUNTAS_RV" (
	"codTicker" varchar(10) NULL,			--Codigo Ticker
	"timesTamp" varchar(30) NULL,			--Hora Ticker
	"action" varchar(3) NULL,				--Accion
	"symbol" varchar(50) NOT NULL,			--Instrumentos
	"volume" decimal(20, 4) NULL,			--Cantidad de Compra
	"amountSale" decimal(20, 4) NULL,		--Cantidad de Venta
	"price" decimal(20, 4) NULL,			--Precio de Compra
	"priceSale" decimal(20, 4) NULL,		--Precio de Venta
	"settlement" varchar(20) NULL,			--Condicion de Liquidacion
	"entryPositionNo" int NULL,
	"bookingRefID" varchar(3) NULL,
	"buyExecutingFirm" varchar(6) NULL,		--Corredora
	"buyEnteringFirm" varchar(6) NULL,		--Operador Directo
	"buyEnteringTrader" varchar(6) NULL,
	"buyClientID" varchar(30) NULL,
	"sellExecutingFirm" varchar(6) NULL,	--Corredora
	"sellEnteringFirm" varchar(6) NULL,		--Operador Directo
	"sellEnteringTrader" varchar(6) NULL,
	"sellClientID" varchar(30) NULL,
	"codIsin" varchar(30) NULL,
	"securityType" varchar(6) NULL,
	"securityExchange" varchar(6) NULL,
	"orderID" varchar(20) NULL
);

DROP TABLE "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS";
CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" (
	"action" 				VARCHAR(2), 
	"timesTamp" 			varchar(30) NULL,			--Hora Ticker
	"sellExecutingFirm" 	VARCHAR(10), 
	"sellEnteringFirm" 		VARCHAR(10), 
	"sellEnteringTrader"	VARCHAR(20),
	"buyExecutingFirm" 		VARCHAR(10), 
	"buyEnteringFirm" VARCHAR(10), 
	"buyEnteringTrader" VARCHAR(20),
	"qty" VARCHAR(10), 
	"symbol" VARCHAR(50), 
	"spotPrice" DECIMAL(16,4) CS_FIXED, 
	"forwardPrice" DECIMAL(16,4) CS_FIXED, 
	"maxRate" DECIMAL(16,4) CS_FIXED, 
	"pubRate" DECIMAL(16,4) CS_FIXED, 
	"settlementType" VARCHAR(10), 
	"expirationDate" VARCHAR(30), 
	"amount" INTEGER CS_INT, 
	"transactionTime" VARCHAR(50), 
	"transactionDay" VARCHAR(50), 
	"prepaid" VARCHAR(5), 
	"folio" VARCHAR(10), 
	"bestStance" VARCHAR(20), 
	"divisible" VARCHAR(15), 
	"modified" VARCHAR(20),
	"codIsin" VARCHAR(20),
	"securityType" VARCHAR(20),
	"securityExchange" VARCHAR(20),
	"orderID" VARCHAR(20)
	);

----------------------------

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.OFERTAS_SERIALIZADOS" (
	"action" VARCHAR(10), 
	"orderID" VARCHAR(50), 
	"executingFirm" VARCHAR(10), 
	"enteringFirm" VARCHAR(10), 
	"clientID" VARCHAR(15), 
	"qty" DECIMAL(16,4) CS_FIXED, 
	"symbol" VARCHAR(50), 
	"spotPrice" DECIMAL(16,4) CS_FIXED, 
	"forwardPrice" DECIMAL(16,4) CS_FIXED, 
	"maxRate" DECIMAL(16,4) CS_FIXED, 
	"settlementType" VARCHAR(10), 
	"expirationDate" VARCHAR(20), 
	"amount" DECIMAL(17,4) CS_FIXED, 
	"modified" VARCHAR(15), 
	"prepaid" VARCHAR(10), 
	"divisible" VARCHAR(15), 
	"od" INTEGER CS_INT) 
UNLOAD PRIORITY 5  AUTO MERGE;


CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.POSTURAS_SERIALIZADOS" (
	"action" VARCHAR(10), 
	"orderID" VARCHAR(50), 
	"executingFirm" VARCHAR(10), 
	"enteringFirm" VARCHAR(10), 
	"clientID" VARCHAR(15), 
	"qty" DECIMAL(16,4) CS_FIXED, 
	"symbol" VARCHAR(50), 
	"spotPrice" DECIMAL(16,4) CS_FIXED, 
	"forwardPrice" DECIMAL(16,4) CS_FIXED, 
	"maxRate" DECIMAL(16,4) CS_FIXED, 
	"settlementType" VARCHAR(10), 
	"expirationDate" VARCHAR(50), 
	"amount" INTEGER CS_INT, 
	"transactionTime" VARCHAR(50), 
	"publicRate" DECIMAL(16,4) CS_FIXED, 
	"ownRate" DECIMAL(16,4) CS_FIXED, 
	"od" VARCHAR(5), 
	"prepaid" VARCHAR(20), 
	"bestStance" VARCHAR(20), 
	"divisible" VARCHAR(20)) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PUNTAS_MONETARIOS_RV" (
	"codTicker" VARCHAR(10), 
	"timesTamp" VARCHAR(30), 
	"action" VARCHAR(3), 
	"symbol" VARCHAR(50) NOT NULL , 
	"volume" DECIMAL(20,10) CS_FIXED, 
	"amountSale" DECIMAL(20,10) CS_FIXED, 
	"price" DECIMAL(20,10) CS_FIXED, 
	"priceSale" DECIMAL(20,10) CS_FIXED, 
	"settlement" VARCHAR(20) NOT NULL , 
	"campoInterno" VARCHAR(50), 
	"entryPositionNo" INTEGER CS_INT, 
	"bookingRefID" VARCHAR(3), 
	"executingFirm" VARCHAR(6), 
	"enteringFirm" VARCHAR(6), 
	"executingTrader" VARCHAR(6), 
	"enteringTrader" VARCHAR(6)) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.RESUMEN_RV" (
	"codTicker" VARCHAR(30), 
	"timeTicker" VARCHAR(30), 
	"instrument" VARCHAR(30), 
	"instrumentMarket" VARCHAR(30),
	"stockMarket" VARCHAR(30), 
	"statusDCV" VARCHAR(30), 
	"avgPrice" VARCHAR(30)) 
UNLOAD PRIORITY 5  AUTO MERGE; 

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.INDICES_FUTUROS" (
	"codTicker" VARCHAR(10), 
	"timestamp" VARCHAR(30), 
	"action" VARCHAR(2), 
	"timesTrade" VARCHAR(30), 
	"nemo" VARCHAR(50), 
	"indice" VARCHAR(50), 
	"volume" DECIMAL(20,4) CS_FIXED, 
	"price" DECIMAL(20,4) CS_FIXED, 
	"variation" DECIMAL(10,2) CS_FIXED) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_FUTUROS" (
	"codTicker" VARCHAR(15), 
	"timestamp" VARCHAR(30), 
	"action" VARCHAR(10), 
	"instruments" VARCHAR(30), 
	"market" VARCHAR(30), 
	"minPrice" DECIMAL(20,4) CS_FIXED, 
	"openPrice" DECIMAL(20,4) CS_FIXED, 
	"hignPrice" DECIMAL(20,4) CS_FIXED, 
	"imbalance" DECIMAL(15,2) CS_FIXED, 
	"tradeVolume" DECIMAL(20,4) CS_FIXED, 
	"avgPrice" DECIMAL(20,4) CS_FIXED, 
	"tradeAmount" DECIMAL(30,4) CS_FIXED, 
	"trend" VARCHAR(10)) 
UNLOAD PRIORITY 5  AUTO MERGE ;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PALOS_FUTUROS" (
	"codTicker" VARCHAR(30), 
	"TimeStamp" VARCHAR(30), 
	"action" VARCHAR(30), 
	"TradeTime" VARCHAR(30), 
	"tradeID" VARCHAR(20), 
	"instrument" VARCHAR(20), 
	"market" VARCHAR(50), 
	"settlement" VARCHAR(20),
	"quantity" INTEGER CS_INT, 
	"price" DECIMAL(20,4) CS_FIXED, 
	"amount" DECIMAL(20,4) CS_FIXED, 
	"closingPrice" DECIMAL(20,4) CS_FIXED, 
	"buyerBroker" VARCHAR(20), 
	"sellerBroker" VARCHAR(20), 
	"BuyingQTY" INTEGER CS_INT, 
	"priceBuyingPrice" DECIMAL(20,4) CS_FIXED, 
	"SellingQty" INTEGER CS_INT, 
	"priceSellingPrice" DECIMAL(20,4) CS_FIXED, 
	"tradeCondition" VARCHAR(5), 
	"entryBuyer" VARCHAR(30), 
	"entrySeller" VARCHAR(5), 
	"orderID" VARCHAR(30)) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PUNTAS_FUTUROS" (
	"codTicker" VARCHAR(10), 
	"timesTamp" VARCHAR(30), 
	"action" VARCHAR(3), 
	"Symbol" VARCHAR(50) NOT NULL , 
	"volume" DECIMAL(20,4) CS_FIXED,
	"amountSale" DECIMAL(20,4) CS_FIXED, 
	"price" DECIMAL(20,4) CS_FIXED, 
	"priceSale" DECIMAL(20,4) CS_FIXED, 
	"settlement" VARCHAR(20), 
	"bookingRefID" VARCHAR(3), 
	"executingFirm" VARCHAR(6), 
	"enteringFirm" VARCHAR(6), 
	"executingTrader" VARCHAR(6), 
	"executingFirmOffer" VARCHAR(6), 
	"enteringFirmOffer" VARCHAR(6), 
	"executingTraderOffer" VARCHAR(6)) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.RESUMEN_FUTUROS" (
	"codTicker" VARCHAR(10), 
	"timeTicker" VARCHAR(30), 
	"Instrument" VARCHAR(30), 
	"instrumentMarket" VARCHAR(10), 
	"stockMarket" VARCHAR(10), 
	"statusDCV" VARCHAR(10), 
	"avgPrice" VARCHAR(30)) 
UNLOAD PRIORITY 5  AUTO MERGE;





