SELECT  
PORT."IdPortafolio"  ,
PORT."CodigoPortafolio" ,
PORT."IdCliente" ,
PORT."IdEmpleadoEjecutivo"
,(PER."Nombres" ||' '||PER."ApellidoPaterno" ||' '||PER."RazonSocial") AS "NombreCompleto",
RUT."IdTipoDocumentoIdentidad" ,RUT."DocumentoIdentidad" 
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT 
INNER JOIN  "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PORT."IdCliente" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" =  CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" RUT  ON RUT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" 
WHERE  CLI."IdPersona"= :idPersona;