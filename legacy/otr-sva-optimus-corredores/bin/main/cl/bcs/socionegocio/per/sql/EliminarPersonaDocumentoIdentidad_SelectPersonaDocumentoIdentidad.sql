SELECT
	DI."IdDocumentoIdentidad" AS "IdDocumentoIdentidad",
	DI."IdPersona" AS "IdPersona",
	DI."Borrado" AS "Borrado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = DI."IdPersona")
WHERE DI."IdDocumentoIdentidad" IN (:IdDocumentoIdentidad);
