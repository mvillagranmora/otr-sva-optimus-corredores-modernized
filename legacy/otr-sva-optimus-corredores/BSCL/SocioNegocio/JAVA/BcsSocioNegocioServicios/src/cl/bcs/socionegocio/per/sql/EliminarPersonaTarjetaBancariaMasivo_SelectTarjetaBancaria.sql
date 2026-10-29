SELECT		
		TARB."IdTarjetaBancaria" AS "IdEntidadPersonaEliminar",
		TARB."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		TARB."Borrado"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"  AS TARB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = TARB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  TARB."IdTarjetaBancaria" IN (:IdEntidad);