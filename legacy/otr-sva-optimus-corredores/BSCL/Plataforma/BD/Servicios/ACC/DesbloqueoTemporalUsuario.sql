SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.DesbloqueoTemporalUsuario";
-- 
--  Proposito: Servicio para el desbloqueo por fechaTemporalBloqueo
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 28/02/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.DesbloqueoTemporalUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametro de entrada que almacenara los accesos del perfil
	IN  p_CodigoUsuario				nvarchar(40), 	

	-- Parametros salida del servicio publico
	OUT p_out_desbloqueo				 NVARCHAR(5),
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
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
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE p_sysDate NVARCHAR(50);
		
		--------------------
		--Logica Aqui
		--------------------
		SELECT CURRENT_UTCTIMESTAMP into p_sysDate from DUMMY;
		
		p_DetalleLog =  select p_CodigoUsuario as "SubRegistro", 'FechaBloqueoTemporal' as "CampoRegistro", TO_VARCHAR("FechaBloqueoTemporal") as "ValorAntiguo", p_sysDate as "ValorNuevo"
		FROM "BSCL.Plataforma.BD::ACC.Usuario"
		WHERE "CodigoUsuario" = p_CodigoUsuario;
		
		--Actualizar los datos del perfil
		UPDATE "BSCL.Plataforma.BD::ACC.Usuario" 
		SET "FechaBloqueoTemporal" = p_sysDate 
		WHERE "CodigoUsuario" = p_CodigoUsuario;


		--Ejecutar el programa de logeo.
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'ACTUALIZACION' 
		,p_IdModulo => 'PLA' 
		,p_IdUsuario => p_IdUsuario 
		,p_IdEmpresa => p_IdEmpresa 
		,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'USUARIO'
		,p_DetalleLog => :p_DetalleLog
		);
		
		p_out_desbloqueo := 'ok++';
		
	END;
END;
