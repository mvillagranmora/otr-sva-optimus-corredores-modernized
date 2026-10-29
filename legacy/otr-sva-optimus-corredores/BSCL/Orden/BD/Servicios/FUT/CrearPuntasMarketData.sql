SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::FUT.CrearPuntasMarketData";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::FUT.CrearPuntasMarketData" (  
    IN codTicker VARCHAR(10),
	IN timesTamp VARCHAR(30),
	IN action VARCHAR(3),
	IN instrumnet VARCHAR(50),
	IN volume DECIMAL(20,4),
	IN amountSale DECIMAL(20,4),
	IN price DECIMAL(20,4),
	IN priceSale DECIMAL(20,4),
	IN settlement VARCHAR(20),
	IN bookingRefID VARCHAR(3),
	IN executingFirm VARCHAR(6),
	IN enteringFirm VARCHAR(6),
	IN executingTrader VARCHAR(6),
	IN executingFirmOffer VARCHAR(6),
	IN enteringFirmOffer VARCHAR(6),
	IN executingTraderOffer VARCHAR(6),
	IN codIsin VARCHAR(15),
	IN securityType VARCHAR(15),
	IN securityExchange VARCHAR(15),
	IN entryType VARCHAR(15),
	IN orderID VARCHAR(30)
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	INSERT INTO "BSCL.Orden.BD::FUT.PUNTAS_FU"
	(	"codTicker"
	   ,"timesTamp"
	   ,"action"
	   ,"instrumnet"
	   ,"volume"
	   ,"amountSale"
	   ,"price"
	   ,"priceSale"
	   ,"settlement"
	   ,"bookingRefID"
	   ,"executingFirm"
	   ,"enteringFirm"
	   ,"executingTrader"
	   ,"executingFirmOffer"
	   ,"enteringFirmOffer"
	   ,"executingTraderOffer"
	   ,"codIsin"
	   ,"securityType"
	   ,"securityExchange"
	   ,"entryType"
	   ,"orderID")
     VALUES
           (:codTicker
           ,:timesTamp
           ,:action
           ,:instrumnet
           ,:volume
           ,:amountSale
           ,:price
           ,:priceSale
           ,:settlement
           ,:bookingRefID
           ,:executingFirm
           ,:enteringFirm
           ,:executingTrader
           ,:executingFirmOffer
           ,:enteringFirmOffer
           ,:executingTraderOffer
           ,:codIsin
           ,:securityType
           ,:securityExchange
           ,:entryType
           ,:orderID);


END;