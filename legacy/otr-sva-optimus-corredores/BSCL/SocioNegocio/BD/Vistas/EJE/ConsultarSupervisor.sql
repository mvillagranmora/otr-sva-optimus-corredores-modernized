SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor" AS
	
SELECT 
EMP."IdEmpresa",
SUP."IdEmpleadoSupervisor",
PER."NombreCompleto",
EMP."CodigoEmpleado",
SUP."IdEstado",
EST."Nombre" as "NombreEstado"
FROM "BSCL.SocioNegocio.BD::EJE.Supervisor" SUP
JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON SUP."IdEmpleadoSupervisor" = EMP."IdEmpleado"
JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = SUP."IdEstado"
JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMP."IdPersona";
 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION;




