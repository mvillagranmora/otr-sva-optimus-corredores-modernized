SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",
COALESCE(tx."codTicker",'') AS "codTicker",tx."timeTicker",tx."action",tx."tradeTime",tx."tradeID",tx."symbol",tx."condIsin",tx."quantity",tx."buyExecutingFirm",tx."sellExecutingFirm",
tx."buyEnteringFirm",tx."sellEnteringFirm",tx."tradeCondition",tx."condition",tx."amount",tx."term",tx."coraMaturity",tx."price","auctionId",tx."tir",tx."signTir",tx."calculationType",
tx."traderBuy",tx."traderSell",tx."dcvTitle",tx."settlDate",tx."valAdjustmentCurrency",tx."currency",tx."currencySettlementAmount",tx."duration",tx."family",tx."buyerFund",tx."sellerFund"
from "BSCL.Orden.BD::ORD.ControlMercado" cm LEFT JOIN "BSCL.Orden.BD::MRF.PALOS_RF" tx on cm."IdMensaje1" = tx."timeTicker" and cm."IdMensaje2" = tx."tradeID"
where cm."Procesado" = :Procesado AND cm."TipoMensaje" = 'MARKET_DATA_PALO_RF'
ORDER BY tx."tradeTime",tx."timeTicker",tx."tradeID",tx."action";