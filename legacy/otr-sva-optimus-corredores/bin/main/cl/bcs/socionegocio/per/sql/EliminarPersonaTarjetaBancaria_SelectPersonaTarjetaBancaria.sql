SELECT
	TARB."IdTarjetaBancaria" AS "IdTarjetaBancaria",
	TARB."IdPersona" AS "IdPersona",
	TARB."Borrado" AS "Borrado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" TARB
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = TARB."IdPersona")
WHERE TARB."IdTarjetaBancaria" IN (:IdTarjetaBancaria);
