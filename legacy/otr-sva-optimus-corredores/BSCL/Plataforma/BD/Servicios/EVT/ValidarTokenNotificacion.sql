SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ValidarTokenNotificacion";
 
CREATE PROCEDURE
-- 
--  Proposito: Valida que no se haya confirmado la notificación y que los ultimos digitos del rut coincidan
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ValidarTokenNotificacion"
 (
									IN p_Token 				NVARCHAR(520),
  									
   									OUT p_Salida 			BIGINT
  								 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
 
	DECLARE p_contador 	  BIGINT;
 
		select COUNT("IdEmpresa") into p_contador  
		from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cn 
	 	WHERE "TokenConfirmacion" = p_Token and "Confirmado" = 'UI_NO' and "ConfirmacionDespacho" = 'UI_SI';

  	IF :p_contador = 1 THEN 
		select "IdEmpresa" into p_Salida
		from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cn 
	 	WHERE "TokenConfirmacion" = p_Token and "Confirmado" = 'UI_NO' and "ConfirmacionDespacho" = 'UI_SI';
	ELSE 
 		p_Salida := -1;	
 	END IF;
 	
 

END;


 
