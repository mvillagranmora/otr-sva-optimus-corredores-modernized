SELECT		
		TJB."IdTarjetaBancaria",
		TJB."IdPersona",	
		IDENT."DocumentoIdentidad",
		TJB."IdBanco",
		TJB."IdTipoTarjetaBancaria",
		TJB."NumeroTarjetaBancaria",
		TJB."FechaVencimiento",
		TJB."CodigoVerificacion",
		TJB."IdEstado",
		PER."Tipo",
		CLI."TipoEnrolamiento",
		CLI."IdCliente"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"  AS TJB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = TJB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE  TJB."IdTarjetaBancaria" IN (:IdTarjetasBancarias);