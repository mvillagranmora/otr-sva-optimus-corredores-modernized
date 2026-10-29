SELECT 
	POR."IdPortafolio",
	POR."CodigoPortafolio",
	CLI."NombreCompleto"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON
	POR."IdCliente" = CLI."IdCliente"
WHERE "IdPortafolio" IN (:idsPortafolios);