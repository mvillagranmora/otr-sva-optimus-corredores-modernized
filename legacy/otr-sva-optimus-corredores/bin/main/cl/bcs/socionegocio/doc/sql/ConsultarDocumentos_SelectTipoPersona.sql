SELECT "Tipo"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = CLI."IdPersona" AND PER."IdPersona" IS NOT NULL)
WHERE CLI."IdCliente" = :p_IdCliente;