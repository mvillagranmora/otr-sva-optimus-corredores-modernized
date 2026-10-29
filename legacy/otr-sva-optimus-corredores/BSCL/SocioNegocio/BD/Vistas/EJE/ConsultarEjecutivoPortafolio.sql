SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio";
GO

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS

SELECT 
SOC."IdSocioDeNegocio" as "IdSocioDeNegocioCliente",
CLI."IdPersona",
CLI."IdCliente",
CLI."NombreCompleto" as "NombreCompletoCliente",
CLI."DocumentoIdentidad",
POR."IdPortafolio",
POR."CodigoPortafolio",
POR."DescripcionPortafolio",
POR."Proposito" as "PropositoPortafolio",
POR."IdEmpleadoEjecutivo" as "IdEmpleadoEjecutivoPortafolio",
EJE."NombreCompleto" as "NombreCompletoEjecutivo",
POR."EsEjecutivoExclusivo",
EJE."IdSucursal",
SUC."CodigoSucursal",
SUC."NombreSucursal",
EJE."IdEmpleadoSupervisor",
EJE."CodigoEmpleado",
(CASE
	WHEN (COALESCE(EJE."IdEmpleadoEjecutivoReemplaza",0) <> 0 AND (CFG."PLA.FechaSistema" BETWEEN CAST(COALESCE(EJE."FechaInicioReemplazo",'0001-01-01') AS DATE) and CAST(COALESCE(EJE."FechaFinReemplazo",'0001-01-01') AS DATE))) THEN 'N'   -- si existe ejecutivo de reemplazo y está activo, este no es el ejecutivo vigente
	WHEN (COALESCE(EJE."IdEmpleadoEjecutivoReemplaza",0) = 0) THEN 'S'   -- si no existe ejecutivo de reemplazo, este es el ejecutivo vigente
	ELSE 'S'
END) as "EsEjecutivoVigente",
null as "FechaInicioReemplazo",
null as "FechaFinReemplazo",
CASE WHEN CLI."IdPersonaEjecutivo" = EJE."IdPersonaEjecutivo" THEN 'S' ELSE 'N' END AS "EsEjecutivoPrincipal",
CLI."IdTipoPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON CLI."IdCliente" = SOC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC ON EJE."IdSucursal" = SUC."IdSucursal"
JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot" CFG ON CFG."IdEmpresa" = EJE."IdEmpresa"



UNION ALL

SELECT 
SOC."IdSocioDeNegocio" as "IdSocioDeNegocioCliente",
CLI."IdPersona",
CLI."IdCliente",
CLI."NombreCompleto" as "NombreCompletoCliente",
CLI."DocumentoIdentidad",
POR."IdPortafolio",
POR."CodigoPortafolio",
POR."DescripcionPortafolio",
POR."Proposito" as "PropositoPortafolio",
EJE."IdEmpleadoEjecutivoReemplaza" as "IdEmpleadoEjecutivoPortafolio",
EJE."NombreCompletoReemplaza" as "NombreCompletoEjecutivo",
POR."EsEjecutivoExclusivo",
EJE."IdSucursal",
SUC."CodigoSucursal",
SUC."NombreSucursal",
EJE."IdEmpleadoSupervisor",
EJE."CodigoEmpleado",
'S' as "EsEjecutivoVigente",   -- si existe ejecutivo de reemplazo(en la fecha actual), es el ejecutivo vigente
EJE."FechaInicioReemplazo",
EJE."FechaFinReemplazo",
CASE WHEN CLI."IdPersonaEjecutivo" = EJE."IdPersonaEjecutivo" THEN 'S' ELSE 'N' END AS "EsEjecutivoPrincipal",
CLI."IdTipoPersona"
FROM 
"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON CLI."IdCliente" = SOC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON CLI."IdCliente" = POR."IdCliente"
JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo" and EJE."FechaInicioReemplazo" is not null
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC ON EJE."IdSucursal" = SUC."IdSucursal"
JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CFG ON CFG."IdEmpresa" = EJE."IdEmpresa" and CFG."CodigoParametro" = 'PLA.FechaSistema'
WHERE CFG."Valor" BETWEEN EJE."FechaInicioReemplazo" and EJE."FechaFinReemplazo";
GO

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
