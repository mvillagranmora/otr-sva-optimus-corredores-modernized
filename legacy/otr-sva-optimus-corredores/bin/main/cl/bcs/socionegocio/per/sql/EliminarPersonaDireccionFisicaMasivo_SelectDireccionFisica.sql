SELECT		
		DIRF."IdDireccionFisica" AS "IdEntidadPersonaEliminar",
		DIRF."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		DIRF."Borrado",
		DIRF."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS DIRF
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = DIRF."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  DIRF."IdDireccionFisica" IN (:IdEntidad);