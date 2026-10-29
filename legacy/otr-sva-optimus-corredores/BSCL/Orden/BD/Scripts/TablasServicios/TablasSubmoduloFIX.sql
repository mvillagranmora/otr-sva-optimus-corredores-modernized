SET SCHEMA "BSCL"; 
DROP TABLE "BSCL.Orden.BD::DMA.FIX_EVENTLOG";
DROP TABLE "BSCL.Orden.BD::DMA.FIX_MESSAGES";
DROP TABLE "BSCL.Orden.BD::DMA.FIX_MESSAGESLOG";
DROP TABLE "BSCL.Orden.BD::DMA.FIX_SESSIONS";

DROP TABLE "BSCL.Orden.BD::DMA.TBSOLICITUDES";
DROP TABLE "BSCL.Orden.BD::DMA.TBSOLICITUDES_2";
DROP TABLE "BSCL.Orden.BD::DMA.TBRESPUESTAS";
DROP TABLE "BSCL.Orden.BD::DMA.TBRESPUESTAS_2";

DROP TABLE "BSCL.Orden.BD::DPC.TBEXECUTIONREPORT";

DROP TABLE "BSCL.Orden.BD::MRV.INDICES_FUTUROS"; 		--MDC_TX_INDICES_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_FUTUROS"; 	--MDC_TX_INSTRUMENTOS_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.PALOS_FUTUROS"; 			--MDC_TX_PALOS_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.PUNTAS_FUTUROS"; 			--MDC_TX_PUNTAS_FUTUROS
DROP TABLE "BSCL.Orden.BD::MRV.RESUMEN_FUTUROS"; 		--MDC_TX_RESUMEN_FUTUROS

DROP TABLE "BSCL.Orden.BD::MRV.INDICES_RV"; 				--MDC_TX_INDICES_RV
DROP TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV"; 		--MDC_TX_INSTRUMENTOS_RV
DROP TABLE "BSCL.Orden.BD::MRV.PALOS_RV"; 				--MDC_TX_PALOS_RV
DROP TABLE "BSCL.Orden.BD::MRV.PUNTAS_MONETARIOS_RV"; 	--MDC_TX_PUNTAS_MONETARIOS_RV
DROP TABLE "BSCL.Orden.BD::MRV.PUNTAS_RV"; 				--MDC_TX_PUNTAS_RV
DROP TABLE "BSCL.Orden.BD::MRV.RESUMEN_RV"; 				--MDC_TX_RESUMEN_RV

DROP TABLE "BSCL.Orden.BD::MRV.OFERTAS_SERIALIZADOS"; 	--MDC_TX_OFERTAS_SERIALIZADOS
DROP TABLE "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS"; 		--MDC_TX_PALOS_SERIALIZADOS
DROP TABLE "BSCL.Orden.BD::MRV.POSTURAS_SERIALIZADOS"; 	--MDC_TX_POSTURAS_SERIALIZADOS

DROP TABLE "BSCL.Orden.BD::MRF.BENCHMARKS"; 				--TEMP_TX_RFBenchmarks
DROP TABLE "BSCL.Orden.BD::MRF.GENERICOS"; 				--TEMP_TX_RFGenericos
DROP TABLE "BSCL.Orden.BD::MRF.INDICES"; 				--TEMP_TX_RFIndices
DROP TABLE "BSCL.Orden.BD::MRF.RESUMEN"; 				--TEMP_TX_RFResumen
DROP TABLE "BSCL.Orden.BD::MRF.PALOS_IF"; 				--TEMP_TX_TRADES_IIF
DROP TABLE "BSCL.Orden.BD::MRF.PALOS_RF"; 				--TEMP_TX_Trades_IRF



