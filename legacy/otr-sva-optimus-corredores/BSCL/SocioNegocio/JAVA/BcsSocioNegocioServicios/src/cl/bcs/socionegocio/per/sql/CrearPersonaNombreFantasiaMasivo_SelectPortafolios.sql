SELECT 
	POR."IdPortafolio",
	PER."IdPersona"
FROM 
	"BSCL.SocioNegocio.BD::PER.Persona" AS PER
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"
WHERE
	PER."IdPersona" IN (:IdPersona)