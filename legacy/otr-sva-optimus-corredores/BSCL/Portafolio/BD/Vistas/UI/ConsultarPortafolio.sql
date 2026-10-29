---------------------------------------------------------------
-- VISTA PARA MANTENEDOR DE PORTAFOLIO.
-- NO UTILIZAR EN EL NEGOCIO, PARA ESTO HACER USO DE LA VISTA  
-- "BSCL.Portafolio.BD.Vistas::POR.ConsultarPortafolio"
---------------------------------------------------------------

SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio"; 
 GO
CREATE VIEW "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio"  
AS
SELECT
	 
	port."IdPortafolio", 
	port."CodigoPortafolio", 	
	port."DescripcionPortafolio",
	port."Proposito", 
	port."IdCliente", 
	port."Estado",
	port."FechaBloqueo", 
	port."ComentarioBloqueo", 
	port."IdTipoBloqueo", 
	port."IdPersonaBrokerExtranjero",
	port."IdPerfilComercialBaseOrigen",
	port."IdPerfilProductos",
	port."IdPerfilProductosExtra",
	port."IdPerfilCobros",
	port."IdPerfilCobrosExtra",
	port."IdPerfilCobrosPeru",
	port."IdPerfilCobrosPeruExtra",
	port."IdPerfilLimites",
	port."IdPerfilLimitesExtra",
	port."IdPerfilCondicionesOperativas",
	port."IdPerfilCondicionesOperativasExtra",
	port."IdEstadoAutorizacion",
	port."IdEstado",
	port."FechaIngreso",
	port."FechaCreacion",
	port."FechaModificacion",
	port."IdDireccionEnvioCorrespondencia",
	port."IdTelefonoContacto",
	port."IdCarga",
	port."IdEmpleadoEjecutivo",
	port."EsEjecutivoExclusivo",
	port."CodigoPropositoPortafolio",
	port."NumeroFicha",
	port."IdNombreFantasia",
	port."Alias",
	port."EsCuentaPropia",
	port."IdEmpresaCuentaPropia",
	port."FechaUltimaCartola",
	port."IdUltimaCartola",
	port."IdAreaCliente",
	port."AreaCliente",

	 cli."DocumentoIdentidad" || ' ' || cli."NombreCompleto" || ' (' || port."CodigoPortafolio" || ')' as "NombreCompletoPortafolio",
	 cli."NombreCompleto",
	 cli."DocumentoIdentidad",
	 cli."IdEmpresa",
	 cli."IdPersona",
	 cli."IdCategoriaInversionista",
	 cli."IdTipoCliente",
	 cli."IdEstado" as "IdEstadoCliente",
	 cli."Tipo" as "TipoPersona",
	 port."IdEstadoPortafolioAutorizacion",
	 port."EstadoPortafolioAutorizacion",
	 port."IdEmpleadoEjecutivoComercial"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
;
GO
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