CREATE COLUMN TABLE "BSCL.Orden.BD::MRF.BENCHMARKS" (
	"codTicker" 			char(3) 		NULL,	--Codigo TICKER
	"timeTicker" 			varchar(20) 	NULL,	--Hora TICKER
	"action" 				char(1) 		NULL,	--Codigo Accion
	"nameBenchmark" 		varchar(08)		NULL,	--Nombre Benchmark
	"closingIndicator" 	    char(01)		NULL,	--Indicador de Cierre
	"benchmark"				decimal(11, 4) 	NULL,	--Valor Benchmark
	"benchmarkPm"			decimal(11, 4) 	NULL,	--Valor Benchmark 13:20 Hrs
	"last"	                decimal(11, 4)  NULL,   --Ultimo
	"major"   	            decimal(11, 4)  NULL,   --Mayor tasa incorporada al cálculo del indice 
	"less"     				decimal(11, 4)  NULL,   --Menor tasa incorporada al cálculo del indice
	"bussNum"				decimal(8) 	NULL,   --Numero Negocios incorporada al cálculo del indice
	"amount" 				decimal(19, 2) 	NULL	--Monto total de las transacciones incorporada al cálculo del indice
) ;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRF.GENERICOS"(
	"codTicker" 			char(3) 		NULL,	--Codigo TICKER
	"timeTicker" 			varchar(20) 	NULL,	--Hora TICKER
	"action" 				char(1) 		NULL,	--Codigo Accion	
	"generic" 				char(20)		NULL,	--Generico RF	
	"bussNum"				decimal(8) 	    NULL,   --Numero Negocios
	"quantity"				decimal(15, 2) 	NULL,	--Cantidad	
	"currency"				varchar(3) 		NULL,	--Moneda
	"amount" 				decimal(19, 4) 	NULL,	--Monto		
	"maxPrice"	            decimal(19, 4)  NULL,  --Precio Mayor
	"minPrice"				decimal(19, 4)  NULL,  --Precio Menor
	"avgPrice"	            decimal(19, 4)  NULL,  --Precio Medio
	"maxTir"				decimal(19, 4) 	NULL,  --TIR Mayor
	"minTir"				decimal(19, 4) 	NULL,  --TIR Minima
	"AvgTir"				decimal(19, 4) 	NULL,  --TIR Medio
	"duration"				decimal(5,  2)  NULL  --Duration
		
) ;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRF.INDICES" (
	"codTicker" 		char(2) 		NULL,	--Codigo TICKER
	"timeTicker" 		varchar(20) 	NULL,	--Hora TICKER
	"action" 			char(1) 		NULL,	--Codigo Accion
	"Indice"			char(15)        NULL,   --Codigo Indice
	"Valor"				decimal(10, 2)	NULL,	--Valor 
	"Variacion"			decimal(6, 2)	NULL,	--Variacion Porcentual
	"VariacionPuntual"	decimal(10, 2)	NULL,	--Variación puntual
	"r7d"				decimal(10, 2)	NULL,	--Rentabilidad ultimos 7 día
	"r30d"				decimal(10, 2)	NULL,	--Rentabilidad últimos 30 día
	"r52s"				decimal(10, 2)	NULL,	--Rentabilidad últimos 52 Semanas
	"min52s"			decimal(10, 2)	NULL,	--Mínimo últimas 52 semanas
	"max52s"			decimal(10, 2)	NULL	--Máximo últimas 52 semanas

);

CREATE TABLE "BSCL.Orden.BD::MRF.RESUMEN"(
	"codTicker" 		char(3) 		NULL,	--Codigo TICKER
	"timeTicker" 		varchar(20) 	NULL,	--Hora TICKER
	"action" 			char(1) 		NULL,	--Codigo Accion
	"symbol"            varchar(12) 	NULL,	--Nemo
	"condIsin"          varchar(15) 	NULL,	--Cond_isin
	"bussNum"			decimal(8) 	    NULL,   --Numero Negocios
	"quantity"          decimal(15,2) 	NULL,	--Cantidad
	"amount" 			decimal(19, 4) 	NULL,	--Monto
	"maxPrice"	        decimal(19, 4)  NULL,  	--Precio Mayor
	"minPrice"			decimal(19, 4)  NULL,  	--Precio Menor
	"avgPrice"	        decimal(19, 4)  NULL,  	--Precio Medio
	"mintir"			decimal(19, 2) 	NULL,  	--TIR Minimo
	"avgtir"			decimal(19, 2) 	NULL,  	--TIR Medio
	"maxtir"			decimal(19, 2) 	NULL,  	--TIR Maximo
	"duration"			decimal(19, 2) 	NULL  	--Duration
) ;

