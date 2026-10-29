SELECT 
	PER."IdPersona"
	,PER."NombreCompleto" 
	,DOCIDEN."DocumentoIdentidad"
	,DOCIDEN."IdTipoDocumentoIdentidad" 			AS "TipoDocumentoIdentidad"
	,CLI."IdCliente" 
	,CLI."IdEstado"									AS "EstadoCliente"
	,PORT."IdPortafolio"
	,PORT."IdEstado" 								AS "EstadoPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio"										AS PORT
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 									AS CLI
	ON("PORT"."IdCliente" = "CLI"."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" 	AS PER
	ON("CLI"."IdPersona" = "PER"."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"						AS DOCIDEN
	ON ("PER"."IdDocumentoIdentidad" = "DOCIDEN"."IdDocumentoIdentidad")
WHERE PORT."IdPortafolio" IN (:IdPortafolio);