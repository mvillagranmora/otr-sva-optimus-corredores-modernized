SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",COALESCE(tx."codTicker",'') AS "codTicker",tx."timeStamp",tx."action",tx."tradeTime",tx."tradeID",tx."instrument",tx."market",tx."settlementCondition",
tx."orderType",tx."indivisible",tx."shortSell",tx."quantity",
tx."price",tx."amount",tx."closingPrice",tx."buyerBroker",tx."buyEnteringFirm",tx."buyEnteringTrader",tx."sellerBroker",tx."sellEnteringFirm",tx."sellEnteringTrader",tx."priceBuyingQTY",
tx."priceBuyingPrice",tx."priceSellingQty",tx."priceSellingPrice" ,tx."priceExchange",tx."timeInForce",tx."entryType",tx."tradingSessionID",tx."codIsin",tx."securityType",tx."priceTime",
tx."buyClientID",tx."sellClientID",tx."buyFundManager",tx."sellFundManager"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MRV.PALOS_RV" tx on cm."IdMensaje1" = tx."timeStamp" and cm."IdMensaje2" = tx."tradeID" || tx."action"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" buyer on buyer."CodigoCorredoraFix" = tx."buyerBroker"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" seller on seller."CodigoCorredoraFix" = tx."sellerBroker"

WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_RV' and (buyer."CodigoCorredoraFix" is not null or seller."CodigoCorredoraFix" is not null)
ORDER BY tx."tradeTime",tx."timeStamp",tx."tradeID",tx."action"