CREATE TABLE "BSCL.Orden.BD::MRF.PALOS_IF" (
	"CODTICKER" 		char(2) 		NULL,	--Codigo TICKER
	"TIMETICKER" 		varchar(16) 	NULL,	--Hora TICKER
	"ACTION" 			char(1) 		NULL,	--Codigo Accion
	"TRADETIME" 		varchar(20) 	NULL,	--Hora Transaccion
	"TRADEID" 			varchar(10)	 	NULL,	--Folio
	"SYMBOL" 			varchar(10) 	NULL,	--Nemo
	"CONDISIN" 			varchar(15) 	NULL,	--Cond_isin
	"BUYEXECUTINGFIRM" 	varchar(3)	 	NULL,	--Codigo de la corredora del comprador.
	"SELLEXECUTINGFIRM" varchar(3) 		NULL,	--Codigo de la corredora del vendedor.
	"BUYENTERINGFIRM" 	varchar(3) 		NULL,	--Codigo del operador interno del comprador.
	"SELLENTERINGFIRM" 	varchar(3) 		NULL,	--Codigo del operador interno del vendendor.
	"TRADECONDITION" 	varchar(15) 	NULL,	--Cond cod
	"CONDITION" 		varchar(10) 	NULL,	--Cond dis
	"ISSUER" 			varchar(10) 	NULL,	--Emisor
	"FAMILY"			char(2) 		NULL,	--Familia
	"ENTERINGDATE" 		char(8) 		NULL,   --Fec_emi
	"EXPIRETIME" 		char(8) 		NULL,   --Fec_vcto
	"LAMINA" 			char(1) 		NULL,	--Lamina
	"AMOUNT" 			decimal(19, 4) 	NULL,	--Monto
	"TERM" 				integer		 	NULL,	--Plazo
	"AUCTIONID" 		varchar(3) 		NULL,	--Remate
	"RATE" 				decimal(9, 4) 	NULL,	--Tasa
	"SIGNRATE" 			char(1) 		NULL,	--Signo tasa
	"RESETTYPE" 		char(2) 		NULL,	--Tipo reaj
	"RESCUEVALUE" 		decimal(19, 4)	NULL,	--Val resc
	"TRADERBUY" 		varchar(3) 		NULL,	--Ope int com
	"TRADERSELL" 		varchar(3) 		NULL,	--Ope int ven
	"DCVTITLE" 			char(1) 		NULL,	--Ind dcv
	"SETTLDATE" 		varchar(10) 	NULL,    --Fecha liq
	"SYMBOLSVS" 		varchar(15) 	NULL,	--Nemo SVS
	"LOCALAMOUNT" 		decimal(19, 4) 	NULL,	--Monto
	"BUYERFUND"			varchar(3) 		NULL,	--Fondo AFP comprador
	"SELLERFUND"		varchar(3) 		NULL	--Fondo AFP vendedor
);

CREATE TABLE "BSCL.Orden.BD::MRF.PALOS_RF" (
	"codTicker"                	char(2) 			NULL,		--Codigo TICKER
	"timeTicker"              	varchar(16) 		NULL,		--Hora TICKER
	"action"                   	char(1) 			NULL,		--Codigo Accion
	"tradeTime"                	varchar(20) 		NULL,		--Hora Transaccion
	"tradeID"                  	varchar(10) 		NULL,		--Folio
	"symbol"                   	varchar(10) 		NULL,		--Nemo
	"condIsin"                 	varchar(15) 		NULL,		--Cond_isin
	"quantity"                 	decimal(15, 2) 	NULL,		--Cantidad
	"buyExecutingFirm"         	varchar(3) 		NULL,		--Codigo de la corredora del comprador.
	"sellExecutingFirm"        	varchar(3) 		NULL,		--Codigo de la corredora del vendedor.
	"buyEnteringFirm"          	varchar(3) 		NULL,		--Codigo del operador interno del comprador.
	"sellEnteringFirm"         	varchar(3) 		NULL,		--Codigo del operador interno del vendendor.
	"tradeCondition"           	varchar(15) 		NULL,		--Cond cod
	"condition"                	varchar(15) 		NULL,		--Cond dis
	"amount"                  	decimal(19, 4) 	NULL,		--Monto
	"term"                     	varchar(10) 		NULL,		--Plazo
	"coraMaturity"             	varchar(9) 		NULL,		--Cond dis
	"price"                    	decimal(19, 4) 	NULL,		--Precio
	"auctionId"                	varchar(3) 		NULL,		--Remate
	"tir"                      	decimal(9, 4) 		NULL,		--TIR
	"signTir"                  	char(1) 			NULL,		--Signo TIR
	"calculationType"          	varchar(1) 		NULL,		--Tipo de Calculo
	"traderBuy"                	varchar(3) 		NULL,		--Ope int com
	"traderSell"               	varchar(3) 		NULL,		--Ope int ven
	"dcvTitle"                 	varchar(1) 		NULL,		--Indicador DCV 
	"settlDate"                	varchar(10) 		NULL,		--Fecha liq
	"valAdjustmentCurrency"    	varchar(3) 		NULL,		--Reajuste
	"currency"               	varchar(3) 		NULL,		--Moneda
	"currencySettlementAmount"	decimal(19, 4) 	NULL,		--Monto moneda	
	"duration"				 	decimal(19, 4) 	NULL,		--Duration
	"family"					varchar(10) 		NULL,		--Familia del instrumento	 
	"buyerFund"				 	varchar(3) 		NULL,		--Fondo AFP comprador
	"sellerFund"				varchar(3) 		NULL		--Fondo AFP vendedor
);

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
 
CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" (
	"action" VARCHAR(2), 
	"sellExecutingFirm" VARCHAR(10), 
	"sellEnteringFirm" VARCHAR(10), 
	"buyExecutingFirm" VARCHAR(10), 
	"buyEnteringFirm" VARCHAR(10), 
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
	"modified" VARCHAR(20)) 
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

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.INDICES_RV" (
	"codTicker" VARCHAR(10), 
	"timestamp" VARCHAR(30), 
	"action" VARCHAR(2), 
	"timesTrade" VARCHAR(30), 
	"nemo" VARCHAR(50), 
	"indice" VARCHAR(50), 
	"volume" DECIMAL(20,3) CS_FIXED, 
	"price" DECIMAL(20,10) CS_FIXED, 
	"variation" DECIMAL(10,2) CS_FIXED) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV" (
	"codTicker" VARCHAR(15), 
	"timestamp" VARCHAR(30), 
	"action" VARCHAR(10), 
	"instruments" VARCHAR(30), 
	"market" VARCHAR(30), 
	"minPrice" DECIMAL(20,10) CS_FIXED, 
	"openPrice" DECIMAL(20,10) CS_FIXED, 
	"hignPrice" DECIMAL(20,10) CS_FIXED, 
	"imbalance" DECIMAL(15,2) CS_FIXED, 
	"tradeVolume" DECIMAL(20,10) CS_FIXED, 
	"avgPrice" DECIMAL(20,10) CS_FIXED, 
	"tradeAmount" DECIMAL(30,10) CS_FIXED, 
	"trend" VARCHAR(10)) 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PALOS_RV" (
	"codTicker" 					VARCHAR(30), 
	"timeStamp" 					VARCHAR(30), 
	"action" VARCHAR(30), 
	"tradeTime" VARCHAR(30), 
	"tradeID" VARCHAR(20), 
	"instrument" VARCHAR(20), 
	"market" VARCHAR(50), 
	"settlementCondition" VARCHAR(20), 
	"orderType" VARCHAR(20), 
	"indivisible" VARCHAR(20),
	"shortSell" VARCHAR(5), 
	"quantity" DECIMAL(20,10) CS_FIXED, 
	"price" DECIMAL(20,10) CS_FIXED, 
	"amount" DECIMAL(20,10) CS_FIXED, 
	"closingPrice" DECIMAL(20,10) CS_FIXED, 
	"buyerBroker" VARCHAR(20), 
	"buyEnteringFirm" VARCHAR(20), 
	"buyEnteringTrader" VARCHAR(20), 
	"sellerBroker" VARCHAR(20), 
	"sellEnteringFirm" VARCHAR(20), 
	"sellEnteringTrader" VARCHAR(20), 
	"priceBuyingQTY" DECIMAL(20,4) CS_FIXED, 
	"priceBuyingPrice" DECIMAL(20,4) CS_FIXED, 
	"priceSellingQty" DECIMAL(20,4) CS_FIXED, 
	"priceSellingPrice" DECIMAL(20,4) CS_FIXED) 
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

CREATE COLUMN TABLE "BSCL.Orden.BD::MRV.PUNTAS_RV" (
	"codTicker" VARCHAR(10), 
	"timesTamp" VARCHAR(30), 
	"action" VARCHAR(3), 
	"symbol" VARCHAR(50) NOT NULL , 
	"volume" DECIMAL(20,10) CS_FIXED, 
	"amountSale" DECIMAL(20,10) CS_FIXED, 
	"price" DECIMAL(20,10) CS_FIXED, 
	"priceSale" DECIMAL(20,10) CS_FIXED, 
	"settlement" VARCHAR(20) NOT NULL ,
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




CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.FIX_EVENTLOG" (
	"ID" INTEGER CS_INT, 
	"TIME" LONGDATE CS_LONGDATE, 
	"BEGINSTRING" VARCHAR(8), 
	"SENDERCOMPID" VARCHAR(64), 
	"TARGETCOMPID" VARCHAR(64), 
	"SESSION_QUALIFIER" VARCHAR(64), 
	"TEXT" TEXT SYNC  PHRASE INDEX RATIO 0.000000 
	FUZZY SEARCH INDEX OFF SEARCH ONLY OFF FAST PREPROCESS 
	ON TEXT MINING OFF TEXT ANALYSIS OFF TOKEN SEPARATORS '\/;,.:-_()[]<>!?*@+{}="&#$~|') 
