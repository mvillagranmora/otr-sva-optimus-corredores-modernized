SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.RechazaEliminacionRolUsuarioMasivo";

CREATE PROCEDURE 
-- 
--  Proposito: Rechazar eliminacion de rol al usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 21/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.RechazaEliminacionRolUsuarioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdRolUsuario						TABLE ("IdRolUsuario" bigint)			--ID Rol usuario que se desea eliminar
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

		--Falta determinar que accion realizar si el usuario rechaza la eliminacion del rol usuario.
	          
	END;				    
END;
