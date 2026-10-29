SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",
	 COALESCE(tx."codTicker",'') AS "codTicker",tx."timeStamp",tx."action",tx."tradeTime",tx."tradeID",tx."instrument",tx."market",tx."settlementCondition",tx."orderType",tx."indivisible"
	,tx."shortSell",tx."quantity",tx."price",tx."amount",tx."closingPrice",tx."buyerBroker",tx."buyEnteringFirm",tx."buyEnteringTrader",tx."buyFund",tx."sellerBroker"
	,tx."sellEnteringFirm",tx."sellEnteringTrader",tx."sellFund",tx."currency",tx."tradeCondition",tx."indDcv"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MOP.PALOS_OP" tx on cm."IdMensaje1" = tx."timeStamp" and cm."IdMensaje2" = tx."tradeID" 
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_OP';
