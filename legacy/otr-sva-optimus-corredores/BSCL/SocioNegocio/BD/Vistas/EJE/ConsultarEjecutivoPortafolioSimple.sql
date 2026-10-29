SET SCHEMA "BSCL";


DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple";


CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" AS
SELECT 
POR."IdPortafolio",
POR."IdEmpleadoEjecutivo",
POR."IdCliente",
EJE."IdEmpleadoReemplaza",
POR."EsEjecutivoExclusivo",
EJE."IdEmpleadoSupervisor",
EJE."FechaInicioReemplazo",
EJE."FechaFinReemplazo",
EJE."IdEstado"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo";


GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
