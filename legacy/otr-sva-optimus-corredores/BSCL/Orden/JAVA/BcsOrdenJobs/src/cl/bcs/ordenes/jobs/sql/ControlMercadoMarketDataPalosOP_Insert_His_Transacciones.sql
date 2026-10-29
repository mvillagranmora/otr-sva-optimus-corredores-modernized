UPSERT "BSCL.Orden.BD::MOP.HIS_PALOS_OP"
(	"codTicker","timeStamp","action","tradeTime","tradeID","instrument","market","settlementCondition","orderType","indivisible","shortSell","quantity","price","amount","closingPrice","buyerBroker",
	"buyEnteringFirm","buyEnteringTrader","buyFund","sellerBroker","sellEnteringFirm","sellEnteringTrader","sellFund","currency","tradeCondition","indDcv")
VALUES(:codTicker,:timeStamp,:action,:tradeTime,:tradeID,:instrument,:market,:settlementCondition,:orderType,:indivisible,:shortSell,:quantity,:price,:amount,:closingPrice,:buyerBroker,
	:buyEnteringFirm,:buyEnteringTrader,:buyFund,:sellerBroker,:sellEnteringFirm,:sellEnteringTrader,:sellFund,:currency,:tradeCondition,:indDcv)
WHERE  "tradeID"=:tradeID AND "timeStamp"=:timeStamp;