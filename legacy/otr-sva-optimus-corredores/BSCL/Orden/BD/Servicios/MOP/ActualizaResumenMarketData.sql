SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::MOP.ActualizaResumenMarketData";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::MOP.ActualizaResumenMarketData"(
	c_codTicker varchar(30),
	c_timeTicker varchar(30),
	c_securityId varchar(30),
	c_symbol varchar(50),
	c_securityExchange varchar(30),
	c_cpRegType varchar(30),
	c_avgPrice varchar(30)
)
AS
BEGIN
	DECLARE c_count decimal(10,0);

	SELECT COUNT(*) INTO c_count FROM "BSCL.Orden.BD::MOP.RESUMEN_OP"
	WHERE "symbol" = c_symbol;

	IF c_count > 0 THEN
		UPDATE "BSCL.Orden.BD::MOP.RESUMEN_OP"
		SET 
			"codTicker" = c_codTicker,
			"timeTicker" = c_timeTicker,
			"securityId" = c_securityId, 
			"securityExchange" = c_securityExchange, 
			"cpRegType" = c_cpRegType,
			"avgPrice" = c_avgPrice
		WHERE 
			"symbol" = c_symbol;
	ELSE
		INSERT INTO "BSCL.Orden.BD::MOP.RESUMEN_OP"
			   ("codTicker"
			   ,"timeTicker"
			   ,"securityId"
			   ,"symbol"
			   ,"securityExchange"
			   ,"cpRegType"
			   ,"avgPrice")
		 VALUES
			   (c_codTicker
			   ,c_timeTicker
			   ,c_securityId
			   ,c_symbol
			   ,c_securityExchange
			   ,c_cpRegType
			   ,c_avgPrice);
	END IF;
END;