-- Obtiene datos de la direccion electronica a modificar

SELECT		
		DIR."IdPersona",	
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
		IDENT."DocumentoIdentidad",
		DIR."Direccion"		
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"  AS DIR
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = DIR."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  DIR."IdDireccionElectronica" = :IdDireccionElectronica;
