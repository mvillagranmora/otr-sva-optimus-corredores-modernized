SELECT PORT."IdPortafolio", PORT."CodigoPortafolio", CLI."IdPortafolioDefecto"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PORT."IdCliente"
WHERE PORT."IdCliente" = :IdCliente AND PORT."Borrado" = 'N'
