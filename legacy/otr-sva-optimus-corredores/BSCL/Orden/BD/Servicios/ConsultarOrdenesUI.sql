SET SCHEMA "BSCL";

-- Por reliberar pieza

DROP PROCEDURE "BSCL.Orden.BD.Servicios::ConsultarOrdenesUI";

--
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::ConsultarOrdenesUI" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	IN p_IdEmpleado			BIGINT,
	
	OUT p_out_Ordenes		"BSCL.Orden.BD.Vistas::ConsultarOrdenesUI"
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"

--READS SQL DATA WITH RESULT VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenUIResultView" AS 
READS SQL DATA WITH RESULT VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenUI" AS 
BEGIN
	
    DECLARE v_RestringeOrdenCliente     NVARCHAR(1);
    --SELECT 1 FROM DUMMY;
    SELECT IFNULL("RestringeOrdenCliente",'S')
    INTO v_RestringeOrdenCliente
    FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
    WHERE "IdEmpleado" = :p_IdEmpleado;
    
    IF v_RestringeOrdenCliente = 'N' THEN
    
        p_out_Ordenes =   SELECT ord.* FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" ord
       						WHERE ord."Rank" = 1;
	
    ELSE
    
	   p_out_Ordenes =	SELECT ord.*
						FROM
						(
							--PORTAFOLIOS QUE SON VISIBLES AL EMPLEADO
							SELECT PORT."IdPortafolio"
							FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" PORT
							WHERE PORT."IdEmpleadoEjecutivo" = :p_IdEmpleado
							OR (PORT."IdEmpleadoSupervisor" = :p_IdEmpleado AND PORT."EsEjecutivoExclusivo" = 'N')
							AND PORT."IdEstado" = 'ACT'
							UNION
							--PORTAFOLIOS QUE VE UN EMPLEADO BACKOFFICE
							SELECT "IdPortafolio"
							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT,
							(
								--EMPLEADOS BACKOFFICE
								SELECT "IdEmpleado"
								FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
								EXCEPT 
								--SUPERVISORES
								SELECT "IdEmpleadoSupervisor" AS "IdEmpleado"
								FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor"
								EXCEPT
								--EJECUTIVOS
								SELECT "IdEmpleadoEjecutivo" AS "IdEmpleado"
								FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
							) EMP
							WHERE EMP."IdEmpleado" = :p_IdEmpleado
						) AS PORT
						INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" ord ON ord."IdPortafolio" = port."IdPortafolio"
						WHERE ord."Rank" = 1
						UNION
						SELECT ord.*
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" ord 
						WHERE ord."IdEmpleadoDigitador" = :p_IdEmpleado
						AND ord."Rank" = 1; 
						
    END IF;
    
END;
