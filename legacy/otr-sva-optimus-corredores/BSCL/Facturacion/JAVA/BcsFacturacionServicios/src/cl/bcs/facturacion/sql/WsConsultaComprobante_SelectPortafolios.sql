
SELECT DISTINCT (POR."CodigoPortafolio") AS "codigoPortafolio"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  AS DOC
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad" 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona" 
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente" 
WHERE DOC."DocumentoIdentidad" = :rut 
GROUP BY POR."CodigoPortafolio"