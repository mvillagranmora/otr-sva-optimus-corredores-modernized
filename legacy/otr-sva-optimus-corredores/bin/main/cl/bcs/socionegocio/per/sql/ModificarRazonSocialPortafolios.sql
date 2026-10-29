UPDATE POR
SET POR."Alias" = :RazonSocial
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
WHERE CLI."IdPersona" = :IdPersona