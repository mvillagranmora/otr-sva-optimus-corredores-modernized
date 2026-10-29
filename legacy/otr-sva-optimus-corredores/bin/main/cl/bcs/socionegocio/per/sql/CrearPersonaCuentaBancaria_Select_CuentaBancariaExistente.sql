SELECT
	"IdBanco" AS "IdBanco",
	"NumeroCuentaBancaria" AS "NumeroCuentaBancaria",
	"IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
WHERE "IdPersona" IN (:IdPersona)
AND "Borrado" = 'N'
