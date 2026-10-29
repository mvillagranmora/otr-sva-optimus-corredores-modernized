
SELECT POR."IdPortafolio", CLI."IdPersona"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = POR."IdCliente")
WHERE POR."IdPortafolio" IN (:IdsPortafolio);