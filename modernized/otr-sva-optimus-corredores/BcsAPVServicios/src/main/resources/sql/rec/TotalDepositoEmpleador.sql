SELECT 
	TO_DECIMAL(COALESCE(SUM("MontoDepositoConvenido"), 0) + COALESCE(SUM("MontoDepositoAPV"), 0), 26,8) AS "Total" 
FROM "BSCL.APV.BD::REC.Empleador"
WHERE "IdRecaudacion" = :IdRecaudacion;