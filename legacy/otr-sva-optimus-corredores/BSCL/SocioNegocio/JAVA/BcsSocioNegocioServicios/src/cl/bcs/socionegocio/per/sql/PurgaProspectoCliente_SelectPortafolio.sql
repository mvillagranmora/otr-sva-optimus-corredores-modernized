SELECT PER."IdPersona", CLI."IdCliente", POR."IdPortafolio" 
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
ON PER."IdPersona" = CLI."IdPersona" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
ON POR."IdCliente" = CLI."IdCliente"
WHERE PER."IdPersona" IN (:IdsPersonas);