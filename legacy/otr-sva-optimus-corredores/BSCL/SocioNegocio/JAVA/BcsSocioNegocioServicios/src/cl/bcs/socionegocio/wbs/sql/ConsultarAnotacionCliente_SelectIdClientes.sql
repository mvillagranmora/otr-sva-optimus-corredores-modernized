SELECT 
	COALESCE(CLI."IdCliente", 0) AS "IdCliente",
	COALESCE(POR."IdPortafolio", 0) AS "IdPortafolio"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCI
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = DOCI."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente" AND POR."CodigoPortafolio" = :CodigoPortafolio
WHERE DOCI."DocumentoIdentidad" IN (:DocumentoIdentidad);