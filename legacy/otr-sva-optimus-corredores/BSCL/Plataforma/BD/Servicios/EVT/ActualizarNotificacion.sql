SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacion";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza las notificaciones
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacion"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
 									
									OUT p_msgout NVARCHAR(520)
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
 
 		declare fechaHoraUTC SECONDDATE;
 		declare contador_enviadas BIGINT;
 		declare contador_reenviadas BIGINT;
 		declare contador_recordatorio BIGINT;
  		
 		v_Enviadas = select cn."IdNotificacion" from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cn 
 					 	WHERE (	"FechaDespachoOrginal" between 
 					 		TO_SECONDDATE(TO_CHAR(CURRENT_UTCTIMESTAMP,'YYYY-MM-DD') || ' 00:00:00','YYYY-MM-DD HH24:MI:SS')
 					 		AND CURRENT_UTCTIMESTAMP and (("EnviadoSrv" = 'N' and "TiempoDespachoSrv" != 'M'))) or ("EnviarSrv" = 'S' and "TiempoDespachoSrv" = 'M' );
 					
 	 	v_ReEnviadas = select cn."IdNotificacion" 
 	 					from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cn 
 					 		WHERE ("ReEnviarSrv" = 'S');
 		
 		v_RecordatoriosEnviados = select cn."IdNotificacion" from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cn 
 					 		WHERE ("FechaDespachoRecordatorio" != TO_SECONDDATE('0001-01-01 0:00:00','YYYY-MM-DD HH24:MI:SS') and "FechaDespachoRecordatorio" < CURRENT_UTCTIMESTAMP AND "RecordatorioEnviado" = 'N' );			
 		
 		SELECT COUNT(1) INTO contador_enviadas from :v_Enviadas	;	
 		SELECT COUNT(1) INTO contador_recordatorio from :v_RecordatoriosEnviados	;
 		
 				
 		
 		 
			
		UPDATE  noti
				SET "Enviado" = 'S',
					"FechaEnviado" = CURRENT_UTCTIMESTAMP,
					"IdUsuarioEnviado" = "IdUsuario", 
					"Enviar" = 'N'
				FROM :v_Enviadas   nott 
				INNER JOIN "BSCL.Plataforma.BD::EVT.Notificacion" noti
				ON nott."IdNotificacion" = noti."IdNotificacion"
				INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" confevt
		ON confevt."IdConfiguracionEvento" = noti."IdConfiguracionEvento";
			
		UPDATE  notdet
				SET  notdet."Despachado" = 'S'
				FROM :v_Enviadas nott  INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" notdet
		 ON nott."IdNotificacion" = notdet."IdNotificacion";
			 

 		UPDATE nott
   				 SET  
   				 "FechaReEnviado" = CURRENT_UTCTIMESTAMP,
   				 "ReEnviado"= 'S', 
   				 "ReEnviar" = 'N'
				 FROM :v_ReEnviadas noti INNER JOIN "BSCL.Plataforma.BD::EVT.Notificacion" nott
			 ON nott."IdNotificacion" = noti."IdNotificacion" and "Enviado" = 'S';
			 
		
		UPDATE nott
   				 SET  "FechaRecordatorioEnviado" = CURRENT_UTCTIMESTAMP, "RecordatorioEnviado"= 'S', "ReEnviar" = 'N', "UsuarioEnviaRecordatorio" = p_IdUsuario
				 FROM :v_RecordatoriosEnviados noti INNER JOIN "BSCL.Plataforma.BD::EVT.Notificacion" nott
			 ON nott."IdNotificacion" = noti."IdNotificacion" and "Enviado" = 'S';
			 
		p_msgout :='OK ' || contador_recordatorio;		 
					 
					 
			 
		  

END;