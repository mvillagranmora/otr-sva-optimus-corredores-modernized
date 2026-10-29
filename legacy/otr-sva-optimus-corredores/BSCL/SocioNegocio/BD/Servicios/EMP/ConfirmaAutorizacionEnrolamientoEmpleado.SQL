SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmaAutorizacionEnrolamientoEmpleado";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar Enrolamiento de empleado
--  Autor: CFLORES - BCS
--  Fecha Creacion: 12/01/2016
--  
"BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmaAutorizacionEnrolamientoEmpleado" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdEmpleado							bigint			--ID empleado que se esta autorizando
) 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	declare v_IdPersona bigint;
	DECLARE ERROR_CLIENTE_INVALIDO CONDITION FOR SQL_ERROR_CODE 10012;	
	
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL ERROR_CLIENTE_INVALIDO;
		END;

		
		--Cambia Estado de Empleado solo si esta pendiente
		UPDATE "BSCL.SocioNegocio.BD::EMP.Empleado"
		   SET "EsEnrolado" = 'S'
		WHERE "IdEmpleado" = :p_IdEmpleado AND "EsEnrolado" IN ('P','N');
		
		
		--Cambia Estado de Persona solo si esta pendiente
		UPDATE PER
		   SET "EsEnrolado" = 'S'
		FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
		INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
			ON (PER."IdPersona" = EMP."IdPersona")
		WHERE EMP."IdEmpleado" = :p_IdEmpleado AND PER."EsEnrolado" IN ('P','N');		
	          
	END;				    
END;
