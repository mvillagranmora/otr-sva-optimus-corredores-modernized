/********************************************************** 
Proposito	: recupera punta antigua de compra
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT top 1 p1."codTicker",p1."timesTamp",p1."action",p1."symbol",p1."volume",p1."amountSale",p1."price",p1."priceSale",p1."settlement",p1."entryPositionNo",
p1."bookingRefID",p1."buyExecutingFirm",p1."buyEnteringFirm",p1."buyEnteringTrader",p1."buyClientID",p1."sellExecutingFirm",
p1."sellEnteringFirm",p1."sellEnteringTrader",p1."sellClientID",p1."codIsin",p1."securityType",p1."securityExchange",p1."orderID"

FROM "BSCL.Orden.BD::MRV.PUNTAS_RV" p1
WHERE "symbol" = :symbol and "settlement" = :settlement and "timesTamp" < :timesTamp and "orderID" not in 
	(select "orderID" from  "BSCL.Orden.BD::MRV.PUNTAS_RV" p2  where "action" = 'E' and p1."orderID" = p2."orderID")
	and "sellExecutingFirm" != '' and "action" = 'S'
order by "entryPositionNo" asc

