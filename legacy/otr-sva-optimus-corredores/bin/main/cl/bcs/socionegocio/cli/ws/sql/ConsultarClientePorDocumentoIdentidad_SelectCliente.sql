SELECT
CLIENTE."IdCliente"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdPersona" = CLIENTE."IdPersona"
WHERE DOCUMENTO_IDENTIDAD."DocumentoIdentidad" IN (:DocumentoIdentidad) AND CLIENTE."Borrado" = 'N'