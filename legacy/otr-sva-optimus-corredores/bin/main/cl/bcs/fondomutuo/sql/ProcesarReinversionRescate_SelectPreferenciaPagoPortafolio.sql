SELECT
	CPPP."IdPreferenciaPagoPortafolio"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" CPPP
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
ON CPPP."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
ON POR."IdCliente" = CLI."IdCliente"
WHERE CLI."IdCliente" = :p_IdCliente 
	  AND CPPP."IdPreferenciaPagoPortafolio" = :p_IdPreferenciaPagoPortafolio
