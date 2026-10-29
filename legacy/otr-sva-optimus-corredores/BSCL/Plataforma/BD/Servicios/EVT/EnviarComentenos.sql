SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.EnviarComentenos";
 
CREATE PROCEDURE
-- 
--  Proposito: ReEnvia una notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.EnviarComentenos"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
  									IN p_IdRolEnviar		BIGINT,
  									IN p_Mensaje			NVARCHAR(2000),
 									
 									OUT p_Salida 			NVARCHAR(520),
 									
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
		 	DECLARE  p_out_MensajeSalida NVARCHAR(40);
		 	DECLARE llaveValor table("IdSecuencia" BIGINT, "Llave" NVARCHAR(40), "Valor" NVARCHAR(300)); 
 			DECLARE archivoAdjunto table("IdSecuencia" BIGINT,"ArchivoAdjunto" BLOB, "Nombre" NVARCHAR(520),"Contrasenia" NVARCHAR(520));
  			DECLARE p_tsalida         table("IdDetalle" BIGINT,"IdEvento" BIGINT, "Llave" NVARCHAR(40), "Valor" NVARCHAR(300));
  			DECLARE p_IdNotificacion BIGINT;
  			DECLARE p_IdPersonaRemitente BIGINT;
  			DECLARE p_IdPersonaRecibe BIGINT;
  			
  			
  			SELECT TOP 1 "IdPersona" INTO p_IdPersonaRecibe from BSCL."BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" where "IdRol" = p_IdRolEnviar;
  			SELECT TOP 1 "IdPersona" INTO p_IdPersonaRemitente from BSCL."BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" where "IdUsuario" = p_IdUsuario;
  			
  			
  			
 			llaveValor = SELECT -1 as "IdSecuencia", '' as "Llave", '' as "Valor" from dummy;
 			archivoAdjunto = SELECT  0 as "IdSecuencia", '' AS "ArchivoAdjunto", '' as "Nombre", '' as "Contrasenia" from dummy;
 			UPDATE "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" SET "NotificaTodosUsuarios" = 'N';
 			
 			UPDATE "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" SET "IdUsuarioRol" = p_IdRolEnviar;
 			
 			
 			evento = SELECT 'COMENTENOS' as "CodigoTipoEvento", 1 as "IdSecuencia", CURRENT_TIMESTAMP as "FechaCreacionSistema" , p_IdPersonaRemitente, p_IdPersonaRecibe from dummy;
			mail = SELECT 1 as "IdSecuencia","IdDireccionElectronica" as "IdDireccion", 'TO' as "TipoDestinatario" from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" Where "IdRol" = p_IdRolEnviar;
			idEventos = SELECT 1 AS "IdEvento" ,1 AS "IdSecuencia" FROM DUMMY;
		
			
 			call "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(p_IdEmpresa,p_IdUsuario,p_IdModulo, :evento,:llaveValor, :archivoAdjunto, :mail,'' , :idEventos);
			
 			
				
 			 
			 
			 CALL "BSCL.Plataforma.BD.Servicios::EVT.AgruparNotificaciones"(p_IdEmpresa,p_IdUsuario,p_IdModulo,p_Salida,:evtAgrupado,:evtNoAgrupado,:notificacion);
			
			UPDATE  evt
			     SET evt."MensajeBody" = p_Mensaje,
			         evt."Procesado" = 'S'
			     FROM "BSCL.Plataforma.BD::EVT.Evento" evt inner join  :idEventos p_evt
			ON evt."IdEvento" = p_evt."IdEvento";
 			
			 UPDATE noti
			 	SET 
			 		"Enviado" = 'S', 
			 		"IdUsuarioEnviado" = p_IdUsuario,
			 		"FechaEnviado" = CURRENT_UTCTIMESTAMP
			 	FROM "BSCL.Plataforma.BD::EVT.Notificacion" noti inner join :notificacion p_noti
			 ON noti."IdNotificacion" = p_noti."IdNotificacion";
			  
			 p_Salida := 'Envío Correcto';
			 
			 
 			
			 
			
 	END;

END;
