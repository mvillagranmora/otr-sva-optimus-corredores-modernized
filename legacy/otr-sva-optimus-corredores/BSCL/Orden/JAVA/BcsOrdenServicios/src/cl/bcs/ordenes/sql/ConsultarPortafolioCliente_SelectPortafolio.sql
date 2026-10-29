SELECT Port."IdPortafolio", Port."CodigoPortafolio", Ident."DocumentoIdentidad" 
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Port
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli
ON Cli."IdCliente" = Port."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per
ON Per."IdPersona" = Cli."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS Ident
ON Ident."IdDocumentoIdentidad" = Per."IdDocumentoIdentidad"
WHERE Ident."DocumentoIdentidad" IN (:documentoIdentidad) AND Port."CodigoPortafolio" IN (:codigoPortafolio)