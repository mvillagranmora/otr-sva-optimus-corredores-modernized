SELECT
POR."IdPortafolio",
DOC."IdTipoDocumentoIdentidad",
DOC."DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = DOC."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"
WHERE POR."IdPortafolio" = :idPortafolio AND DOC."Predeterminado" = 'S';