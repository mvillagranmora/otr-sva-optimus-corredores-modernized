
SELECT
	ECC."IdEventoCapital",
	ECC."IdEventoCapitalCliente",
	ECC."IdTipoEventoCapital",
	ECC."IdEstado",
	EC."IdEmpresa"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS ECC
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EC ON (EC."IdEventoCapital" = ECC."IdEventoCapital")
WHERE "IdEventoCapitalCliente" IN (:IdEventoCapitalCliente);
