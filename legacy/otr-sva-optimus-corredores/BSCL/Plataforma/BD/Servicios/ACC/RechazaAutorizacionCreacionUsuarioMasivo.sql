SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.RechazaAutorizacionCreacionUsuarioMasivo";

CREATE PROCEDURE 
-- 
--  Proposito: Rechazar Enrolamiento de usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 18/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.RechazaAutorizacionCreacionUsuarioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_in_IdUsuario						TABLE("IdUsuario" bigint)			--ID Usuario que se esta autorizando
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
		UPDATE USU
		
		   SET "EsAutorizado" = 'N'
		FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
		JOIN :p_in_IdUsuario U ON USU."IdUsuario" = U."IdUsuario" AND USU."EsAutorizado" = 'P';	
		
	          
	END;				    
END;