UNLOAD PRIORITY 5  AUTO MERGE ;

CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.FIX_MESSAGES" (
	"BEGINSTRING" VARCHAR(8), 
	"SENDERCOMPID" VARCHAR(64), 
	"TARGETCOMPID" VARCHAR(64), 
	"SESSION_QUALIFIER" VARCHAR(64),
	"MSGSEQNUM" INTEGER CS_INT, 
	"MESSAGE" TEXT SYNC  PHRASE INDEX RATIO 0.000000 FUZZY SEARCH INDEX OFF SEARCH ONLY OFF FAST PREPROCESS ON TEXT MINING OFF TEXT ANALYSIS OFF TOKEN SEPARATORS '\/;,.:-_()[]<>!?*@+{}="&#$~|') 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.FIX_MESSAGESLOG" (
	"ID" INTEGER CS_INT, 
	"TIME" LONGDATE CS_LONGDATE, 
	"BEGINSTRING" VARCHAR(8), 
	"SENDERCOMPID" VARCHAR(64), 
	"TARGETCOMPID" VARCHAR(64), 
	"SESSION_QUALIFIER" VARCHAR(64), 
	"TEXT" TEXT SYNC  PHRASE INDEX RATIO 0.000000 FUZZY SEARCH INDEX OFF SEARCH ONLY OFF FAST PREPROCESS ON TEXT MINING OFF TEXT ANALYSIS OFF TOKEN SEPARATORS '\/;,.:-_()[]<>!?*@+{}="&#$~|') 
UNLOAD PRIORITY 5  AUTO MERGE;

CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.FIX_SESSIONS" (
	"BEGINSTRING" VARCHAR(8) NOT NULL , 
	"SENDERCOMPID" VARCHAR(64), 
	"TARGETCOMPID" VARCHAR(64), 
	"SESSION_QUALIFIER" VARCHAR(64), 
	"CREATION_TIME" LONGDATE CS_LONGDATE, 
	"INCOMING_SEQNUM" INTEGER CS_INT, 
	"OUTGOING_SEQNUM" INTEGER CS_INT) 
UNLOAD PRIORITY 5  AUTO MERGE;


CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.TBSOLICITUDES" (
	"SECUENCIA" DECIMAL(8,0) CS_FIXED, 
	"IN_RUTEO" VARCHAR(26), 
	"OUT_RUTEO" VARCHAR(26), 
	"PROCESADO" VARCHAR(1), 
	"SEC_ORDEN" DECIMAL(8,0) CS_FIXED, 
	"RUT_CLI" VARCHAR(11), 
	"NEMO" VARCHAR(10), 
	"CANTIDAD" DECIMAL(12,0) CS_FIXED, 
	"PRECIO" DECIMAL(15,4) CS_FIXED, 
	"TIPO_OPERAC" VARCHAR(1), 
	"FEC_ING_ORDEN" VARCHAR(26), 
	"IND_VALIDEZ" VARCHAR(1), 
	"FEC_VCTO_VALIDEZ" VARCHAR(10), 
	"CAN_ASIG_ACUM" DECIMAL(12,0) CS_FIXED, 
	"ESTADO" VARCHAR(2), 
	"MONEDA" VARCHAR(3), 
	"BOLSA" VARCHAR(10), 
	"CONDICION_LIQUIDACION" VARCHAR(10), 
	"SPONSORING_FIRM" VARCHAR(3), 
	"MERCADO" VARCHAR(2), 
	"SUB_MERCADO" VARCHAR(2), 
	"PREPAGO" VARCHAR(1), 
	"TASA_MAXIMA" DECIMAL(5,2) CS_FIXED, 
	"TASA_PROPIA" DECIMAL(5,2) CS_FIXED, 
	"ID_REFERENCIA" VARCHAR(32), 
	"FECHA_VENCIMIENTO" VARCHAR(32), 
	"DIVISIBLE" VARCHAR(1), 
	"OD" VARCHAR(1), 
	"SEC_ORDEN_OD_COMPRA" DECIMAL(8,0) CS_FIXED, 
	"SEC_ORDEN_OD_VENTA" DECIMAL(8,0) CS_FIXED, 
	"OP_INTERNO" VARCHAR(3), 
	"SEC_ORDEN_2" VARCHAR(32), 
	"ID_CLIENTE" VARCHAR(32), 
	"CANTIDAD_VISIBLE" DECIMAL(12,0) CS_FIXED) 
