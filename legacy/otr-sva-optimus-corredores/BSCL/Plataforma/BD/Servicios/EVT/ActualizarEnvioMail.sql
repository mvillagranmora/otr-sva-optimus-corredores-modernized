SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarEnvioMail";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza las notificaciones enviadas
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarEnvioMail"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
  									 
  									
 									
									OUT p_msgout	NVARCHAR(520)
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
 
  
 			UPDATE "BSCL.Plataforma.BD::EVT.Mail"  
				SET "EstadoEnviado" = 'S',"FechaEnviado" = CURRENT_UTCTIMESTAMP 
			WHERE "EstadoEnviado" = 'N';
			
		 
			 
			p_msgout := 'OK';		 
					 
					 
			 
		  

END;
