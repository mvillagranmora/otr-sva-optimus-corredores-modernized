SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::MOP.ActualizaPuntasMarketDataVenta";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::MOP.ActualizaPuntasMarketDataVenta"(
	IN c_codTicker varchar(30),
	IN c_timeStamp varchar(30),
	IN c_action varchar(2),
	IN c_symbol varchar(50),
	IN c_amount decimal(20,10),
	IN c_price decimal(20,10),
	IN c_settlement varchar(20),
	IN c_executingFirm varchar(10),
	IN c_enteringFirm varchar(10),
	IN c_enteringTrader varchar(10),
	IN c_entryPositionNo decimal(10,0),
	IN c_bookingRefID varchar(30)
)
AS
BEGIN
	DECLARE c_count decimal(10,0);

	SELECT COUNT(*) INTO c_count FROM "BSCL.Orden.BD::MOP.PUNTAS_OP"
	WHERE 
		"symbol" = c_symbol and 
		"settlement" = c_settlement;

	IF c_count > 0 THEN
		UPDATE "BSCL.Orden.BD::MOP.PUNTAS_OP"
		SET 
			"timeStamp" = c_timeStamp,
			"action" = c_action,
			"amountSale" = c_amount, 
			"priceSale" = c_price,
			"sellExecutingFirm" = c_executingFirm,
			"sellEnteringFirm" = c_enteringFirm,
			"sellEnteringTrader" = c_enteringTrader,
			"entryPositionNo" = c_entryPositionNo
		WHERE 
			"symbol" = c_symbol and 
			"settlement" = c_settlement;
	ELSE
		INSERT INTO "BSCL.Orden.BD::MOP.PUNTAS_OP"
			   ("codTicker"
			   ,"timeStamp"
			   ,"action"
			   ,"symbol"
			   ,"amountSale"
			   ,"priceSale"
			   ,"settlement"
			   ,"sellExecutingFirm"
			   ,"sellEnteringFirm"
			   ,"sellEnteringTrader"
			   ,"entryPositionNo"
			   ,"bookingRefID")
		 VALUES
			   (c_codTicker
			   ,c_timeStamp
			   ,c_action
			   ,c_symbol
			   ,c_amount
			   ,c_price
			   ,c_settlement
			   ,c_executingFirm
			   ,c_enteringFirm
			   ,c_enteringTrader
			   ,c_entryPositionNo
			   ,c_bookingRefID);
	END IF;
END;



