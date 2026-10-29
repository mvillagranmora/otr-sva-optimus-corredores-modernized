SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ConfirmarNotificacion";
 
CREATE PROCEDURE
-- 
--  Proposito: Confirma una notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ConfirmarNotificacion"
 (
									IN p_Token 				NVARCHAR(520),
  									IN p_Comentario			NVARCHAR(520),
  									IN p_Accion				BIGINT, --1 : Confirma / 2 : Rechaza
 									
 									OUT p_Salida 			BIGINT,
 									OUT p_MensajeSalida 	NVARCHAR(520)
 								 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
  	DECLARE p_contador 	  BIGINT;
  	DECLARE p_IdEmpresa   BIGINT;
 	DECLARE p_IdUsuario   BIGINT;
	
	
	
 	
 	SELECT ::ROWCOUNT INTO p_contador FROM DUMMY;
 	
 	IF :p_contador > 0 THEN
 	
 
 
 		p_Salida := 1;
		p_MensajeSalida := 'Actualizo Notificación ';
 	
	 
	 
	ELSE
	
		p_Salida := -1;
		p_MensajeSalida := 'Notificación no encontrada';
	
	END IF;
 	


END;



