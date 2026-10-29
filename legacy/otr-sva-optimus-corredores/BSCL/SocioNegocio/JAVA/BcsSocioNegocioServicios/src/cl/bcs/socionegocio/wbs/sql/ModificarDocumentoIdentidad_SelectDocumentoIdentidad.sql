-- Obtiene datos de los Documentos de Identidad modificar
SELECT		
		IDENT."IdDocumentoIdentidad",
		IDENT."IdPersona",
		IDENT."TipoPersona",		
		IDENT."IdEstado",
		IDENT."NumeroSerie",
		CAST(IDENT."FechaEmision" AS VARCHAR(10)) AS "FechaEmision",
		CAST(IDENT."FechaVencimiento" AS VARCHAR(10)) "FechaVencimiento",
		IDENT."Predeterminado",
		IDENT."DocumentoIdentidad" AS "NumeroDocumento",
		IDENT."IdTipoDocumentoIdentidad" AS "TipoDocumento",
		IDENT."DocumentoIdentidadAsociado",
		IDENT."IdTipoDocumentoIdentidadAsociado",
		PER."Tipo",
		CLI."TipoEnrolamiento",
		CLI."IdCliente"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  AS IDENT
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = IDENT."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE  IDENT."IdDocumentoIdentidad" IN (:IdDocumentoIdentidad);