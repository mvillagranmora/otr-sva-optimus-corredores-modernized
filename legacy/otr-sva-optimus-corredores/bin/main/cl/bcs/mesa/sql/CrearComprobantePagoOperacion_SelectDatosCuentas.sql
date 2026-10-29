SELECT 
	 CTA."IdCuentaBancaria"
	,CTA."NumeroCuentaBancaria" 
	, BCO."NombreBanco" 
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTA
INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BCO ON BCO."IdBanco" = CTA."IdBanco" 
WHERE CTA."IdCuentaBancaria" IN  (:IdCuentaBancaria)