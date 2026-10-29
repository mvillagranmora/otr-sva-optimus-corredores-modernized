SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::FUT.ActualizaPuntasMarketDataCompra";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::FUT.ActualizaPuntasMarketDataCompra" ( 
    IN timesTamp VARCHAR(30),
	IN instrumnet VARCHAR(50),
	IN volume DECIMAL(20,4),
	IN price DECIMAL(20,4),
	IN executingFirm VARCHAR(6),
	IN enteringFirm VARCHAR(6)
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	UPDATE "BSCL.Orden.BD::FUT.PUNTAS_FU"
	SET 
		"timesTamp" = :timesTamp,
		"volume" = :volume,
		"price" = :price,
		"executingFirm" = :executingFirm,
		"enteringFirm" = :enteringFirm
	WHERE
		"instrumnet" = :instrumnet;


END;
--SELECT * FROM "BSCL.Orden.BD::FUT.PUNTAS_FU"