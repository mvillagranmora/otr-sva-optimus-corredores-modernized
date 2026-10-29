SELECT 
	C."IdCliente" AS "IdCliente", 
	P."Nombres" AS "Nombres", 
	P."ApellidoPaterno" AS "ApellidoPaterno",
	P."ApellidoMaterno" AS "ApellidoMaterno", 
	C."IdEstado" AS "Estado",
	TDI."NombreTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
	DI."DocumentoIdentidad" AS "DocumentoIdentidad"
		FROM "BSCL.SocioNegocio.BD::PER.Persona" P
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON (DI."IdPersona" = P."IdPersona"  AND P."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" C ON (P."IdPersona" = C."IdPersona")
		LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" TDI ON (DI."IdTipoDocumentoIdentidad" = TDI."IdTipoDocumentoIdentidad")
			WHERE C."IdCliente" IN (:IdCliente)