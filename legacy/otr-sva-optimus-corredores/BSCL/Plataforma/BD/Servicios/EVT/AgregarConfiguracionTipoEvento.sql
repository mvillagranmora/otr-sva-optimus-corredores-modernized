SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionTipoEvento";

CREATE PROCEDURE
-- 
--  Proposito: AgregarConfiguracionTipoEvento
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionTipoEvento"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
 									IN p_CodigoTipoEvento			nvarchar(40),
 									IN p_NotificacionTodosUsuarios	nvarchar(1),
 									

 									OUT p_out_msg nvarchar(520)
									
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 




 
		declare v_count BIGINT;
 		
 		
		
		select count(1) INTO v_count from "BSCL.Plataforma.BD::EVT.TipoEvento" where "CodigoTipoEvento" = p_CodigoTipoEvento;

		IF :v_count > 0 THEN 
		
			INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" 
			VALUES (
			 "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionTipoEvento".nextval,
			 p_IdEmpresa,
			 p_CodigoTipoEvento,
			 p_NotificacionTodosUsuarios,
			 'N',
			 'N'
			 );
 		 	   SELECT   'OK' into p_out_msg  from dummy;
		 ELSE
		 
 		 	   SELECT   'No existe tipo evento' into p_out_msg  from dummy;
		 END IF;
		
	

END;

 
