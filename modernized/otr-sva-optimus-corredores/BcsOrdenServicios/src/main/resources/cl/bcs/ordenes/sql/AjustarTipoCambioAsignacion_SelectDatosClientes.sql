
SELECT 
	 CLI."IdCliente" 
	,PER."RazonSocial" AS "NombreCliente"
	,CLI."IdEstado"  AS "EstadoCliente"
	,IDE."NombreTipoDocumentoIdentidad" AS "TipoDocumento"
	,PDI."DocumentoIdentidad" 
	,POR."IdPortafolio" 
	,POR."CodigoPortafolio" 
	,POR."IdEstado" AS "EstadoPortafolio"
	,PER."IdPersona"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS  CLI 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona" 
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS  POR ON CLI."IdCliente"  = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" PDI ON PDI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" 
INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS IDE ON  IDE."IdTipoDocumentoIdentidad" = PDI."IdTipoDocumentoIdentidad" 
WHERE CLI."IdCliente" IN (:IdCliente) 
AND POR."IdPortafolio" IN (:IdPortafolio)