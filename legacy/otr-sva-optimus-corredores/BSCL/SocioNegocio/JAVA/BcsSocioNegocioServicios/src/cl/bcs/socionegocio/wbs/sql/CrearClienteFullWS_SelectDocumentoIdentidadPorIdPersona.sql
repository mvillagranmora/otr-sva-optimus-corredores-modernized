SELECT
	DI."IdTipoDocumentoIdentidad" AS "TipoDocumento",
	DI."DocumentoIdentidad" AS "NumeroDocumento",
	DI."NumeroSerie",
	--DI."FechaEmision",
	TO_NVARCHAR(DI."FechaEmision", 'YYYY-MM-DD') AS "FechaEmision",
	--DI."FechaVencimiento"
	TO_NVARCHAR(DI."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI 
WHERE DI ."IdPersona" IN (:IdPersona);