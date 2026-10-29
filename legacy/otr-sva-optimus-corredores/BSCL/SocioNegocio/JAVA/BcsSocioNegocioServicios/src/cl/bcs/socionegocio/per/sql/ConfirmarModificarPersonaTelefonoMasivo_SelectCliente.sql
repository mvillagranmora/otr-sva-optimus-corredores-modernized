SELECT CLI."IdCliente",CLI."IdPersona" FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON TEL."IdPersona" = CLI."IdPersona"
WHERE TEL."IdTelefono" = (:IdTelefono)