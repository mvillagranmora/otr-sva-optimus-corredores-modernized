SET SCHEMA BSCL;
DROP table #custodia;

CREATE LOCAL TEMPORARY TABLE #custodia (
		"NumeroCalculoCostoFactorAjuste"	BIGINT,
		"CantidadRescate"					DECIMAL(26,8),
		"CantidadTotalCartera"				DECIMAL(26,8),
		"CostoFactorAjusteTotalCartera"		DECIMAL(26,8)
   
   );
INSERT INTO #custodia
SELECT 1,2,3,4 FROM DUMMY;

SELECT * FROM "BSCL.FondoMutuo.BD.Utilitarios::ObtenerCostoFactorAjuste"(#custodia);