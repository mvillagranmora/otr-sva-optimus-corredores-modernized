SELECT DA."IdDocumento"           AS "IdDocumento"
	 , DA."IdAnexo"               AS "IdAnexo"
	 , DA."NombreArchivoAdjunto"  AS "NombreArchivo"
	 , DA."FormatoArchivoAdjunto" AS "ExtensionArchivo"
	 , DA."ArchivoAdjunto"        AS "ContenidoArchivo"
FROM "BSCL.Plataforma.BD::DOC.Anexo" DA
	 LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON (DOC."IdDocumento" = DA."IdDocumento" AND DOC."IdDocumento" IS NOT NULL)
	 LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdCliente" = DOC."IdCliente" AND CLI."IdCliente" IS NOT NULL)
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON (DI."IdPersona" = CLI."IdPersona" AND DI."IdPersona" IS NOT NULL)
WHERE DI."DocumentoIdentidad" = :DocumentoIdentidadCliente
  AND DI."Borrado" = 'N'
  AND DI."IdEstado" = 'ACT'
  AND DA."IdAnexo" IN (:IdsAnexos);