SELECT	PORT."IdPortafolio","IdDireccionNotificacion","OperaCustodiaRV","OperaCustodiaRFIF","PermiteVenderNominalRV","PermiteVenderNominalRFIF",
		"OperaCuentaInversion",
		"OperaLineaSobregiro",
		"MecanismoCaptacion",
		"IdLibroOrdenes",
		CLI."EsClienteFinal",
		BRO."IdPersona" AS "IdPersonaBrokerExtranjero",
		"CodigoIfrs"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = PORT."IdCliente"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" IND ON PORT."IdPortafolio" = IND."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BrokerExtranjero" BRO ON BRO."IdPersona" = CLI."IdPersona"
WHERE PORT."IdPortafolio" IN (:IdPortafolio)
