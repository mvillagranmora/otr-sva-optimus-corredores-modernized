SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CambiarClaveUsuario";
-- 
--  Proposito: Servicio para la actualizacion de la clave de un usuario.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 16/02/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CambiarClaveUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametro de entrada para actualizar la clave
	IN  p_Clave						nvarchar(520), 
	IN  p_ClaveNueva				nvarchar(520), 

	-- Parametros salida del servicio publico
	OUT p_out_update				 NVARCHAR(5),
	
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
		DECLARE v_CodigoUsuario		nvarchar(40);		
		DECLARE v_Clave				NVARCHAR(520);
		
		DECLARE p_DetalleLog 		TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE p_sysDate 			NVARCHAR(20);
		DECLARE v_count BIGINT;
		p_out_update := 'nok++';

		
		--------------------
		--Logica Aqui
		--------------------

		select "CodigoUsuario", "Clave" into v_CodigoUsuario, v_Clave
		FROM "BSCL.Plataforma.BD::ACC.Usuario"
		WHERE "IdUsuario" = p_IdUsuario; 

		--aplicar hash a clave antigua.
		SELECT HASH_SHA256(to_binary(p_Clave)) "CLAVE" INTO p_Clave FROM dummy; 
		--valida que la clave antigua sea igual a la almacenada en la bd
		IF v_Clave <> p_Clave THEN
			v_hash= select 'CLAVE_NO_COINCIDE' as "Codigo",:v_CodigoUsuario as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CLAVE_NO_COINCIDE',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'CLAVE_NO_COINCIDE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									select * from :p_out_ErroresDetalle; 
			SIGNAL SALIDA_CONTROLADA; 		  			
		END IF;
		
		
		p_DetalleLog =  select upper(v_CodigoUsuario) as "SubRegistro", 'Clave' as "CampoRegistro", '*****' as "ValorAntiguo", '*****' as "ValorNuevo"
		FROM dummy;
		
		BEGIN
			--aplicar hash a clave nueva
			SELECT HASH_SHA256(to_binary(p_ClaveNueva)) "CLAVE" INTO p_ClaveNueva FROM dummy;
			
			SELECT COUNT(*) into v_count FROM 
				(SELECT "Clave", "UltimoCambioDeClave" from "BSCL.Plataforma.BD::ACC.LogUsuarioClave" 
				WHERE "IdUsuario" = p_IdUsuario order by "UltimoCambioDeClave" desc limit 10) LogUsuario
				where "Clave" = p_ClaveNueva; 
				
			IF (v_Clave = p_ClaveNueva or v_count > 0) THEN
				--LAS CLAVES SON IGUALES, ERROR
				v_hash= select 'Clave' as "Codigo", '*****' as "Valor" FROM DUMMY;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_CLAVES_IGUALES',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'UI_CLAVES_IGUALES' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
					SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			UPDATE "BSCL.Plataforma.BD::ACC.Usuario" 
			SET "Clave" = p_ClaveNueva,
				"DebeCambiarClave" = 'N',
				"UltimoCambioClave" = CURRENT_UTCTIMESTAMP
			WHERE "IdUsuario" = p_IdUsuario;
			
			p_out_update := 'ok+++';
		END;
		
		--Ejecutar el programa de logeo.
		SELECT CURRENT_DATE into p_sysDate from DUMMY;
		
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

	END;
END;