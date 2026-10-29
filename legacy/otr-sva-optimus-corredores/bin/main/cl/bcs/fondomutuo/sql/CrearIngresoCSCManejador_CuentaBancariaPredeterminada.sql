SELECT 
	 BAN."CodigoBanco"
	,BAN."IdBanco" 
	,CTA."IdCuentaBancaria"
	,CTA."NumeroCuentaBancaria"
	,BAN."NombreBanco"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "CTA"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SON"  ON "CTA"."IdPersona" = "SON"."IdPersona"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BAN" ON ("CTA"."IdBanco" = "BAN"."IdBanco")
WHERE "Predeterminado" = 'S'
AND SON."IdCliente" = :IdCliente
AND CTA."IdMoneda" = 'CLP'