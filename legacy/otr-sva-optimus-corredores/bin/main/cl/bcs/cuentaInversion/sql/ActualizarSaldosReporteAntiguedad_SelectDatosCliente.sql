SELECT CTA."IdCuentaInversion",CTA."IdCliente",CTA."IdPortafolio",CTA."IdMoneda" FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON DOC."IdPersona" = CLI."IdPersona"
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON POR."IdCliente" = CLI."IdCliente"
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA
	ON CTA."IdCliente" = CLI."IdCliente" AND CTA."IdPortafolio" = POR."IdPortafolio" 
	WHERE DOC."DocumentoIdentidad" = :Rut AND POR."CodigoPortafolio" = :CodigoPortafolio AND CTA."IdMoneda" = :IdMoneda;