SELECT		
		TEL."IdTelefono" AS "IdEntidadPersonaEliminar",
		TEL."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		TEL."Borrado",
		TEL."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.Telefono"  AS TEL
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = TEL."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  TEL."IdTelefono" IN (:IdEntidad);