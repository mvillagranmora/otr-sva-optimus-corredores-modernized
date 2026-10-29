UPDATE POR
	SET POR."IdTelefonoContacto"  = TEL."IdTelefono",
	    POR."IdDireccionEnvioCorrespondencia" = DIRF."IdDireccionFisica" 
FROM 
	"BSCL.Portafolio.BD::POR.Portafolio" POR
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente" 
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL ON TEL."IdPersona" = CLI."IdPersona" 
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIRF ON DIRF."IdPersona" = CLI."IdPersona" 
	WHERE CLI."IdCliente" = :IdCliente AND POR."IdPortafolio" = :IdPortafolio;