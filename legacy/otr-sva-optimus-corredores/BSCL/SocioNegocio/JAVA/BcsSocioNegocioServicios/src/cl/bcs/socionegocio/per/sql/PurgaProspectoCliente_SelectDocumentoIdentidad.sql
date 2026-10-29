SELECT DI."IdPersona", CLI."IdCliente", DI."IdDocumentoIdentidad", DI."DocumentoIdentidad", DI."DocumentoIdentidadAsociado"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdPersona" = DI."IdPersona" 
WHERE DI."IdPersona" IN (:IdsPersonas);