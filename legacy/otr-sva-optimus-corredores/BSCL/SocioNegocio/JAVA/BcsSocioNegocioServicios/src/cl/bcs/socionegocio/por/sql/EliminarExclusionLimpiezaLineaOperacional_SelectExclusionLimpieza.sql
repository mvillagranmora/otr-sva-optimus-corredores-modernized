SELECT 
	CP."IdPortafolio"
	,CP."CodigoPortafolio"
	,CLI."DocumentoIdentidad"
	,CLI."NombreCompleto"
	,CP."ExcluyeLimpiezaLineaOperacional"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS CP
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI on CP."IdCliente" = CLI."IdCliente"
WHERE CP."IdPortafolio" IN (:idsPortafolio);