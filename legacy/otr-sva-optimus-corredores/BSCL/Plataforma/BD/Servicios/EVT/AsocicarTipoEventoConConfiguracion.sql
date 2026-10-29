SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AsocicarTipoEventoConConfiguracion";

CREATE PROCEDURE
-- 
--  Proposito: Asocia TipoEvento con una configuracion
--  Autor: - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AsocicarTipoEventoConConfiguracion"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                IN p_IdConfiguracionEvento BIGINT,
 									IN p_CodigoTipoEvento	table("CodigoTipoEvento" NVARCHAR(40)),
  									 
 									
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
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	

		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN
 		tablaaux = 

 		SELECT 
 		COD."CodigoTipoEvento" , 
 		"BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionTipoEvento".nextval AS "IdConfTipoEvt" 
 		from :p_CodigoTipoEvento cod;
 
 		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" 
		SELECT 
		"BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoDetalle".nextval,
		p_IdConfiguracionEvento,
		p_IdUsuario,
		CURRENT_UTCTIMESTAMP,
		"CodigoTipoEvento",
		'ACT'
		from :p_CodigoTipoEvento;
		
		
		
   		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" 
			SELECT 
				"IdConfTipoEvt",
				p_IdEmpresa,      
				"CodigoTipoEvento",
				"NotificaTodosUsuarios",
				"ExcluyeDestinatario",
				"CopiarRemitente"
			FROM :tablaaux   INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento"
		ON "IdConfiguracionEvento" = p_IdConfiguracionEvento;
			
  		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionUsuarioTipoEvento".NEXTVAL,
				"IdConfTipoEvt",
				'R',
				"IdUsuarioRol"
			FROM :tablaaux  INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento" 
		ON "IdConfiguracionEvento" = p_IdConfiguracionEvento;
	
		
		 p_DetalleLog = SELECT '' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo",  '' as "ValorNuevo" from dummy;		
	    		
	    
	    CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'INFORMATIVO' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ASOCIAR EVENTO A NOTIFICACION'
			,p_IdRegistro => p_IdConfiguracionEvento
			,p_DetalleLog => :p_DetalleLog
			);
		
	 
		  
	END;

END;
