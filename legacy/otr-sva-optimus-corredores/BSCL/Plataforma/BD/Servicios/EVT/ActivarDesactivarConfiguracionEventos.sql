SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActivarDesactivarConfiguracionEventos";

CREATE PROCEDURE 
-- 
--  Proposito: Activa / Desactiva configuraciones de evento
--  Autor: VARIACODE - BCS
--  Fecha Creacion: 04/03/2016
--  
"BSCL.Plataforma.BD.Servicios::EVT.ActivarDesactivarConfiguracionEventos"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
 

	IN p_IdConfiguracionEvento		table ("IdConfiguracionDetalle" BIGINT),
	IN p_Accion						BIGINT, -- 1 = Activar / 2 = Desactivar
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
    )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
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
			--Error interno no manejado  (El log ser� captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
	    
	    DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
	    
	    
	    
	    UPDATE   conf
	    SET  "IdEstado" = CASE 
	    						WHEN p_Accion = 1 THEN 'ACT'
	    						WHEN p_Accion = 2 THEN 'INA'
	    				   END
	    FROM :p_IdConfiguracionEvento conf_param INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CONF ON conf."IdConfiguracionEvento" = conf_param."IdConfiguracionDetalle";	  
	    
	    p_DetalleLog = SELECT "Descripcion" as "SubRegistro", 'IdEstado' as "CampoRegistro", "IdEstado" as "ValorAntiguo",  CASE WHEN p_Accion = 1 THEN 'ACT' WHEN p_Accion = 2 THEN 'INA' END  as "ValorNuevo"			
	    	FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf
	   		 LEFT JOIN :p_IdConfiguracionEvento conf_param
	    	  ON conf."IdConfiguracionEvento" = conf_param."IdConfiguracionDetalle";	
	    
	    CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'INFORMATIVO' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ACTIVAR / DESACTIVAR NOTIFICACION'
			,p_DetalleLog => :p_DetalleLog
			);
	 
	END ;	
END;