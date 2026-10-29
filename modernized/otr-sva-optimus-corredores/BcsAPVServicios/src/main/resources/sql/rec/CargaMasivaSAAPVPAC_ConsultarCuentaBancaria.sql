SELECT
	 PER."IdPersona"
	,COALESCE(BCO."IdBanco",0) AS "IdBanco"
	,COALESCE(BCO."CodigoBanco",'') AS "CodigoBanco"
	,COALESCE(CTA."IdCuentaBancaria",0) AS "IdCuentaBancaria"
	,COALESCE(CTA."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" BCO
	ON (BCO."CodigoBanco" IN (:CodigoBanco) AND
		BCO."IdEmpresa" = :IdEmpresa AND
		BCO."IdEstado" = 'ACT')
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CTA
	ON (CTA."NumeroCuentaBancaria" IN (:NumeroCuentaBancaria) AND
		PER."IdPersona" = CTA."IdPersona" AND
		BCO."IdBanco" = CTA."IdBanco" AND
		CTA."IdEstado" = 'ACT' AND
		CTA."Borrado" = 'N')
WHERE PER."IdPersona" IN (:IdPersona)
;