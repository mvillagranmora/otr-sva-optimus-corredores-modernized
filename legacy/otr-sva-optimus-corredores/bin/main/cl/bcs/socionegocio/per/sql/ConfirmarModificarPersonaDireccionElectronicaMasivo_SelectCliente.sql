SELECT CLI."IdCliente",CLI."IdPersona",CLI."IdTipoCliente" FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIE 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON DIE."IdPersona" = CLI."IdPersona"
WHERE DIE."IdDireccionElectronica" = (:IdDireccionElectronica)