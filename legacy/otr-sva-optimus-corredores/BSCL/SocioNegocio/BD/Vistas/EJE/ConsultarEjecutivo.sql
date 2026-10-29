SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS
	
SELECT 
EMP."IdEmpresa",
EJE."IdEmpleadoEjecutivo",
EMP."IdPersona" as "IdPersonaEjecutivo",
PER."NombreCompleto",
EMP."CodigoEmpleado",
EMP."IdSucursal",
SUC."NombreSucursal",
SUC."CodigoSucursal",
EJE."IdEstado",
EST."Nombre" as "NombreEstado",
SUP."IdEmpleadoSupervisor",
SUP."NombreCompleto" as "NombreSupervisor",
EJE."IdEmpleadoReemplaza" as "IdEmpleadoEjecutivoReemplaza",
PER2."NombreCompleto" as "NombreCompletoReemplaza",
CASE WHEN TO_DATE(CONF."Valor") BETWEEN EJE."FechaInicioReemplazo" AND EJE."FechaFinReemplazo" THEN EJE."FechaInicioReemplazo"
     ELSE '0001-01-01'
END AS "FechaInicioReemplazo",
CASE WHEN TO_DATE(CONF."Valor") BETWEEN EJE."FechaInicioReemplazo" AND EJE."FechaFinReemplazo" THEN EJE."FechaFinReemplazo"
     ELSE '0001-01-01'
END AS "FechaFinReemplazo",
PER."DocumentoIdentidad"

FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor" SUP ON SUP."IdEmpleadoSupervisor" = EJE."IdEmpleadoSupervisor"
JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = EJE."IdEstado"
JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMP."IdPersona"
JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON CONF."CodigoParametro" = 'PLA.FechaSistema' AND EMP."IdEmpresa" = CONF."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP2 ON EJE."IdEmpleadoReemplaza" = EMP2."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER2 ON PER2."IdPersona" = EMP2."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC ON EMP."IdSucursal" = SUC."IdSucursal" ;
GO 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

