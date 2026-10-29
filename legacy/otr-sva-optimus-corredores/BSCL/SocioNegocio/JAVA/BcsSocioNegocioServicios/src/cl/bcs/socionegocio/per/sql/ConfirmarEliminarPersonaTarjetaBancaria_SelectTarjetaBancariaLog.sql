SELECT
	TARB."IdTarjetaBancaria" AS "IdTarjetaBancaria",
	TARB."IdPersona" AS "IdPersona",
	COALESCE(TARB."NumeroTarjetaBancaria", '') AS "NumeroTarjetaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" TARB
WHERE TARB."IdTarjetaBancaria" IN (:IdTarjetaBancaria);
