SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmaAutorizacionEnrolamientoPersona";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar Enrolamiento de Persona
--  Autor: CFLORES - BCS
--  Fecha Creacion: 27/01/2016
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmaAutorizacionEnrolamientoPersona" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdPersona							bigint			--ID Persona que se esta autorizando
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

		
		--Cambia Estado de Persona solo si esta pendiente
		UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
		   SET "EsEnrolado" = 'S'
		WHERE "IdPersona" = :p_IdPersona AND "EsEnrolado" IN ('P','N');	
	          
	END;				    
END;
