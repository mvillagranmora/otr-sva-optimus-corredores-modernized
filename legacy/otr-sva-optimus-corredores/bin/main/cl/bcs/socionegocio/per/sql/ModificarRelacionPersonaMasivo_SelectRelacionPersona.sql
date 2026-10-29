SELECT		
		REL."IdRelacionPersona",
		REL."IdTipoRelacion",
		REL."IdPersonaOrigen",
		REL."IdPersonaDestino",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
		IDENT."DocumentoIdentidad",
		REL."FechaInicioRelacion",
		REL."FechaFinRelacion",
		REL."IdEstado",
		REL."IdCiudad",
		REL."Ciudad",		
		REL."PorcentajeParticipacion",
		REL."RetiraDocumentos",
		REL."FinalidadRelacion",
		REL."CondicionesRelacion",
		REL."Calle",
		REL."Numero",
		REL."Email",
		REL."Telefono",
		REL."Complemento"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"  AS REL
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = REL."IdPersonaDestino")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  REL."IdRelacionPersona" IN (:IdRelacionPersona);