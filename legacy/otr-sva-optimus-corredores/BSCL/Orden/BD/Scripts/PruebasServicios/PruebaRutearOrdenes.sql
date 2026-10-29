SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	
		"IdOrden"	 BIGINT,
		"IdBolsaDistribucion" NVARCHAR(3)
);


INSERT INTO #TT ("IdOrden","IdBolsaDistribucion")
select 310437,'BCS' from dummy;

--SET 'DEPURACION' = 'S';
CALL "BSCL"."BSCL.Orden.BD.Servicios::RutearOrdenes"(1,#tt,?,?)
update "BSCL.Orden.BD::ORD.Orden" set "IdTipoCanalDistribucion" = 'RUA' where "IdOrden" = 310437
rollback
commit;

select * from "RUEBAS"."TEMP_TX_RFBenchmarks";
select * from "RUEBAS"."TEMP_TX_RFGenericos";
select * from "RUEBAS"."TEMP_TX_RFIndices";
select * from "RUEBAS"."TEMP_TX_RFResumen";
select * from "RUEBAS"."TEMP_TX_TRADES_IIF" where ISSUER = 'CENTRAL' order by tradeid;
select CASE RIGHT(LEFT("tradeCondition",2),1) WHEN ' ' THEN 'T2' WHEN 'M' THEN 'T1' WHEN 'H' THEN 'T0' WHEN '3' THEN 'T3' WHEN '5' THEN 'T5' ELSE '?' END,* from "RUEBAS"."TEMP_TX_Trades_IRF";
delete from "RUEBAS"."TEMP_TX_Trades_IRF" where "symbol" = 'BARAU-R'

delete from "BSCL"."BSCL.Orden.BD::ORD.ControlMercado"
tradeCondition
TH V        C  
SELECT * FROM "RUEBAS"."MDC_TX_PALOS_RV"
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado"
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado"

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.MarketDataRV_CrearTransaccionesRV_Entrada_Entrada"



SELECT cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",COALESCE(tx."codTicker",'') AS "codTicker",tx."timeStamp",tx."action",tx."tradeTime",tx."tradeID",tx."instrument",tx."market",tx."settlementCondition",
tx."orderType",tx."indivisible",tx."shortSell",tx."quantity",
tx."price",tx."amount",tx."closingPrice",tx."buyerBroker",tx."buyEnteringFirm",tx."buyEnteringTrader",tx."sellerBroker",tx."sellEnteringFirm",tx."sellEnteringTrader",tx."priceBuyingQTY",
tx."priceBuyingPrice",tx."priceSellingQty",tx."priceSellingPrice"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm
LEFT JOIN "RUEBAS"."MDC_TX_PALOS_RV" tx on cm."IdMensaje1" = tx."tradeTime" and cm."IdMensaje2" = tx."tradeID"
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_RV'







TipoMensaje;IdMensaje1;IdMensaje2;FechaIngresoRegistro;FechaProcesamiento;Procesado;Log
MARKET_DATA_PALO_RF;20161223121816669;161101;23-12-2016 10:47:52.433;?;N;?

SELECT * FROM "RUEBAS"."TEMP_TX_Trades_IRF" where "tradeTime" = '20161223121816669' and "tradeID" = '161101'


SELECT "codTicker","timeTicker","action","tradeTime","tradeID","symbol","condIsin","quantity","buyExecutingFirm","sellExecutingFirm","buyEnteringFirm","sellEnteringFirm","tradeCondition","condition","amount","term","coraMaturity","price","auctionId","tir","signTir","calculationType","traderBuy","traderSell","dcvTitle","settlDate","valAdjustmentCurrency","currency","currencySettlementAmount","duration","family","buyerFund","sellerFund"
FROM "RUEBAS"."TEMP_TX_Trades_IRF"

SELECT \"codTicker\",\"timeTicker\",\"action\",\"tradeTime\",\"tradeID\",\"symbol\",\"condIsin\",\"quantity\",\"buyExecutingFirm\",\"sellExecutingFirm\",\"buyEnteringFirm\",\"sellEnteringFirm\",\"tradeCondition\",\"condition\",\"amount\",\"term\",\"coraMaturity\",\"price\",\"auctionId\",\"tir\",\"signTir\",\"calculationType\",\"traderBuy\",\"traderSell\",\"dcvTitle\",\"settlDate\",\"valAdjustmentCurrency\",\"currency\",\"currencySettlementAmount\",\"duration\",\"family\",\"buyerFund\",\"sellerFund\"
FROM "RUEBAS"."TEMP_TX_Trades_IRF"

RF;2016122310450025;I;20161223121816669;161101;BARAU-R;N/A;100.000;035;070; ; ;TM          C  ;PM;30.377,32;18/04;00;30,14;T1;15,38;+;1; ; ;S;20161221;UF;CLP;800.123.244;8,66;LH; ; 

select * from "RUEBAS"."MDC_TX_PALOS_RV"
select * from "RUEBAS"."TEMP_TX_Trades_IRF";

SELECT * FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot"
SELECT * FROM "BSCL.Plataforma.BD::PLA.Configuracion"

SELECT * FROM "RUEBAS"."MDC_TX_PUNTAS_RV"
SELECT * FROM "RUEBAS"."MDC_TX_PALOS_RV"

SELECT CURRENT_time from dummy

select  TO_NVARCHAR(ADD_SECONDS("FechaIngresoRegistro",-10800), 'YYYY-MM-DD HH24:MI:SS'),"FechaIngresoRegistro",*
FROM "BSCL.Orden.BD::ORD.Orden" ord where ord."IdOrden" = 310437

update "BSCL.Orden.BD::ORD.Orden" set "IdEstadoOrden" = 'ING' where "IdOrden" = 309550

select * from "BSCL.Orden.BD::ORD.EventoBitacora"
select CURRENT_TIME,* from "RUEBAS"."TBSOLICITUDES" where SEC_ORDEN = 310438;

select

select * from "RUEBAS"."TBRESPUESTAS";

delete from "RUEBAS"."TBSOLICITUDES";
delete from "RUEBAS"."TBRESPUESTAS";




insert into "RUEBAS"."TBSOLICITUDES" 
values(
1, -- order(incrementar para cada ingreso)
'2016-08-24',
'2016-08-24:00:00:00.000000',
'N',
1, -- sec(incrementar para cada ingreso)
NULL,
'FALABELLA',
20,
4859,
'C',
'2016-08-24',
'D',
'2016-08-24',
NULL,
'VT',
'CLP',
'XSGO',
'',
'035',
'AC',
NULL,
NULL,
NULL,
NULL,
NULL,
NULL,
NULL,
'N',
4,
4,
'001',
'MP002',
'GGROBIER'
)

