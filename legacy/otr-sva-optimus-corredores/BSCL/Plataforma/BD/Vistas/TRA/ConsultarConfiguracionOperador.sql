SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador"
AS

SELECT 
	CO."CodigoOperador",
	PER."IdPersona",
	PER."NombreCompleto" as "NombreCorredora",
	COR."IdCorredora",
	COR."NombreCorto",
	POR."Alias" AS "RazonSocial",
	CO."UtilizaPortafolio",
	CO."IdPortafolio",
	CO."ReservaTransaccion",
	POR."CodigoPortafolio" as "CodigoPortafolio",
	CLI."NombreCompleto" as "NombreCliente",
	CLI."DocumentoIdentidad" as "DocumentoIdentidad",
	CASE CO."UtilizaPortafolio" WHEN 'N' THEN 0 ELSE SOC."IdSocioDeNegocio" END as "IdSocioDeNegocio",
	CO."IdEstado",
	ECO."Nombre" as "Estado",
	CO."FechaCreacion",
	CO."FechaModificacion",
	CO."IdUsuarioCrea",
	CO."IdUsuarioModifica",
	CO."IdEmpresa",
	LIBORD."IdLibroOrdenes" as "IdLibroOrdenes",
	LIBORD."NombreLibroOrdenes" as "LibroOrdenes"
FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" as CO
INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" COR ON CO."IdCorredora" = COR."IdCorredora"
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LIBORD ON (LIBORD."IdLibroOrdenes"=CO."IdLibroOrdenes")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on PER."IdPersona" = COR."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR	ON (POR."IdPortafolio" = CO."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = por."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC ON SOC."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ECO	ON (ECO."IdEstado" = CO."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