UNLOAD PRIORITY 5  AUTO MERGE ;

CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.TBSOLICITUDES_2" (
	"SECUENCIA" DECIMAL(8,0) CS_FIXED, 
	"IN_RUTEO" VARCHAR(26), 
	"OUT_RUTEO" VARCHAR(26), 
	"PROCESADO" VARCHAR(1), 
	"SEC_ORDEN" DECIMAL(8,0) CS_FIXED, 
	"RUT_CLI" VARCHAR(11), 
	"NEMO" VARCHAR(10), 
	"CANTIDAD" DECIMAL(12,0) CS_FIXED, 
	"PRECIO" DECIMAL(15,4) CS_FIXED, 
	"TIPO_OPERAC" VARCHAR(1), 
	"FEC_ING_ORDEN" VARCHAR(26), 
	"IND_VALIDEZ" VARCHAR(1), 
	"FEC_VCTO_VALIDEZ" VARCHAR(10), 
	"CAN_ASIG_ACUM" DECIMAL(12,0) CS_FIXED, 
	"ESTADO" VARCHAR(2), 
	"MONEDA" VARCHAR(3), 
	"BOLSA" VARCHAR(10), 
	"CONDICION_LIQUIDACION" VARCHAR(10), 
	"SPONSORING_FIRM" VARCHAR(3), 
	"MERCADO" VARCHAR(2), 
	"SUB_MERCADO" VARCHAR(2), 
	"PREPAGO" VARCHAR(1), 
	"TASA_MAXIMA" DECIMAL(5,2) CS_FIXED, 
	"TASA_PROPIA" DECIMAL(5,2) CS_FIXED, 
	"ID_REFERENCIA" VARCHAR(32), 
	"FECHA_VENCIMIENTO" VARCHAR(32), 
	"DIVISIBLE" VARCHAR(1), 
	"OD" VARCHAR(1), 
	"SEC_ORDEN_OD_COMPRA" DECIMAL(8,0) CS_FIXED, 
	"SEC_ORDEN_OD_VENTA" DECIMAL(8,0) CS_FIXED, 
	"OP_INTERNO" VARCHAR(3), 
	"SEC_ORDEN_2" VARCHAR(32), 
	"ID_CLIENTE" VARCHAR(32), 
	"CANTIDAD_VISIBLE" DECIMAL(12,0) CS_FIXED) 
UNLOAD PRIORITY 5  AUTO MERGE ;

CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.TBRESPUESTAS" (
	"NRO_ENTRADA" DECIMAL(8,0) CS_FIXED, 
	"FOLIO_TRANS" VARCHAR(20), 
	"FECHA" VARCHAR(10), 
	"NEMO" VARCHAR(10), 
	"SEC_ORDEN" DECIMAL(8,0) CS_FIXED, 
	"CANTIDAD" DECIMAL(12,0) CS_FIXED, 
	"PRECIO" DECIMAL(15,4) CS_FIXED, 
	"CAN_ASIG_ACUM" DECIMAL(12,0) CS_FIXED, 
	"PRECIO_PROM" DECIMAL(15,4) CS_FIXED, 
	"MENSAJE" VARCHAR(255), 
	"ESTADO" VARCHAR(2), 
	"PROCESADO" VARCHAR(1), 
	"IN_SGC" VARCHAR(23), 
	"MONEDA" VARCHAR(3), 
	"BOLSA" VARCHAR(10), 
	"CONDICION_LIQUIDACION" VARCHAR(10), 
	"SPONSORING_FIRM" VARCHAR(3), 
	"EXEC_ID" VARCHAR(50), 
	"ORDER_ID" VARCHAR(32), 
	"MERCADO" VARCHAR(2), 
	"SUB_MERCADO" VARCHAR(2), 
	"PREPAGO" VARCHAR(1), 
	"TASA_MAXIMA" DECIMAL(5,2) CS_FIXED, 
	"TASA_PROPIA" DECIMAL(5,2) CS_FIXED, 
	"TASA_ADJUDICACION" DECIMAL(5,2) CS_FIXED, 
	"MEJOR_POSTURA" VARCHAR(1), 
	"ID_REFERENCIA" VARCHAR(32),
	"FECHA_VENCIMIENTO" VARCHAR(32), 
	"DIVISIBLE" VARCHAR(1), 
	"OD" VARCHAR(1), 
	"HORA_ADJUDICACION" VARCHAR(32), 
	"SEC_ORDER_OD" DECIMAL(8,0) CS_FIXED, 
	"TIPO_OPERAC" VARCHAR(1), 
	"OP_INTERNO" VARCHAR(3), 
	"SEC_ORDEN_2" VARCHAR(32), 
	"ID_CLIENTE" VARCHAR(32)) 
