SELECT CLI."IdCliente", DOC."DocumentoIdentidad", CLI."IdEstado" 
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (CLI."IdPersona" = DOC."IdPersona") AND DOC."Predeterminado" ='S'
WHERE DOC."IdPersona" IS NOT NULL AND CLI."IdCliente" IN (:idCliente);