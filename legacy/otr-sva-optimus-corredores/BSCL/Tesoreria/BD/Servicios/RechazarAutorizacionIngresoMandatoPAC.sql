SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Servicios::RechazarAutorizacionIngresoMandatoPAC";

CREATE PROCEDURE 
-- 
--  
-- 
--  
--  
"BSCL.Tesoreria.BD.Servicios::RechazarAutorizacionIngresoMandatoPAC" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(20),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_Contrato								TABLE ("IdMandatoPAC"	BIGINT)			--ID Contraparte que se esta autorizando
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
			UPDATE CNT
			   SET "Estado" = 'RZD'
			   FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" CNT
			   JOIN :p_Contrato C ON C."IdMandatoPAC" = CNT."IdMandatoPAC"
			WHERE "Estado" = 'PAU';	
		
	          
	END;				    
END;

