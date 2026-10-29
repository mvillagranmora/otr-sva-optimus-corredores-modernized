SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PRO.ConfirmaAutorizacionEnrolamientoProveedor";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar Enrolamiento de empleado
--  Autor: CFLORES - BCS
--  Fecha Creacion: 18/01/2016
--  
"BSCL.SocioNegocio.BD.Servicios::PRO.ConfirmaAutorizacionEnrolamientoProveedor" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_IdProveedor							bigint			--ID empleado que se esta autorizando
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
		   SET "EsEnrolado" = 'S'
		WHERE "IdProveedor" = :p_IdProveedor AND "EsEnrolado" IN ('P','N');	
		
		--Cambia Estado de Persona solo si esta pendiente
		UPDATE PER
		   SET "EsEnrolado" = 'S'
		FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
		INNER JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" PRO
			ON (PER."IdPersona" = PRO."IdPersona")
		WHERE PRO."IdProveedor" = :p_IdProveedor AND PER."EsEnrolado" IN ('P','N');	
		
	          
	END;				    
END;
