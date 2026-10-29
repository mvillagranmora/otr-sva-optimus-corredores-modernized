SET SCHEMA "BSCL";  
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultaEventoCapitalClientePendientes";
  
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultaEventoCapitalClientePendientes" 
(	-- Parametros entrada del servicio publico
	 IN p_IdEventoCapital					BIGINT     		DEFAULT 0
	,IN p_IdTipoEventoCapital				NVARCHAR(2) 	DEFAULT ''
	,OUT p_out_CantidadPendiente			BIGINT
)
AS  
BEGIN	
	
		SELECT  COUNT(*) INTO p_out_CantidadPendiente
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	
		WHERE 
			"IdEventoCapital" 				= p_IdEventoCapital					AND
			"IdTipoEventoCapital"			= p_IdTipoEventoCapital				AND
			"IdEstado"						= 'PEN';
		
		SELECT p_out_CantidadPendiente FROM DUMMY;
			
END;

