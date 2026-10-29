SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConfirmaEliminacionRolUsuario";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar eliminacion de rol al usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 21/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConfirmaEliminacionRolUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdRolUsuario						bigint			--ID RolUsuario que se desea eliminar
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

		
		--Eliminar el registro de la tabla rolUsuario
		DELETE FROM "BSCL.Plataforma.BD::ACC.RolUsuario" where "IdRolUsuario" = p_IdRolUsuario;
	          
	END;				    
END;
