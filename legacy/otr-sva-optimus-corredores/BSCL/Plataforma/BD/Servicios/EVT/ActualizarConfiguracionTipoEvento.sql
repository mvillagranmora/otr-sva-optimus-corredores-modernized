SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarConfiguracionTipoEvento";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza una configuracion para que sea asociada a un tipo evento
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarConfiguracionTipoEvento"
 (

	                                
	                                IN p_IdConfiguracionEvento 		bigint,
 									IN p_ExcluyeDestinatario		NVARCHAR(1),
 									IN p_IdRol 						BIGINT,
 									IN p_EnviarRemitente			NVARCHAR(1)
 									
  								 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 


  		codigosTipoEvento = 
  				SELECT "CodigoTipoEvento" FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle"
  					WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento;
  		
  		UPDATE  conftpevt
	  		SET	"NotificaTodosUsuarios" = CASE
	  											WHEN p_IdRol <= 0 THEN 'S'
	  											ELSE 'N'
	  									  END,
	  			"ExcluyeDestinatario" = p_ExcluyeDestinatario,
	  			"CopiarRemitente" = p_EnviarRemitente
	  		FROM :codigosTipoEvento cod INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" conftpevt
		ON cod."CodigoTipoEvento" = conftpevt."CodigoTipoEvento";
		
		
		UPDATE  conftpevt
	  		SET	"IdUsuarioRol" = p_IdRol
	  		FROM :codigosTipoEvento cod 
	  	INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	ON cod."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" conftpevt
		ON cte."IdConfiguracionTipoEvento" = conftpevt."IdConfiguracionTipoEvento";
		
		
		UPDATE "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento" 
				SET "IdUsuarioRol" = p_IdRol,
				    "CopiarRemitente" = p_EnviarRemitente,
				    "ExcluyeDestinatario" = p_ExcluyeDestinatario,
				    "NotificaTodosUsuarios" = CASE
	  											WHEN p_IdRol <= 0 THEN 'S'
	  											ELSE 'N'
	  									  END
	  	 WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento;
		
		
	 
		
		
 
		  
 
END;