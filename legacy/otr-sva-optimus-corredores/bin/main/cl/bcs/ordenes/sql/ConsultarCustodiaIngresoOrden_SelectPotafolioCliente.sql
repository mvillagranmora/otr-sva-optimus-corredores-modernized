

SELECT
	POR."IdPortafolio",
	POR."CodigoPortafolio",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno"			
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	COALESCE(IDENT."DocumentoIdentidad",'')  AS "DocumentoIdentidad"
		
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente"=POR."IdCliente") 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona"=CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE "IdPortafolio" IN (:IdsPortafolio);
