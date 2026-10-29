-- Obtiene datos del cliente portafolio
SELECT DISTINCT POR."IdPortafolio", POR."CodigoPortafolio", Ident."DocumentoIdentidad" 
FROM "BSCL.Orden.BD::GBL.AsignacionMasivaPorArchivo_Entrada" AS GBL
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."CodigoPortafolio" = GBL."CodigoPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" AND IDENT."DocumentoIdentidad" = GBL."RutCliente")
WHERE CLI."IdEmpresa" = :IdEmpresa;