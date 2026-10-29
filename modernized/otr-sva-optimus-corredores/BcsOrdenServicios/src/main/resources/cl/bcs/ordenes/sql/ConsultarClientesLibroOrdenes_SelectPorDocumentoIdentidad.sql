SELECT 
     lib."IdLibroOrdenes",
	 cli."IdCliente",
	 cli."IdEstado",
	 cli."IdPersona",
	 cli."DocumentoIdentidad",
	 cli."TipoDocumentoIdentidad",
	 cli."RazonSocial",
	 cli."NombreCompleto",
	 cli."IdEmpresa",
	 port."IdPortafolio",
	 port."CodigoPortafolio",
	 port."Proposito",
	 port."IdEstado" as "IdEstadoPortafolio",
	 port."DescripcionPortafolio",
	 Coalesce(port."IdPersonaBrokerExtranjero", 0) AS "IdPersonaBrokerExtranjero",
	 lib."NombreLibroOrdenes"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente" 
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" indPort on indPort."IdPortafolio" = port."IdPortafolio"
INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" lib on indPort."IdLibroOrdenes" = lib."IdLibroOrdenes"
WHERE cli."IdEstado" = 'ACT' AND cli."DocumentoIdentidad" IN (:DocumentoIdentidad)
ORDER BY cli."DocumentoIdentidad",port."CodigoPortafolio"