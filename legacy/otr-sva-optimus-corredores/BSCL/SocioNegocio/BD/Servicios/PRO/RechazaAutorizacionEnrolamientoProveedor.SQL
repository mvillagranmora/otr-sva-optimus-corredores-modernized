SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PRO.RechazaAutorizacionEnrolamientoProveedor";

CREATE PROCEDURE 
-- 
--  Proposito: Rechazar Enrolamiento de proveedor
--  Autor: CFLORES - BCS
--  Fecha Creacion: 18/01/2016
--  
"BSCL.SocioNegocio.BD.Servicios::PRO.RechazaAutorizacionEnrolamientoProveedor" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdProveedor							bigint			--ID Proveedor que se esta autorizando
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

		
			--Cambia Estado de Proveedor solo si esta pendiente
			UPDATE "BSCL.SocioNegocio.BD::PRO.Proveedor"
			   SET "EsEnrolado" = 'N'
			WHERE "IdProveedor" = :p_IdProveedor AND "EsEnrolado" = 'P';	
		
	          
	END;				    
END;