UNLOAD PRIORITY 5  AUTO MERGE ;


CREATE COLUMN TABLE "BSCL.Orden.BD::DMA.TBRESPUESTAS_2" (
	"NRO_ENTRADA" DECIMAL(8,0) CS_FIXED, 
	"FOLIO_TRANS" VARCHAR(20), 
	"FECHA" VARCHAR(10), 
	"NEMO" VARCHAR(10), 
	"SEC_ORDEN" DECIMAL(8,0) CS_FIXED, 
	"CANTIDAD" DECIMAL(12,0) CS_FIXED, 
	"PRECIO" DECIMAL(15,4) CS_FIXED, 
	"CAN_ASIG_ACUM" DECIMAL(12,0) CS_FIXED, 
	"PRECIO_PROM" DECIMAL(15,4) CS_FIXED, 
	"MENSAJE" VARCHAR(255), 
	"ESTADO" VARCHAR(2), 
	"PROCESADO" VARCHAR(1), 
	"IN_SGC" VARCHAR(23), 
	"MONEDA" VARCHAR(3), 
	"BOLSA" VARCHAR(10), 
	"CONDICION_LIQUIDACION" VARCHAR(10), 
	"SPONSORING_FIRM" VARCHAR(3), 
	"EXEC_ID" VARCHAR(50), 
	"ORDER_ID" VARCHAR(32), 
	"MERCADO" VARCHAR(2), 
	"SUB_MERCADO" VARCHAR(2), 
	"PREPAGO" VARCHAR(1), 
	"TASA_MAXIMA" DECIMAL(5,2) CS_FIXED, 
	"TASA_PROPIA" DECIMAL(5,2) CS_FIXED, 
	"TASA_ADJUDICACION" DECIMAL(5,2) CS_FIXED, 
	"MEJOR_POSTURA" VARCHAR(1), 
	"ID_REFERENCIA" VARCHAR(32),
	"FECHA_VENCIMIENTO" VARCHAR(32), 
	"DIVISIBLE" VARCHAR(1), 
	"OD" VARCHAR(1), 
	"HORA_ADJUDICACION" VARCHAR(32), 
	"SEC_ORDER_OD" DECIMAL(8,0) CS_FIXED, 
	"TIPO_OPERAC" VARCHAR(1), 
	"OP_INTERNO" VARCHAR(3), 
	"SEC_ORDEN_2" VARCHAR(32), 
	"ID_CLIENTE" VARCHAR(32)) 
UNLOAD PRIORITY 5  AUTO MERGE ;


