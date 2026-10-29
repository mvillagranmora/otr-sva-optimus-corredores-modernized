SELECT
	DOC."IdTarjetaBancaria" AS "IdTarjetaBancaria",
	DOC."IdPersona" AS "IdPersona",
	COALESCE(CAST(DOC."IdBanco" AS NVARCHAR(255)),'') AS "IdBanco",
	COALESCE(CAST(DOC."IdTipoTarjetaBancaria" AS NVARCHAR(255)),'') AS "IdTipoTarjetaBancaria",
	COALESCE(CAST(DOC."NumeroTarjetaBancaria" AS NVARCHAR(255)),'') AS "NumeroTarjetaBancaria",
	COALESCE(CAST(DOC."FechaVencimiento" AS NVARCHAR(255)),'') AS "FechaVencimiento",
	COALESCE(CAST(DOC."CodigoVerificacion" AS NVARCHAR(255)),'') AS "CodigoVerificacion",
	COALESCE(CAST(DOC."IdEstado" AS NVARCHAR(255)),'') AS "IdEstado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidadPersona",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" DOC
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = DOC."IdPersona")
WHERE DOC."IdTarjetaBancaria" IN (:IdTarjetaBancaria);
