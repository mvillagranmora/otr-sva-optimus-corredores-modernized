WITH Transaccion AS (
SELECT "timeStamp", "tradeID", 
  ROW_NUMBER () OVER (PARTITION BY "tradeID" ORDER BY "timeStamp" DESC) AS "Row"
FROM "BSCL.Orden.BD::MRV.HIS_PALOS_RV"
WHERE "tradeTime" = :TradeTime AND "tradeID" = :Folio
)

SELECT HIS."codTicker", HIS."timeStamp", HIS."action", HIS."tradeTime",	HIS."tradeID", HIS."instrument", HIS."market", HIS."settlementCondition", HIS."orderType", HIS."indivisible", HIS."shortSell",
 HIS."quantity", HIS."price", HIS."amount", HIS."closingPrice", HIS."buyerBroker", HIS."buyEnteringFirm", HIS."buyEnteringTrader", HIS."sellerBroker", HIS."sellEnteringFirm", HIS."sellEnteringTrader",
 HIS."priceBuyingQTY", HIS."priceBuyingPrice", HIS."priceSellingQty", HIS."priceSellingPrice", HIS."codIsin", HIS."securityType", HIS."timeInForce", HIS."tradingSessionID", HIS."entryType", HIS."priceExchange",
 HIS."priceTime"
FROM Transaccion AS T
INNER JOIN "BSCL.Orden.BD::MRV.HIS_PALOS_RV" AS HIS ON (HIS."timeStamp" = T."timeStamp" AND  T."tradeID" = HIS."tradeID")
WHERE T."Row" = 1;