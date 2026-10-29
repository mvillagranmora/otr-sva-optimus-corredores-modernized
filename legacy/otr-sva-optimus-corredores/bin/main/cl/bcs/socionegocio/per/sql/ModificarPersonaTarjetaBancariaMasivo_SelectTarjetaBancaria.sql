SELECT		
		TJB."IdTarjetaBancaria",
		TJB."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
		IDENT."DocumentoIdentidad",
		TJB."IdBanco",
		TJB."IdTipoTarjetaBancaria",
		TJB."NumeroTarjetaBancaria",
		TJB."FechaVencimiento",
		TJB."CodigoVerificacion",
		TJB."IdEstado"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"  AS TJB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = TJB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  TJB."IdTarjetaBancaria" IN (:IdTarjetaBancaria);