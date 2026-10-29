/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: SH
fecha		: 16/01/2025
***********************************************************/
SELECT ins."IdInstrumento", ins."IdTipoInstrumento",
insrv."COD_TICKER" as "codTicker", insrv."TIME_STAMP" as "timeStamp", insrv."ACTIONN" as "action", insrv."INSTRUMENTS" as "instruments",
insrv."COD_ISIN" as "codIsin", insrv."MARKET_ID" as "market",
insrv."MIN_PRICE" as "minPrice", insrv."OPEN_PRICE" as "openPrice", insrv."HIGH_PRICE" as "highPrice",
insrv."IMBALANCE" as "imbalance", insrv."TRADE_VOLUME" as "tradeVolume", insrv."AVG_PRICE" as "avgPrice",
insrv."TRADE_AMOUNT" as "tradeAmount", COALESCE(insrv."LAST_PRICE",0) as "closingPrice",

'' as "trend", '' as "dcv", '' as "instrumentType",'' as "timeStampLastTrade", 0.0 as "lastPrice", 0.0 as "quantityLastTrade"

FROM "BSCL.Orden.BD::MME.INSTRUMENTOS_RV" insrv 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = insrv."INSTRUMENTS" 
ORDER BY insrv."INSTRUMENTS"
