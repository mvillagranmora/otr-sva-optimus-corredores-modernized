SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioUI";

CREATE PROCEDURE
-- 
--  Proposito: wraper para consulta utilizada en el combobox cliente - portafolio, solo muestra cliente asociados al empleado..
--  Autor: CHERNANDEZ
--  Fecha Creacion: 13/01/2016
--  Contador para recompilacion sin modificaciones : 1 
"BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioUI" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdEmpleado			BIGINT,
	IN p_IgnoraFiltro		NVARCHAR(1) DEFAULT 'N',
	
	OUT p_out_Portafolios		"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
								
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA WITH RESULT VIEW "BSCL.Portafolio.BD.Vistas::ConsultarClientePortafolioUI" AS 
BEGIN
	DECLARE v_RestringeCarteraCliente	NVARCHAR(1);
	DECLARE v_FechaActual				NVARCHAR(10) DEFAULT TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD');

	--Identificar si el empleado tiene restricciones sobre su cartera de clientes.
	SELECT "RestringeCarteraCliente" INTO v_RestringeCarteraCliente from "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoUI" where "IdEmpleado" = :p_IdEmpleado;
	
	SELECT "Valor" INTO v_FechaActual FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = p_IdEmpresa; 
	
	p_out_Portafolios =   SELECT por.*
				FROM (
					--PORTAFOLIOS QUE SON VISIBLES AL EMPLEADO
					SELECT PORT."IdPortafolio"
					FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" PORT
					WHERE PORT."IdEmpleadoEjecutivo" = :p_IdEmpleado OR (PORT."IdEmpleadoSupervisor" = :p_IdEmpleado and PORT."EsEjecutivoExclusivo" = 'N') or p_IgnoraFiltro = 'S' and PORT."IdEstado" = 'ACT'
					UNION
					--PORTAFOLIOS QUE VE UN EMPLEADO BACKOFFICE
					SELECT "IdPortafolio" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT,
					(
						--EMPLEADOS BACKOFFICE
						SELECT "IdEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
						EXCEPT
						--SUPERVISORES
						SELECT "IdEmpleadoSupervisor" AS "IdEmpleado" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor"
						EXCEPT
						--EJECUTIVOS
						SELECT "IdEmpleadoEjecutivo" AS "IdEmpleado" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
						--EMPLEADOS REEMPLAZO
						EXCEPT
						SELECT "IdEmpleadoEjecutivo" AS "IdEmpleado" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
						WHERE "IdEmpleadoEjecutivoReemplaza" = :p_IdEmpleado AND :v_FechaActual BETWEEN "FechaInicioReemplazo" AND "FechaFinReemplazo"
					
					) EMP
					WHERE EMP."IdEmpleado" = :p_IdEmpleado
					UNION
                    --Todos los portafolios en los que el empleado esta reemplazando
                    SELECT PORT."IdPortafolio"
                    FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" PORT
                    WHERE "IdEmpleadoReemplaza" = :p_IdEmpleado AND :v_FechaActual BETWEEN "FechaInicioReemplazo" AND "FechaFinReemplazo"
					UNION
					--Todos los portafolios si el empleado no tiene restricciones a su cartera de clientes
					SELECT PORT."IdPortafolio"
					FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" PORT WHERE v_RestringeCarteraCliente='N' and PORT."EsEjecutivoExclusivo" = 'N'  
				) as PORT
				
				INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" por on por."IdPortafolio" = port."IdPortafolio"
				WHERE "IdEmpresa" = :p_IdEmpresa;
END;
