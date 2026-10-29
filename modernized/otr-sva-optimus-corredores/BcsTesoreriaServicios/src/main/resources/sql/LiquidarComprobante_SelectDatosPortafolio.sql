SELECT
IDENT."DocumentoIdentidad"
,PERSONA_NOMBRE."NombreCompleto"
,PORT."IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON PORT."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PERSONA_NOMBRE ON PERSONA_NOMBRE."IdPersona" = PER."IdPersona"
WHERE "IdPortafolio" IN (:IdPortafolio)