SELECT		
		TARJ."IdTarjetaBancaria" AS "IdEntidadPersonaEliminar",
		TARJ."IdPersona",
		PER."Tipo" AS "TipoPersona",
		COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		TARJ."Borrado"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"  AS TARJ
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = TARJ."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE
	TARJ."IdPersona" IN (:IdEntidad)
	AND TARJ."Borrado" = 'N';