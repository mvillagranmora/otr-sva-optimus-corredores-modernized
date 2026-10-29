SELECT		
		CTAB."IdCuentaBancaria" AS "IdEntidadPersonaEliminar",
		CTAB."IdPersona",
		PER."Tipo" AS "TipoPersona",
		COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		CTAB."Borrado",
		CTAB."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"  AS CTAB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CTAB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  CTAB."IdPersona" IN (:IdEntidad)
	AND CTAB."Borrado" = 'N';