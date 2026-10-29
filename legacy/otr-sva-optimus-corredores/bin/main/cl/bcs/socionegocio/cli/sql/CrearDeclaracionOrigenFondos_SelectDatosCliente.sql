SELECT
CASE PER."Tipo" 
	WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" 
	ELSE PER."RazonSocial"
END AS "NombreCompleto"
,DI."DocumentoIdentidad" AS "DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE CLI."IdCliente" = :IdCliente