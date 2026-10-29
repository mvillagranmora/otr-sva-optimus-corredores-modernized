SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacionDetalle";
 
CREATE PROCEDURE
-- 
--  Proposito: Agrega detalles de las notificaciones que son gatilladas por eventos
--  Autor: VARIACODE  - BCS 
--  Fecha Creacion: 04/03/2016
--   

"BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacionDetalle"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
  									IN p_NotificacionDetalle  TABLE ("IdNotificacion" BIGINT, "IdEvento" BIGINT),  								 
 									
 									OUT p_msgout			NVARCHAR(520)
 								
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

			INSERT INTO "BSCL.Plataforma.BD::EVT.NotificacionDetalle" 
		 		SELECT   
			 		"BSCL.Plataforma.BD.Secuencias::EVT.NotificacionDetalle".nextval,
			 		nd."IdNotificacion",
			 		"IdEvento",
			 		'N'
		 		FROM "BSCL.Plataforma.BD::EVT.Notificacion" n
		 		RIGHT JOIN :p_NotificacionDetalle nd
		 		on n."IdNotificacion" =  nd."IdNotificacion";

		p_msgout := 'OK';
		
		 

 
END;
