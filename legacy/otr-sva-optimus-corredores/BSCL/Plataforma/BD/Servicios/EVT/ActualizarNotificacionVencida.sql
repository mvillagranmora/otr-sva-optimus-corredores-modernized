SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacionVencida";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza una notificacion vencida
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/07/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacionVencida"
 (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),

	OUT p_Salida 			NVARCHAR(520)							
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
		DECLARE v_notificaciones_vencidas BIGINT;
		
		--OBTENER NOMBRE DE LAS NOTIFICACIONES A VENCER PARA DEJAR UN LOG
		p_DetalleLog =
	 			SELECT e."CodigoTipoEvento" as "SubRegistro", 'AccionConfirmada' as "CampoRegistro", 'PEN' as "ValorAntiguo",  'VCD'  as "ValorNuevo"
	 			FROM "BSCL.Plataforma.BD::EVT.Notificacion" n
				INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" nd ON n."IdNotificacion" = nd."IdNotificacion"
				INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" e ON e."IdEvento" = nd."IdEvento"
	    		WHERE "FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP AND "ConfirmacionDespacho" = 'S' and "Confirmado" = 'N';
	
		--CONTAR CUANTAS NOTIFICACIONES SERAN VENCIDAS
		SELECT COUNT("AccionConfirmada") INTO v_notificaciones_vencidas
		FROM "BSCL.Plataforma.BD::EVT.Notificacion"
		WHERE "FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP AND "ConfirmacionDespacho" = 'S' and "Confirmado" = 'N' and "FechaVencimiento" != '0001-01-01' and "Enviado" = 'N';

		--VENCER LAS NOTIFICACIONES
	 	UPDATE "BSCL.Plataforma.BD::EVT.Notificacion" 
	 	SET 
	 		"AccionConfirmada" = 'VCD', 
	 		"Confirmado" ='S', 
	 		"FechaConfirmado" = CURRENT_UTCTIMESTAMP  , 
	 		"Observacion" = 'Notificación vencida'
	 	WHERE "FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP 
	 	AND "ConfirmacionDespacho" = 'S' 
	 	AND "Confirmado" = 'N'
	 	AND "FechaVencimiento" != '0001-01-01'
	 	AND "Enviado" = 'N';
	 	
	 	IF :v_notificaciones_vencidas > 0 THEN 
	 		--LOGEAR SOLO SI EXISTEN NOTIFICACIONES A VENCER
		    CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'MODIFICACION' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ACTUALIZAR NOTIFICACION A VENCIDAS'
			,p_IdRegistro => 0
			,p_DetalleLog => :p_DetalleLog
			);
		END IF;

		p_Salida := 'Actualización Correcta';	 	

END;