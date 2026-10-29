SELECT 
DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
,PORTAFOLIO."CodigoPortafolio"
,PORTAFOLIO."IdCliente"
,PORTAFOLIO."IdPortafolio"
,CLIENTE."IdEstado" AS "IdEstadoCliente"
,PORTAFOLIO."IdEstado" AS "IdEstadoPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdPersona" = CLIENTE."IdPersona"
WHERE DOCUMENTO_IDENTIDAD."DocumentoIdentidad" IN (:DocumentoIdentidad) AND CLIENTE."Borrado" = 'N' AND PORTAFOLIO."Proposito" != 'PROPOSITOAPV'