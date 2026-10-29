SELECT 
	PER."IdPersona"
	,PERNOM."NombreCompleto" 
	,DOCIDEN."DocumentoIdentidad"
	,DOCIDEN."IdTipoDocumentoIdentidad" 			AS "TipoDocumentoIdentidad"
	,CLI."IdCliente" 
	,CLI."IdEstado" 								AS "EstadoCliente"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" 										AS CLI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" 	AS PER
	ON(CLI."IdPersona" = PER."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" 	AS PERNOM
	ON(PERNOM."IdPersona" = PER."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"						AS DOCIDEN
	ON (PER."IdDocumentoIdentidad" = DOCIDEN."IdDocumentoIdentidad")
WHERE CLI."IdCliente" IN (:IdCliente);