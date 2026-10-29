SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.RechazaAsignacionRolUsuarioMasivo";

CREATE PROCEDURE 
-- 
--  Proposito: Rechazar asignacion de rol al usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 21/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.RechazaAsignacionRolUsuarioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_RolUsuario						TABLE ("IdRolUsuario"	BIGINT)			--ID usuario que se esta autorizando
) 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE ERROR_CLIENTE_INVALIDO CONDITION FOR SQL_ERROR_CODE 10012;	
	DECLARE id bigint;
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL ERROR_CLIENTE_INVALIDO;
		END;
		
		select top 1 "IdRolUsuario" into id from :p_RolUsuario;
				
		

				

		--Elimina rol al usuario
		DELETE FROM "BSCL.Plataforma.BD::ACC.RolUsuario"
		WHERE "IdRolUsuario" in (select "IdRolUsuario" from :p_RolUsuario);
			          
	END;				    
END;
