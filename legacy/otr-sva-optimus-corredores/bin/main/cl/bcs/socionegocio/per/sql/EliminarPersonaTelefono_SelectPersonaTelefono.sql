SELECT
	T."IdTelefono" AS "IdTelefono",
	T."IdPersona" AS "IdPersona",
	T."Borrado" AS "Borrado",
	T."Predeterminado" AS "Predeterminado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" T
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = T."IdPersona")
WHERE T."IdTelefono" IN (:IdTelefono);
