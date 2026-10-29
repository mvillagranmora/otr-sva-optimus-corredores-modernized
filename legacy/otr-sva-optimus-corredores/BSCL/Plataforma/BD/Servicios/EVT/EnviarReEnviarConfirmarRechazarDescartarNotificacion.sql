SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.EnviarReEnviarConfirmarRechazarDescartarNotificacion";
 
CREATE PROCEDURE
-- 
--  Proposito: Enviar/ReEnvia una notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.EnviarReEnviarConfirmarRechazarDescartarNotificacion"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
  									IN p_in_IdNotificacion 	TABLE("IdNotificacion" BIGINT),
  									IN p_Accion			 	BIGINT,--1 = Enviar / 2 = ReEnviar / 3 = Confirmar / 4 = Rechazar / 5 = Descartar / 6 = Leer
 									
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
  			IF :p_Accion =  1 THEN 
 				UPDATE  notif
					    SET 
					    "Enviar" = 'S',
  					    "IdUsuarioEnviado" = p_IdUsuario,
  					    "FechaDespachoOrginal" = CURRENT_UTCTIMESTAMP
					    from :p_in_IdNotificacion noti INNER JOIN "BSCL.Plataforma.BD::EVT.Notificacion" notif
				ON notif."IdNotificacion" = noti."IdNotificacion" and "TiempoDespacho" = 'M';
	   		  
	   		   p_out_AdvertenciasDetalle = select "IdNotificacion"||' '  as "Codigo", '' as "RequiereAutorizacion",'asd' as "Descripcion" from :p_in_IdNotificacion;
			END IF;
			IF :p_Accion = 2 THEN 
			
				  	UPDATE notif
					    SET 
					    "ReEnviar" = 'S', 
 					    "IdUsuarioReEnviado" = p_IdUsuario
					    from :p_in_IdNotificacion noti INNER JOIN "BSCL.Plataforma.BD::EVT.Notificacion" notif
		   			ON notif."IdNotificacion" = noti."IdNotificacion" and "Enviar" = 'S';
		   			p_out_AdvertenciasDetalle = select "IdNotificacion"||' '  as "Codigo", '' as "RequiereAutorizacion",'asd' as "Descripcion" from :p_in_IdNotificacion;
		   			
	   		   
			END IF;
			
			IF :p_Accion =  3 THEN 
			
				UPDATE notif
					    SET
					    "ConfirmacionDespacho" = 'S',
					    "AccionConfirmada" = 'ACP'
					    from :p_in_IdNotificacion noti INNER JOIN  "BSCL.Plataforma.BD::EVT.Notificacion" notif 
		   		ON notif."IdNotificacion" = noti."IdNotificacion" and "Enviar" = 'S' ;
	   			p_out_AdvertenciasDetalle = select "IdNotificacion"||' '  as "Codigo", '' as "RequiereAutorizacion",'asd' as "Descripcion" from :p_in_IdNotificacion;
	   		
			END IF;
			
			IF :p_Accion = 4 THEN 
			
				   UPDATE  notif
					    SET
					    "AccionConfirmada" = 'RZD',
					    "ConfirmacionDespacho" = 'S'
					    from :p_in_IdNotificacion noti INNER JOIN  "BSCL.Plataforma.BD::EVT.Notificacion" notif 
		   		   ON notif."IdNotificacion" = noti."IdNotificacion";
	   		   	   p_out_AdvertenciasDetalle = select "IdNotificacion"||' '  as "Codigo", '' as "RequiereAutorizacion",'asd' as "Descripcion" from :p_in_IdNotificacion;
	   		   
			END IF;
			
			IF :p_Accion = 5 THEN 
			
			
					UPDATE  notif
						    SET 
						    "Descartado" = 'S', 
 						    "IdUsuarioDescarte" = p_IdUsuario,
 						    "FechaDescarte" = CURRENT_UTCTIMESTAMP
						    from :p_in_IdNotificacion noti INNER JOIN  "BSCL.Plataforma.BD::EVT.Notificacion" notif 
			   		ON notif."IdNotificacion" = noti."IdNotificacion";
			   		p_out_AdvertenciasDetalle = select "IdNotificacion"||' '  as "Codigo", '' as "RequiereAutorizacion",'asd' as "Descripcion" from :p_in_IdNotificacion;			   		
			   		
			END IF;
			
			IF :p_Accion = 6 THEN
			  
			  UPDATE  notif
			    SET 
			    "Leida" = 'S'
			    from :p_in_IdNotificacion noti INNER JOIN  "BSCL.Plataforma.BD::EVT.Notificacion" notif 
			   	ON notif."IdNotificacion" = noti."IdNotificacion";
			   	p_out_AdvertenciasDetalle = select "IdNotificacion"||' '  as "Codigo", '' as "RequiereAutorizacion",'asd' as "Descripcion" from :p_in_IdNotificacion;
			   		
			
			END IF;
			
			p_DetalleLog = SELECT 'IdNotificacion' as "SubRegistro", 'AccionConfirmada' as "CampoRegistro", "AccionConfirmada" as "ValorAntiguo",  
								CASE WHEN p_Accion = 1 THEN 'Enviar' 
								     WHEN p_Accion = 2 THEN 'ReEnviar' 
								     WHEN p_Accion = 3 THEN 'Confirmar'
								     WHEN p_Accion = 4 THEN 'Rechazar' 
								     WHEN p_Accion = 5 THEN 'Descartar' 
								END  as "ValorNuevo"			
	    					FROM "BSCL.Plataforma.BD::EVT.Notificacion" conf
	   						LEFT JOIN :p_in_IdNotificacion conf_param
	    	 				ON conf."IdNotificacion" = conf_param."IdNotificacion";	
	    
	    
	    	CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'MODIFICACION' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ACTUALIZAR NOTIFICACION'
			,p_DetalleLog => :p_DetalleLog
			);
			
			
 	END;

END;
