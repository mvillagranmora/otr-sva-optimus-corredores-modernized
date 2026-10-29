SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::FUT.ActualizaPuntasMarketDataVenta";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::FUT.ActualizaPuntasMarketDataVenta" ( 
    IN timesTamp VARCHAR(30),
	IN instrumnet VARCHAR(50),
	IN amountSale DECIMAL(20,4),
	IN priceSale DECIMAL(20,4),
	IN executingFirmOffer VARCHAR(6),
	IN enteringFirmOffer VARCHAR(6)
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	UPDATE "BSCL.Orden.BD::FUT.PUNTAS_FU"
	SET 
		"timesTamp" = :timesTamp,
		"amountSale" = :amountSale,
		"priceSale" = :priceSale,
		"executingFirmOffer" = :executingFirmOffer,
		"enteringFirmOffer" = :enteringFirmOffer
	WHERE
		"instrumnet" = :instrumnet;


END;
--SELECT * FROM "BSCL.Orden.BD::FUT.PUNTAS_FU"