SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarPlantilla";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza una plantilla para que sea usada en alguna notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarPlantilla"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                IN p_IdPlantilla 		bigint,
	                                IN p_Template			NVARCHAR(100000),
	                                IN p_Asunto				NVARCHAR(520),
	                                IN p_Nombre				NVARCHAR(520),
 									 
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
		DECLARE countrow BIGINT;		
		p_DetalleLog =
	 			SELECT "Nombre" as "SubRegistro", 'Template' as "CampoRegistro", '' as "ValorAntiguo",  ''  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.Plantilla"  WHERE "IdPlantilla" = p_IdPlantilla;
	

	 	UPDATE "BSCL.Plataforma.BD::EVT.Plantilla"
	 		 SET 
	 	 		"Template" = p_Template,
	 	 		"IdUsuarioModifica" = p_IdUsuario, 
	 	 		"FechaModificacion" = CURRENT_UTCTIMESTAMP,
 	 	 		"Asunto" = p_Asunto 
	 	 where "IdPlantilla" = p_IdPlantilla;
	 	
 	 	
	    CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'INFORMATIVO' 
		,p_IdModulo => 'EVT' 
		,p_IdUsuario => p_IdUsuario 
		,p_IdEmpresa => p_IdEmpresa 
		,p_FechaSistemaLog => CURRENT_DATE
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'ACTUALIZAR NOTIFICACION'
		,p_IdRegistro => p_IdPlantilla
		,p_DetalleLog => :p_DetalleLog
		);
		
		SELECT ::ROWCOUNT INTO countrow FROM DUMMY;
		
		if :countrow > 0 then
			p_Salida := ' Actualización Correcta';
		else
		
			p_Salida := 'Actualización incorrecta ';
		end if;
			 	
	 	
	 	

	   
		  
		  
	END;

END;
