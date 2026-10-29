SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenesGrupo"; 
 GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenesGrupo" AS 

SELECT 
     lib."IdLibroOrdenes",
	 cli."IdCliente",
	 cli."IdEstado",
	 cli."IdPersona",
	 cli."DocumentoIdentidad",
	 cli."TipoDocumentoIdentidad",
	 cli."NombreCompleto",
	 port."IdPortafolio",
	 port."CodigoPortafolio",
	 port."Proposito",
	 port."IdEstado" as "IdEstadoPortafolio",
	 port."DescripcionPortafolio",
	 coalesce(grupoport."IdGrupo",0) as "IdGrupo",
	 grupoport."Codigo" as "CodigoGrupo",
	 grupoport."Porcentaje" as "Porcentaje",
	 coalesce(grupoport."NombreGrupo",'') as "NombreGrupo",
	 cli."IdSocioDeNegocio",
	 COALESCE(lib."NombreLibroOrdenes",'-') as "NombreLibroOrdenes"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" indPort on indPort."IdPortafolio" = port."IdPortafolio"
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" lib on indPort."IdLibroOrdenes" = lib."IdLibroOrdenes"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarPortafoliosGrupo" grupoport on grupoport."IdPortafolio" = port."IdPortafolio"
WHERE cli."IdEstado" = 'ACT'
ORDER BY cli."DocumentoIdentidad",port."CodigoPortafolio";
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenesGrupo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
