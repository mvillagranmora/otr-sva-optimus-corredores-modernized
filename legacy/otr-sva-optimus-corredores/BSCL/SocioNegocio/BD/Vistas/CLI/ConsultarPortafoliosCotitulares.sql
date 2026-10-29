SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarPortafoliosCotitulares"; 
 		
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarPortafoliosCotitulares" AS

SELECT 
rel."IdPersonaOrigen",
cliOrigen."IdEmpresa",
cliOrigen."IdCliente" as "IdClienteOrigen",
cli."IdPersona",
cli."IdCliente",
cli."NombreCompleto",
cli."DocumentoIdentidad",
por."IdPortafolio",
por."CodigoPortafolio",
por."Proposito",
por."Estado",
por."FechaIngreso"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" rel
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cliOrigen on cliOrigen."IdPersona" = rel."IdPersonaOrigen" 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdPersona" = rel."IdPersonaDestino" and cliOrigen."IdEmpresa" = cli."IdEmpresa"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por on por."IdCliente" = cli."IdCliente"
where "IdTipoRelacion" = 'COTITU';


GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarPortafoliosCotitulares" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 