SET SCHEMA "BSCL";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoActivoPortafolio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoActivoPortafolio" AS
SELECT 
	SOC."IdSocioDeNegocio" as "IdSocioDeNegocioCliente",
	CLI."IdCliente",
	CLI."NombreCompleto" as "NombreCompletoCliente",
	CLI."IdPortafolio",
	CLI."CodigoPortafolio",
	CLI."DescripcionPortafolio",
	COALESCE(REE."IdEmpleadoEjecutivoReemplaza",EJE."IdEmpleadoEjecutivo") as "IdEmpleadoEjecutivoPortafolio",
	COALESCE(REE."NombreCompletoReemplaza",EJE."NombreCompleto") as "NombreCompletoEjecutivo",
	CLI."EsEjecutivoExclusivo",
	EJE."IdSucursal",
	SUC."NombreSucursal",
	EJE."IdEmpleadoSupervisor",
	EJE."FechaInicioReemplazo",
	EJE."FechaFinReemplazo",
	CAST(CFG."Valor" as DATE) as "FechaSistema"
FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" CLI
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CFG ON CFG."IdEmpresa" = CLI."IdEmpresa" and "CodigoParametro" = 'PLA.FechaSistema'
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON CLI."IdCliente" = SOC."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE ON CLI."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" REE ON CLI."IdEmpleadoEjecutivo" = REE."IdEmpleadoEjecutivo" AND REE."IdEmpleadoEjecutivoReemplaza" IS NOT NULL AND CFG."Valor" BETWEEN REE."FechaInicioReemplazo" AND REE."FechaFinReemplazo" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC ON EJE."IdSucursal" = SUC."IdSucursal";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoActivoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