CREATE COLUMN TABLE "BSCL.Orden.BD::DPC.TBEXECUTIONREPORT" (
	"ORDERID" VARCHAR(50), 
	"SECONDARYORDERID" VARCHAR(50), 
	"SECONDARYCLORDID" VARCHAR(50), 
	"SECONDARYEXECID" VARCHAR(50), 
	"CLORDID" VARCHAR(50), 
	"ORIGCLORDID" VARCHAR(50), 
	"CLORDLINKID" VARCHAR(50), 
	"QUOTERESPID" VARCHAR(50), 
	"ORDSTATUSREQID" VARCHAR(50), 
	"MASSSTATUSREQID" VARCHAR(50), 
	"TOTNUMREPORTS" INTEGER CS_INT, 
	"LASTRPTREQUESTED" VARCHAR(1), 
	"TRADEORIGINATIONDATE" VARCHAR(50), 
	"LISTID" VARCHAR(50), 
	"CROSSID" VARCHAR(50), 
	"ORIGCROSSID" VARCHAR(50), 
	"CROSSTYPE" INTEGER CS_INT, 
	"EXECID" VARCHAR(50), 
	"EXECREFID" VARCHAR(50), 
	"EXECTYPE" VARCHAR(1), 
	"ORDSTATUS" VARCHAR(1),
	"ORDREJREASON" INTEGER CS_INT, 
	"EXECRESTATEMENTREASON" INTEGER CS_INT, 
	"SETTLTYPE" VARCHAR(1), 
	"SETTLDATE" VARCHAR(50), 
	"CASHMARGIN" VARCHAR(1), 
	"CLEARINGFEEINDICATOR" VARCHAR(50), 
	"SIDE" VARCHAR(1), 
	"QTYTYPE" INTEGER CS_INT, 
	"ORDTYPE" VARCHAR(1), 
	"PRICETYPE" INTEGER CS_INT, 
	"PRICE" DECIMAL(25,5) CS_FIXED, 
	"STOPPX" DECIMAL(25,5) CS_FIXED, 
	"CURRENCY" VARCHAR(50), 
	"SETTLCURRENCY" VARCHAR(50), 
	"TIMEINFORCE" VARCHAR(1), 
	"EXPIREDATE" VARCHAR(50), 
	"EXECINST" VARCHAR(50), 
	"LASTQTY" DECIMAL(25,5) CS_FIXED, 
	"LASTPX" DECIMAL(25,5) CS_FIXED, 
	"TRADINGSESSIONID" VARCHAR(50), 
	"LEAVESQTY" DECIMAL(25,5) CS_FIXED, 
	"CUMQTY" DECIMAL(25,5) CS_FIXED, 
	"AVGPX" DECIMAL(25,5) CS_FIXED, 
	"DAYORDERQTY" DECIMAL(25,5) CS_FIXED, 
	"DAYCUMQTY" DECIMAL(25,5) CS_FIXED, 
	"DAYAVGPX" DECIMAL(25,5) CS_FIXED, 
	"TRADEDATE" VARCHAR(50), 
	"REPORTTOEXCH" VARCHAR(1), 
	"GROSSTRADEAMT" DECIMAL(25,5) CS_FIXED, 
	"ORIGAMOUNT" DECIMAL(25,5) CS_FIXED, 
	"HANDLINST" VARCHAR(1), 
	"MINQTY" DECIMAL(25,5) CS_FIXED, 
	"MAXFLOOR" DECIMAL(25,5) CS_FIXED, 
	"TEXTO" TEXT SYNC  PHRASE INDEX RATIO 0.000000 FUZZY SEARCH INDEX OFF SEARCH ONLY OFF FAST PREPROCESS ON TEXT MINING OFF TEXT ANALYSIS OFF TOKEN SEPARATORS '\/;,.:-_()[]<>!?*@+{}="&#$~|', 
	"ORDERQTY2" DECIMAL(25,5) CS_FIXED, 
	"MULTILEGREPORTINGTYPE" VARCHAR(1), 
	"REGISTID" VARCHAR(50), 
	"COPYMSGINDICATOR" VARCHAR(1), 
	"MOVETOOIB" VARCHAR(50), 
	"TRADEID" VARCHAR(50), 
	"OLDQTY" DECIMAL(25,5) CS_FIXED, 
	"OLDPRICE" DECIMAL(25,5) CS_FIXED, 
	"STOPPXCONDITION" VARCHAR(1), 
	"PRICE2" DECIMAL(25,5) CS_FIXED, 
	"ROUNDLOTBOOK" VARCHAR(1), 
	"PREPAYMENT" VARCHAR(1), 
	"MAXRATE" DECIMAL(25,5) CS_FIXED, 
	"OWNRATE" DECIMAL(25,5) CS_FIXED, 
	"PUBLICRATE" DECIMAL(25,5) CS_FIXED, 
	"BESTSTANCE" VARCHAR(1), 
	"REFORDERID" VARCHAR(50), 
	"REFORDERIDSOURCE" VARCHAR(1), 
	"AUCTIONID" VARCHAR(50), 
	"LASTMKT" VARCHAR(50), 
	"DCVTITLE" VARCHAR(50), 
	"EXPRESSED" VARCHAR(50), 
	"REFORDERSUBID" VARCHAR(50), 
	"REFFRACTIONID" VARCHAR(50), 
	"NOOFFERS" INTEGER CS_INT, 
	"EXPIRETIME" VARCHAR(50), 
	"TRANSACTTIME" VARCHAR(50), 
	"SYMBOL" VARCHAR(50), 
	"ISSUER" VARCHAR(50), 
	"SECURITYTYPE" VARCHAR(50), 
	"SECURITYID" VARCHAR(50), 
	"ENTERINGFIRM" VARCHAR(50), 
	"EXECUTINGFIRM" VARCHAR(50), 
	"ENTERINGTRADER" VARCHAR(50), 
	"FUNDMANAGERCLIENTID" VARCHAR(50)) 
UNLOAD PRIORITY 5  AUTO MERGE ;



















