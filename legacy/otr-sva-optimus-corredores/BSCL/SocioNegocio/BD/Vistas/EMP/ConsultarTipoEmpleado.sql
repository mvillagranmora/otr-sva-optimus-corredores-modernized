SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoEmpleado"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoEmpleado"  AS 

-------------------------------------------------------------
-- EMPLEADO SUPERVISOR: Supervisores
-------------------------------------------------------------
SELECT S."IdEmpleadoSupervisor" AS "IdEmpleado",U."IdUsuario",'SUPERVISOR' AS "TipoEmpleado"
  FROM "BSCL.SocioNegocio.BD::EJE.Supervisor" as S
 INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = S."IdEmpleadoSupervisor"
  		 WHERE U."TipoEntidad" = 'EMP'

-------------------------------------------------------------
-- EMPLEADOS FRONTOFFICE: Ejecutivos que no son supervisores  
-------------------------------------------------------------
UNION
SELECT EJE."IdEmpleado",EJE."IdUsuario",'FRONTOFFICE' AS "TipoEmpleado"
  FROM (
  		SELECT EJE."IdEmpleadoEjecutivo" AS "IdEmpleado",U."IdUsuario"
		  FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
		 INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = EJE."IdEmpleadoEjecutivo"
		 WHERE U."TipoEntidad" = 'EMP' AND EJE."IdEstado" = 'ACT'
		EXCEPT
		SELECT S."IdEmpleadoSupervisor" AS "IdEmpleado",U."IdUsuario"
		  FROM "BSCL.SocioNegocio.BD::EJE.Supervisor" as S
		 INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = S."IdEmpleadoSupervisor"
  		 WHERE U."TipoEntidad" = 'EMP' 
  	   ) EJE

-------------------------------------------------------------
-- EMPLEADOS BACKOFFICE: Empleados que no son ejecutivos ni supervisores
-------------------------------------------------------------
UNION				
SELECT EMP."IdEmpleado",EMP."IdUsuario",'BACKOFFICE' AS "TipoEmpleado"
  FROM (
		SELECT E."IdEmpleado",U."IdUsuario"
		  FROM "BSCL.SocioNegocio.BD::EMP.Empleado" as E
		 INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = E."IdEmpleado"
  		 WHERE U."TipoEntidad" = 'EMP' AND E."IdEstado" = 'ACT'				
		EXCEPT	
	--EJECUTIVOS
		SELECT E."IdEmpleadoEjecutivo" AS "IdEmpleado",U."IdUsuario"
		  FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as E
		 INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = E."IdEmpleadoEjecutivo"
  		 WHERE U."TipoEntidad" = 'EMP' 	
		EXCEPT
	--SUPERVISORES
		SELECT S."IdEmpleadoSupervisor" AS "IdEmpleado",U."IdUsuario"
		  FROM "BSCL.SocioNegocio.BD::EJE.Supervisor" as S
		 INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = S."IdEmpleadoSupervisor"
  		 WHERE U."TipoEntidad" = 'EMP'   		 
	   ) EMP;
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoEmpleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 