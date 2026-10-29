-- Obtiene datos de los Documentos de Identidad modificar
SELECT		
		IDENT."IdDocumentoIdentidad",
		IDENT."IdPersona",
		IDENT."TipoPersona",		
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		IDENT."IdEstado",
		IDENT."NumeroSerie",
		IDENT."FechaEmision",
		IDENT."FechaVencimiento",
		IDENT."Predeterminado",
		IDENT."DocumentoIdentidad",
		IDENT."IdTipoDocumentoIdentidad",
		IDENT."DocumentoIdentidadAsociado",
		IDENT."IdTipoDocumentoIdentidadAsociado"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  AS IDENT
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = IDENT."IdPersona")
WHERE  IDENT."IdDocumentoIdentidad" IN (:IdDocumentoIdentidad);

