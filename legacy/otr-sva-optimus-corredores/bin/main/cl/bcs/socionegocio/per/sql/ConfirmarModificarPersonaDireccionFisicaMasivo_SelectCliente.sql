SELECT CLI."IdCliente",CLI."IdPersona",CLI."IdTipoCliente" FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIF 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON DIF."IdPersona" = CLI."IdPersona"
WHERE DIF."IdDireccionFisica" = (:IdDireccionFisica)