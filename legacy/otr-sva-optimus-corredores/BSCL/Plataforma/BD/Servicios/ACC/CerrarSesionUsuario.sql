SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario";

CREATE PROCEDURE
-- 
--  Proposito: Cierra una sesion de usuario 
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 15/02/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN p_Token						NVARCHAR(520),
		
	-- Salida Correcta
	OUT p_out_estado				NVARCHAR(5),
		 
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
		declare v_existe 						int;
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE p_sysDate NVARCHAR(20);
		DECLARE p_timestampValidacionUsuario		timestamp default CURRENT_UTCTIMESTAMP; 
		DECLARE p_SesionAntigua TABLE ("IdTokenSesion" nvarchar(520), "IdUsuario" BIGINT, "TokenValido" NVARCHAR(1), "FechaUltimoUso" TIMESTAMP, "UsoToken" INTEGER, "FechaIngreso" TIMESTAMP, "FechaSalida" TIMESTAMP);
		
		p_out_estado:='nok++';
									
		--Valida que el token exista y sea valido
		select count(1) into v_existe 
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';
		if v_existe = 0 then
			v_hash= select 'TOKEN' as "Codigo",:p_Token as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('USUARIO_TOKEN_INVALIDO',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'USUARIO_TOKEN_INVALIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									select * from :p_out_ErroresDetalle; 
			SIGNAL SALIDA_CONTROLADA; 		  			
		end if;		
								
		
		--agregar datos al log.
		p_SesionActual = select * from "BSCL.Plataforma.BD::ACC.Sesion" WHERE "IdUsuario" = p_IdUsuario order by "FechaUltimoUso" desc limit 1;
		
		--agregar al log los valores de la ultima sesion creada y la nueva.
		p_DetalleLog =  (select 'SESION' as "SubRegistro", 
		'FechaSalida' as "CampoRegistro", 
		TO_VARCHAR("FechaSalida") as "ValorAntiguo", 
		TO_VARCHAR(p_timestampValidacionUsuario) as "ValorNuevo"
		FROM :p_SesionActual)
		UNION ALL (SELECT 'SESION' as "SubRegistro", 
		'FechaUltimoUso' as "CampoRegistro",
		TO_VARCHAR("FechaUltimoUso") as "ValorAntiguo", 
		TO_VARCHAR(p_timestampValidacionUsuario) as "ValorNuevo"   
		FROM :p_SesionActual) 
		UNION ALL (SELECT 'SESION' as "SubRegistro", 
		'TokenValido' as "CampoRegistro",
		"TokenValido" as "ValorAntiguo", 
		'N' as "ValorNuevo"   
		FROM :p_SesionActual)
		UNION ALL (SELECT 'SESION' as "SubRegistro", 
		'ACCION' as "CampoRegistro",
		'' as "ValorAntiguo", 
		'TERMINO SESION' as "ValorNuevo"   
		FROM :p_SesionActual);
		
		--Actualiza datos de conexion
		update "BSCL.Plataforma.BD::ACC.Usuario" 
		set "Conectado" = 'N'
	    where "IdEmpresa" = :p_IdEmpresa and "IdUsuario" = :p_IdUsuario;
	    		    		
		update "BSCL.Plataforma.BD::ACC.Sesion" 
		   set "FechaSalida" = p_timestampValidacionUsuario,
		   		"TokenValido" = 'N',
		   	   "UsoToken" = "UsoToken" + 1
	    where "IdTokenSesion" = :p_Token;		
		--Salida	
	    p_out_estado 	 	:= 'ok+++'; 	
		
		
		--obtener fecha del sistema
		SELECT CURRENT_DATE into p_sysDate from DUMMY;
		--Ejecutar el programa de logeo.
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'INFORMATIVO' 
		,p_IdModulo => 'PLA' 
		,p_IdUsuario => p_IdUsuario 
		,p_IdEmpresa => p_IdEmpresa 
		,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'SESION'
		,p_DetalleLog => :p_DetalleLog
		);
		--------------------
		--FIN SP 
		--------------------
	END;	
END;