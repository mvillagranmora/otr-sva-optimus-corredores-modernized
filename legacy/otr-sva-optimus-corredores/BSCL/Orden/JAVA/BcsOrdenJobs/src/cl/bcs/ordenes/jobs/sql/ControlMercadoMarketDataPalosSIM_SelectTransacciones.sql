SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",
cm."IdMensaje2" AS "_IdMensaje2",
COALESCE(tx."action",'') AS "action",
tx."sellExecutingFirm",tx."sellEnteringFirm",tx."sellEnteringTrader",
tx."buyExecutingFirm",tx."buyEnteringFirm",tx."buyEnteringTrader",
tx."qty",tx."symbol",tx."spotPrice",tx."forwardPrice",tx."maxRate",
tx."pubRate",tx."settlementType",tx."expirationDate",tx."amount",
tx."transactionTime",tx."transactionDay",tx."prepaid",tx."folio",
tx."bestStance",tx."divisible",tx."modified",tx."codIsin",tx."securityType",
tx."securityExchange",tx."orderID",tx."buyClientID",tx."sellClientID"

FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" tx on cm."IdMensaje1" = tx."transactionTime" and cm."IdMensaje2" = tx."folio" || tx."action"
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_SERIALIZADO';

