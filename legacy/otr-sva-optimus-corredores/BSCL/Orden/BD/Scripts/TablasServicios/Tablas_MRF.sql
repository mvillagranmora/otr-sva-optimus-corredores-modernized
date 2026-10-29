SET SCHEMA "BSCL"; 

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
	"maxPrice"	            decimal(21, 6)  NULL,  --Precio Mayor
	"minPrice"				decimal(21, 6)  NULL,  --Precio Menor
	"avgPrice"	            decimal(21, 6)  NULL,  --Precio Medio
	"maxTir"				decimal(23, 6) 	NULL,  --TIR Mayor
	"minTir"				decimal(23, 6) 	NULL,  --TIR Minima
	"AvgTir"				decimal(23, 6) 	NULL,  --TIR Medio
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
	"maxPrice"	        decimal(21, 6)  NULL,  	--Precio Mayor
	"minPrice"			decimal(21, 6)  NULL,  	--Precio Menor
	"avgPrice"	        decimal(21, 6)  NULL,  	--Precio Medio
	"mintir"			decimal(23, 6) 	NULL,  	--TIR Minimo
	"avgtir"			decimal(21, 6) 	NULL,  	--TIR Medio
	"maxtir"			decimal(21, 6) 	NULL,  	--TIR Maximo
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
	"RATE" 				decimal(11, 6) 	NULL,	--Tasa
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
	"price"                    	decimal(21, 6) 	NULL,		--Precio
	"auctionId"                	varchar(3) 		NULL,		--Remate
	"tir"                      	decimal(11, 6) 		NULL,		--TIR
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
