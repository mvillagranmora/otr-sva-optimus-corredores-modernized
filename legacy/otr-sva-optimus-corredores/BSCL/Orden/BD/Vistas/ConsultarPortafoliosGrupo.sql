
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarPortafoliosGrupo"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarPortafoliosGrupo"
AS  
SELECT
	 GRUPOR."IdGrupoPortafolio",
	 GRU."IdGrupo",
	 GRU."Nombre" as "NombreGrupo",
	 GRU."IdEstado" as "IdEstadoGrupo",
	 GRU."Codigo" as "Codigo",
	 CLI."IdCliente",
	 CLI."IdEstado" as "IdEstadoCliente",
	 CLI."NombreEstado" as "EstadoCliente",
	 CLI."DocumentoIdentidad",
	 CLI."RazonSocial",
	 CLI."NombreCompleto",
	 PORT."IdPortafolio", 
	 PORT."IdEstado" as "IdEstadoPortafolio", 
	 CEGP."NombreEstado" as "EstadoPortafolio",
	 PORT."CodigoPortafolio",
	 PORT."Proposito", 	
	 PORT."DescripcionPortafolio",
	 GRUPOR."Porcentaje",
	 CLO."NombreLibroOrdenes",
	 CLO."IdLibroOrdenes"
FROM "BSCL.Orden.BD.Vistas::ConsultarGrupo" GRU
INNER JOIN "BSCL.Orden.BD::ORD.GrupoPortafolio" GRUPOR ON GRU."IdGrupo" = GRUPOR."IdGrupo"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."IdPortafolio" = GRUPOR."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI on CLI."IdCliente" = PORT."IdCliente"
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio" CEGP on PORT."IdEstado" = CEGP."IdEstado"
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenes" CLO on GRUPOR."IdPortafolio" = CLO."IdPortafolio";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarPortafoliosGrupo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
