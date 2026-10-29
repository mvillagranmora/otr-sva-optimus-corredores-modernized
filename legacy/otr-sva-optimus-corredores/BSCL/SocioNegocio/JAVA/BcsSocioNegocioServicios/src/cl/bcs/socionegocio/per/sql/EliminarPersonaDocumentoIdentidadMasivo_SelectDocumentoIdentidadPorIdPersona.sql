SELECT		
	IDENT."IdDocumentoIdentidad" AS "IdEntidadPersonaEliminar",
	IDENT."IdPersona",
	PER."Tipo" AS "TipoPersona" ,
	COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento" ,
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
		ELSE PER."RazonSocial"
	END  AS "NombreCompleto",
	IDENT."DocumentoIdentidad",
	IDENT."Borrado"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  AS IDENT
 	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = IDENT."IdPersona")
WHERE  IDENT."IdPersona" IN (:IdEntidad)
	AND IDENT."Borrado" = 'N';