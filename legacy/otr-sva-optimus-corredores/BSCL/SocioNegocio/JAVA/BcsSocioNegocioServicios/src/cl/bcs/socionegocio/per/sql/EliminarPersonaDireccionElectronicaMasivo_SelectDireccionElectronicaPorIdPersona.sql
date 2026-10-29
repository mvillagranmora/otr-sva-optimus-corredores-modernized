SELECT		
		DIRE."IdDireccionElectronica" AS "IdEntidadPersonaEliminar",
		DIRE."IdPersona",
		PER."Tipo" AS "TipoPersona" ,
		COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento" ,
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		DIRE."Borrado",
		DIRE."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"  AS DIRE
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = DIRE."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  DIRE."IdPersona" IN (:IdEntidad)
	AND DIRE."Borrado" = 'N';