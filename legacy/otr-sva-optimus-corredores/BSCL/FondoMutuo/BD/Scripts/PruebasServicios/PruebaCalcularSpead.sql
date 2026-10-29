SET SCHEMA BSCL;
DROP table #custodia;

CREATE LOCAL TEMPORARY TABLE #custodia ((
									"NumeroCalculoSpreadRescate"		BIGINT,
									"ValorFactorReajuste"				DECIMAL(26,8),
									"CostoFactorReajusteRescate"		DECIMAL(26,8),
									"CantidadRescate"					DECIMAL(26,8),
									"ValorCuota"						DECIMAL(26,8)
								);
INSERT INTO #custodia
SELECT 1,26000,0.25511906,3,2.5123 FROM DUMMY;