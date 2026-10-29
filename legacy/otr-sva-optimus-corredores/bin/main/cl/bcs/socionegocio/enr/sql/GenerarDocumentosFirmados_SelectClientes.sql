SELECT 
CLI."IdEmpresa" ,
CLI ."IdCliente" ,
PER."IdPersona",
CLI."IdPersonaEjecutivo",
DCID."IdTipoDocumentoIdentidad",
DCID."DocumentoIdentidad",
PER."Tipo" AS "IdTipoPersona"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DCID ON  DCID."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" 
WHERE CLI."IdCliente" =:IdCliente
AND CLI."IdEmpresa" = :IdEmpresa;
