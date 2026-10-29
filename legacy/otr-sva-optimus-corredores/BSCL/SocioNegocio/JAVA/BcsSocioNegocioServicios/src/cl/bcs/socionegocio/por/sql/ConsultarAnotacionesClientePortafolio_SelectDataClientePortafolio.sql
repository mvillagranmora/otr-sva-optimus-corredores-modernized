SELECT  
	CLI."DocumentoIdentidad",
	CLI."IdCliente",
	TO_NVARCHAR(POR."CodigoPortafolio") AS "CodigoPortafolio",
	POR."IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON POR."IdCliente" = CLI."IdCliente"
WHERE POR."IdPortafolio" = :IdPortafolio;