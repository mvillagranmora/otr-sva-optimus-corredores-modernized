SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionUsuarioTipoEvento";

CREATE PROCEDURE
-- 
--  Proposito: AgregarConfiguracionUsuarioTipoEvento 
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionUsuarioTipoEvento"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
 									IN p_IdConfiguracionTipoEvento	BIGINT,
 									IN p_TipoUsuario				nvarchar(40),
 									IN p_IdUsuarioRol	 			bigint,
 									

 									
									OUT p_msg_out	nvarchar(520)
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

 
		declare v_count BIGINT;
  		
		
		
 		select count(1) INTO v_count from "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" where "IdConfiguracionTipoEvento" = p_IdConfiguracionTipoEvento ;
		 
				
 		
		IF :v_count > 0 THEN 
		
			INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" 
			VALUES (
			 "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionUsuarioTipoEvento".nextval,
			 p_IdConfiguracionTipoEvento,
			 p_TipoUsuario,
			 p_IdUsuarioRol
			
			 );
		   SELECT 'OK' into p_msg_out from dummy;
			 
		 ELSE
		 
 		 	 SELECT 'No existe detalle asociado a la configuración' into p_msg_out from dummy;
		 END IF;
		
		
 

END;

 
