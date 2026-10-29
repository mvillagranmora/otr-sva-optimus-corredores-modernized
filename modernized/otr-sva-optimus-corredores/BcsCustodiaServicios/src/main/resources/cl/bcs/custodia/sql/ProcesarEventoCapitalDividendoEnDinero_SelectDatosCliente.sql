SELECT	CLI."IdCliente",
		PER."TipoDocumentoIdentidad",
		PER."DocumentoIdentidad", 
		PER."NombreCompleto", 
		COALESCE(PER."IdPaisResidencia", EMP."IdPais") AS  "IdPaisResidencia",
		TIPPER."IdTipoPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  
		ON ( PER."IdPersona" = CLI."IdPersona" )
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TIPPER 
		ON (TIPPER."IdTipoPersona" = PER."Tipo")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
		ON EMP."IdEmpresa" = :IdEmpresa
WHERE "IdCliente" IN (:IdCliente);