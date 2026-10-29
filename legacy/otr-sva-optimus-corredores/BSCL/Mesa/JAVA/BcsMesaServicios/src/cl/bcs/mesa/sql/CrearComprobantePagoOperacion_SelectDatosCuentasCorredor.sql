SELECT 
	 CTA."IdCuentaBanco" AS "IdCuentaBancaria"
	,CTA."NumeroCuentaBancaria" 
	, BCO."NombreBanco" 
FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA
INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BCO ON BCO."IdBanco" = CTA."IdBanco" 
WHERE CTA."IdCuentaBanco" IN  (:IdCuentaBancaria)

