SELECT 
DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
,PORTAFOLIO."CodigoPortafolio"
,PORTAFOLIO."IdCliente"
,CLIENTE."IdEstado" AS "IdEstadoCliente"
,PORTAFOLIO."IdPortafolio"
,PORTAFOLIO."IdEstado" AS "IdEstadoPortafolio"
,PERSONA_NOMBRE."NombreCompleto"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdPersona" = CLIENTE."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PERSONA_NOMBRE ON PERSONA_NOMBRE."IdPersona" = CLIENTE."IdPersona"
WHERE DOCUMENTO_IDENTIDAD."DocumentoIdentidad" IN (:DocumentoIdentidad) AND CLIENTE."Borrado" = 'N'