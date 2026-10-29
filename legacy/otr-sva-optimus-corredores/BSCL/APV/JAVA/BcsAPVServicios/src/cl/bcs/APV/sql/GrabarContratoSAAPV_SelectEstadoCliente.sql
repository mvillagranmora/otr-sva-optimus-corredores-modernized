SELECT CLI."IdCliente", DOC."DocumentoIdentidad", CLI."IdEstado" 
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (CLI."IdPersona" = DOC."IdPersona")
WHERE DOC."IdPersona" IS NOT NULL AND DOC."DocumentoIdentidad" IN (:documentoIdentidad)
AND DOC."Predeterminado" = 'S';