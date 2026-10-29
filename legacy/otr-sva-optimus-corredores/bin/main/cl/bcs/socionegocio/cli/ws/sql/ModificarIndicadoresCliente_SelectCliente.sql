SELECT Cliente."IdCliente"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON Persona."IdDocumentoIdentidad" = DocumentoIdentidad."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Persona."IdPersona" = Cliente."IdPersona"
WHERE DocumentoIdentidad."DocumentoIdentidad" = :DocumentoIdentidad AND DocumentoIdentidad."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad AND Cliente."IdCliente" IS NOT NULL;