SELECT
	DOC."IdDocumentoIdentidad" AS "IdDocumentoIdentidad",
	DOC."IdPersona" AS "IdPersona",
	COALESCE(CAST(DOC."IdTipoDocumentoIdentidad" AS NVARCHAR(255)), '') AS "IdTipoDocumentoIdentidad",
	COALESCE(CAST(DOC."DocumentoIdentidad" AS NVARCHAR(255)), '') AS "DocumentoIdentidad",
	COALESCE(CAST(DOC."NumeroSerie" AS NVARCHAR(255)), '') AS "NumeroSerie",
	COALESCE(CAST(DOC."FechaEmision" AS NVARCHAR(255)), '') AS "FechaEmision",
	COALESCE(CAST(DOC."FechaVencimiento" AS NVARCHAR(255)), '') AS "FechaVencimiento",
	COALESCE(CAST(DOC."IdEstado" AS NVARCHAR(255)), '') AS "IdEstado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidadPersona",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = DOC."IdPersona")
WHERE DOC."IdDocumentoIdentidad" IN (:IdDocumentoIdentidad);
