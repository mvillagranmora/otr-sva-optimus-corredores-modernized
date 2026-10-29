SELECT 
	DOC."DocumentoIdentidad" AS "DocumentoIdentidad",
	DOC."DocumentoIdentidad" AS "DocumentoIdentidadTraspaso"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Predeterminado" ='S'
WHERE CLI."IdCliente" = :IdCliente;