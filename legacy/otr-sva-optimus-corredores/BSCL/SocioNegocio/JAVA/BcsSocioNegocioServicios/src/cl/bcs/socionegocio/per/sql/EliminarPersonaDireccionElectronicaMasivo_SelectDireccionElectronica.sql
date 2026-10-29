SELECT		
		DIRE."IdDireccionElectronica" AS "IdEntidadPersonaEliminar",
		DIRE."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		DIRE."Borrado",
		DIRE."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"  AS DIRE
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = DIRE."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  DIRE."IdDireccionElectronica" IN (:IdEntidad);