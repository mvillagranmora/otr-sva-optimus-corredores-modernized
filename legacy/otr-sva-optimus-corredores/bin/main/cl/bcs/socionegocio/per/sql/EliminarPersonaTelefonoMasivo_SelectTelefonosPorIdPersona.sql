SELECT		
		TEL."IdTelefono" AS "IdEntidadPersonaEliminar",
		TEL."IdPersona",
		PER."Tipo" AS "TipoPersona",
		COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		TEL."Borrado",
		TEL."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.Telefono"  AS TEL
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = TEL."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE
	TEL."IdPersona" IN (:IdEntidad)
	AND TEL."Borrado" = 'N';