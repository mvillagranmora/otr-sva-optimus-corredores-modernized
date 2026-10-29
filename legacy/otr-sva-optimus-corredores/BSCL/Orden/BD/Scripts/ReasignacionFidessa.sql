SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ReasignacionFidessa";
CREATE PROCEDURE

"BSCL.Orden.BD.Servicios::ReasignacionFidessa" (
	IN p_companyId			BIGINT,
	IN p_userId				BIGINT,
	IN p_reallocationOrigin	NVARCHAR(10),									--'ALLOC': REALLOCATION FROM ALLOCATION, 'TRADE': REALLOCATION FROM TRADE
	IN p_reallocations 		TABLE(	"messageSeq" 			BIGINT, 		--MESSAGE SEQUENCE NUMBER
									"allocationId" 			BIGINT,			--OPTIMUS ALLOCATION ID, 		MANDATORY IF REALLOCATION FROM ALLOCATION
									"tradeFolio"			NVARCHAR(40),	--TRADE FOLIO(IN BCS), 			MANDATORY IF REALLOCATION FROM TRADE
									"tradeSide"				NVARCHAR(1),	--'B' buy side, 'S' sell side, 	MANDATORY IF REALLOCATION FROM TRADE
									"tradeDate"				NVARCHAR(10),	--TRADE DATE(IN BCS) YYYY-MM-DD,MANDATORY IF REALLOCATION FROM TRADE
									"portfolioId" 			BIGINT,			--OPTIMUS PORTFOLIO ID
									"quantity" 				DECIMAL(26,8),	--REALLOCATION QUANTITY
									"settlementCurrency"	NVARCHAR(3)),		--CURRENCY FOR CLIENT SETTLEMENT: CLP (CHILEAN PESO), USD (US DOLLARS), EUR (EUROS), ETC (OPTIMUS CURRENCY CODES)
	IN p_commissions	TABLE(
								"messageSeq" 			BIGINT,			--MESSAGE SEQUENCE NUMBER, REFERENCES REALLOCATION
								"commissionId"			NVARCHAR(6),	--OPTIMUS COMMISSION ID
								"comissionValue" 		DECIMAL(26,8)	--COMMISSION NEW VALUE
							), 

	OUT p_out_ErrorCounter	BIGINT,		--IF 0, THEN ALL THE REALLOCATIONS ARE CREATED, IF > 0 , THERE WAS AN ERROR AND NO REALLOCATIONS ARE CREATED

	OUT p_out_reallocations		TABLE (
									"messageSeq" 		BIGINT,				--MESSAGE SEQUENCE NUMBER
									"allocationId"		BIGINT,				--REALLOCATION ID
									"transacionId"		BIGINT,				--TRANSACTION ID
									"allocationDate"	SECONDDATE,			--REALLOCATION TIMESTAMP
									"quantity"			DECIMAL(26,8),		--QUANTITY
									"amount"			DECIMAL(26,8),		--AMOUNT (IN TRADE CURRENCY)
									"price"				DECIMAL(26,8),		--PRICE (TRADE PRICE)
									"tradeFolio"		NVARCHAR(40),		--TRADE FOLIO
									"tradeSide"			NVARCHAR(1),		--TRADE SIDE,Buy(B), Sell(S)
									"tradeDate"			NVARCHAR(10),		--TRADE DATE YYYY-MM-DD
									"portfolioId" 		BIGINT) DEFAULT EMPTY,	--OPTIMUS PORTFOLIO ID
	OUT p_out_allocationErrors	TABLE (
								"messageSeq" 		BIGINT,				--MESSAGE SEQUENCE NUMBER
								"errorCode"			NVARCHAR(100),		--OPTIMUS ERROR CODE
								"errorDescription"	NVARCHAR(520)		--OPTIMUS ERROR DESCRIPTION
								) DEFAULT EMPTY
	

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	
AS
BEGIN
	/*p_out_ErrorCounter = 0;
	p_out_reallocations = 	SELECT "messageSeq","allocationId","IdTransaccion" AS "transacionId","FechaOperacion" AS "allocationDate","quantity","Monto" AS "amount","Precio" AS "price","tradeFolio","tradeSide","tradeDate","portfolioId"
						 	FROM :p_reallocations
						 	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on tran."IdTransaccion" = 70286129;
		*/
END;
