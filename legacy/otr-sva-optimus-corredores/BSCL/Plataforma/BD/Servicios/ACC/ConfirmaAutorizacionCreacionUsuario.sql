SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConfirmaAutorizacionCreacionUsuario";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar Creacion de usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 18/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConfirmaAutorizacionCreacionUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_in_IdUsuario						bigint			--ID usuario que se esta autorizando
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
		UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
		   SET "EsAutorizado" = 'S'
		WHERE "IdUsuario" = :p_in_IdUsuario AND "EsAutorizado" = 'P';	
	          
	END;				    
END;
