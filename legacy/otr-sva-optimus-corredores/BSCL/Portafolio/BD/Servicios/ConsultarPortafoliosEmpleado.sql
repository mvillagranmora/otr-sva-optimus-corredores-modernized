SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Portafolio.BD.Servicios::ConsultarPortafoliosEmpleado";
GO
CREATE PROCEDURE
-- 
--  Proposito: devuelve portafolios que ve el empleado
--  Autor: CHERNANDEZ
--  Fecha Creacion: 13/01/2016
--   
"BSCL.Portafolio.BD.Servicios::ConsultarPortafoliosEmpleado" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdEmpleado			BIGINT,
	IN p_IgnoraFiltro		NVARCHAR(1) DEFAULT 'N',
	
	OUT p_out_Portafolios	TABLE("IdPortafolio"	BIGINT)
								
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL" AS 
BEGIN
	DECLARE v_RestringeCarteraCliente	NVARCHAR(1);
	DECLARE v_FechaActual				NVARCHAR(10) DEFAULT TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD');

	SELECT "RestringeCarteraCliente" INTO v_RestringeCarteraCliente 
	from "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoUI" where "IdEmpleado" = :p_IdEmpleado;
	
	v_EsEjecutivo = SELECT "IdEmpleadoEjecutivo" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" where "IdEmpleadoEjecutivo" = :p_IdEmpleado;
	IF (NOT IS_EMPTY(:v_EsEjecutivo)) THEN
		P_OUT_PORTAFOLIOS = SELECT "IdPortafolio" FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdEmpleadoEjecutivo" = :p_IdEmpleado;
	END IF;
	
	v_EsSupervisor = SELECT "IdEmpleadoSupervisor" FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor" where "IdEmpleadoSupervisor" = :p_IdEmpleado;
	IF (NOT IS_EMPTY(:v_EsSupervisor)) THEN
		P_OUT_PORTAFOLIOS = SELECT "IdPortafolio" FROM :P_OUT_PORTAFOLIOS
							UNION
							SELECT "IdPortafolio" 
							FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" eje 
							INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" port on eje."IdEmpleadoEjecutivo" = port."IdEmpleadoEjecutivo" AND eje."IdEmpleadoSupervisor" = :p_IdEmpleado
							WHERE port."EsEjecutivoExclusivo" = 'N';
	END IF;
	
	v_Reemplazos =	SELECT "IdEmpleadoEjecutivo"
					FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 
					WHERE "IdEmpleadoEjecutivoReemplaza" = :p_IdEmpleado AND :v_FechaActual BETWEEN "FechaInicioReemplazo" AND "FechaFinReemplazo";
					
	IF (NOT IS_EMPTY(:v_Reemplazos)) THEN
		P_OUT_PORTAFOLIOS = SELECT "IdPortafolio" FROM :P_OUT_PORTAFOLIOS
							UNION
							SELECT "IdPortafolio" 
							FROM :v_Reemplazos reem  
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" eje on eje."IdEmpleadoEjecutivo" = reem."IdEmpleadoEjecutivo"
							INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" port on eje."IdEmpleadoEjecutivo" = port."IdEmpleadoEjecutivo";
		
	END IF;
	
	
	IF (IS_EMPTY(:v_EsEjecutivo) AND IS_EMPTY(:v_EsSupervisor)) THEN
		P_OUT_PORTAFOLIOS = SELECT "IdPortafolio" FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" WHERE "IdEmpresa" = p_IdEmpresa;	
	END IF;
	
	IF (:v_RestringeCarteraCliente = 'N' OR :p_IgnoraFiltro = 'S') THEN
		P_OUT_PORTAFOLIOS = SELECT "IdPortafolio" FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" WHERE "IdEmpresa" = p_IdEmpresa;	
	END IF;
	
	
	
END;
