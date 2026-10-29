SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConfirmaAsignacionRolUsuario";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar asignacion de rol al usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 21/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConfirmaAsignacionRolUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdRolUsuario                      TABLE ("IdRolUsuario" bigint)   			--ID usuario que se esta autorizando
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

		
		--Cambia Estado de Usuario solo si esta pendiente
		UPDATE RU
		   SET RU."EsAutorizado" = 'S'
		FROM "BSCL.Plataforma.BD::ACC.RolUsuario" RU, :p_IdRolUsuario PUR
		WHERE RU."IdRolUsuario" = PUR."IdRolUsuario" AND "EsAutorizado" = 'P';	
	          
	END;				    
END;
