SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Servicios::ConfirmarAutorizacionIngresoMandatoPAC";

CREATE PROCEDURE 
-- 
--  
-- 
--  
--  
"BSCL.Tesoreria.BD.Servicios::ConfirmarAutorizacionIngresoMandatoPAC" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(20),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_Contrato								TABLE ("IdMandatoPAC"	BIGINT)			--ID Contraparte que se esta autorizando
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
			UPDATE CSC
			   SET "Estado" = COALESCE(CNT."EstadoContratoPorAutorizar",CSC."Estado"),
			   "DiaDescuento" = COALESCE(CNT."DiaDescuentoPorAutorizar",CSC."DiaDescuento")
			   FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CSC 
			   JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" CNT ON CNT."FolioCSC" = CSC."FolioCSC" AND CSC."TipoPago"= 'PAC'
			   JOIN :p_Contrato C ON C."IdMandatoPAC" = CNT."IdMandatoPAC"
			WHERE CNT."Estado" = 'PAU' AND CNT."FolioCSC" != '0';
			
			UPDATE SAAPV
			   SET "IdEstado" = COALESCE(CNT."EstadoContratoPorAutorizar",SAAPV."IdEstado"),
			   "DiaDescuento" = COALESCE(CNT."DiaDescuentoPorAutorizar",SAAPV."DiaDescuento")
			   FROM "BSCL.APV.BD::SCS.ContratoSAAPV" SAAPV 
			   JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" CNT ON CNT."FolioSAAPV" = SAAPV."FolioContratoSAAPV" AND SAAPV."IdCliente" = CNT."IdCliente"
			   JOIN :p_Contrato C ON C."IdMandatoPAC" = CNT."IdMandatoPAC"
			WHERE CNT."Estado" = 'PAU' AND CNT."FolioSAAPV" != '0';
		
			
			UPDATE CNT
			   SET "Estado" = COALESCE(CNT."EstadoPorAutorizar",'ING')
			   FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" CNT
			   JOIN :p_Contrato C ON C."IdMandatoPAC" = CNT."IdMandatoPAC"
			WHERE "Estado" = 'PAU';	
		
	          
	END;				    
END;
