SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClientesGrupo"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClientesGrupo" AS 
SELECT  GRU."IdGrupo",   
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
		PORT."IdEmpleadoEjecutivo"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarGrupoClientes" GRU 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.GrupoClientes" GRUCLI ON GRU."IdGrupo" = GRUCLI."IdGrupo" 
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."IdPortafolio" = GRUCLI."IdPortafolio" 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI on CLI."IdCliente" = PORT."IdCliente" 
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio" CEGP on PORT."IdEstado" = CEGP."IdEstado"; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClientesGrupo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 