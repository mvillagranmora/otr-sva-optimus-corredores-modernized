SELECT		
		IDENT."IdDocumentoIdentidad" AS "IdEntidadPersonaEliminar",
		IDENT."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		IDENT."Borrado"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  AS IDENT
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = IDENT."IdPersona")
WHERE  IDENT."IdDocumentoIdentidad" IN (:IdEntidad);