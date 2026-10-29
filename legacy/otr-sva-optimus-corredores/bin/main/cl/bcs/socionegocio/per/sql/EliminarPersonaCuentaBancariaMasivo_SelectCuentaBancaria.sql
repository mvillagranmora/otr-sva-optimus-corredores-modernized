SELECT		
		CTAB."IdCuentaBancaria" AS "IdEntidadPersonaEliminar",
		CTAB."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		CTAB."Borrado",
		CTAB."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"  AS CTAB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CTAB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  CTAB."IdCuentaBancaria" IN (:IdEntidad);