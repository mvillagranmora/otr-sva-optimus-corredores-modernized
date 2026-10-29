SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ReEnviarNotificacion";
 
CREATE PROCEDURE
-- 
--  Proposito: ReEnvia una notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ReEnviarNotificacion"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
  									IN p_IdNotificacion 	BIGINT,
  									IN p_Mail			 	NVARCHAR(520),
 									
 									OUT p_Salida 			NVARCHAR(520),
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
 
			DECLARE contador BIGINT;
			
			select length(trim(p_Mail)) into contador from dummy;
			
			IF :contador > 0 THEN 
			
				UPDATE "BSCL.Plataforma.BD::EVT.Notificacion" notif
					    SET 
					    "ReEnviar" = 'S',
					     "ReEnviado" = 'N', 
 					    "IdUsuarioReEnviado" = p_IdUsuario,
 					    "DireccionAlternativa" = p_Mail
 		   		WHERE "IdNotificacion" = p_IdNotificacion and "Enviado" = 'S';
			
			ELSE
				
				UPDATE "BSCL.Plataforma.BD::EVT.Notificacion" notif
					    SET 
					    "ReEnviar" = 'S',
					    "ReEnviado" = 'N',  
 					    "IdUsuarioReEnviado" = p_IdUsuario,
 					    "DireccionAlternativa" = ''
 		   		WHERE "IdNotificacion" = p_IdNotificacion and "Enviado" = 'S';
			
			END IF;
			
				
	   		p_Salida := 'Actualización Correcta ' ||  p_Mail;
			
			
			p_DetalleLog = SELECT 'IdNotificacion' as "SubRegistro", 'AccionConfirmada' as "CampoRegistro", "AccionConfirmada" as "ValorAntiguo",  
								'S'  as "ValorNuevo"			
	    					FROM "BSCL.Plataforma.BD::EVT.Notificacion" conf
	    	 				WHERe conf."IdNotificacion" = p_IdNotificacion;	
	    
	    
	    	CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'MODIFICACION' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ACTUALIZAR NOTIFICACION'
			,p_DetalleLog => :p_DetalleLog
			);
			
			
 	END;

END;
