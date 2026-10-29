SELECT PER."DocumentoIdentidad"
FROM
"BSCL.SocioNegocio.BD::CLI.Cliente" CLI
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" PER ON CLI."IdPersona"=PER."IdPersona"
WHERE CLI."IdCliente" = :p_IdClienteSolicita