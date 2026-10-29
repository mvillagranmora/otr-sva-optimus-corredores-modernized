SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.ActualizarSiguienteFechaEjecucionAgenda";

CREATE PROCEDURE
-- 
--  Proposito: Actualiza estado de Agenda 
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.ActualizarSiguienteFechaEjecucionAgenda" (

	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdAgenda 			BIGINT,
	IN p_Segundos			BIGINT,
	
	
-- Salidas para mensajes de advertencias y errores
	OUT p_msgout 			NVARCHAR(540)
	 
	
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	DECLARE p_fechaDeEjecucion SECONDDATE;
 
 	SELECT add_seconds(CURRENT_UTCTIMESTAMP,p_Segundos) into p_fechaDeEjecucion FROM DUMMY;
 	
 	 
  	
  	IF :p_segundos >= 0 THEN
	 	UPDATE "BSCL.Plataforma.BD::BAT.Agenda" 
	 		SET 
	  		"SiguienteFechaDeEjecucion" = p_fechaDeEjecucion
	 	WHERE "IdAgenda" = p_IdAgenda;
 	END IF;
	
	p_msgout := 'OK';
		
		
		

END;
