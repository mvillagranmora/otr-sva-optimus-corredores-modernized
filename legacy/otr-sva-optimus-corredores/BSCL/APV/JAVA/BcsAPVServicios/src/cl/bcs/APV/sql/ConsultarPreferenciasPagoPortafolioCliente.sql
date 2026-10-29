SELECT
	CPPP."IdPreferenciaPagoPortafolio", CPPP."IdPortafolio",*
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" CPPP
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
ON CPPP."IdPortafolio" = POR."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
ON POR."IdCliente" = CLI."IdCliente"
WHERE CLI."IdCliente" = :IdCliente
AND CPPP."IdMedioDePago" = 'CHE'
AND CPPP."IdTipoOperacion" = 'VENACN'
AND CPPP."Estado" = 'ACTIVO'
AND POR."IdEstado" <> 'INA';