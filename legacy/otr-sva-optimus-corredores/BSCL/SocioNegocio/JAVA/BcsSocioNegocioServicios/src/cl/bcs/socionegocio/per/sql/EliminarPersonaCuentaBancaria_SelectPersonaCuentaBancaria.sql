SELECT
	CTB."IdCuentaBancaria" AS "IdCuentaBancaria",
	CTB."IdPersona" AS "IdPersona",
	COALESCE(CTB."Borrado", '') AS "Borrado",
	COALESCE(CTB."NumeroCuentaBancaria", '') AS "NumeroCuentaBancaria",
	COALESCE(P."Nombres", '') || ' ' || COALESCE(P."ApellidoPaterno", '') || ' ' || COALESCE(P."ApellidoMaterno", '') AS "NombreCompleto",
	COALESCE(DI."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(DI."IdTipoDocumentoIdentidad", '') AS "IdTipoDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CTB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" P ON (P."IdPersona" = CTB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON (DI."IdDocumentoIdentidad" = P."IdDocumentoIdentidad")
WHERE CTB."IdCuentaBancaria" IN (:IdCuentaBancaria);
