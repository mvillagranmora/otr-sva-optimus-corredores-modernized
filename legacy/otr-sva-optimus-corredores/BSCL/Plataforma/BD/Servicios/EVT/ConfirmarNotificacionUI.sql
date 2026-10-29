SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ConfirmarNotificacionUI";
 
CREATE PROCEDURE
-- 
--  Proposito: Confirma / rechaza una notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ConfirmarNotificacionUI"
 (
 									IN p_IdEmpresa          		BIGINT,
 								    IN p_IdUsuario          		BIGINT,
   									IN p_IdModulo           		NVARCHAR(3),
									
									IN p_IdNotificacion 			TABLE("IdNotificacion" BIGINT),
  									IN p_Comentario					NVARCHAR(520),
  									IN p_Accion						BIGINT, --1 : Confirma / 2 : Rechaza
 										
 									OUT p_Salida 			BIGINT,
 									OUT p_MensajeSalida 	NVARCHAR(520),
 									OUT p_out_AdvertenciasContador int,
									OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
									OUT p_out_ErroresContador int,
									OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
 								 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;


		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN
  	DECLARE p_contador 	  BIGINT;
	 
  	UPDATE notif
  		SET
 			"IdUsuarioConfimado" = p_IdUsuario,
 			"Observacion" = p_Comentario,
			"Confirmado"= 'S',
			"AccionConfirmada" = CASE 
									WHEN p_Accion = 1 THEN 'APD'
									WHEN p_Accion = 2 THEN 'RZD'
								 END,
			"FechaConfirmado" = CURRENT_UTCTIMESTAMP,
			"Leida" = 'S'
  	FROM :p_IdNotificacion NT
  	INNER JOIN 	"BSCL.Plataforma.BD::EVT.Notificacion" notif
  	ON notif."IdNotificacion" = NT."IdNotificacion"
 	WHERE "ConfirmacionDespacho" = 'S' and "Confirmado" = 'N';
 	
 	
 	
  	SELECT ::ROWCOUNT INTO p_contador FROM DUMMY;
 	
 	IF :p_contador > 0 THEN
 		p_Salida := 1;
 		IF :p_Accion = 1 THEN 
 			p_MensajeSalida := 'Notificación Confirmada '  ;
 		ELSE
 			p_MensajeSalida := 'Notificación Rechazada '  ;
 		END IF;
		p_DetalleLog = SELECT 'IdNotificacion' as "SubRegistro", 'AccionConfirmada' as "CampoRegistro", "AccionConfirmada" as "ValorAntiguo",  
								'S'  as "ValorNuevo"			
	    					FROM "BSCL.Plataforma.BD::EVT.Notificacion"  nt
	    					INNER JOIN :p_IdNotificacion notif
	    	 				ON nt."IdNotificacion" = notif."IdNotificacion" ;
	    
	CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'MODIFICACION' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'CONFIRMAR / RECHAZAR NOTIFICACION'
			,p_DetalleLog => :p_DetalleLog
			);
			
			p_out_AdvertenciasContador	:= 0;
			p_out_ErroresContador		:= 0;
			p_out_AdvertenciasDetalle	= select  '1' as "Codigo",'1' as "RequiereAutorizacion",'1' as "Descripcion" from dummy where 2=2;
			p_out_ErroresDetalle		= select '1' as "Codigo",'1' as "Descripcion" from dummy where 2=2;
	ELSE
	
		p_Salida := -1;
		p_MensajeSalida := 'Notificacion no requiere confirmación o fue confirmada';
			p_out_AdvertenciasContador	:= -1;
			p_out_ErroresContador		:= -1;
			p_out_AdvertenciasDetalle	= select  '1' as "Codigo",'1' as "RequiereAutorizacion",p_MensajeSalida as "Descripcion" from dummy where 2=2;
			p_out_ErroresDetalle		= select '1' as "Codigo",p_MensajeSalida as "Descripcion" from dummy where 2=2;
	END IF;
 	
 END;
END;



