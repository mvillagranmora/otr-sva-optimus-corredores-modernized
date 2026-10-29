INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada"
("IdInterno","action", "timesTamp","sellExecutingFirm", "sellEnteringFirm", "sellEnteringTrader", "buyExecutingFirm", "buyEnteringFirm", 
"buyEnteringTrader", "qty", "symbol", "spotPrice", "forwardPrice", "maxRate", "pubRate", "settlementType", 
"expirationDate", "amount", "transactionTime", "transactionDay", "prepaid", "folio", "bestStance", "divisible", 
"modified", "codIsin", "securityType", "securityExchange", "orderID")
VALUES(:IdInterno,:action,:timesTamp,:sellExecutingFirm,:sellEnteringFirm,:sellEnteringTrader,:buyExecutingFirm,:buyEnteringFirm,
:buyEnteringTrader,:qty,:symbol,:spotPrice,:forwardPrice,:maxRate,:pubRate,:settlementType,
:expirationDate,:amount,:transactionTime,:transactionDay,:prepaid,:folio,:bestStance,:divisible,
:modified,:codIsin,:securityType,:securityExchange,:orderID);
