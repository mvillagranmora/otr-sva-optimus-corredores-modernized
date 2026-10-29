SELECT 
CLIWS."IdCliente" 
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCI
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLIWS ON CLIWS."IdPersona" = DOCI."IdPersona" 
WHERE DOCI."DocumentoIdentidad" = (:DocumentoIdentidad) AND DOCI."IdTipoDocumentoIdentidad" = (:TipoDocumentoIdentidad);