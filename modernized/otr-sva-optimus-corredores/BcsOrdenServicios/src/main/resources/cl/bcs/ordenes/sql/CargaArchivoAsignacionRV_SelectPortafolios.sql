SELECT PORTAFOLIO."IdPortafolio",PORTAFOLIO."IdCliente",PORTAFOLIO."CodigoPortafolio",DOCUMENTO."DocumentoIdentidad" 
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORTAFOLIO
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = PORTAFOLIO."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO ON (CLIENTE."IdPersona" = DOCUMENTO."IdPersona")
WHERE DOCUMENTO."IdTipoDocumentoIdentidad" = 'RUT' AND PORTAFOLIO."CodigoPortafolio" IN (:CodigoPortafolios) AND DOCUMENTO."DocumentoIdentidad" IN (:DocumentoIdentidad)