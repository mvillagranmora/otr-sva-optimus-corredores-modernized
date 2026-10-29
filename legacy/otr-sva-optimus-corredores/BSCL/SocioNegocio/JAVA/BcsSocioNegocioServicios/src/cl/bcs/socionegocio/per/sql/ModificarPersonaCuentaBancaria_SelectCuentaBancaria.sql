
-- Obtiene datos de la cuenta bancaria a modificar
SELECT		
		CBAN."IdPersona",	
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
		IDENT."DocumentoIdentidad",
		CBAN."NumeroCuentaBancaria"
		
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CBAN
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (CBAN."IdPersona" = PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  CBAN."IdCuentaBancaria" = :IdCuentaBancaria;
			
	