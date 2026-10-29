SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.FondoMutuo.BD.Servicios::ConfirmarAutorizacionIngresoCSC";

CREATE PROCEDURE 
-- 
--  
-- 
--  
--  
"BSCL.FondoMutuo.BD.Servicios::ConfirmarAutorizacionIngresoCSC" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(20),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_Contrato								TABLE ("IdFolioCSC"	BIGINT)			--ID Contraparte que se esta autorizando
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
			   SET "Estado" = 'ING'
			   FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CNT
			   JOIN :p_Contrato C ON C."IdFolioCSC" = CNT."IdFolioCSC"
			WHERE "Estado" = 'PAU';	
		
	          
	END;				    
END;


